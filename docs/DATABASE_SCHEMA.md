# Database Schema Specification

The application uses **Room Database** (SQLite under the hood) as its local storage engine. Below are the relational entities, tables, and constraints.

---

## 1. Finance Domain Tables

### `accounts`
Tracks account balances (e.g., Cash, Bank Account, Digital Wallet).
```sql
CREATE TABLE accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL, -- 'CASH', 'BANK', 'E_WALLET'
    current_balance REAL NOT NULL DEFAULT 0.0,
    currency TEXT NOT NULL DEFAULT 'USD',
    created_at INTEGER NOT NULL
);
```

### `categories`
Categories for income and expenses.
```sql
CREATE TABLE categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL, -- 'EXPENSE', 'INCOME'
    icon_key TEXT NOT NULL,
    color_hex TEXT NOT NULL
);
```

### `transactions`
Records of spending and earnings.
```sql
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    account_id INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    type TEXT NOT NULL, -- 'EXPENSE', 'INCOME', 'TRANSFER'
    amount REAL NOT NULL,
    timestamp INTEGER NOT NULL,
    notes TEXT,
    FOREIGN KEY(account_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE RESTRICT
);
CREATE INDEX index_transactions_timestamp ON transactions(timestamp);
CREATE INDEX index_transactions_account ON transactions(account_id);
```

### `budgets`
Monthly/weekly spending limits per category.
```sql
CREATE TABLE budgets (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    category_id INTEGER NOT NULL,
    allocated_amount REAL NOT NULL,
    period_start INTEGER NOT NULL,
    period_end INTEGER NOT NULL,
    FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE
);
```

---

## 2. Study Domain Tables

### `courses`
Academic subjects/classes in which the student is enrolled.
```sql
CREATE TABLE courses (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL,
    code TEXT, -- e.g., 'CS101'
    instructor TEXT,
    color_hex TEXT NOT NULL,
    is_archived INTEGER NOT NULL DEFAULT 0
);
```

### `course_schedules`
Weekly recurring class slots.
```sql
CREATE TABLE course_schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    course_id INTEGER NOT NULL,
    day_of_week INTEGER NOT NULL, -- 1 = Monday, 7 = Sunday
    start_time_minutes INTEGER NOT NULL, -- Minutes from midnight (e.g., 540 for 09:00)
    end_time_minutes INTEGER NOT NULL,
    room_or_link TEXT,
    FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE
);
```

### `tasks`
Assignments, project milestones, and homework.
```sql
CREATE TABLE tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    course_id INTEGER,
    title TEXT NOT NULL,
    description TEXT,
    due_date INTEGER NOT NULL,
    priority TEXT NOT NULL, -- 'LOW', 'MEDIUM', 'HIGH'
    is_completed INTEGER NOT NULL DEFAULT 0,
    reminder_time INTEGER,
    FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE SET NULL
);
CREATE INDEX index_tasks_due_date ON tasks(due_date);
```

### `study_sessions`
Focus and Pomodoro tracking logs.
```sql
CREATE TABLE study_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    course_id INTEGER,
    task_id INTEGER,
    duration_seconds INTEGER NOT NULL,
    start_time INTEGER NOT NULL,
    end_time INTEGER NOT NULL,
    notes TEXT,
    FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE SET NULL,
    FOREIGN KEY(task_id) REFERENCES tasks(id) ON DELETE SET NULL
);
CREATE INDEX index_study_sessions_start ON study_sessions(start_time);
```
