# Architecture Specification

## 1. Architectural Style & Principles

This application follows **Clean Architecture** principles combined with a **Feature-First / Screaming Architecture** packaging structure.

```
       ┌────────────────────────┐
       │   Presentation Layer   │  (Jetpack Compose / UI + ViewModels + UI State)
       └───────────┬────────────┘
                   │ depends on
       ┌───────────▼────────────┐
       │      Domain Layer      │  (Entities, Value Objects, Use Cases / Interactors)
       └───────────▲────────────┘
                   │ inverted dependency (Repository Interfaces)
       ┌───────────┴────────────┐
       │       Data Layer       │  (Room DAOs, DataSources, Repository Impls, DataStore)
       └────────────────────────┘
```

### Core Tenets
1. **Separation of Concerns**: UI components display state and capture intent; domain layer holds pure business rules; data layer handles persistence and caching.
2. **Unidirectional Data Flow (UDF)**:
   - UI emits user actions/intents.
   - ViewModel executes Use Cases and emits an immutable `UiState` via Kotlin `StateFlow`.
   - UI renders state passively.
3. **Domain Independence**: The `domain` package has zero dependencies on Android framework classes (no `Context`, no Android UI types). Pure Kotlin.
4. **Single Source of Truth (SSOT)**: Local Room database acts as the single source of truth for all transactional finance and study scheduling records.

---

## 2. Module & Package Structure

Namespace: `com.abpi.student.finance.study.organizer`

```
app/src/main/java/com/abpi/student/finance/study/organizer/
├── core/
│   ├── database/             # Room Database instance, migrations, converters
│   ├── datastore/            # User preferences (theme, currency, notification prefs)
│   ├── di/                   # Dependency Injection (Hilt modules)
│   ├── notifications/        # Alarms, reminder managers, NotificationChannels
│   ├── ui/                   # Shared design system, theme, typography, components
│   └── util/                 # Extensions, date/time helpers, formatters
│
├── features/
│   ├── finance/
│   │   ├── data/
│   │   │   ├── local/        # Room Entities, DAOs (TransactionDao, BudgetDao)
│   │   │   ├── mapper/       # Entity <-> Domain mappers
│   │   │   └── repository/   # FinanceRepositoryImpl
│   │   ├── domain/
│   │   │   ├── model/        # Expense, Income, Budget, Category, Account
│   │   │   ├── repository/   # FinanceRepository (interface)
│   │   │   └── usecase/      # AddTransactionUseCase, GetMonthlyBudgetSummaryUseCase, etc.
│   │   └── presentation/
│   │       ├── dashboard/    # Finance summary cards, quick add
│   │       ├── transactions/ # Transaction list, filter, details
│   │       ├── budget/       # Budget allocation & tracking
│   │       └── analytics/    # Spending breakdown charts & reports
│   │
│   ├── study/
│   │   ├── data/
│   │   │   ├── local/        # CourseDao, TaskDao, StudySessionDao
│   │   │   ├── mapper/       # Entity <-> Domain mappers
│   │   │   └── repository/   # StudyRepositoryImpl
│   │   ├── domain/
│   │   │   ├── model/        # Course, Assignment/Task, Exam, StudySession
│   │   │   ├── repository/   # StudyRepository (interface)
│   │   │   └── usecase/      # GetUpcomingDeadlinesUseCase, SaveStudySessionUseCase, etc.
│   │   └── presentation/
│   │       ├── schedule/     # Timetable, weekly/daily class schedule
│   │       ├── tasks/        # Assignments & homework tracker
│   │       ├── pomodoro/     # Study timer & session logger
│   │       └── analytics/    # Study hours vs target breakdown
│   │
│   └── dashboard/            # High-level home aggregating finance & upcoming study deadlines
```

---

## 3. Technology Stack & Key Libraries

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose + Material 3
- **Asynchronous & Reactive**: Kotlin Coroutines + Flow (`StateFlow`, `SharedFlow`)
- **Dependency Injection**: Jetpack Hilt / Dagger
- **Local Persistence**: Jetpack Room (SQLite) with relational queries and indices
- **Preferences**: Jetpack Preferences DataStore
- **Background Tasks & Reminders**: Android `AlarmManager` (for exact study/exam alarms) and `WorkManager` (periodic budget reset / weekly summary notifications)
- **Testing Strategy**:
  - **Unit**: JUnit 5, MockK / Fake repositories, Turbine (for `StateFlow` testing)
  - **Instrumentation**: AndroidX Test, Compose UI Test rules

---

## 4. State Management Contract (MVI/MVVM)

Each screen component is backed by:
1. `ScreenUiState`: Immutable data class containing screen state (Loading, Success with data, Error).
2. `ScreenEvent` (or Intent): Sealed interface representing user actions (e.g., `OnSaveTransactionClicked`, `OnDeleteTask`).
3. `ScreenSideEffect`: Sealed interface for one-off events (e.g., `ShowSnackbar`, `NavigateBack`).
