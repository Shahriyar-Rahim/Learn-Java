package library.ui;

import library.model.BookCopy;
import library.model.BookDetail;

import java.util.List;

/**
 * Terminal UI rendering helpers for formatted banners, menus, tables, and messages.
 */
public final class ConsoleRenderer {

    private ConsoleRenderer() {
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println("==========================================================================");
        int pad = Math.max(0, (74 - title.length()) / 2);
        System.out.println(" ".repeat(pad) + title);
        System.out.println("==========================================================================");
    }

    public static void printSubHeader(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    public static void printSuccess(String message) {
        System.out.println("\n  [SUCCESS] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("\n  [WARNING] " + message);
    }

    public static void printError(String message) {
        System.out.println("\n  [ERROR] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("  [INFO] " + message);
    }

    public static void printBookDetail(BookDetail d) {
        System.out.println();
        System.out.println("+------------------------------------------------------------------------+");
        System.out.printf("| Book ID    : %-58s |\n", d.getBook().getId());
        System.out.printf("| Title      : %-58s |\n", d.getBook().getTitle());
        System.out.printf("| ISBN       : %-58s |\n", d.getBook().getIsbn());
        System.out.printf("| Author(s)  : %-58s |\n", d.getAuthorNames());
        System.out.printf("| Section    : %-58s |\n", d.getSectionName());
        System.out.printf("| Publisher  : %-58s |\n", d.getBook().getPublisher());
        System.out.printf("| Pub Date   : %-58s |\n", d.getBook().getPublicationDate() != null ? d.getBook().getPublicationDate() : "N/A");
        System.out.printf("| Edition    : %-58s |\n", d.getBook().getEdition());
        System.out.printf("| Language   : %-58s |\n", d.getBook().getLanguage());
        System.out.printf("| Status     : %-58s |\n", d.getBook().isArchived() ? "ARCHIVED" : "ACTIVE");
        System.out.printf("| Inventory  : Total: %d | Available: %d | Borrowed: %d | Reserved: %d |\n",
                d.getTotalCopies(), d.getAvailableCopies(), d.getBorrowedCopies(), d.getReservedCopies());
        System.out.println("+------------------------------------------------------------------------+");
        if (!d.getBook().getDescription().isEmpty()) {
            System.out.println("  Description: " + d.getBook().getDescription());
        }
        if (!d.getCopies().isEmpty()) {
            System.out.println("  Physical Copies:");
            for (BookCopy c : d.getCopies()) {
                System.out.printf("   - Copy %-14s | Status: %-10s | Cond: %-5s | Loc: %s\n",
                        c.getCopyId(), c.getStatus(), c.getCondition(), c.getLocation());
            }
        }
        System.out.println("+------------------------------------------------------------------------+");
    }

    public static void printBookList(List<BookDetail> books) {
        if (books.isEmpty()) {
            System.out.println("  (No books found in catalogue)");
            return;
        }
        System.out.println();
        System.out.printf("  %-10s | %-30s | %-20s | %-12s | %-7s\n", "Book ID", "Title", "Author(s)", "Section", "Avail");
        System.out.println("  " + "-".repeat(10) + "-+-" + "-".repeat(30) + "-+-" + "-".repeat(20) + "-+-" + "-".repeat(12) + "-+-" + "-".repeat(7));
        for (BookDetail d : books) {
            String title = truncate(d.getBook().getTitle(), 30);
            String authors = truncate(d.getAuthorNames(), 20);
            String sec = truncate(d.getSectionName(), 12);
            String avail = d.getAvailableCopies() + "/" + d.getTotalCopies();
            if (d.getBook().isArchived()) {
                avail = "ARCH";
            }
            System.out.printf("  %-10s | %-30s | %-20s | %-12s | %-7s\n",
                    d.getBook().getId(), title, authors, sec, avail);
        }
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 2) + "..";
    }
}
