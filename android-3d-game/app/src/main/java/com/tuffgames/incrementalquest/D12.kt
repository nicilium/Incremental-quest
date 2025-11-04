package com.tuffgames.incrementalquest

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sqrt

class D12 {
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val program: Int

    private val vertexCount = 180 // 12 Flächen * 5 Dreiecke * 3 Vertices

    // Vertex Shader
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec4 vColor;
        varying vec4 fColor;

        void main() {
            gl_Position = uMVPMatrix * vPosition;
            fColor = vColor;
        }
    """.trimIndent()

    // Fragment Shader
    private val fragmentShaderCode = """
        precision mediump float;
        varying vec4 fColor;

        void main() {
            gl_FragColor = fColor;
        }
    """.trimIndent()

    // Goldener Schnitt für Dodekaeder
    private val phi = (1.0f + sqrt(5.0f)) / 2.0f
    private val scale = 1.0f

    // 20 Vertices eines Dodekaeders (Standard-Konstruktion)
    private val dodecaVertices = floatArrayOf(
        // Würfel-Vertices (±1, ±1, ±1)
        1f, 1f, 1f,       // 0
        1f, 1f, -1f,      // 1
        1f, -1f, 1f,      // 2
        1f, -1f, -1f,     // 3
        -1f, 1f, 1f,      // 4
        -1f, 1f, -1f,     // 5
        -1f, -1f, 1f,     // 6
        -1f, -1f, -1f,    // 7

        // (0, ±φ, ±1/φ)
        0f, phi, 1f/phi,      // 8
        0f, phi, -1f/phi,     // 9
        0f, -phi, 1f/phi,     // 10
        0f, -phi, -1f/phi,    // 11

        // (±1/φ, 0, ±φ)
        1f/phi, 0f, phi,      // 12
        1f/phi, 0f, -phi,     // 13
        -1f/phi, 0f, phi,     // 14
        -1f/phi, 0f, -phi,    // 15

        // (±φ, ±1/φ, 0)
        phi, 1f/phi, 0f,      // 16
        phi, -1f/phi, 0f,     // 17
        -phi, 1f/phi, 0f,     // 18
        -phi, -1f/phi, 0f     // 19
    ).map { it * scale }.toFloatArray()

    // 12 Pentagon-Flächen (bewährte Topologie)
    // Diese Definitionen sind aus verlässlichen geometrischen Quellen
    private val pentagons = arrayOf(
        intArrayOf(0, 16, 17, 2, 12),     // 0 - Rot
        intArrayOf(0, 12, 14, 4, 8),      // 1 - Grün
        intArrayOf(0, 8, 9, 1, 16),       // 2 - Blau
        intArrayOf(1, 9, 5, 15, 13),      // 3 - Gelb
        intArrayOf(1, 13, 3, 17, 16),     // 4 - Magenta
        intArrayOf(2, 17, 3, 11, 10),     // 5 - Cyan
        intArrayOf(2, 10, 6, 14, 12),     // 6 - Orange
        intArrayOf(4, 14, 6, 19, 18),     // 7 - Pink
        intArrayOf(4, 18, 5, 9, 8),       // 8 - Lila
        intArrayOf(5, 18, 19, 7, 15),     // 9 - Türkis (KORRIGIERT)
        intArrayOf(7, 19, 6, 10, 11),     // 10 - Lime (KORRIGIERT)
        intArrayOf(3, 13, 15, 7, 11)      // 11 - Braun (KORRIGIERT)
    )

    // Farben für die 12 Flächen
    private val faceColors = mapOf(
        0 to floatArrayOf(1f, 0f, 0f, 1f),         // Rot
        1 to floatArrayOf(0f, 1f, 0f, 1f),         // Grün
        2 to floatArrayOf(0f, 0f, 1f, 1f),         // Blau
        3 to floatArrayOf(1f, 1f, 0f, 1f),         // Gelb
        4 to floatArrayOf(1f, 0f, 1f, 1f),         // Magenta
        5 to floatArrayOf(0f, 1f, 1f, 1f),         // Cyan
        6 to floatArrayOf(1f, 0.5f, 0f, 1f),       // Orange
        7 to floatArrayOf(1f, 0.75f, 0.8f, 1f),    // Pink
        8 to floatArrayOf(0.5f, 0f, 0.5f, 1f),     // Lila
        9 to floatArrayOf(0.25f, 0.88f, 0.82f, 1f), // Türkis
        10 to floatArrayOf(0.5f, 1f, 0f, 1f),      // Lime
        11 to floatArrayOf(0.55f, 0.27f, 0.07f, 1f) // Braun
    )

    init {
        // Vertices und Farben für alle Flächen aufbauen
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()

        for (faceIndex in 0 until 12) {
            val pentagon = pentagons[faceIndex]
            val color = faceColors[faceIndex]!!

            // Pentagon-Zentrum berechnen (Durchschnitt der 5 Vertices)
            var centerX = 0f
            var centerY = 0f
            var centerZ = 0f
            for (i in 0 until 5) {
                val idx = pentagon[i] * 3
                centerX += dodecaVertices[idx]
                centerY += dodecaVertices[idx + 1]
                centerZ += dodecaVertices[idx + 2]
            }
            centerX /= 5f
            centerY /= 5f
            centerZ /= 5f

            // 5 Dreiecke vom Zentrum zu den Kanten (Counter-Clockwise für korrektes Culling)
            for (i in 0 until 5) {
                val i1 = pentagon[i] * 3
                val i2 = pentagon[(i + 1) % 5] * 3

                // Dreieck: Vertex i -> Zentrum -> Vertex i+1 (CCW)
                // Vertex i
                vertices.add(dodecaVertices[i1])
                vertices.add(dodecaVertices[i1 + 1])
                vertices.add(dodecaVertices[i1 + 2])

                // Zentrum
                vertices.add(centerX)
                vertices.add(centerY)
                vertices.add(centerZ)

                // Vertex i+1
                vertices.add(dodecaVertices[i2])
                vertices.add(dodecaVertices[i2 + 1])
                vertices.add(dodecaVertices[i2 + 2])

                // Farbe für alle 3 Vertices
                for (j in 0 until 3) {
                    colors.add(color[0])
                    colors.add(color[1])
                    colors.add(color[2])
                    colors.add(color[3])
                }
            }
        }

        // Vertex Buffer initialisieren
        val vb = ByteBuffer.allocateDirect(vertices.size * 4)
        vb.order(ByteOrder.nativeOrder())
        vertexBuffer = vb.asFloatBuffer()
        vertexBuffer.put(vertices.toFloatArray())
        vertexBuffer.position(0)

        // Color Buffer initialisieren
        val cb = ByteBuffer.allocateDirect(colors.size * 4)
        cb.order(ByteOrder.nativeOrder())
        colorBuffer = cb.asFloatBuffer()
        colorBuffer.put(colors.toFloatArray())
        colorBuffer.position(0)

        // Shader kompilieren und Programm erstellen
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        // Shader-Programm aktivieren
        GLES20.glUseProgram(program)

        // Vertex Position Handle
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        // Color Handle
        val colorHandle = GLES20.glGetAttribLocation(program, "vColor")
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 16, colorBuffer)

        // MVP Matrix Handle
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // D12 zeichnen
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}
