# PHASE 1.1: Core Infrastructure Implementation Plan

## 🚨 CRITICAL: Main Src Infrastructure Gaps

### ❌ Missing Repository Implementation Framework

The shared domain has excellent base classes but no infrastructure implementations:

```kotlin
// IMPLEMENT: Base repository adapter in main src
@ApplicationScoped
abstract class BaseRepositoryAdapter<T : AggregateRoot<ID>, ID : AggregateId> {

    abstract suspend fun findById(id: ID): T?
    abstract suspend fun save(entity: T): T
    abstract suspend fun delete(id: ID): Boolean

    // Shared event publishing logic
    protected suspend fun publishEvents(entity: T) {
        entity.domainEvents.forEach { event ->
            eventPublisher.publish(event)
        }
        entity.clearEvents()
    }
}
```

### ❌ Missing Event Publishing Infrastructure

Integration events are defined but not wired:

```kotlin
// IMPLEMENT: Event publisher in main src infrastructure
@ApplicationScoped
class DomainEventPublisherImpl : DomainEventPublisher {

    @Inject
    lateinit var eventStore: EventStore

    override suspend fun publish(event: DomainEvent) {
        // Store event
        eventStore.save(event)

        // Publish to message bus
        messagingChannel.send(event)

        // Log for audit
        logger.info("Published domain event: ${event.eventType}")
    }
}
```

### ❌ Missing Module Registration System

Finance module exists but main src doesn't know about it:

```kotlin
// IMPLEMENT: Module registry in main src
@ApplicationScoped
class ModuleRegistry {

    private val modules = mutableMapOf<String, ModuleInfo>()

    fun registerModule(name: String, facade: Any, healthCheck: () -> Boolean) {
        modules[name] = ModuleInfo(name, facade, healthCheck)
    }

    fun getModuleHealth(): Map<String, String> {
        return modules.mapValues { (_, info) ->
            if (info.healthCheck()) "UP" else "DOWN"
        }
    }
}
```

## 🎯 Implementation Tasks:

1. **BaseRepositoryAdapter** - Shared persistence patterns
2. **DomainEventPublisherImpl** - Event publishing infrastructure
3. **ModuleRegistry** - Dynamic module discovery
4. **TransactionManagerImpl** - Cross-module transaction coordination
5. **ValidationServiceImpl** - Shared business rule validation

## 📊 Success Criteria:

-   ✅ All shared infrastructure services implemented
-   ✅ Event publishing working end-to-end
-   ✅ Module registration system operational
-   ✅ Health checks reporting all components
