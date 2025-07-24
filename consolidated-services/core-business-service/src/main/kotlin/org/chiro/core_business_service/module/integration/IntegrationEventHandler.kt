package org.chiro.core_business_service.module.integration

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import org.chiro.core_business_service.module.facade.*
import org.jboss.logging.Logger

/**
 * Integration event handlers for cross-module communication
 * These handlers coordinate between different modules when business events occur
 */
@ApplicationScoped
class IntegrationEventHandler {
    
    private val logger = Logger.getLogger(IntegrationEventHandler::class.java)
    
    @Inject
    lateinit var financeModuleFacade: FinanceModuleFacade
    
    @Inject
    lateinit var inventoryModuleFacade: InventoryModuleFacade
    
    @Inject
    lateinit var salesModuleFacade: SalesModuleFacade
    
    @Inject
    lateinit var manufacturingModuleFacade: ManufacturingModuleFacade
    
    @Inject
    lateinit var procurementModuleFacade: ProcurementModuleFacade
    
    // Product Management Integration
    
    /**
     * When a product is created in inventory, update pricing in other modules
     */
    suspend fun handleProductCreated(@Observes event: ProductCreatedEvent) {
        logger.info("🆕 Processing ProductCreatedEvent for product ${event.productId}")
        
        try {
            // No specific cross-module actions needed for product creation yet
            // This could be extended to:
            // - Create default GL accounts for the product
            // - Set up default pricing rules
            // - Create default BOMs
            
            logger.info("✅ ProductCreatedEvent processed successfully")
        } catch (e: Exception) {
            logger.error("❌ Error processing ProductCreatedEvent for product ${event.productId}", e)
        }
    }
    
    // Sales Order Integration
    
    /**
     * When a sales order is created, reserve inventory and record financial transactions
     */
    suspend fun handleSalesOrderCreated(@Observes event: SalesOrderCreatedEvent) {
        logger.info("📝 Processing SalesOrderCreatedEvent for order ${event.orderId}")
        
        try {
            // Reserve inventory for each item
            for (item in event.items) {
                try {
                    inventoryModuleFacade.reserveStock(
                        ReserveStockRequest(
                            productId = item.productId,
                            locationId = "DEFAULT", // TODO: Get from order or configuration
                            quantity = item.quantity
                        )
                    )
                    logger.debug("Reserved ${item.quantity} units of product ${item.productId}")
                } catch (e: Exception) {
                    logger.warn("Failed to reserve stock for product ${item.productId}: ${e.message}")
                    // Could implement compensation logic here
                }
            }
            
            // Record accounts receivable transaction in finance
            financeModuleFacade.recordTransaction(
                RecordTransactionRequest(
                    entries = listOf(
                        TransactionEntryDto(
                            accountId = "ACCOUNTS_RECEIVABLE", // TODO: Get from configuration
                            amount = event.orderTotal,
                            type = "DEBIT"
                        ),
                        TransactionEntryDto(
                            accountId = "SALES_REVENUE", // TODO: Get from configuration
                            amount = event.orderTotal,
                            type = "CREDIT"
                        )
                    )
                )
            )
            
            logger.info("✅ SalesOrderCreatedEvent processed successfully")
        } catch (e: Exception) {
            logger.error("❌ Error processing SalesOrderCreatedEvent for order ${event.orderId}", e)
        }
    }
    
    /**
     * When a sales order is shipped, update inventory and record cost of goods sold
     */
    suspend fun handleSalesOrderShipped(@Observes event: SalesOrderShippedEvent) {
        logger.info("🚚 Processing SalesOrderShippedEvent for order ${event.orderId}")
        
        try {
            var totalCogs = "0.00"
            
            // Update inventory levels and calculate COGS
            for (item in event.items) {
                try {
                    // Adjust inventory for shipped quantities
                    inventoryModuleFacade.adjustStock(
                        AdjustStockRequest(
                            productId = item.productId,
                            locationId = "DEFAULT", // TODO: Get from shipment
                            quantity = -item.quantity,
                            reason = "Sales shipment - Order ${event.orderId}"
                        )
                    )
                    
                    // TODO: Calculate COGS based on inventory costing method
                    // This would involve getting the product cost from inventory
                    
                    logger.debug("Adjusted inventory for shipped product ${item.productId}")
                } catch (e: Exception) {
                    logger.warn("Failed to adjust inventory for product ${item.productId}: ${e.message}")
                }
            }
            
            // Record COGS transaction
            if (totalCogs != "0.00") {
                financeModuleFacade.recordTransaction(
                    RecordTransactionRequest(
                        entries = listOf(
                            TransactionEntryDto(
                                accountId = "COST_OF_GOODS_SOLD",
                                amount = totalCogs,
                                type = "DEBIT"
                            ),
                            TransactionEntryDto(
                                accountId = "INVENTORY_ASSET",
                                amount = totalCogs,
                                type = "CREDIT"
                            )
                        )
                    )
                )
            }
            
            logger.info("✅ SalesOrderShippedEvent processed successfully")
        } catch (e: Exception) {
            logger.error("❌ Error processing SalesOrderShippedEvent for order ${event.orderId}", e)
        }
    }
    
    // Manufacturing Integration
    
