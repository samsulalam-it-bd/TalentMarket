package com.example.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.*
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdMobManager {
    fun initialize(context: Context) {
        MobileAds.initialize(context) {}
    }
}

object RemoteConfigManager {
    private const val TAG = "RemoteConfigConfig"
    private var isInitialized = false

    // Default configuration values
    val defaults = mapOf(
        "ad_provider" to "admob", // "admob" or "house"
        "admob_enabled" to true,
        "feed_ad_frequency" to 5L,
        "admob_banner_id" to "ca-app-pub-3940256099942544/6300978111", // Test Banner
        "admob_interstitial_id" to "ca-app-pub-3940256099942544/1033173712" // Test Interstitial
    )

    fun initialize(context: Context, onComplete: () -> Unit = {}) {
        if (isInitialized) {
            onComplete()
            return
        }
        try {
            val remoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance()
            val configSettings = com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) // 0 for instant refresh in A/B testing
                .build()
            
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(defaults)

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    isInitialized = true
                    val success = task.isSuccessful
                    Log.d(TAG, "Remote Config fetch finished. Success=$success. Provider=${getAdProvider()}")
                    onComplete()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Remote Config: ${e.message}", e)
            onComplete()
        }
    }

    fun getAdProvider(): String {
        return try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance().getString("ad_provider")
        } catch (e: Exception) {
            "admob"
        }
    }

    fun isAdMobEnabled(): Boolean {
        return try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance().getBoolean("admob_enabled")
        } catch (e: Exception) {
            true
        }
    }

    fun getFeedAdFrequency(): Long {
        return try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance().getLong("feed_ad_frequency")
        } catch (e: Exception) {
            5L
        }
    }

    fun getAdMobBannerId(): String {
        return try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance().getString("admob_banner_id")
        } catch (e: Exception) {
            "ca-app-pub-3940256099942544/6300978111"
        }
    }

    fun getAdMobInterstitialId(): String {
        return try {
            com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance().getString("admob_interstitial_id")
        } catch (e: Exception) {
            "ca-app-pub-3940256099942544/1033173712"
        }
    }
}

class InterstitialAdLoader(private val context: Context, private val adUnitId: String) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun loadAd() {
        if (isLoading || interstitialAd != null) {
            return
        }
        isLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    Log.e("AdMob", adError.message)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    Log.i("AdMob", "Ad was loaded.")
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadAd() // Preload next one
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null // so it doesn't get shown again
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d("AdMob", "The interstitial ad wasn't ready yet.")
            onAdDismissed()
            loadAd() // Try to load for next time
        }
    }
}

class SmartInterstitialAdLoader(private val context: Context) {
    private val admobLoader: InterstitialAdLoader

    init {
        // Fetch IDs from Remote Config dynamically
        val admobId = RemoteConfigManager.getAdMobInterstitialId()
        admobLoader = InterstitialAdLoader(context, admobId)
    }

    fun loadAd() {
        val resolvedProvider = AdMediationManager.getResolvedProvider("interstitial")
        Log.d("SmartInterstitial", "Preloading interstitial for resolved provider: $resolvedProvider")
        when (resolvedProvider) {
            AdProvider.ADMOB -> admobLoader.loadAd()
            else -> { /* House or empty */ }
        }
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        val resolvedProvider = AdMediationManager.getResolvedProvider("interstitial")
        Log.d("SmartInterstitial", "Showing interstitial from resolved provider: $resolvedProvider")
        when (resolvedProvider) {
            AdProvider.ADMOB -> admobLoader.showAd(activity, onAdDismissed)
            else -> onAdDismissed()
        }
    }
}

@Composable
fun BannerAdView(adUnitId: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun SmartBannerAd(
    adUnitId: String,
    viewModel: TalentViewModel,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier
) {
    val resolvedProvider = remember { AdMediationManager.getResolvedProvider("banner") }
    var currentProvider by remember(resolvedProvider) { mutableStateOf(resolvedProvider) }

    Log.d("SmartBannerAd1", "Rendering banner starting with resolved provider: $resolvedProvider, current: $currentProvider")

    when (currentProvider) {
        AdProvider.ADMOB -> {
            val configAdId = RemoteConfigManager.getAdMobBannerId()
            AndroidView(
                modifier = modifier.fillMaxWidth(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = configAdId.ifEmpty { adUnitId }
                        adListener = object : com.google.android.gms.ads.AdListener() {
                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                val next = AdMediationManager.getFallbackProvider(AdProvider.ADMOB)
                                Log.e("SmartBannerAd1", "AdMob banner load failed: ${loadAdError.message}. Switching to fallback: $next")
                                currentProvider = next
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
        else -> {
            // Fallback House Campaign Banner Ad!
            val adPlacements by viewModel.adPlacements.collectAsStateWithLifecycle()
            if (adPlacements.isNotEmpty()) {
                val randomAd = remember(adPlacements) { adPlacements.random() }
                
                LaunchedEffect(randomAd.id) {
                    viewModel.recordAdView(randomAd.id)
                }
                
                FallbackHouseAdCard(
                    ad = randomAd,
                    onClick = {
                        viewModel.recordAdClick(randomAd.id)
                        when (randomAd.targetType) {
                            "premium_subscription" -> navController.navigate("premium")
                            "job_boost" -> navController.navigate("boost")
                            "external_url" -> {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(randomAd.targetUrl))
                                    navController.context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun FallbackHouseAdCard(
    ad: com.example.data.AdPlacement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = remember(ad.customGradientStart) {
        try { Color(android.graphics.Color.parseColor(ad.customGradientStart)) }
        catch (e: Exception) { Color(0xFF7C3AED) }
    }
    val endColor = remember(ad.customGradientEnd) {
        try { Color(android.graphics.Color.parseColor(ad.customGradientEnd)) }
        catch (e: Exception) { Color(0xFFDB2777) }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(colors = listOf(startColor, endColor))
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1.0f).padding(end = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "SPONSORED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = ad.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ad.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                // Action Button
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = startColor
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                ) {
                    Text(
                        text = ad.actionText.ifEmpty { "Learn More" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
