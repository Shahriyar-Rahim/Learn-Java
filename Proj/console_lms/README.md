# Pure Java CLI Library Management System (LMS)

A robust, enterprise-grade, modular Console/CLI Library Management System built entirely with **Pure Java SE (Java 17+)** using **zero external dependencies or frameworks**.

---

## 📌 Architecture & Design Highlights

* **Pure Java SE Standard Library**: Implemented strictly with Java standard APIs (`java.io`, `java.nio.file`, `java.time`, `java.util`, `java.util.concurrent`, `java.security`, `java.util.logging`, `java.util.zip`). No Spring, Hibernate, or third-party JSON libraries.
* **Custom JSON Engine**: Lightweight and RFC-compliant recursive-descent JSON parser and serializer (`library.persistence.json`) supporting primitives, nested objects, and arrays.
* **Atomic Persistence & Disaster Recovery**: All transactional entity changes use atomic file writes (write to `.tmp` -> `Files.move(..., ATOMIC_MOVE)` -> sync). Before overwriting, existing files are rotated to `.bak`. If a file becomes corrupted or unreadable, the system automatically detects and recovers from `.bak`.
* **Security & Authentication**:
  - Passwords hashed using industry-standard **PBKDF2WithHmacSHA256** with unique cryptographic salts (16 bytes, 65,536 iterations).
  - Secure in-memory password capture via `char[]` and immediate zeroing/clearing (`Arrays.fill(pwd, '\0')`).
  - Path-traversal sanitization on report and export filenames (`SecurityUtil.sanitizeFilename`).
  - Role-Based Access Control (RBAC): `ADMIN` vs `MEMBER`, with account lifecycle states (`PENDING`, `ACTIVE`, `SUSPENDED`, `BLOCKED`).
* **Layered Clean Architecture**:
  - `model`: Domain entities, value objects, and enums.
  - `repository`: In-memory thread-safe indexes with backing file persistence.
  - `service`: Business rules, validation, inventory allocation, hold queues, and audit logging.
  - `ui`: Console menus, table formatters, and terminal controllers.
  - `reporting`: Analytical metrics aggregation and export to formatted TXT, CSV, and compressed ZIP archives.
  - `security`: Password hashing, user session management, and input sanitization.

---

## 📂 Project Directory Structure

```text
console_lms/
├── data/                       # Persistent JSON storage & audit logs
│   ├── users.json
│   ├── books.json
│   ├── book_copies.json
│   ├── authors.json
│   ├── sections.json
│   ├── borrows.json
│   ├── holds.json
│   ├── configuration.json
│   ├── audit_logs.json
│   └── logs/                  # System operational text logs
├── reports/                    # Exported analytical reports (TXT, CSV, ZIP)
├── scripts/                    # Shell helper scripts
│   ├── build.sh               # Compiles main application to out/
│   ├── test.sh                # Compiles & executes automated test runner
│   └── run.sh                 # Builds (if needed) and runs Main CLI
├── src/
│   ├── main/java/library/     # Main application source code
│   │   ├── configuration/     # System configuration
│   │   ├── exception/         # Domain exception hierarchy
│   │   ├── model/             # Entities, Enums, DTOs
│   │   ├── persistence/       # Custom JSON engine & DataStore
│   │   ├── reporting/         # Analytical reporting & Exporters
│   │   ├── repository/        # Data access objects with in-memory caching
│   │   ├── security/          # PBKDF2 hashing, sessions, sanitization
│   │   ├── service/           # Core business domain logic & workflows
│   │   ├── ui/                # CLI Controllers & Console renderers
│   │   ├── util/              # String, Date, and Collection helpers
│   │   ├── validation/        # Email, ISBN, input validators
│   │   └── Main.java          # CLI entry point
│   └── test/java/library/test/# Automated test suite & test runner
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

* **Java Development Kit (JDK) 17** or newer (`javac` and `java` on `PATH`).
* Linux / macOS / WSL Bash shell environment.

### 1. Build the Application

To compile the application classes to `out/`:

```bash
./scripts/build.sh
```

### 2. Run the Automated Test Suite

To compile and run the 9 automated test suites (including recovery, advanced features, and 30-step end-to-end simulation):

```bash
./scripts/test.sh
```

### 3. Launch the Interactive CLI

To run the Library Management System:

```bash
./scripts/run.sh
```

---

## 🔑 Core Features & Workflows

1. **Bootstrap & Admin Setup**:
   - On the very first run, detects the absence of administrators and prompts to create the primary administrator.
2. **User Registration & Approval**:
   - Public member registration sets user state to `PENDING`.
   - Administrators review and approve/activate registrations.
3. **Catalogue Management**:
   - Classify items into sections/categories.
   - Register authors and books with ISBN validation, publication year, edition, and summary.
   - Track physical book copies by individual barcode, condition (`NEW`, `GOOD`, `FAIR`, `POOR`, `DAMAGED`), and location.
4. **Circulation & Lending**:
   - Borrow available copies with due date calculation based on configurable loan periods.
   - Renewable loans subject to max renewal limits and active hold constraints.
   - Returns inspect physical condition and immediately reallocate to waiting hold queues if applicable.
5. **FIFO Hold & Reservation Queue**:
   - When all copies are borrowed, users can place holds.
   - Priority allocation assigns available returns to the first member in the hold queue.
6. **Analytics & Reports**:
   - Generate summary metrics (inventory counts, active loans, overdue items, hold queues).
   - Export reports directly into `reports/` in TXT, CSV, or ZIP formats.
7. **Comprehensive Audit Log**:
   - Every security and operational event (logins, registrations, borrows, returns, configuration changes) is logged with timestamps, actors, and IP/terminal origin.
