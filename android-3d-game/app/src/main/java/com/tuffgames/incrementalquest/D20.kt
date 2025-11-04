package com.tuffgames.incrementalquest

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sqrt

class D20 {
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val program: Int

    private val vertexCount = 60 // 20 Flächen * 3 Vertices

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

    // Goldener Schnitt für Ikosaeder
    private val phi = (1.0f + sqrt(5.0f)) / 2.0f
    private val scale = 1.0f

    // 12 Vertices eines Ikosaeders
    private val icoVertices = floatArrayOf(
        0f, 1f, phi,
        0f, -1f, phi,
        0f, 1f, -phi,
        0f, -1f, -phi,

        1f, phi, 0f,
        -1f, phi, 0f,
        1f, -phi, 0f,
        -1f, -phi, 0f,

        phi, 0f, 1f,
        phi, 0f, -1f,
        -phi, 0f, 1f,
        -phi, 0f, -1f
    ).map { it * scale }.toFloatArray()

    // 20 Flächen (Dreiecke) - Indices in icoVertices
    private val faces = intArrayOf(
        0, 1, 8,   // Face 0 - Gold
        0, 8, 4,   // Face 1 - Silber
        0, 4, 5,   // Face 2 - Bronze
        0, 5, 10,  // Face 3 - Navy
        0, 10, 1,  // Face 4 - Maroon

        1, 6, 8,   // Face 5 - Olive
        8, 6, 9,   // Face 6 - Teal
        8, 9, 4,   // Face 7 - Coral
        4, 9, 2,   // Face 8 - Rot
        4, 2, 5,   // Face 9 - Grün

        5, 2, 11,  // Face 10 - Blau
        5, 11, 10, // Face 11 - Gelb
        10, 11, 7, // Face 12 - Magenta
        10, 7, 1,  // Face 13 - Cyan
        1, 7, 6,   // Face 14 - Orange

        3, 2, 9,   // Face 15 - Pink
        3, 9, 6,   // Face 16 - Lila
        3, 6, 7,   // Face 17 - Türkis
        3, 7, 11,  // Face 18 - Lime
        3, 11, 2   // Face 19 - Braun
    )

    // Farben für die 20 Flächen (RGB + Alpha)
    private val faceColors = mapOf(
        0 to floatArrayOf(1f, 0.84f, 0f, 1f),     // Gold
        1 to floatArrayOf(0.75f, 0.75f, 0.75f, 1f), // Silber
        2 to floatArrayOf(0.8f, 0.5f, 0.2f, 1f),   // Bronze
        3 to floatArrayOf(0f, 0f, 0.5f, 1f),       // Navy
        4 to floatArrayOf(0.5f, 0f, 0f, 1f),       // Maroon
        5 to floatArrayOf(0.5f, 0.5f, 0f, 1f),     // Olive
        6 to floatArrayOf(0f, 0.5f, 0.5f, 1f),     // Teal
        7 to floatArrayOf(1f, 0.5f, 0.5f, 1f),     // Coral
        8 to floatArrayOf(1f, 0f, 0f, 1f),         // Rot
        9 to floatArrayOf(0f, 1f, 0f, 1f),         // Grün
        10 to floatArrayOf(0f, 0f, 1f, 1f),        // Blau
        11 to floatArrayOf(1f, 1f, 0f, 1f),        // Gelb
        12 to floatArrayOf(1f, 0f, 1f, 1f),        // Magenta
        13 to floatArrayOf(0f, 1f, 1f, 1f),        // Cyan
        14 to floatArrayOf(1f, 0.5f, 0f, 1f),      // Orange
        15 to floatArrayOf(1f, 0.75f, 0.8f, 1f),   // Pink
        16 to floatArrayOf(0.5f, 0f, 0.5f, 1f),    // Lila
        17 to floatArrayOf(0.25f, 0.88f, 0.82f, 1f), // Türkis
        18 to floatArrayOf(0.5f, 1f, 0f, 1f),      // Lime
        19 to floatArrayOf(0.55f, 0.27f, 0.07f, 1f) // Braun
    )

    init {
        // Vertices und Farben für alle Flächen aufbauen
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()

        for (faceIndex in 0 until 20) {
            val i1 = faces[faceIndex * 3] * 3
            val i2 = faces[faceIndex * 3 + 1] * 3
            val i3 = faces[faceIndex * 3 + 2] * 3

            // Drei Vertices des Dreiecks
            vertices.add(icoVertices[i1])
            vertices.add(icoVertices[i1 + 1])
            vertices.add(icoVertices[i1 + 2])

            vertices.add(icoVertices[i2])
            vertices.add(icoVertices[i2 + 1])
            vertices.add(icoVertices[i2 + 2])

            vertices.add(icoVertices[i3])
            vertices.add(icoVertices[i3 + 1])
            vertices.add(icoVertices[i3 + 2])

            // Farbe für alle 3 Vertices dieser Fläche
            val color = faceColors[faceIndex]!!
            for (i in 0 until 3) {
                colors.add(color[0])
                colors.add(color[1])
                colors.add(color[2])
                colors.add(color[3])
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

        // D20 zeichnen
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}
