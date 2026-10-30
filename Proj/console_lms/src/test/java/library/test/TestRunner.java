package library.test;

import java.util.ArrayList;
import java.util.List;

/**
 * Master test runner. Executes all test suites, logs results, and outputs execution summary.
 */
public class TestRunner {

    private record TestSuite(String name, Runnable runnable) {}

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("          LIBRARY MANAGEMENT SYSTEM - AUTOMATED TEST SUITE                ");
        System.out.println("==========================================================================");

        List<TestSuite> suites = List.of(
                new TestSuite("Authentication & User Management Tests", AuthAndUserTests::run),
                new TestSuite("Book Catalogue & Physical Inventory Tests", BookAndCatalogTests::run),
                new TestSuite("Borrowing, Returns & Renewal Rules Tests", BorrowAndReturnTests::run),
                new TestSuite("Hold Queue & Priority Allocation Tests", HoldQueueTests::run),
                new TestSuite("Persistence, Reload & Disaster Recovery Tests", PersistenceAndRecoveryTests::run),
                new TestSuite("Reporting, Metrics & Export Tests (TXT, CSV, ZIP)", ReportAndExportTests::run),
                new TestSuite("Security, Path Traversal & Edge-Case Tests", SecurityAndEdgeCasesTests::run),
                new TestSuite("Advanced Features & Utility Tests", AdvancedFeaturesAndUtilitiesTests::run),
                new TestSuite("Complete 30-Step End-to-End Simulation", EndToEndSimulation::run)
        );

        int passed = 0;
        int failed = 0;
        List<String> failedNames = new ArrayList<>();
        long startTotal = System.currentTimeMillis();

        for (TestSuite suite : suites) {
            System.out.printf("\n[RUNNING] %s ...\n", suite.name());
            long suiteStart = System.currentTimeMillis();
            try {
                suite.runnable().run();
                long elapsed = System.currentTimeMillis() - suiteStart;
                System.out.printf("  [PASS] %s (%d ms)\n", suite.name(), elapsed);
                passed++;
            } catch (Throwable t) {
                long elapsed = System.currentTimeMillis() - suiteStart;
                System.out.printf("  [FAIL] %s (%d ms)\n", suite.name(), elapsed);
                System.err.println("       Error: " + t.getMessage());
                t.printStackTrace(System.err);
                failed++;
                failedNames.add(suite.name());
            }
        }

        long totalElapsed = System.currentTimeMillis() - startTotal;
        System.out.println("\n==========================================================================");
        System.out.println("                         TEST EXECUTION SUMMARY                           ");
        System.out.println("==========================================================================");
        System.out.printf("  Total Test Suites: %d\n", suites.size());
        System.out.printf("  Passed           : %d\n", passed);
        System.out.printf("  Failed           : %d\n", failed);
        System.out.printf("  Total Duration   : %d ms\n", totalElapsed);
        System.out.println("==========================================================================");

        if (failed > 0) {
            System.err.println("\nFAILURES DETECTED in: " + failedNames);
            System.exit(1);
        } else {
            System.out.println("\nALL TESTS PASSED SUCCESSFULLY! (Exit code 0)\n");
            System.exit(0);
        }
    }
}
