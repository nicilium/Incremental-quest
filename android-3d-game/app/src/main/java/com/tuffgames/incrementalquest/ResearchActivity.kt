package com.tuffgames.incrementalquest

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * ResearchActivity - Tech Tree System
 *
 * Shows 4 research paths with unlockable nodes
 * Nodes have prerequisites and cost research points
 */
class ResearchActivity : Activity() {

    private lateinit var mainLayout: LinearLayout
    private lateinit var researchPointsText: TextView
    private lateinit var pathsContainer: LinearLayout

    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            GameState.processOfflineClicks()
            updateResearchPoints()
            updateHandler.postDelayed(this, 100)
        }
    }

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
        title.text = "📖 RESEARCH 📖"
        title.textSize = 32f
        title.setTextColor(Color.rgb(100, 200, 255))
        title.gravity = Gravity.CENTER
        mainLayout.addView(title)

        // Research points display
        researchPointsText = TextView(this)
        researchPointsText.textSize = 20f
        researchPointsText.setTextColor(Color.rgb(255, 215, 0))
        researchPointsText.gravity = Gravity.CENTER
        researchPointsText.setPadding(0, 15, 0, 10)
        mainLayout.addView(researchPointsText)

        // Info text
        val infoText = TextView(this)
        infoText.text = "Research unlocks powerful game mechanics!\nEarn 1 research point per minute\nSome nodes require prerequisites"
        infoText.textSize = 13f
        infoText.setTextColor(Color.LTGRAY)
        infoText.gravity = Gravity.CENTER
        infoText.setPadding(10, 10, 10, 20)
        mainLayout.addView(infoText)

        // ScrollView for paths
        val scrollView = ScrollView(this)
        pathsContainer = LinearLayout(this)
        pathsContainer.orientation = LinearLayout.VERTICAL
        scrollView.addView(pathsContainer)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        // Populate research paths
        populatePaths()

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
        updateResearchPoints()
    }

    private fun populatePaths() {
        pathsContainer.removeAllViews()

        // Production Path
        addPathSection("⚙️ PRODUCTION PATH", ResearchPath.PRODUCTION, Color.rgb(255, 150, 50))

        // Efficiency Path
        addPathSection("💡 EFFICIENCY PATH", ResearchPath.EFFICIENCY, Color.rgb(50, 255, 150))

        // Power Path
        addPathSection("⚡ POWER PATH", ResearchPath.POWER, Color.rgb(255, 50, 150))

        // Automation Path
        addPathSection("🤖 AUTOMATION PATH", ResearchPath.AUTOMATION, Color.rgb(150, 50, 255))
    }

    private fun addPathSection(title: String, path: ResearchPath, color: Int) {
        // Path header
        val header = TextView(this)
        header.text = "━━━ $title ━━━"
        header.textSize = 18f
        header.setTextColor(color)
        header.gravity = Gravity.CENTER
        header.setPadding(0, 15, 0, 10)
        pathsContainer.addView(header)

        // Get nodes for this path
        val nodes = ResearchNode.values().filter { it.path == path }

        // Add nodes
        nodes.forEach { node ->
            pathsContainer.addView(createResearchNodeCard(node))
        }
    }

    private fun createResearchNodeCard(node: ResearchNode): LinearLayout {
        val isUnlocked = GameState.hasResearch(node)
        val canUnlock = GameState.canUnlockResearch(node)

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(
            when {
                isUnlocked -> Color.rgb(20, 60, 20)
                canUnlock -> Color.rgb(30, 30, 60)
                else -> Color.rgb(50, 50, 50)
            }
        )
        card.setPadding(15, 15, 15, 15)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 0, 0, 10)
        card.layoutParams = cardParams

        // Node name
        val nameText = TextView(this)
        nameText.text = node.displayName
        nameText.textSize = 18f
        nameText.setTextColor(Color.WHITE)
        nameText.gravity = Gravity.CENTER
        card.addView(nameText)

        // Description
        val descText = TextView(this)
        descText.text = node.description
        descText.textSize = 14f
        descText.setTextColor(Color.LTGRAY)
        descText.gravity = Gravity.CENTER
        descText.setPadding(0, 5, 0, 10)
        card.addView(descText)

        // Prerequisites
        if (node.prerequisites.isNotEmpty()) {
            val prereqText = TextView(this)
            val prereqNames = node.prerequisites.joinToString(", ") { it.displayName }
            prereqText.text = "Requires: $prereqNames"
            prereqText.textSize = 12f
            prereqText.setTextColor(Color.rgb(255, 200, 100))
            prereqText.gravity = Gravity.CENTER
            prereqText.setPadding(0, 5, 0, 10)
            card.addView(prereqText)
        }

        if (isUnlocked) {
            // Already unlocked
            val unlockedText = TextView(this)
            unlockedText.text = "✅ UNLOCKED"
            unlockedText.textSize = 18f
            unlockedText.setTextColor(Color.rgb(100, 255, 100))
            unlockedText.gravity = Gravity.CENTER
            unlockedText.setPadding(0, 10, 0, 10)
            card.addView(unlockedText)
        } else {
            // Cost display
            val costText = TextView(this)
            costText.text = "Cost: ${node.cost.toInt()} RP"
            costText.textSize = 16f
            costText.setTextColor(Color.rgb(255, 215, 0))
            costText.gravity = Gravity.CENTER
            costText.setPadding(0, 5, 0, 10)
            card.addView(costText)

            // Unlock button
            val unlockButton = Button(this)
            unlockButton.text = if (canUnlock) "UNLOCK" else "LOCKED"
            unlockButton.textSize = 16f

            if (canUnlock) {
                unlockButton.setBackgroundColor(Color.rgb(50, 150, 50))
                unlockButton.setTextColor(Color.WHITE)
                unlockButton.setOnClickListener {
                    if (GameState.unlockResearch(node)) {
                        populatePaths()
                        updateResearchPoints()
                    }
                }
            } else {
                unlockButton.setBackgroundColor(Color.rgb(100, 100, 100))
                unlockButton.setTextColor(Color.DKGRAY)
                unlockButton.isEnabled = false
            }

            card.addView(unlockButton)

            // Show why locked if not enough points or missing prerequisites
            if (!canUnlock) {
                val reasonText = TextView(this)
                if (GameState.researchPoints < node.cost) {
                    reasonText.text = "Need ${(node.cost - GameState.researchPoints).toInt()} more RP"
                } else {
                    reasonText.text = "Missing prerequisites"
                }
                reasonText.textSize = 12f
                reasonText.setTextColor(Color.rgb(255, 100, 100))
                reasonText.gravity = Gravity.CENTER
                reasonText.setPadding(0, 5, 0, 0)
                card.addView(reasonText)
            }
        }

        return card
    }

    private fun updateResearchPoints() {
        val rp = GameState.researchPoints
        researchPointsText.text = String.format("🔬 Research Points: %.1f", rp)
    }

    override fun onResume() {
        super.onResume()
        updateHandler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        updateHandler.removeCallbacks(updateRunnable)
        GameState.markActiveTime()
        GameState.saveState(this)
    }
}
