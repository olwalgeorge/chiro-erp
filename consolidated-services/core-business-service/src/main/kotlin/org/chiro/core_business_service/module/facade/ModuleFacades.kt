package org.chiro.core_business_service.module.facade

import org.chiro.core_business_service.shared.application.query.PageRequest
import org.chiro.core_business_service.shared.application.query.PageResult

/**
 * Finance module facade for inter-module communication
 * Provides a clean interface for other modules to interact with finance functionality
 */
interface FinanceModuleFacade {
    
    /**
     * Account Management
     */
    suspend fun getAccountById(accountId: String): AccountDto?
    suspend fun getAccountsByType(accountType: String): List<AccountDto>
    suspend fun createAccount(request: CreateAccountRequest): AccountDto
    
    /**
     * Transaction Management
     */
    suspend fun recordTransaction(request: RecordTransactionRequest): TransactionDto
    suspend fun getTransactionsByAccount(accountId: String, pageRequest: PageRequest): PageResult<TransactionDto>
    
    /**
     * Financial Reporting
     */
    suspend fun getAccountBalance(accountId: String): BalanceDto
    suspend fun getTrialBalance(asOfDate: String? = null): TrialBalanceDto
    
    /**
     * Customer/Vendor Management
     */
    suspend fun createCustomer(request: CreateCustomerRequest): CustomerDto
    suspend fun createVendor(request: CreateVendorRequest): VendorDto
    suspend fun getCustomerById(customerId: String): CustomerDto?
    suspend fun getVendorById(vendorId: String): VendorDto?
}

/**
 * Inventory module facade for inter-module communication
 */
interface InventoryModuleFacade {
    
    /**
     * Product Management
     */
    suspend fun getProductById(productId: String): ProductDto?
    suspend fun getProductsBySku(sku: String): ProductDto?
    suspend fun createProduct(request: CreateProductRequest): ProductDto
    suspend fun updateProduct(productId: String, request: UpdateProductRequest): ProductDto
    
    /**
     * Stock Management
     */
    suspend fun getStockLevel(productId: String, locationId: String): StockLevelDto
    suspend fun reserveStock(request: ReserveStockRequest): StockReservationDto
    suspend fun releaseStock(reservationId: String): Unit
    suspend fun adjustStock(request: AdjustStockRequest): StockMovementDto
    
    /**
     * Location Management
     */
    suspend fun getLocationById(locationId: String): LocationDto?
    suspend fun getActiveLocations(): List<LocationDto>
}

/**
 * Sales module facade for inter-module communication
 */
interface SalesModuleFacade {
    
    /**
     * Order Management
     */
    suspend fun createSalesOrder(request: CreateSalesOrderRequest): SalesOrderDto
    suspend fun getSalesOrderById(orderId: String): SalesOrderDto?
    suspend fun updateSalesOrderStatus(orderId: String, status: String): SalesOrderDto
    suspend fun cancelSalesOrder(orderId: String, reason: String): SalesOrderDto
    
    /**
     * Customer Management
     */
    suspend fun getCustomerOrders(customerId: String, pageRequest: PageRequest): PageResult<SalesOrderDto>
    suspend fun getCustomerCreditStatus(customerId: String): CustomerCreditStatusDto
    
    /**
     * Pricing
     */
    suspend fun calculateOrderTotal(request: CalculateOrderTotalRequest): OrderTotalDto
    suspend fun applyDiscount(orderId: String, discount: DiscountDto): SalesOrderDto
}

/**
 * Manufacturing module facade for inter-module communication
 */
interface ManufacturingModuleFacade {
    
    /**
     * Work Order Management
     */
    suspend fun createWorkOrder(request: CreateWorkOrderRequest): WorkOrderDto
    suspend fun getWorkOrderById(workOrderId: String): WorkOrderDto?
    suspend fun updateWorkOrderStatus(workOrderId: String, status: String): WorkOrderDto
    suspend fun completeWorkOrder(workOrderId: String, request: CompleteWorkOrderRequest): WorkOrderDto
    
    /**
     * Bill of Materials
     */
    suspend fun getBomByProduct(productId: String): BillOfMaterialsDto?
    suspend fun createBom(request: CreateBomRequest): BillOfMaterialsDto
    
    /**
     * Production Planning
     */
    suspend fun getProductionCapacity(date: String): ProductionCapacityDto
    suspend fun scheduleProduction(request: ScheduleProductionRequest): ProductionScheduleDto
}

/**
 * Procurement module facade for inter-module communication
 */
interface ProcurementModuleFacade {
    
