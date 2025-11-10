package com.tuffgames.incrementalquest

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * ShopActivity - In-App Purchase Shop
 *
 * Shows all available IAPs organized by tier
 */
class ShopActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var shopContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Main layout
        mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(15, 15, 45))
        mainLayout.setPadding(20, 20, 20, 20)

        // Title
        val title = TextView(this)
        title.text = "💰 SHOP"
        title.textSize = 32f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Subtitle
        val subtitle = TextView(this)
        subtitle.text = "Optional purchases to accelerate your progress"
        subtitle.textSize = 14f
        subtitle.setTextColor(Color.LTGRAY)
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 10, 0, 20)
        mainLayout.addView(subtitle)

        // Scroll view for shop items
        val scrollView = ScrollView(this)
        shopContainer = LinearLayout(this)
        shopContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(shopContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Populate shop
        populateShop()

        // Back button
        val backButton = Button(this)
        backButton.text = "← BACK"
        backButton.setBackgroundColor(Color.rgb(80, 80, 80))
        backButton.setTextColor(Color.WHITE)
        backButton.setOnClickListener {
            finish()
        }
        mainLayout.addView(backButton)

        setContentView(mainLayout)
    }

    private fun populateShop() {
        shopContainer.removeAllViews()

        // Tier 1: Starter Boosts
        addTierHeader("⭐ STARTER BOOSTS")
        IAPType.getTierIAPs(1).forEach { iap ->
            shopContainer.addView(createIAPCard(iap))
        }

        // Tier 2: Progression Boosts
        addTierHeader("⭐⭐ PROGRESSION BOOSTS")
        IAPType.getTierIAPs(2).forEach { iap ->
            shopContainer.addView(createIAPCard(iap))
        }

        // Tier 3: Power Boosts
        addTierHeader("⭐⭐⭐ POWER BOOSTS")
        IAPType.getTierIAPs(3).forEach { iap ->
            shopContainer.addView(createIAPCard(iap))
        }

        // Tier 4: Premium
        addTierHeader("👑 PREMIUM")
        IAPType.getTierIAPs(4).forEach { iap ->
            shopContainer.addView(createIAPCard(iap))
        }
    }

    private fun addTierHeader(title: String) {
        val header = TextView(this)
        header.text = title
        header.textSize = 20f
        header.setTextColor(Color.YELLOW)
        header.gravity = Gravity.CENTER
        header.setPadding(0, 20, 0, 10)
        shopContainer.addView(header)
    }

    private fun createIAPCard(iapType: IAPType): LinearLayout {
        val isPurchased = GameState.hasPurchased(iapType)

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(if (isPurchased) Color.rgb(20, 60, 20) else Color.rgb(30, 30, 60))
        card.setPadding(20, 20, 20, 20)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 15)
        card.layoutParams = cardParams

        // Header with emoji and name
        val header = TextView(this)
        header.text = "${iapType.emoji} ${iapType.displayName}"
        header.textSize = 22f
        header.setTextColor(Color.WHITE)
        header.gravity = Gravity.CENTER
        card.addView(header)

        // Price
        val priceText = TextView(this)
        priceText.text = iapType.priceString
        priceText.textSize = 18f
        priceText.setTextColor(Color.rgb(255, 215, 0))
        priceText.gravity = Gravity.CENTER
        priceText.setPadding(0, 5, 0, 5)
        card.addView(priceText)

        // Description
        val description = TextView(this)
        description.text = iapType.description
        description.textSize = 14f
        description.setTextColor(Color.LTGRAY)
        description.gravity = Gravity.CENTER
        description.setPadding(0, 5, 0, 15)
        card.addView(description)

        if (isPurchased) {
            // Owned indicator
            val ownedText = TextView(this)
            ownedText.text = "✅ OWNED"
            ownedText.textSize = 18f
            ownedText.setTextColor(Color.rgb(50, 255, 50))
            ownedText.gravity = Gravity.CENTER
            ownedText.setPadding(0, 10, 0, 10)
            card.addView(ownedText)
        } else {
            // Purchase button
            val purchaseButton = Button(this)
            purchaseButton.text = "💳 Purchase ${iapType.priceString}"
            purchaseButton.textSize = 16f
            purchaseButton.setBackgroundColor(Color.rgb(50, 150, 50))
            purchaseButton.setTextColor(Color.WHITE)
            purchaseButton.setPadding(20, 20, 20, 20)
            purchaseButton.setOnClickListener {
                // Start purchase flow
                IAPManager.purchaseIAP(
                    activity = this,
                    iapType = iapType,
                    onPurchaseSuccess = {
                        // Apply purchase in GameState
                        val success = GameState.purchaseIAP(iapType)
                        if (success) {
                            // Save state
                            GameState.saveState(this)

                            // Refresh UI
                            populateShop()
                        }
                    },
                    onPurchaseFailed = { error ->
                        // Could show error toast here
                    }
                )
            }
            card.addView(purchaseButton)
        }

        return card
    }
}
