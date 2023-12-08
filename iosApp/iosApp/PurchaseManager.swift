
import Foundation
import StoreKit
import shared

// from: https://github.com/RevenueCat/storekit2-demo-app

@MainActor
class PurchaseManager: NSObject, ObservableObject {

    private let productIds = ["pro"]

    @Published
    private(set) var products: [Product] = []
    @Published
    private(set) var purchasedProductIDs = Set<String>()

    private let entitlementManager: EntitlementManager
    private var productsLoaded = false
    private var updates: Task<Void, Never>? = nil
    private var commonBilling: CommonBilling

    init(
        entitlementManager: EntitlementManager,
        commonBilling: CommonBilling
    ) {
        self.entitlementManager = entitlementManager
        self.commonBilling = commonBilling
        self.commonBilling.updateState(billingState: BillingState.NotPurchased(lastBillingMessage: "Initializing..."))
        super.init()
        
        self.updates = observeTransactionUpdates()
        SKPaymentQueue.default().add(self)
    }

    deinit {
        self.updates?.cancel()
    }

    func loadProducts() async throws {
        guard !self.productsLoaded else { return }
        self.products = try await Product.products(for: productIds)
        self.productsLoaded = true
        
       await updatePurchasedProducts()
    }

    func purchase(_ productStr: String) async throws {
       // find the product from the productStr
       let product = products.first { product in
          product.id == productStr
       }
       guard let product = product else { return }
       
        let result = try await product.purchase()

        switch result {
        case let .success(.verified(transaction)):
            // Successful purchase
            await transaction.finish()  // Required to complete transaction
            self.commonBilling.updateState(billingState: BillingState.Purchased())
            self.commonBilling.updateMessage(message: "Purchase successful")
            await self.updatePurchasedProducts()
        case let .success(.unverified(_, error)):
            // Successful purchase but transaction/receipt can't be verified
            // Could be a jail-broken phone
            self.commonBilling.updateState(billingState: BillingState.Error(errorMessage: error.localizedDescription))
            self.commonBilling.updateMessage(message: "Purchase successful but receipt is unverified")
            break
        case .pending:
            // Transaction waiting on SCA (Strong Customer Authentication) or
            // approval from Ask to Buy
            self.commonBilling.updateMessage(message: "Purchase is pending")
            self.commonBilling.updateState(billingState: BillingState.Pending())
            break
        case .userCancelled:
            // ^^^
            //self.billing.updateStatus(billingStatus: BillingStatus.Error("userCancelled"))
            //self.commonBilling.updateMessage(message: "Purchase cancelled")
            break
        @unknown default:
            self.commonBilling.updateState(billingState: BillingState.Error(errorMessage: "Unknown status"))
            self.commonBilling.updateMessage(message: "Purchase failed - unknown status")
            break
        }
    }
   
   func consume(_ productStr: String) async throws {
      print("consume not implemented")
      self.commonBilling.updateMessage(message: "Consume not implemented - use StoreKit tool to refund purchase")
   }
   
   func purchaseCommandError(_ errorMessage: String) {
      self.commonBilling.updateState(billingState: BillingState.Error(errorMessage: errorMessage))
      self.commonBilling.updateMessage(message: "Purchase command error: \(errorMessage)")
   }

    func updatePurchasedProducts() async {
        self.purchasedProductIDs.removeAll()
        var revokeReason: String? = nil
       
        for await result in Transaction.currentEntitlements {
            // With products, the `currentEntitlements` is automatically removed, so a reason is never given. This is only for subscriptions. Left here for reference.
            revokeReason = result.unsafePayloadValue.revocationReason?.localizedDescription ?? "Unknown revoke reason"
           
            // Check if the payment is verified, in kotlin: let transaction = if(result is VerificationResult.verified) result else continue
            guard case .verified(let transaction) = result else {
                continue
            }

            // Approve the entitlement (unless it's revoked)
            if transaction.revocationDate == nil {
                self.purchasedProductIDs.insert(transaction.productID)
            }
        }

        // Note: If *ANY* products found, add entitlement.
        self.entitlementManager.isProPurchased = !self.purchasedProductIDs.isEmpty
        if(self.entitlementManager.isProPurchased) {
            self.commonBilling.updateState(
               billingState: BillingState.Purchased()
            )
        } else {
           self.commonBilling.updateState(
               billingState: BillingState.NotPurchased(lastBillingMessage: revokeReason ?? "")
           )
        }
    }

    private func observeTransactionUpdates() -> Task<Void, Never> {
        Task(priority: .background) { [unowned self] in
            for await verificationResult in Transaction.updates {
                // verificationResult is checked in updatePurchasedProducts()
                print("verificationResult.payloadData.description=\(verificationResult.payloadData.description)")
               
               await self.updatePurchasedProducts()
            }
        }
    }
}

extension PurchaseManager: SKPaymentTransactionObserver {
    func paymentQueue(_ queue: SKPaymentQueue, updatedTransactions transactions: [SKPaymentTransaction]) {
        for transaction in transactions where transaction.transactionState != .purchasing { // todo - needed?
           self.commonBilling.updateMessage(message: "paymentQueue updatedTransactions: \(transaction.transactionState.rawValue)")
           queue.finishTransaction(transaction)
        }
        print("paymentQueue updatedTransactions")
    }

    func paymentQueue(_ queue: SKPaymentQueue, shouldAddStorePayment payment: SKPayment, for product: SKProduct) -> Bool {
        return true
    }

    @available(iOS 14, *)
    func paymentQueue(_ queue: SKPaymentQueue, didRevokeEntitlementsForProductIdentifiers productIdentifiers: [String]) {
        print("paymentQueue didRevokeEntitlementsForProductIdentifiers: \(productIdentifiers)")
        self.commonBilling.updateMessage(message: "Revoked purchase: \(productIdentifiers)")
    }
}
