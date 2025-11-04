package com.tuffgames.incrementalquest

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class D8 {
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val program: Int

    private val vertexCount = 24 // 8 Flächen * 3 Vertices

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

        // D8 als regulärer Oktaeder
        // 6 Vertices (wie ein 3D-Kreuz)
        val scale = 1.5f
        val v0 = floatArrayOf(0f, scale, 0f)      // Oben
        val v1 = floatArrayOf(0f, -scale, 0f)     // Unten
        val v2 = floatArrayOf(scale, 0f, 0f)      // Rechts
        val v3 = floatArrayOf(-scale, 0f, 0f)     // Links
        val v4 = floatArrayOf(0f, 0f, scale)      // Vorne
        val v5 = floatArrayOf(0f, 0f, -scale)     // Hinten

        // Farben für die 8 Flächen
        val faceColors = listOf(
            floatArrayOf(1f, 0f, 0f, 1f),         // 0 - Rot
            floatArrayOf(0f, 1f, 0f, 1f),         // 1 - Grün
            floatArrayOf(0f, 0f, 1f, 1f),         // 2 - Blau
            floatArrayOf(1f, 1f, 0f, 1f),         // 3 - Gelb
            floatArrayOf(1f, 0f, 1f, 1f),         // 4 - Magenta
            floatArrayOf(0f, 1f, 1f, 1f),         // 5 - Cyan
            floatArrayOf(1f, 0.5f, 0f, 1f),       // 6 - Orange
            floatArrayOf(1f, 0.75f, 0.8f, 1f)     // 7 - Pink
        )

        // Obere 4 Flächen (mit v0)
        addTriangle(vertices, colors, v0, v4, v2, faceColors[0])  // Vorne Rechts Oben - Rot
        addTriangle(vertices, colors, v0, v2, v5, faceColors[1])  // Hinten Rechts Oben - Grün
        addTriangle(vertices, colors, v0, v5, v3, faceColors[2])  // Hinten Links Oben - Blau
        addTriangle(vertices, colors, v0, v3, v4, faceColors[3])  // Vorne Links Oben - Gelb

        // Untere 4 Flächen (mit v1)
        addTriangle(vertices, colors, v1, v2, v4, faceColors[4])  // Vorne Rechts Unten - Magenta
        addTriangle(vertices, colors, v1, v5, v2, faceColors[5])  // Hinten Rechts Unten - Cyan
        addTriangle(vertices, colors, v1, v3, v5, faceColors[6])  // Hinten Links Unten - Orange
        addTriangle(vertices, colors, v1, v4, v3, faceColors[7])  // Vorne Links Unten - Pink

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

        // D8 zeichnen
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}
