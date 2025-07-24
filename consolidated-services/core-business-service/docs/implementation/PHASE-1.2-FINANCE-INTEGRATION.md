# PHASE 1.2: Finance Module Integration Plan

## 🎯 Goal: Wire Finance Module to Main Src Orchestration

### Current State Analysis:

-   ✅ Finance domain layer: 85% complete (excellent foundation)
-   ❌ Finance infrastructure: 0% implemented (critical gap)
-   ❌ Main src integration: 0% wired (broken communication)

## 🚨 CRITICAL INTEGRATION TASKS:

### 1. Finance Repository Infrastructure

**Location: `modules/finance/src/main/kotlin/org/chiro/finance/infrastructure/persistence/`**

```kotlin
// Implement using main src BaseRepositoryAdapter:
@ApplicationScoped
class AccountRepositoryImpl : BaseRepositoryAdapter<Account, AccountId>(), AccountRepository {

    override suspend fun findById(id: AccountId): Account? {
        return Account.findById<Account>(id.value)
    }

    override suspend fun findByCode(accountCode: String): Account? {
        return Account.find("accountCode", accountCode).firstResult<Account>()
    }

    override suspend fun findByType(accountType: AccountType): List<Account> {
        return Account.find("accountType", accountType).list<Account>()
    }

    override suspend fun save(account: Account): Account {
        account.persist<Account>()
        publishEvents(account) // Use main src event publishing
        return account
    }
}
```

### 2. Finance Module Facade Implementation

**Location: `modules/finance/src/main/kotlin/org/chiro/finance/infrastructure/facade/`**

```kotlin
// Bridge finance module to main src integration layer:
@ApplicationScoped
class FinanceModuleFacadeImpl : FinanceModuleFacade {

    @Inject
    lateinit var accountService: AccountApplicationService

    @Inject
    lateinit var journalEntryService: JournalEntryApplicationService

    // Wire to main src facade interface
    override suspend fun getAccountById(accountId: String): AccountDto? {
        return accountService.getAccountById(AccountId(accountId))
    }

    override suspend fun recordTransaction(request: RecordTransactionRequest): TransactionDto {
        val command = CreateJournalEntryCommand(
            entries = request.entries.map { entry ->
                JournalEntryLineCommand(
                    accountId = AccountId(entry.accountId),
                    amount = Money.parse(entry.amount),
                    type = if (entry.type == "DEBIT") DebitCredit.DEBIT else DebitCredit.CREDIT
                )
            }
        )
        return journalEntryService.createJournalEntry(command)
    }
}
```

### 3. Finance Event Integration

**Location: `modules/finance/src/main/kotlin/org/chiro/finance/infrastructure/event/`**

```kotlin
// Connect finance domain events to main src integration events:
@ApplicationScoped
class FinanceEventAdapter {

    @Inject
    lateinit var eventPublisher: DomainEventPublisher

    @EventListener
    suspend fun handleAccountCreated(event: AccountCreatedDomainEvent) {
        // Convert to integration event for main src
        val integrationEvent = AccountCreatedEvent(
            accountId = event.accountId.value,
            accountName = event.accountName,
            accountType = event.accountType.name,
            currency = event.currency.code,
            aggregateId = event.accountId.value
        )
        eventPublisher.publish(integrationEvent)
    }

    @EventListener
    suspend fun handleTransactionRecorded(event: TransactionRecordedDomainEvent) {
        val integrationEvent = TransactionRecordedEvent(
            transactionId = event.transactionId.value,
            amount = event.amount.toString(),
            currency = event.currency.code,
            customerId = event.customerId?.value,
            vendorId = event.vendorId?.value,
            aggregateId = event.transactionId.value
        )
        eventPublisher.publish(integrationEvent)
    }
}
```

### 4. Module Registration with Main Src

**Location: `modules/finance/src/main/kotlin/org/chiro/finance/FinanceModuleConfiguration.kt`**

```kotlin
@ApplicationScoped
class FinanceModuleConfiguration {

    @Inject
    lateinit var moduleRegistry: ModuleRegistry

    @Inject
    lateinit var financeModuleFacade: FinanceModuleFacadeImpl

    fun onStartup(@Observes StartupEvent event) {
        // Register finance module with main src
        moduleRegistry.registerModule(
            name = "finance",
            facade = financeModuleFacade,
            healthCheck = { checkFinanceModuleHealth() }
        )

        logger.info("💰 Finance module registered with Core Business Service")
    }

    private fun checkFinanceModuleHealth(): Boolean {
        return try {
            // Check database connectivity
            Account.count() >= 0
            true
        } catch (e: Exception) {
            logger.error("Finance module health check failed", e)
            false
        }
    }
}
```

## 📊 Implementation Priority:

1. **AccountRepositoryImpl** (Day 1-2) - Foundation for all finance operations
2. **JournalEntryRepositoryImpl** (Day 3-4) - Transaction processing capability
3. **FinanceModuleFacadeImpl** (Day 5-6) - Main src integration bridge
4. **FinanceEventAdapter** (Day 7-8) - Event-driven coordination
5. **Module Registration** (Day 9-10) - Health monitoring and discovery

## ✅ Success Criteria:

-   Finance module health check shows "UP" in main src health endpoint
-   Account creation works end-to-end (REST API → Application → Domain → Database)
-   Transaction recording triggers integration events to main src
-   Main src IntegrationEventHandler receives and processes finance events
-   Finance operations appear in main src unified logging and monitoring
