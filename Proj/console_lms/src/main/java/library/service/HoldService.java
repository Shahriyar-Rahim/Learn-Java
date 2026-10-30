package library.service;

import library.exception.EntityNotFoundException;
import library.exception.HoldException;
import library.exception.UnauthorizedException;
import library.exception.ValidationException;
import library.model.*;
import library.repository.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages the FIFO Hold / Reservation Queue with automatic copy allocation and expiry handling.
 */
public class HoldService {

    private final HoldRepository holdRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;
    private final ConfigurationRepository configRepository;
    private final AuditLogService auditLogService;

    public HoldService(HoldRepository holdRepository, BookRepository bookRepository,
                       BookCopyRepository copyRepository, UserRepository userRepository,
                       BorrowRepository borrowRepository, ConfigurationRepository configRepository,
                       AuditLogService auditLogService) {
        this.holdRepository = holdRepository;
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.userRepository = userRepository;
        this.borrowRepository = borrowRepository;
        this.configRepository = configRepository;
        this.auditLogService = auditLogService;
    }

    public List<HoldRecord> getAllHolds() {
        return holdRepository.findAll();
    }

    public HoldRecord getHoldById(String holdId) {
        return holdRepository.findById(holdId)
                .orElseThrow(() -> new EntityNotFoundException("HoldRecord", holdId));
    }

    public List<HoldRecord> getActiveHoldsForBook(String bookId) {
        return holdRepository.findActiveByBookId(bookId);
    }

    public List<HoldRecord> getActiveHoldsForUser(String userId) {
        return holdRepository.findActiveByUserId(userId);
    }

    public List<HoldRecord> getAllHoldsForUser(String userId) {
        return holdRepository.findAllByUserId(userId);
    }

    public synchronized HoldRecord placeHold(String userId, String bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book", bookId));

        if (user.getState() != UserState.APPROVED) {
            throw new UnauthorizedException("User " + user.getUsername() + " is not approved to place holds (Account Status: " + user.getState() + ").");
        }

        if (book.isArchived()) {
            throw new ValidationException("Cannot place a hold on an archived book.");
        }

        // Check if user already has an active loan for this book
        if (borrowRepository.findActiveByUserIdAndBookId(userId, bookId).isPresent()) {
            throw new HoldException("You already have an active borrowed copy of this book.");
        }

        // Check duplicate active hold
        if (holdRepository.findActiveByUserIdAndBookId(userId, bookId).isPresent()) {
            throw new HoldException("You already have an active hold placed on this book.");
        }

        // Check user active holds limit
        int maxHolds = configRepository.get().getMaxActiveHolds();
        int currentHolds = holdRepository.countActiveHoldsByUser(userId);
        if (currentHolds >= maxHolds) {
            throw new HoldException("Cannot place hold: You have reached the maximum limit of " + maxHolds + " active holds.");
        }

        // If copies are currently available and unreserved, user should borrow directly
        List<BookCopy> availableCopies = copyRepository.findAvailableByBookId(bookId);
        if (!availableCopies.isEmpty()) {
            // Also check if any available copy is unreserved
            boolean hasUnreservedCopy = false;
            for (BookCopy c : availableCopies) {
                if (holdRepository.findReservedByCopyId(c.getCopyId()).isEmpty()) {
                    hasUnreservedCopy = true;
                    break;
                }
            }
            if (hasUnreservedCopy) {
                throw new HoldException("Copies of this book are currently available on the shelf. You can borrow directly instead of placing a hold.");
            }
        }

        int priority = holdRepository.getNextPriority(bookId);
        String holdId = holdRepository.nextId();
        HoldRecord hold = new HoldRecord(
                holdId,
                userId,
                bookId,
                LocalDateTime.now(),
                HoldStatus.ACTIVE,
                priority,
                null,
                null,
                null,
                "Position " + priority + " in queue"
        );

        holdRepository.save(hold);
        auditLogService.log("HOLD_PLACED", user.getUsername(), "HOLD", "Placed hold for book '" + book.getTitle() + "' at queue position " + priority, true);
        return hold;
    }

    public synchronized void cancelHold(String holdId, String requestingUserId, boolean isAdmin) {
        HoldRecord hold = getHoldById(holdId);
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new HoldException("Hold " + holdId + " is not active (Status: " + hold.getStatus() + ").");
        }

        if (!isAdmin && !hold.getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("You are not authorized to cancel another user's hold.");
        }

        hold.setStatus(HoldStatus.CANCELLED);
        String reservedCopyId = hold.getReservedCopyId();
        hold.setReservedCopyId(null);
        holdRepository.save(hold);

        auditLogService.log("HOLD_CANCELLED", requestingUserId, "HOLD", "Cancelled hold " + holdId + " for book " + hold.getBookId(), true);

        // If this hold had a reserved copy, reallocate it
        if (reservedCopyId != null) {
            copyRepository.findById(reservedCopyId).ifPresent(copy -> {
                fulfillNextHoldForBook(hold.getBookId(), copy);
            });
        }
    }

    /**
     * Attempts to allocate a physical copy to the next eligible user in the FIFO hold queue for that book.
     * If an eligible hold is found, marks the copy RESERVED and sets expiry date.
     * If no active hold remains, marks the copy AVAILABLE.
     */
    public synchronized boolean fulfillNextHoldForBook(String bookId, BookCopy copy) {
        List<HoldRecord> activeHolds = holdRepository.findActiveByBookId(bookId);
        int expiryDays = configRepository.get().getHoldExpiryDays();

        for (HoldRecord candidate : activeHolds) {
            User user = userRepository.findById(candidate.getUserId()).orElse(null);
            if (user == null || user.getState() != UserState.APPROVED) {
                // User is no longer valid or approved, mark this hold cancelled and continue
                candidate.setStatus(HoldStatus.CANCELLED);
                candidate.setNotes("Cancelled automatically: User account inactive or not approved");
                holdRepository.save(candidate);
                continue;
            }

            // Assign copy to this hold
            copy.setStatus(CopyStatus.RESERVED);
            copyRepository.save(copy);

            candidate.setReservedCopyId(copy.getCopyId());
            candidate.setExpiryDate(LocalDateTime.now().plusDays(expiryDays));
            candidate.setNotes("Reserved copy " + copy.getCopyId() + ", pickup deadline: " + candidate.getExpiryDate().toLocalDate());
            holdRepository.save(candidate);

            auditLogService.log("HOLD_RESERVED", "SYSTEM", "HOLD", 
                    "Reserved copy " + copy.getCopyId() + " for user " + user.getUsername() + " (Hold " + candidate.getHoldId() + ")", true);
            return true;
        }

        // No eligible holds: mark available
        copy.setStatus(CopyStatus.AVAILABLE);
        copyRepository.save(copy);
        return false;
    }

    /**
     * Checks for holds whose pickup window has expired.
     */
    public synchronized void processExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        for (HoldRecord h : holdRepository.findAll()) {
            if (h.isExpired(now)) {
                h.setStatus(HoldStatus.EXPIRED);
                String copyId = h.getReservedCopyId();
                h.setReservedCopyId(null);
                holdRepository.save(h);

                auditLogService.log("HOLD_EXPIRED", "SYSTEM", "HOLD", "Hold " + h.getHoldId() + " expired for user " + h.getUserId(), true);

                if (copyId != null) {
                    copyRepository.findById(copyId).ifPresent(copy -> {
                        fulfillNextHoldForBook(h.getBookId(), copy);
                    });
                }
            }
        }
    }
}
