package com.example.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillingManager(private val context: Context) {
  
  private val billingClient = BillingClient.newBuilder(context)
    .setListener { billingResult, purchases ->
      if (billingResult.responseCode == BillingClient.BillingResponseCode.OK 
          && purchases != null) {
        purchases.forEach { handlePurchase(it) }
      }
    }
    .enablePendingPurchases()
    .build()

  // Product IDs — must match Google Play Console exactly
  companion object {
    const val PLAN_SILVER = "talentmarket_silver_monthly"
    const val PLAN_GOLD = "talentmarket_gold_monthly"
    const val PLAN_BUSINESS = "talentmarket_business_monthly"
    const val PLAN_ENTERPRISE = "talentmarket_enterprise_monthly"
  }

  fun startConnection(onReady: () -> Unit) {
    billingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(result: BillingResult) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
          onReady()
        }
      }
      override fun onBillingServiceDisconnected() {
        // retry connection
      }
    })
  }

  suspend fun queryProducts(productIds: List<String>): List<ProductDetails> {
    val subParams = QueryProductDetailsParams.newBuilder()
      .setProductList(
        productIds.map { id ->
          QueryProductDetailsParams.Product.newBuilder()
            .setProductId(id)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        }
      ).build()
    
    val inAppParams = QueryProductDetailsParams.newBuilder()
      .setProductList(
        productIds.map { id ->
          QueryProductDetailsParams.Product.newBuilder()
            .setProductId(id)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        }
      ).build()

    val subResult = billingClient.queryProductDetails(subParams).productDetailsList ?: emptyList()
    val inAppResult = billingClient.queryProductDetails(inAppParams).productDetailsList ?: emptyList()
    
    return subResult + inAppResult
  }

  fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
    val isSubscription = productDetails.productType == BillingClient.ProductType.SUBS
    val offerToken = if (isSubscription) {
      productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
    } else {
      null
    }
    
    val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
      .setProductDetails(productDetails)
    if (offerToken != null) {
      productDetailsParams.setOfferToken(offerToken)
    }
    
    val billingFlowParams = BillingFlowParams.newBuilder()
      .setProductDetailsParamsList(
        listOf(productDetailsParams.build())
      ).build()
    
    billingClient.launchBillingFlow(activity, billingFlowParams)
  }

  private fun handlePurchase(purchase: Purchase) {
    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
      // Acknowledge purchase
      val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
        .setPurchaseToken(purchase.purchaseToken)
        .build()
      billingClient.acknowledgePurchase(acknowledgeParams) { }
      
      // Save to Firestore
      savePurchaseToFirestore(purchase)
    }
  }

  private fun savePurchaseToFirestore(purchase: Purchase) {
    val productId = purchase.products.firstOrNull() ?: return
    
    // Check if this is a one-time boost product
    // not a subscription plan
    val boostProductIds = listOf(
      "boost_7_days",
      "boost_14_days", 
      "boost_30_days",
      "boost_3_months"
    )
    
    if (boostProductIds.any { productId.contains(it) }) {
      handleBoostPurchase(purchase, productId)
      return
    }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val plan = when {
      purchase.products.contains(PLAN_SILVER) -> "silver"
      purchase.products.contains(PLAN_GOLD) -> "gold"
      purchase.products.contains(PLAN_BUSINESS) -> "business"
      purchase.products.contains(PLAN_ENTERPRISE) -> "enterprise"
      else -> "free"
    }
    
    val endDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
    
    FirebaseFirestore.getInstance()
      .collection("users")
      .document(userId)
      .update(mapOf(
        "subscription.plan" to plan,
        "subscription.isActive" to true,
        "subscription.startDate" to System.currentTimeMillis(),
        "subscription.endDate" to endDate,
        "subscription.transactionId" to purchase.purchaseToken
      ))
      .addOnFailureListener {
        com.example.ui.FirestoreErrorHandler.handleError(it, "Billing-Subscription")
      }
  }

  private fun handleBoostPurchase(
    purchase: Purchase, 
    productId: String
  ) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    
    // Determine boost duration from product ID
    val boostDays = when {
      productId.contains("7") -> 7
      productId.contains("14") -> 14
      productId.contains("30") -> 30
      productId.contains("3_month") -> 90
      else -> 7
    }
    
    val boostEndDate = System.currentTimeMillis() + 
      (boostDays.toLong() * 24 * 60 * 60 * 1000)
    
    // Save boost purchase record
    db.collection("boost_purchases").add(
      hashMapOf(
        "userId" to userId,
        "productId" to productId,
        "purchasedAt" to System.currentTimeMillis(),
        "boostEndDate" to boostEndDate,
        "amount" to 0,
        "status" to "active"
      )
    )
    .addOnFailureListener {
      com.example.ui.FirestoreErrorHandler.handleError(it, "Billing-BoostRecord")
    }
    
    // Update user's boost status only
    // NOT subscription fields
    db.collection("users").document(userId)
      .update(mapOf(
        "isBoosted" to true,
        "boostEndDate" to boostEndDate
      ))
      .addOnFailureListener {
        com.example.ui.FirestoreErrorHandler.handleError(it, "Billing-UserBoost")
      }
  }
}

