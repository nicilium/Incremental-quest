package com.tuffgames.incrementalquest

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class TavernActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Vollbild-Modus
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Haupt-Layout
        val mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setBackgroundColor(Color.rgb(20, 15, 10))  // Dunkler Taverne-Look
        mainLayout.setPadding(20, 20, 20, 20)

        // ScrollView für Content
        val scrollView = ScrollView(this)
        val contentLayout = LinearLayout(this)
        contentLayout.orientation = LinearLayout.VERTICAL
        scrollView.addView(contentLayout)

        // ASCII-Art Taverne
        val tavernArt = TextView(this)
        tavernArt.text = """
╔═══════════════════════════════════╗
║    🍺 THE DRUNKEN D20 🍺          ║
║                                   ║
║   🕯️        🪵🍺🪵        🕯️     ║
║   │      ┌────────┐      │       ║
║   │      │ [===] │       │       ║
║   │      │  BAR   │      │       ║
║   │      └────────┘      │       ║
║ 🪑                          🪑   ║
║   🎲 🎰 🗡️ ⚔️ 🏹 ✨        ║
╚═══════════════════════════════════╝
        """.trimIndent()
        tavernArt.textSize = 10f
        tavernArt.setTextColor(Color.rgb(255, 200, 100))
        tavernArt.typeface = Typeface.MONOSPACE
        tavernArt.gravity = Gravity.CENTER
        tavernArt.setPadding(0, 0, 0, 20)
        contentLayout.addView(tavernArt)

        // Patrick Portrait (Platzhalter)
        val patrickPortrait = TextView(this)
        patrickPortrait.text = """
    ╔══════════╗
    ║ 👨‍🍳 🍺   ║
    ║ PATRICK  ║
    ╚══════════╝
        """.trimIndent()
        patrickPortrait.textSize = 12f
        patrickPortrait.setTextColor(Color.rgb(255, 180, 80))
        patrickPortrait.typeface = Typeface.MONOSPACE
        patrickPortrait.gravity = Gravity.CENTER
        patrickPortrait.setPadding(0, 10, 0, 10)
        contentLayout.addView(patrickPortrait)

        // Patrick's FRECHE Begrüßung
        val welcomeText = TextView(this)
        welcomeText.text = """
Patrick wirft einen dreckigen Lappen auf die Theke und grinst dich frech an:

"Na endlich! Ich dachte schon, du würfelst dich nie bis hierher durch, du Grünschnabel! 🎲"

Er schenkt sich selbst einen Schnaps ein und kippt ihn runter:

"Der D20? HAH! Das war doch nur der ANFANG, mein kleiner Würfel-Narr! Hier, in MEINER Taverne, beginnt der RICHTIGE Spaß!"

*KNALLT das Glas auf die Theke*

"Willkommen im RPG-Part, du Held... oder auch nicht! Das wird sich noch zeigen! 😏"

"Hier triffst du auf andere Abenteurer, nimmst Quests an, und wenn du nicht total bescheuert bist, wählst du dir sogar eine KLASSE aus!"

"Aber ERST musst du mir beweisen, dass du kein kompletter Versager bist!"

Patrick deutet mit dem Daumen auf ein zerknittertes Pergament an der Wand:

"📜 QUEST #1: Spiel dir einen ZWEITEN D20 frei, du Wicht! Dann reden wir über Klassen!"

Er grinst hinterhältig:

"Ach ja, und falls du NOCH MEHR Würfel brauchst... Ich kenne da einen Typen. Fragt sich nur, ob du dir das leisten kannst! 💰"

*zwinkert*

"Jetzt HUSTE RAUS und zeig mir, was du drauf hast! RAUS AUS MEINER TAVERNE!"

        """.trimIndent()
        welcomeText.textSize = 14f
        welcomeText.setTextColor(Color.rgb(230, 230, 230))
        welcomeText.setPadding(20, 20, 20, 20)
        welcomeText.setBackgroundColor(Color.rgb(40, 30, 20))
        contentLayout.addView(welcomeText)

        // Quest Status Card
        val questCard = createQuestCard()
        contentLayout.addView(questCard)

        // Extra Dice Hinweis
        val extraDiceButton = Button(this)
        extraDiceButton.text = "💰 Extra Dice Shop (Patrick's Tipp)"
        extraDiceButton.textSize = 16f
        extraDiceButton.setBackgroundColor(Color.rgb(100, 50, 200))
        extraDiceButton.setTextColor(Color.WHITE)
        extraDiceButton.setPadding(20, 20, 20, 20)
        val extraDiceParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        extraDiceParams.setMargins(0, 20, 0, 10)
        extraDiceButton.layoutParams = extraDiceParams
        extraDiceButton.setOnClickListener {
            val intent = Intent(this, ExtraDiceActivity::class.java)
            startActivity(intent)
        }
        contentLayout.addView(extraDiceButton)

        // Class System Teaser (wenn freigeschaltet)
        if (GameState.classUnlocked) {
            val classCard = createClassSelectionCard()
            contentLayout.addView(classCard)
        }

        // Back button
        val backButton = Button(this)
        backButton.text = "🚪 Taverne verlassen"
        backButton.textSize = 18f
        backButton.setBackgroundColor(Color.rgb(80, 60, 40))
        backButton.setTextColor(Color.WHITE)
        backButton.setPadding(20, 20, 20, 20)
        backButton.setOnClickListener { finish() }
        contentLayout.addView(backButton)

        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0, 1f
        ))

        setContentView(mainLayout)
    }

    private fun createQuestCard(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(60, 40, 20))
        card.setPadding(20, 20, 20, 20)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 20, 0, 0)
        card.layoutParams = params

        val title = TextView(this)
        title.text = "📜 AKTUELLE QUEST"
        title.textSize = 20f
        title.setTextColor(Color.rgb(255, 215, 0))
        title.gravity = Gravity.CENTER
        card.addView(title)

        val questText = TextView(this)
        val currentQuest = GameState.getCurrentQuest()

        if (currentQuest == "SECOND_D20") {
            val extraDiceCount = GameState.getExtraDiceCount()
            questText.text = """
Quest: Zweiter D20
Status: ${if (extraDiceCount >= 2) "✅ ABGESCHLOSSEN!" else "🔄 In Progress"}

"Besorg dir einen zweiten D20, du Faulpelz!"
- Patrick

Extra Dice besessen: $extraDiceCount / 2
            """.trimIndent()

            if (extraDiceCount >= 2) {
                // Quest complete!
                GameState.completeQuest("SECOND_D20")
            }
        } else if (GameState.classUnlocked) {
            questText.text = "Keine aktive Quest.\nWähle eine Klasse und warte auf neue Abenteuer!"
        } else {
            questText.text = "Fehler: Keine Quest aktiv!"
        }

        questText.textSize = 14f
        questText.setTextColor(Color.WHITE)
        questText.setPadding(10, 15, 10, 10)
        card.addView(questText)

        return card
    }

    private fun createClassSelectionCard(): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundColor(Color.rgb(40, 30, 60))
        card.setPadding(20, 20, 20, 20)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 20, 0, 20)
        card.layoutParams = params

        val title = TextView(this)
        title.text = "⚔️ KLASSENWAHL FREIGESCHALTET! ⚔️"
        title.textSize = 22f
        title.setTextColor(Color.rgb(255, 215, 0))
        title.gravity = Gravity.CENTER
        card.addView(title)

        val patrickText = TextView(this)
        patrickText.text = """
Patrick lehnt sich zurück und grinst:

"Na also! Du hast es geschafft, du Knalltüte!
Zwei D20... nicht schlecht für einen Anfänger!

Hier, nimm diesen Paladin-Orden! Du bist jetzt ein
HEILIGER KRIEGER! Ein Tank mit dicken Muskeln und
göttlicher Magie! Jetzt wird's ernst! 🛡️⚔️"
        """.trimIndent()
        patrickText.textSize = 13f
        patrickText.setTextColor(Color.rgb(200, 200, 200))
        patrickText.setPadding(10, 15, 10, 15)
        card.addView(patrickText)

        // Aktuelle Klasse
        val currentClass = GameState.selectedClass
        if (currentClass != null) {
            val selectedText = TextView(this)
            selectedText.text = """
Aktuelle Klasse: ${currentClass.emoji} ${currentClass.displayName}
Level: ${GameState.getCharacterStats()?.level ?: 1}
            """.trimIndent()
            selectedText.textSize = 16f
            selectedText.setTextColor(Color.rgb(100, 255, 100))
            selectedText.gravity = Gravity.CENTER
            selectedText.setPadding(0, 10, 0, 10)
            card.addView(selectedText)

            // Character Stats anzeigen (D&D 5e)
            GameState.getCharacterStats()?.let { stats ->
                // D&D Attributes mit Modifiers
                val attributesText = TextView(this)
                attributesText.text = """
D&D ATTRIBUTE:
💪 STR: ${stats.strength} (${formatModifier(stats.getModifier(DndAttribute.STRENGTH))})
🤸 DEX: ${stats.dexterity} (${formatModifier(stats.getModifier(DndAttribute.DEXTERITY))})
💚 CON: ${stats.constitution} (${formatModifier(stats.getModifier(DndAttribute.CONSTITUTION))})
📚 INT: ${stats.intelligence} (${formatModifier(stats.getModifier(DndAttribute.INTELLIGENCE))})
🧠 WIS: ${stats.wisdom} (${formatModifier(stats.getModifier(DndAttribute.WISDOM))})
✨ CHA: ${stats.charisma} (${formatModifier(stats.getModifier(DndAttribute.CHARISMA))})
                """.trimIndent()
                attributesText.textSize = 12f
                attributesText.setTextColor(Color.rgb(255, 215, 0))
                attributesText.gravity = Gravity.START
                attributesText.setPadding(20, 15, 10, 5)
                card.addView(attributesText)

                // Combat Stats
                val combatStatsText = TextView(this)
                combatStatsText.text = """
❤️ HP: ${stats.currentHP}/${stats.maxHP}${if (stats.temporaryHP > 0) " (+${stats.temporaryHP} temp)" else ""}
✨ Mana: ${stats.currentMana}/${stats.maxMana}
🛡️ AC: ${stats.getArmorClass()}
⚡ Initiative: ${formatModifier(stats.getInitiative())}
🎯 Proficiency: +${stats.getProficiencyBonus()}
                """.trimIndent()
                combatStatsText.textSize = 12f
                combatStatsText.setTextColor(Color.rgb(100, 255, 100))
                combatStatsText.gravity = Gravity.START
                combatStatsText.setPadding(20, 5, 10, 5)
                card.addView(combatStatsText)

                // XP Progress
                val xpText = TextView(this)
                xpText.text = "📊 EXP: ${stats.experience} / ${stats.getNextLevelXP()}"
                xpText.textSize = 12f
                xpText.setTextColor(Color.rgb(200, 200, 255))
                xpText.gravity = Gravity.CENTER
                xpText.setPadding(0, 5, 0, 10)
                card.addView(xpText)
            }

            // COMBAT SECTION - nur wenn Klasse gewählt
            // Divider
            val combatDivider = TextView(this)
            combatDivider.text = "═══ ⚔️ KÄMPFE ⚔️ ═══"
            combatDivider.textSize = 14f
            combatDivider.setTextColor(Color.RED)
            combatDivider.gravity = Gravity.CENTER
            combatDivider.setPadding(0, 20, 0, 10)
            card.addView(combatDivider)

            val combatInfoText = TextView(this)
            combatInfoText.text = """
Patrick lehnt sich über die Theke und zeigt auf ein schwarzes Brett:

"Hier kannst du deine Fähigkeiten unter Beweis stellen!"

📖 STORY: Epische Abenteuer mit smarter KI
💼 AUFTRÄGE: Zufallskämpfe für Belohnungen
            """.trimIndent()
            combatInfoText.textSize = 12f
            combatInfoText.setTextColor(Color.rgb(220, 220, 220))
            combatInfoText.setPadding(10, 5, 10, 15)
            card.addView(combatInfoText)

            // Story Combat Button
            val storyCombatButton = Button(this)
            storyCombatButton.text = """
📖 STORY-KAMPF
${if (!GameState.getActiveCombat()?.isTutorial?.let { it } == true && GameState.getCharacterStats()?.level == 1)
    "⚠️ TUTORIAL verfügbar!" else "Episches Abenteuer"}
            """.trimIndent()
            storyCombatButton.textSize = 14f
            storyCombatButton.setBackgroundColor(Color.rgb(100, 40, 40))
            storyCombatButton.setTextColor(Color.WHITE)
            storyCombatButton.setPadding(10, 10, 10, 10)
            storyCombatButton.setOnClickListener {
                // Check if loadout is complete
                if (!GameState.isLoadoutComplete()) {
                    val alert = android.app.AlertDialog.Builder(this)
                    alert.setTitle("⚠️ Loadout unvollständig!")
                    alert.setMessage("Du musst erst deine Fähigkeiten auswählen!\n\nGehe zu 'Loadout konfigurieren'")
                    alert.setPositiveButton("OK", null)
                    alert.show()
                    return@setOnClickListener
                }

                // Start Story Combat
                val combat = if (GameState.getCharacterStats()?.level == 1) {
                    GameState.getTutorialCombat()
                } else {
                    GameState.getStoryCombat()
                }

                if (combat != null) {
                    val intent = Intent(this, CombatActivity::class.java)
                    startActivity(intent)
                }
            }
            card.addView(storyCombatButton)

            // Auftrag Combat Button
            val auftragCombatButton = Button(this)
            auftragCombatButton.text = """
💼 AUFTRAG
Zufallskampf für Belohnungen
            """.trimIndent()
            auftragCombatButton.textSize = 14f
            auftragCombatButton.setBackgroundColor(Color.rgb(40, 40, 100))
            auftragCombatButton.setTextColor(Color.WHITE)
            auftragCombatButton.setPadding(10, 10, 10, 10)
            auftragCombatButton.setOnClickListener {
                // Check if loadout is complete
                if (!GameState.isLoadoutComplete()) {
                    val alert = android.app.AlertDialog.Builder(this)
                    alert.setTitle("⚠️ Loadout unvollständig!")
                    alert.setMessage("Du musst erst deine Fähigkeiten auswählen!\n\nGehe zu 'Loadout konfigurieren'")
                    alert.setPositiveButton("OK", null)
                    alert.show()
                    return@setOnClickListener
                }

                // Start Auftrag Combat
                val combat = GameState.getAuftragCombat()
                if (combat != null) {
                    val intent = Intent(this, CombatActivity::class.java)
                    startActivity(intent)
                }
            }
            card.addView(auftragCombatButton)

            // Loadout Config Button (wichtig!)
            val loadoutButton = Button(this)
            loadoutButton.text = "⚡ Loadout konfigurieren"
            loadoutButton.textSize = 12f
            loadoutButton.setBackgroundColor(Color.rgb(60, 60, 60))
            loadoutButton.setTextColor(Color.YELLOW)
            loadoutButton.setPadding(10, 5, 10, 5)
            loadoutButton.setOnClickListener {
                val alert = android.app.AlertDialog.Builder(this)
                alert.setTitle("🚧 In Entwicklung")
                alert.setMessage("Loadout-Konfiguration kommt im nächsten Update!\n\nFür jetzt: Standard-Loadout wird verwendet.")
                alert.setPositiveButton("OK", null)
                alert.show()
            }
            card.addView(loadoutButton)
        } else {
            // Paladin-Button (nur beim ersten Mal)
            val paladinButton = Button(this)
            paladinButton.text = """
🛡️ PALADIN WERDEN! ⚔️

Heiliger Krieger - Tank mit Heilmagie

D&D Stats (Level 1):
💪 STR 16 (+3)  🤸 DEX 10 (+0)  💚 CON 14 (+2)
📚 INT 8 (-1)   🧠 WIS 12 (+1)  ✨ CHA 16 (+3)

❤️ HP: 12 (d10)  🛡️ AC: 10  ✨ Mana: 15 (CHA)
Hit Dice: d10  |  Casting Stat: Charisma

3 Equipment Sets:
• Heiliger Beschützer (Tank)
• Lichträcher (Balance)
• Heilung (Support)
            """.trimIndent()
            paladinButton.textSize = 13f
            paladinButton.setBackgroundColor(Color.rgb(80, 60, 100))
            paladinButton.setTextColor(Color.WHITE)
            paladinButton.setPadding(15, 15, 15, 15)
            val buttonParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            buttonParams.setMargins(0, 10, 0, 0)
            paladinButton.layoutParams = buttonParams

            paladinButton.setOnClickListener {
                GameState.selectClass(PlayerClass.PALADIN)
                GameState.saveState(this)
                // Refresh UI
                recreate()
            }

            card.addView(paladinButton)
        }

        return card
    }

    // Helper function to format D&D modifiers
    private fun formatModifier(modifier: Int): String {
        return if (modifier >= 0) "+$modifier" else "$modifier"
    }
}
