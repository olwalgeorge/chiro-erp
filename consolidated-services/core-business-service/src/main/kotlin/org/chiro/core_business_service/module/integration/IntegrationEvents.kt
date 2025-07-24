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
    override val aggregateId: String = customerId
) : BaseIntegrationEvent(aggregateId, "Customer")

data class AccountCreatedEvent(
    val accountId: String,
    val accountName: String,
    val accountType: String,
    val currency: String,
    override val aggregateId: String = accountId
) : BaseIntegrationEvent(aggregateId, "Account")

data class TransactionRecordedEvent(
    val transactionId: String,
    val amount: String,
    val currency: String,
    val customerId: String? = null,
    val vendorId: String? = null,
    override val aggregateId: String = transactionId
) : BaseIntegrationEvent(aggregateId, "Transaction")

// Inventory Integration Events
data class ProductCreatedEvent(
    val productId: String,
    val sku: String,
    val productName: String,
    val price: String,
    val currency: String,
    override val aggregateId: String = productId
) : BaseIntegrationEvent(aggregateId, "Product")

data class StockLevelChangedEvent(
    val productId: String,
    val locationId: String,
    val previousQuantity: Int,
    val newQuantity: Int,
    val changeReason: String,
    override val aggregateId: String = productId
) : BaseIntegrationEvent(aggregateId, "Stock")

data class StockReservedEvent(
    val reservationId: String,
    val productId: String,
    val locationId: String,
    val quantity: Int,
    val orderId: String? = null,
    val workOrderId: String? = null,
    override val aggregateId: String = reservationId
) : BaseIntegrationEvent(aggregateId, "StockReservation")

data class StockReleasedEvent(
    val reservationId: String,
    val productId: String,
    val locationId: String,
    val quantity: Int,
    override val aggregateId: String = reservationId
) : BaseIntegrationEvent(aggregateId, "StockReservation")

// Sales Integration Events
data class SalesOrderCreatedEvent(
    val orderId: String,
    val customerId: String,
    val orderTotal: String,
    val currency: String,
    val items: List<OrderItemInfo>,
    override val aggregateId: String = orderId
) : BaseIntegrationEvent(aggregateId, "SalesOrder")

data class SalesOrderStatusChangedEvent(
    val orderId: String,
    val previousStatus: String,
    val newStatus: String,
    val customerId: String,
    override val aggregateId: String = orderId
) : BaseIntegrationEvent(aggregateId, "SalesOrder")

data class SalesOrderShippedEvent(
    val orderId: String,
    val customerId: String,
    val shippingAddress: String,
    val trackingNumber: String? = null,
    val items: List<OrderItemInfo>,
    override val aggregateId: String = orderId
) : BaseIntegrationEvent(aggregateId, "SalesOrder")

// Manufacturing Integration Events
data class WorkOrderCreatedEvent(
    val workOrderId: String,
    val productId: String,
    val quantity: Int,
    val dueDate: String,
    val salesOrderId: String? = null,
    override val aggregateId: String = workOrderId
) : BaseIntegrationEvent(aggregateId, "WorkOrder")

data class WorkOrderCompletedEvent(
    val workOrderId: String,
    val productId: String,
    val plannedQuantity: Int,
    val actualQuantity: Int,
    val locationId: String,
    override val aggregateId: String = workOrderId
) : BaseIntegrationEvent(aggregateId, "WorkOrder")

data class ProductionStartedEvent(
    val workOrderId: String,
    val productId: String,
    val quantity: Int,
    val startDate: String,
    override val aggregateId: String = workOrderId
) : BaseIntegrationEvent(aggregateId, "WorkOrder")

// Procurement Integration Events
data class PurchaseOrderCreatedEvent(
    val purchaseOrderId: String,
    val vendorId: String,
    val orderTotal: String,
    val currency: String,
    val items: List<PurchaseOrderItemInfo>,
    override val aggregateId: String = purchaseOrderId
) : BaseIntegrationEvent(aggregateId, "PurchaseOrder")

data class PurchaseOrderReceivedEvent(
    val purchaseOrderId: String,
    val vendorId: String,
    val receivedDate: String,
    val items: List<PurchaseOrderItemInfo>,
    override val aggregateId: String = purchaseOrderId
) : BaseIntegrationEvent(aggregateId, "PurchaseOrder")

data class VendorCreatedEvent(
    val vendorId: String,
    val vendorName: String,
    val email: String,
    val paymentTerms: String? = null,
    override val aggregateId: String = vendorId
) : BaseIntegrationEvent(aggregateId, "Vendor")

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
