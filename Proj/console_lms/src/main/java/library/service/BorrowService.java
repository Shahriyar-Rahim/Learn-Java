package library.service;

import library.exception.BorrowingException;
import library.exception.EntityNotFoundException;
import library.exception.UnauthorizedException;
import library.exception.ValidationException;
import library.model.*;
import library.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles transactional borrowing, returns, renewals, and overdue tracking.
 */
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final UserRepository userRepository;
    private final HoldRepository holdRepository;
    private final HoldService holdService;
    private final ConfigurationRepository configRepository;
    private final AuditLogService auditLogService;

    public BorrowService(BorrowRepository borrowRepository, BookRepository bookRepository,
                         BookCopyRepository copyRepository, UserRepository userRepository,
                         HoldRepository holdRepository, HoldService holdService,
                         ConfigurationRepository configRepository, AuditLogService auditLogService) {
        this.borrowRepository = borrowRepository;
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.userRepository = userRepository;
        this.holdRepository = holdRepository;
        this.holdService = holdService;
        this.configRepository = configRepository;
        this.auditLogService = auditLogService;
    }

    public BorrowRecord getBorrowById(String borrowId) {
        return borrowRepository.findById(borrowId)
                .orElseThrow(() -> new EntityNotFoundException("BorrowRecord", borrowId));
    }

    public List<BorrowRecord> getAllLoans() {
        return borrowRepository.findAll();
    }

    public List<BorrowRecord> getActiveLoans() {
        List<BorrowRecord> list = new ArrayList<>();
        for (BorrowRecord r : borrowRepository.findAll()) {
            if (r.getStatus() == BorrowStatus.ACTIVE) {
                list.add(r);
            }
        }
        return list;
    }

    public List<BorrowRecord> getActiveLoansForUser(String userId) {
        return borrowRepository.findActiveByUserId(userId);
    }

    public List<BorrowRecord> getAllLoansForUser(String userId) {
        return borrowRepository.findAllByUserId(userId);
    }

    public List<BorrowRecord> getOverdueLoans(LocalDate referenceDate) {
        LocalDate ref = referenceDate != null ? referenceDate : LocalDate.now();
        List<BorrowRecord> overdue = new ArrayList<>();
        for (BorrowRecord r : borrowRepository.findAll()) {
            if (r.isOverdue(ref)) {
                overdue.add(r);
            }
        }
        return overdue;
    }

    /**
     * Executes atomic book checkout with all business and hold queue rules enforced.
     */
    public synchronized BorrowRecord borrowBook(String userId, String bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book", bookId));

        // 1. User state verification
        if (user.getState() != UserState.APPROVED) {
            throw new UnauthorizedException("User '" + user.getUsername() + "' cannot borrow books. Account status is " + user.getState().getDescription() + ".");
        }

        // 2. Book archival status
        if (book.isArchived()) {
            throw new ValidationException("Book '" + book.getTitle() + "' is archived and cannot be checked out.");
        }

        LibraryConfiguration config = configRepository.get();

        // 3. User active loans limit
        int activeLoans = borrowRepository.countActiveLoansByUser(userId);
        if (activeLoans >= config.getMaxActiveLoans()) {
            throw new BorrowingException("Borrow limit reached: You currently have " + activeLoans + " active loans (maximum allowed: " + config.getMaxActiveLoans() + "). Return a book to borrow again.");
        }

        // 4. Duplicate book checkout check
        if (borrowRepository.findActiveByUserIdAndBookId(userId, bookId).isPresent()) {
            throw new BorrowingException("You already have an active loan for '" + book.getTitle() + "'. Duplicate borrowing of the same title is not permitted.");
        }

        // 5. Check if user has an active hold on this book
        Optional<HoldRecord> userHoldOpt = holdRepository.findActiveByUserIdAndBookId(userId, bookId);
        BookCopy copyToBorrow = null;

        if (userHoldOpt.isPresent()) {
            HoldRecord userHold = userHoldOpt.get();
            if (userHold.getReservedCopyId() != null) {
                // Borrow the specific reserved copy
                copyToBorrow = copyRepository.findById(userHold.getReservedCopyId()).orElse(null);
            }
        }

        // If not using a previously reserved copy, find an unreserved available copy
        if (copyToBorrow == null) {
            List<BookCopy> available = copyRepository.findAvailableByBookId(bookId);
            List<HoldRecord> activeHolds = holdRepository.findActiveByBookId(bookId);

            // If active holds exist from other users, check if any available copy is free for non-hold borrowers
            for (BookCopy c : available) {
                // Ensure this copy is not held/reserved by someone else
                Optional<HoldRecord> reservedHold = holdRepository.findReservedByCopyId(c.getCopyId());
                if (reservedHold.isEmpty()) {
                    // Check queue priority: if there are waiting holds and user is not at head of queue
                    if (!activeHolds.isEmpty() && (userHoldOpt.isEmpty() || activeHolds.get(0).getPriority() < userHoldOpt.get().getPriority())) {
                        throw new BorrowingException("All available copies of '" + book.getTitle() + "' are reserved for members in the hold queue. You may place a hold to reserve the next available copy.");
                    }
                    copyToBorrow = c;
                    break;
                }
            }
        }

        if (copyToBorrow == null) {
            throw new BorrowingException("No copies of '" + book.getTitle() + "' are currently available for checkout. Please place a hold.");
        }

        // 6. Transactional State Changes
        copyToBorrow.setStatus(CopyStatus.BORROWED);
        copyRepository.save(copyToBorrow);

        String borrowId = borrowRepository.nextId();
        LocalDate today = LocalDate.now();
        LocalDate due = today.plusDays(config.getDefaultLoanDurationDays());

        BorrowRecord record = new BorrowRecord(
                borrowId,
                userId,
                bookId,
                copyToBorrow.getCopyId(),
                today,
                due,
                null,
                0,
                BorrowStatus.ACTIVE,
                "Borrowed copy " + copyToBorrow.getCopyId()
        );
        borrowRepository.save(record);

        // Fulfill hold if user had one
        if (userHoldOpt.isPresent()) {
            HoldRecord userHold = userHoldOpt.get();
            userHold.setStatus(HoldStatus.FULFILLED);
            userHold.setFulfillmentDate(LocalDateTime.now());
            userHold.setReservedCopyId(null);
            userHold.setNotes("Fulfilled on " + today + " (Borrow ID: " + borrowId + ")");
            holdRepository.save(userHold);
            auditLogService.log("HOLD_FULFILLED", user.getUsername(), "HOLD", "Hold fulfilled for book '" + book.getTitle() + "'", true);
        }

        auditLogService.log("BOOK_BORROWED", user.getUsername(), "LOAN", 
                "Borrowed '" + book.getTitle() + "' (Copy: " + copyToBorrow.getCopyId() + ", Due: " + due + ")", true);

        return record;
    }

    /**
     * Executes atomic book return, restores copy status, and triggers hold queue fulfillment.
     */
    public synchronized void returnBook(String borrowId, String returningUserId, boolean isAdmin,
                                        BookCondition condition, String notes) {
        BorrowRecord record = getBorrowById(borrowId);
        if (record.getStatus() != BorrowStatus.ACTIVE) {
            throw new BorrowingException("Cannot return book: Loan " + borrowId + " is already marked as " + record.getStatus() + ".");
        }

        if (!isAdmin && !record.getUserId().equals(returningUserId)) {
            throw new UnauthorizedException("You are not authorized to return another member's loan.");
        }

        BookCopy copy = copyRepository.findById(record.getCopyId())
                .orElseThrow(() -> new EntityNotFoundException("BookCopy", record.getCopyId()));

        // Mark loan record as RETURNED
        record.setStatus(BorrowStatus.RETURNED);
        record.setReturnDate(LocalDate.now());
        if (notes != null && !notes.isBlank()) {
            record.setNotes(notes.trim());
        }
        borrowRepository.save(record);

        // Update condition if specified
        if (condition != null) {
            copy.setCondition(condition);
        }

        // Trigger hold fulfillment queue
        boolean reservedForHold = holdService.fulfillNextHoldForBook(record.getBookId(), copy);

        User borrower = userRepository.findById(record.getUserId()).orElse(null);
        String borrowerName = borrower != null ? borrower.getUsername() : record.getUserId();

        auditLogService.log("BOOK_RETURNED", returningUserId, "LOAN", 
                "Returned loan " + borrowId + " (Copy " + copy.getCopyId() + "). " + 
                (reservedForHold ? "Reserved for next hold." : "Marked AVAILABLE."), true);
    }

    /**
     * Renews an active eligible loan.
     */
    public synchronized void renewLoan(String borrowId, String requestingUserId, boolean isAdmin) {
        BorrowRecord record = getBorrowById(borrowId);
        if (record.getStatus() != BorrowStatus.ACTIVE) {
            throw new BorrowingException("Cannot renew: Loan " + borrowId + " is not active (Status: " + record.getStatus() + ").");
        }

        if (!isAdmin && !record.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("You cannot renew a loan belonging to another member.");
        }

        User user = userRepository.findById(record.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User", record.getUserId()));

        if (user.getState() != UserState.APPROVED) {
            throw new UnauthorizedException("Account " + user.getUsername() + " is " + user.getState() + ". Renewal not permitted.");
        }

        LibraryConfiguration config = configRepository.get();

        // Check renewal limit
        if (record.getRenewalCount() >= config.getMaxRenewals()) {
            throw new BorrowingException("Cannot renew: Maximum renewal limit of " + config.getMaxRenewals() + " reached for this loan.");
        }

        // Check if other members have placed holds on this book
        List<HoldRecord> activeHolds = holdRepository.findActiveByBookId(record.getBookId());
        if (!activeHolds.isEmpty()) {
            throw new BorrowingException("Cannot renew: Other library members have placed holds waiting for this title.");
        }

        record.setRenewalCount(record.getRenewalCount() + 1);
        record.setDueDate(record.getDueDate().plusDays(config.getRenewalPeriodDays()));
        borrowRepository.save(record);

        auditLogService.log("LOAN_RENEWED", requestingUserId, "LOAN", 
                "Renewed loan " + borrowId + " (Renewal #" + record.getRenewalCount() + ", New Due Date: " + record.getDueDate() + ")", true);
    }
}
