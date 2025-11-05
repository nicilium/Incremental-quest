package com.tuffgames.incrementalquest

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sqrt

class D4 {
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val program: Int

    private val vertexCount = 12 // 4 Flächen * 3 Vertices

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

    init {
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()

        // D4 als regulärer Tetraeder
        // 4 Vertices definieren einen Tetraeder
        val scale = 1.5f
        val h = sqrt(2.0 / 3.0).toFloat() * scale  // Höhe für regulären Tetraeder

        // Tetraeder-Vertices (zentriert)
        val v0 = floatArrayOf(0f, h, 0f)                    // Oben
        val v1 = floatArrayOf(-scale, -h/2, scale)          // Vorne Links
        val v2 = floatArrayOf(scale, -h/2, scale)           // Vorne Rechts
        val v3 = floatArrayOf(0f, -h/2, -scale * 1.5f)      // Hinten

        // Farben für die 4 Flächen
        // WICHTIG: Zuordnung basierend auf GameRenderer Rotationen!
        // RED rotation (-55°, 0°) zeigt Face 0 (Vorne) → Rot zuordnen
        // GREEN rotation (-55°, -120°) zeigt Face 2 (Rechts, da Y=-120° rechte Seite nach vorne dreht) → Grün zuordnen
        // BLUE rotation (-55°, 120°) zeigt Face 1 (Links, da Y=+120° linke Seite nach vorne dreht) → Blau zuordnen
        // YELLOW rotation (135°, 180°) zeigt Face 3 (Unten) → Gelb zuordnen

        val redColor = floatArrayOf(1f, 0f, 0f, 1f)      // Rot = 1 Punkt
        val greenColor = floatArrayOf(0f, 1f, 0f, 1f)    // Grün = 2 Punkte
        val blueColor = floatArrayOf(0f, 0f, 1f, 1f)     // Blau = 3 Punkte
        val yellowColor = floatArrayOf(1f, 1f, 0f, 1f)   // Gelb = 4 Punkte

        // Face 0: Vorne (v0, v1, v2) - ROT (wird bei RED rotation gezeigt)
        addTriangle(vertices, colors, v0, v1, v2, redColor)

        // Face 1: Links (v0, v3, v1) - BLAU (wird bei BLUE rotation gezeigt)
        addTriangle(vertices, colors, v0, v3, v1, blueColor)

        // Face 2: Rechts (v0, v2, v3) - GRÜN (wird bei GREEN rotation gezeigt)
        addTriangle(vertices, colors, v0, v2, v3, greenColor)

        // Face 3: Unten (v1, v3, v2) - GELB (wird bei YELLOW rotation gezeigt)
        addTriangle(vertices, colors, v1, v3, v2, yellowColor)

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

    private fun addTriangle(
        vertices: MutableList<Float>,
        colors: MutableList<Float>,
        v1: FloatArray,
        v2: FloatArray,
        v3: FloatArray,
        color: FloatArray
    ) {
        // Vertex 1
        vertices.add(v1[0])
        vertices.add(v1[1])
        vertices.add(v1[2])

        // Vertex 2
        vertices.add(v2[0])
        vertices.add(v2[1])
        vertices.add(v2[2])

        // Vertex 3
        vertices.add(v3[0])
        vertices.add(v3[1])
        vertices.add(v3[2])

        // Farbe für alle 3 Vertices
        for (i in 0 until 3) {
            colors.add(color[0])
            colors.add(color[1])
            colors.add(color[2])
            colors.add(color[3])
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

        // D4 zeichnen
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}