    /**
     * Purchase Order Management
     */
    suspend fun createPurchaseOrder(request: CreatePurchaseOrderRequest): PurchaseOrderDto
    suspend fun getPurchaseOrderById(orderId: String): PurchaseOrderDto?
    suspend fun approvePurchaseOrder(orderId: String, approver: String): PurchaseOrderDto
    suspend fun receivePurchaseOrder(orderId: String, request: ReceivePurchaseOrderRequest): PurchaseOrderDto
    
    /**
     * Vendor Management
     */
    suspend fun getVendorById(vendorId: String): VendorDto?
    suspend fun getVendorsByProduct(productId: String): List<VendorDto>
    suspend fun getVendorPricing(vendorId: String, productId: String): VendorPricingDto?
    
    /**
     * Requisition Management
     */
    suspend fun createRequisition(request: CreateRequisitionRequest): RequisitionDto
    suspend fun approveRequisition(requisitionId: String, approver: String): RequisitionDto
    suspend fun convertRequisitionToPo(requisitionId: String): PurchaseOrderDto
}

// DTOs for facade communication
data class AccountDto(val id: String, val name: String, val type: String, val balance: String)
data class TransactionDto(val id: String, val accountId: String, val amount: String, val description: String)
data class BalanceDto(val accountId: String, val balance: String, val currency: String)
data class TrialBalanceDto(val accounts: List<AccountDto>, val totalDebits: String, val totalCredits: String)
data class CustomerDto(val id: String, val name: String, val email: String, val status: String)
data class VendorDto(val id: String, val name: String, val email: String, val status: String)

data class ProductDto(val id: String, val sku: String, val name: String, val price: String)
data class StockLevelDto(val productId: String, val locationId: String, val quantity: Int, val reserved: Int)
data class StockReservationDto(val id: String, val productId: String, val quantity: Int)
data class StockMovementDto(val id: String, val productId: String, val quantity: Int, val type: String)
data class LocationDto(val id: String, val name: String, val type: String)

data class SalesOrderDto(val id: String, val customerId: String, val status: String, val total: String)
data class CustomerCreditStatusDto(val customerId: String, val creditLimit: String, val availableCredit: String)
data class OrderTotalDto(val subtotal: String, val tax: String, val total: String)
data class DiscountDto(val type: String, val value: String, val reason: String)

data class WorkOrderDto(val id: String, val productId: String, val quantity: Int, val status: String)
data class BillOfMaterialsDto(val productId: String, val components: List<ComponentDto>)
data class ComponentDto(val productId: String, val quantity: Int)
data class ProductionCapacityDto(val date: String, val capacity: Int, val utilized: Int)
data class ProductionScheduleDto(val workOrderId: String, val scheduledDate: String)

data class PurchaseOrderDto(val id: String, val vendorId: String, val status: String, val total: String)
data class VendorPricingDto(val vendorId: String, val productId: String, val price: String)
data class RequisitionDto(val id: String, val requesterId: String, val status: String)

// Request DTOs
data class CreateAccountRequest(val name: String, val type: String, val parentAccountId: String?)
data class RecordTransactionRequest(val entries: List<TransactionEntryDto>)
data class TransactionEntryDto(val accountId: String, val amount: String, val type: String)
data class CreateCustomerRequest(val name: String, val email: String)
data class CreateVendorRequest(val name: String, val email: String)

data class CreateProductRequest(val sku: String, val name: String, val price: String)
data class UpdateProductRequest(val name: String?, val price: String?)
data class ReserveStockRequest(val productId: String, val locationId: String, val quantity: Int)
data class AdjustStockRequest(val productId: String, val locationId: String, val quantity: Int, val reason: String)

data class CreateSalesOrderRequest(val customerId: String, val items: List<OrderItemDto>)
data class OrderItemDto(val productId: String, val quantity: Int, val price: String)
data class CalculateOrderTotalRequest(val items: List<OrderItemDto>)

data class CreateWorkOrderRequest(val productId: String, val quantity: Int, val dueDate: String)
data class CompleteWorkOrderRequest(val actualQuantity: Int, val completionNotes: String)
data class CreateBomRequest(val productId: String, val components: List<ComponentDto>)
data class ScheduleProductionRequest(val workOrderId: String, val scheduledDate: String)

data class CreatePurchaseOrderRequest(val vendorId: String, val items: List<PurchaseOrderItemDto>)
data class PurchaseOrderItemDto(val productId: String, val quantity: Int, val price: String)
data class ReceivePurchaseOrderRequest(val items: List<ReceivedItemDto>)
data class ReceivedItemDto(val productId: String, val quantity: Int)
data class CreateRequisitionRequest(val requesterId: String, val items: List<RequisitionItemDto>)
data class RequisitionItemDto(val productId: String, val quantity: Int, val justification: String)