    /**
     * When a work order is created, reserve raw materials
     */
    suspend fun handleWorkOrderCreated(@Observes event: WorkOrderCreatedEvent) {
        logger.info("🏭 Processing WorkOrderCreatedEvent for work order ${event.workOrderId}")
        
        try {
            // Get BOM for the product
            val bom = manufacturingModuleFacade.getBomByProduct(event.productId)
            
            if (bom != null) {
                // Reserve materials for each component
                for (component in bom.components) {
                    val requiredQuantity = component.quantity * event.quantity
                    
                    try {
                        inventoryModuleFacade.reserveStock(
                            ReserveStockRequest(
                                productId = component.productId,
                                locationId = "PRODUCTION", // TODO: Get from work order
                                quantity = requiredQuantity
                            )
                        )
                        logger.debug("Reserved $requiredQuantity units of component ${component.productId}")
                    } catch (e: Exception) {
                        logger.warn("Failed to reserve component ${component.productId}: ${e.message}")
                    }
                }
            }
            
            logger.info("✅ WorkOrderCreatedEvent processed successfully")
        } catch (e: Exception) {
            logger.error("❌ Error processing WorkOrderCreatedEvent for work order ${event.workOrderId}", e)
        }
    }
    
    /**
     * When a work order is completed, update inventory and record production costs
     */
    suspend fun handleWorkOrderCompleted(@Observes event: WorkOrderCompletedEvent) {
        logger.info("✅ Processing WorkOrderCompletedEvent for work order ${event.workOrderId}")
        
        try {
            // Add finished goods to inventory
            inventoryModuleFacade.adjustStock(
                AdjustStockRequest(
                    productId = event.productId,
                    locationId = event.locationId,
                    quantity = event.actualQuantity,
                    reason = "Production completion - Work Order ${event.workOrderId}"
                )
            )
            
            // TODO: Record production costs in finance
            // This would involve:
            // - Material costs
            // - Labor costs
            // - Overhead allocation
            
            logger.info("✅ WorkOrderCompletedEvent processed successfully")
        } catch (e: Exception) {
            logger.error("❌ Error processing WorkOrderCompletedEvent for work order ${event.workOrderId}", e)
        }
    }
    
    // Procurement Integration
    
    /**
     * When a purchase order is received, update inventory and record liabilities
     */
    suspend fun handlePurchaseOrderReceived(@Observes event: PurchaseOrderReceivedEvent) {
        logger.info("📦 Processing PurchaseOrderReceivedEvent for PO ${event.purchaseOrderId}")
        
        try {
            var totalAmount = "0.00"
            
            // Update inventory for received items
            for (item in event.items) {
                try {
                    inventoryModuleFacade.adjustStock(
                        AdjustStockRequest(
                            productId = item.productId,
                            locationId = "RECEIVING", // TODO: Get from receipt
                            quantity = item.quantity,
                            reason = "Purchase receipt - PO ${event.purchaseOrderId}"
                        )
                    )
                    
                    // Add to total amount (simplified - should get actual cost)
                    totalAmount = item.lineTotal
                    
                    logger.debug("Received ${item.quantity} units of product ${item.productId}")
                } catch (e: Exception) {
                    logger.warn("Failed to update inventory for product ${item.productId}: ${e.message}")
                }
            }
            
            // Record accounts payable transaction
            if (totalAmount != "0.00") {
                financeModuleFacade.recordTransaction(
                    RecordTransactionRequest(
                        entries = listOf(
                            TransactionEntryDto(
                                accountId = "INVENTORY_ASSET",
                                amount = totalAmount,
                                type = "DEBIT"
                            ),
                            TransactionEntryDto(
                                accountId = "ACCOUNTS_PAYABLE",
                                amount = totalAmount,
                                type = "CREDIT"
                            )
                        )
                    )
                )
            }
            
            logger.info("✅ PurchaseOrderReceivedEvent processed successfully")
        } catch (e: Exception) {
            logger.error("❌ Error processing PurchaseOrderReceivedEvent for PO ${event.purchaseOrderId}", e)
        }
    }
    
    // Customer/Vendor Management Integration
    
    /**
     * When a customer is created, set up finance accounts
     */
    suspend fun handleCustomerCreated(@Observes event: CustomerCreatedEvent) {
        logger.info("👤 Processing CustomerCreatedEvent for customer ${event.customerId}")
        
        try {
            // Create customer-specific accounts receivable account if needed
            // This is optional - many systems use a single AR account
            
            logger.info("✅ CustomerCreatedEvent processed successfully")
        } catch (e: Exception) {
            logger.error("❌ Error processing CustomerCreatedEvent for customer ${event.customerId}", e)
        }
    }
    
    /**
     * When a vendor is created, set up finance accounts
     */
    suspend fun handleVendorCreated(@Observes event: VendorCreatedEvent) {
        logger.info("🏢 Processing VendorCreatedEvent for vendor ${event.vendorId}")
        
        try {
            // Create vendor-specific accounts payable account if needed
            // This is optional - many systems use a single AP account
            
            logger.info("✅ VendorCreatedEvent processed successfully")
        } catch (e: Exception) {
            logger.error("❌ Error processing VendorCreatedEvent for vendor ${event.vendorId}", e)
        }
    }
}
