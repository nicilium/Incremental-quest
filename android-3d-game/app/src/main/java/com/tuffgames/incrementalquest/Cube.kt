package com.tuffgames.incrementalquest

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Cube {
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val program: Int

    private val vertexCount = 36 // 6 Flächen * 2 Dreiecke * 3 Vertices

    // Vertex Shader - transformiert 3D-Koordinaten
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

    // Fragment Shader - färbt Pixel
    private val fragmentShaderCode = """
        precision mediump float;
        varying vec4 fColor;

        void main() {
            gl_FragColor = fColor;
        }
    """.trimIndent()

    // Würfel Vertices (8 Ecken definieren 6 Flächen)
    private val vertices = floatArrayOf(
        // Vorderseite (2 Dreiecke)
        -1f, -1f,  1f,  1f, -1f,  1f,  1f,  1f,  1f,
        -1f, -1f,  1f,  1f,  1f,  1f, -1f,  1f,  1f,

        // Rückseite
        -1f, -1f, -1f, -1f,  1f, -1f,  1f,  1f, -1f,
        -1f, -1f, -1f,  1f,  1f, -1f,  1f, -1f, -1f,

        // Links
        -1f, -1f, -1f, -1f, -1f,  1f, -1f,  1f,  1f,
        -1f, -1f, -1f, -1f,  1f,  1f, -1f,  1f, -1f,

        // Rechts
         1f, -1f, -1f,  1f,  1f, -1f,  1f,  1f,  1f,
         1f, -1f, -1f,  1f,  1f,  1f,  1f, -1f,  1f,

        // Oben
        -1f,  1f, -1f, -1f,  1f,  1f,  1f,  1f,  1f,
        -1f,  1f, -1f,  1f,  1f,  1f,  1f,  1f, -1f,

        // Unten
        -1f, -1f, -1f,  1f, -1f, -1f,  1f, -1f,  1f,
        -1f, -1f, -1f,  1f, -1f,  1f, -1f, -1f,  1f
    )

    // Farben für jede Vertex (RGB + Alpha)
    private val colors = floatArrayOf(
        // Vorderseite - Rot
        1f, 0f, 0f, 1f,  1f, 0f, 0f, 1f,  1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,  1f, 0f, 0f, 1f,  1f, 0f, 0f, 1f,

        // Rückseite - Grün
        0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,  0f, 1f, 0f, 1f,

        // Links - Blau
        0f, 0f, 1f, 1f,  0f, 0f, 1f, 1f,  0f, 0f, 1f, 1f,
        0f, 0f, 1f, 1f,  0f, 0f, 1f, 1f,  0f, 0f, 1f, 1f,

        // Rechts - Gelb
        1f, 1f, 0f, 1f,  1f, 1f, 0f, 1f,  1f, 1f, 0f, 1f,
        1f, 1f, 0f, 1f,  1f, 1f, 0f, 1f,  1f, 1f, 0f, 1f,

        // Oben - Magenta
        1f, 0f, 1f, 1f,  1f, 0f, 1f, 1f,  1f, 0f, 1f, 1f,
        1f, 0f, 1f, 1f,  1f, 0f, 1f, 1f,  1f, 0f, 1f, 1f,

        // Unten - Cyan
        0f, 1f, 1f, 1f,  0f, 1f, 1f, 1f,  0f, 1f, 1f, 1f,
        0f, 1f, 1f, 1f,  0f, 1f, 1f, 1f,  0f, 1f, 1f, 1f
    )

    init {
        // Vertex Buffer initialisieren
        val vb = ByteBuffer.allocateDirect(vertices.size * 4)
        vb.order(ByteOrder.nativeOrder())
        vertexBuffer = vb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        // Color Buffer initialisieren
        val cb = ByteBuffer.allocateDirect(colors.size * 4)
        cb.order(ByteOrder.nativeOrder())
        colorBuffer = cb.asFloatBuffer()
        colorBuffer.put(colors)
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

        // Würfel zeichnen
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}
