# Finance Service

This is the Finance microservice for the Chiro ERP system, implementing comprehensive financial management including:

-   Accounts Receivable and Payable
-   Journal Entries and General Ledger
-   Multi-currency support
-   Cost accounting (FIFO, LIFO, Average, Standard, Actual)
-   Financial reporting
-   Bank reconciliation
-   Dunning and payment management
-   Fiscal period management
-   Audit trail functionality

## Architecture

The service follows Clean Architecture (Hexagonal Architecture) principles with clear separation of:

-   **Application Layer**: Use cases and application services
-   **Domain Layer**: Core business logic and rules
-   **Infrastructure Layer**: External integrations and persistence

## Key Features

-   Event-driven integration via Kafka
-   Multi-tenant support
-   Comprehensive audit logging
-   Real-time balance calculations
-   Flexible chart of accounts
-   Integration with other ERP modules
