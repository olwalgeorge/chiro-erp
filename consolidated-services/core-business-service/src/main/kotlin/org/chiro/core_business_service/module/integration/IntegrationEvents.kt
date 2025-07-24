package org.chiro.core_business_service.module.integration

import org.chiro.core_business_service.shared.domain.event.BaseIntegrationEvent

/**
 * Integration events for cross-module communication in the Core Business Service
 * These events enable loose coupling between modules while maintaining consistency
 */

// Finance Integration Events
data class CustomerCreatedEvent(
    val customerId: String,
    val customerName: String,
    val email: String,
    val creditLimit: String? = null,
    aggregateId: String = customerId
) : BaseIntegrationEvent(aggregateId)

data class AccountCreatedEvent(
    val accountId: String,
    val accountName: String,
    val accountType: String,
    val currency: String,
    aggregateId: String = accountId
) : BaseIntegrationEvent(aggregateId)

data class TransactionRecordedEvent(
    val transactionId: String,
    val amount: String,
    val currency: String,
    val customerId: String? = null,
    val vendorId: String? = null,
    aggregateId: String = transactionId
) : BaseIntegrationEvent(aggregateId)

// Inventory Integration Events
data class ProductCreatedEvent(
    val productId: String,
    val sku: String,
    val productName: String,
    val price: String,
    val currency: String,
    aggregateId: String = productId
) : BaseIntegrationEvent(aggregateId)

data class StockLevelChangedEvent(
    val productId: String,
    val locationId: String,
    val previousQuantity: Int,
    val newQuantity: Int,
    val changeReason: String,
    aggregateId: String = productId
) : BaseIntegrationEvent(aggregateId)

data class StockReservedEvent(
    val reservationId: String,
    val productId: String,
    val locationId: String,
    val quantity: Int,
    val orderId: String? = null,
    val workOrderId: String? = null,
    aggregateId: String = reservationId
) : BaseIntegrationEvent(aggregateId)

data class StockReleasedEvent(
    val reservationId: String,
    val productId: String,
    val locationId: String,
    val quantity: Int,
    aggregateId: String = reservationId
) : BaseIntegrationEvent(aggregateId)

// Sales Integration Events
data class SalesOrderCreatedEvent(
    val orderId: String,
    val customerId: String,
    val orderTotal: String,
    val currency: String,
    val items: List<OrderItemInfo>,
    aggregateId: String = orderId
) : BaseIntegrationEvent(aggregateId)

data class SalesOrderStatusChangedEvent(
    val orderId: String,
    val previousStatus: String,
    val newStatus: String,
    val customerId: String,
    aggregateId: String = orderId
) : BaseIntegrationEvent(aggregateId)

data class SalesOrderShippedEvent(
    val orderId: String,
    val customerId: String,
    val shippingAddress: String,
    val trackingNumber: String? = null,
    val items: List<OrderItemInfo>,
    aggregateId: String = orderId
) : BaseIntegrationEvent(aggregateId)

// Manufacturing Integration Events
data class WorkOrderCreatedEvent(
    val workOrderId: String,
    val productId: String,
    val quantity: Int,
    val dueDate: String,
    val salesOrderId: String? = null,
    aggregateId: String = workOrderId
) : BaseIntegrationEvent(aggregateId)

data class WorkOrderCompletedEvent(
    val workOrderId: String,
    val productId: String,
    val plannedQuantity: Int,
    val actualQuantity: Int,
    val locationId: String,
    aggregateId: String = workOrderId
) : BaseIntegrationEvent(aggregateId)

data class ProductionStartedEvent(
    val workOrderId: String,
    val productId: String,
    val quantity: Int,
    val startDate: String,
    aggregateId: String = workOrderId
) : BaseIntegrationEvent(aggregateId)

// Procurement Integration Events
data class PurchaseOrderCreatedEvent(
    val purchaseOrderId: String,
    val vendorId: String,
    val orderTotal: String,
    val currency: String,
    val items: List<PurchaseOrderItemInfo>,
    aggregateId: String = purchaseOrderId
) : BaseIntegrationEvent(aggregateId)

data class PurchaseOrderReceivedEvent(
    val purchaseOrderId: String,
    val vendorId: String,
    val receivedDate: String,
    val items: List<PurchaseOrderItemInfo>,
    aggregateId: String = purchaseOrderId
) : BaseIntegrationEvent(aggregateId)

data class VendorCreatedEvent(
    val vendorId: String,
    val vendorName: String,
    val email: String,
    val paymentTerms: String? = null,
    aggregateId: String = vendorId
) : BaseIntegrationEvent(aggregateId)

// Supporting data classes
data class OrderItemInfo(
    val productId: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: String,
    val lineTotal: String
)

data class PurchaseOrderItemInfo(
    val productId: String,
    val sku: String,
    val quantity: Int,
    val unitPrice: String,
    val lineTotal: String
)
