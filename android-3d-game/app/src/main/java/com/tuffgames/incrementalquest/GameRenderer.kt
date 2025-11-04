package com.tuffgames.incrementalquest

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.random.Random

class GameRenderer(
    private val gameState: GameState,
    private val onScoreUpdate: () -> Unit
) : GLSurfaceView.Renderer {
    private lateinit var cube: Cube
    private lateinit var d10: D10
    private lateinit var d12: D12
    private lateinit var d20: D20

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    private var currentRotX = 0f
    private var currentRotY = 0f
    private var targetRotX = 0f
    private var targetRotY = 0f
    private var isAnimating = false
    private val animationSpeed = 10f  // Grad pro Frame (ausbalanciert für flüssige aber nicht zu schnelle Rotation)

    // Autoklicker
    private var lastAutoClickTime = 0L

    @Volatile
    var currentColor: CubeColor = CubeColor.RED
        private set

    // Rotationen um jede Seite frontal zu zeigen
    // Jede Farbe hat X- und Y-Rotation
    data class Rotation(val x: Float, val y: Float)

    private val colorRotations = mapOf(
        CubeColor.RED to Rotation(0f, 0f),        // Vorderseite
        CubeColor.GREEN to Rotation(0f, 180f),    // Rückseite
        CubeColor.BLUE to Rotation(0f, -90f),     // Links
        CubeColor.YELLOW to Rotation(0f, 90f),    // Rechts
        CubeColor.MAGENTA to Rotation(-90f, 0f),  // Oben
        CubeColor.CYAN to Rotation(90f, 0f),      // Unten
        // D10 Farben
        CubeColor.ORANGE to Rotation(0f, 45f),
        CubeColor.PINK to Rotation(0f, 135f),
        CubeColor.PURPLE to Rotation(0f, -45f),
        CubeColor.TURQUOISE to Rotation(0f, -135f),
        // D12 Farben
        CubeColor.LIME to Rotation(45f, 0f),
        CubeColor.BROWN to Rotation(-45f, 0f),
        // D20 Farben
        CubeColor.GOLD to Rotation(45f, 45f),
        CubeColor.SILVER to Rotation(45f, -45f),
        CubeColor.BRONZE to Rotation(-45f, 45f),
        CubeColor.NAVY to Rotation(-45f, -45f),
        CubeColor.MAROON to Rotation(45f, 90f),
        CubeColor.OLIVE to Rotation(-45f, 90f),
        CubeColor.TEAL to Rotation(45f, -90f),
        CubeColor.CORAL to Rotation(-45f, -90f)
    )

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Hintergrundfarbe setzen (dunkelblau)
        GLES20.glClearColor(0.1f, 0.1f, 0.3f, 1.0f)

        // Tiefentest aktivieren
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Alle Würfel erstellen
        cube = Cube()
        d10 = D10()
        d12 = D12()
        d20 = D20()

        // Erste zufällige Farbe wählen
        currentColor = getRandomColor()
        val rotation = colorRotations[currentColor] ?: Rotation(0f, 0f)
        currentRotX = rotation.x
        currentRotY = rotation.y
        targetRotX = currentRotX
        targetRotY = currentRotY

        // UI aktualisieren
        onScoreUpdate()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        // Projektionsmatrix erstellen (Perspektive)
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 15f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Bildschirm löschen
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Autoklicker-Logik
        if (gameState.autoClickerActive) {
            val currentTime = System.currentTimeMillis()
            val interval = gameState.getAutoClickerInterval()
            if (currentTime - lastAutoClickTime >= interval) {
                performClick()
                lastAutoClickTime = currentTime
            }
        }

        // Animation durchführen wenn aktiv
        if (isAnimating) {
            var animatingX = true
            var animatingY = true

            // X-Achse animieren
            val diffX = targetRotX - currentRotX
            val shortestDiffX = when {
                diffX > 180f -> diffX - 360f
                diffX < -180f -> diffX + 360f
                else -> diffX
            }

            if (abs(shortestDiffX) > animationSpeed) {
                currentRotX += if (shortestDiffX > 0) animationSpeed else -animationSpeed
            } else {
                currentRotX = targetRotX
                animatingX = false
            }

            // Y-Achse animieren
            val diffY = targetRotY - currentRotY
            val shortestDiffY = when {
                diffY > 180f -> diffY - 360f
                diffY < -180f -> diffY + 360f
                else -> diffY
            }

            if (abs(shortestDiffY) > animationSpeed) {
                currentRotY += if (shortestDiffY > 0) animationSpeed else -animationSpeed
            } else {
                currentRotY = targetRotY
                animatingY = false
            }

            // Animation beenden wenn beide Achsen fertig sind
            if (!animatingX && !animatingY) {
                isAnimating = false
            }

            // Normalisiere auf 0-360
            currentRotX = currentRotX % 360f
            if (currentRotX < 0) currentRotX += 360f
            currentRotY = currentRotY % 360f
            if (currentRotY < 0) currentRotY += 360f
        }

        // Kamera Position setzen
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 12f,   // Kamera Position (x, y, z)
            0f, 0f, 0f,    // Blickpunkt
            0f, 1f, 0f)    // Up-Vektor

        // Rotationen anwenden: erst X-Achse, dann Y-Achse
        val rotXMatrix = FloatArray(16)
        val rotYMatrix = FloatArray(16)
        Matrix.setRotateM(rotXMatrix, 0, currentRotX, 1f, 0f, 0f)
        Matrix.setRotateM(rotYMatrix, 0, currentRotY, 0f, 1f, 0f)

        // Rotationen kombinieren
        Matrix.multiplyMM(rotationMatrix, 0, rotYMatrix, 0, rotXMatrix, 0)

        // Matrizen kombinieren
        val tempMatrix = FloatArray(16)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, rotationMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

        // Richtigen Würfel basierend auf Upgrade zeichnen
        when {
            gameState.d20Active -> d20.draw(mvpMatrix)
            gameState.d12Active -> d12.draw(mvpMatrix)
            gameState.d10Active -> d10.draw(mvpMatrix)
            else -> cube.draw(mvpMatrix)
        }
    }

    fun onTouch(x: Float, y: Float, screenWidth: Int, screenHeight: Int) {
        // Klicks sind jederzeit erlaubt - keine Animation-Sperre mehr
        performClick()
    }

    private fun performClick() {
        // Punkte für aktuelle Farbe hinzufügen
        gameState.onColorClicked(currentColor)

        // Neue zufällige Farbe für nächsten Klick wählen
        currentColor = getRandomColor()

        // Zielrotation für neue Farbe setzen
        val rotation = colorRotations[currentColor] ?: Rotation(0f, 0f)
        targetRotX = rotation.x
        targetRotY = rotation.y

        // Animation starten
        isAnimating = true

        // UI aktualisieren (zeigt neue Farbe)
        onScoreUpdate()
    }

    private fun getRandomColor(): CubeColor {
        return gameState.getAvailableColors().random()
    }
}
