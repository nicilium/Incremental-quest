package com.tuffgames.incrementalquest

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class D10 {
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val program: Int

    private val vertexCount = 30 // 10 Flächen * 3 Vertices

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

        // D10 als Pentagonale Dipyramide
        // 1 Vertex oben, 5 Vertices in der Mitte (Pentagon), 1 Vertex unten
        val scale = 1.5f
        val topVertex = floatArrayOf(0f, 1.5f * scale, 0f)
        val bottomVertex = floatArrayOf(0f, -1.5f * scale, 0f)

        // Pentagon in der Mitte (5 Vertices)
        val pentagonVertices = mutableListOf<FloatArray>()
        for (i in 0 until 5) {
            val angle = (i * 2 * PI / 5).toFloat()
            pentagonVertices.add(floatArrayOf(
                cos(angle) * scale,
                0f,
                sin(angle) * scale
            ))
        }

        // Farben für die 10 Flächen
        val faceColors = listOf(
            floatArrayOf(1f, 0f, 0f, 1f),         // 0 - Rot
            floatArrayOf(0f, 1f, 0f, 1f),         // 1 - Grün
            floatArrayOf(0f, 0f, 1f, 1f),         // 2 - Blau
            floatArrayOf(1f, 1f, 0f, 1f),         // 3 - Gelb
            floatArrayOf(1f, 0f, 1f, 1f),         // 4 - Magenta
            floatArrayOf(0f, 1f, 1f, 1f),         // 5 - Cyan
            floatArrayOf(1f, 0.5f, 0f, 1f),       // 6 - Orange
            floatArrayOf(1f, 0.75f, 0.8f, 1f),    // 7 - Pink
            floatArrayOf(0.5f, 0f, 0.5f, 1f),     // 8 - Lila
            floatArrayOf(0.25f, 0.88f, 0.82f, 1f) // 9 - Türkis
        )

        // 5 obere Dreiecke (top -> pentagon[i] -> pentagon[i+1])
        for (i in 0 until 5) {
            val color = faceColors[i]
            val p1 = pentagonVertices[i]
            val p2 = pentagonVertices[(i + 1) % 5]

            // Top
            vertices.add(topVertex[0])
            vertices.add(topVertex[1])
            vertices.add(topVertex[2])

            // Pentagon Vertex i
            vertices.add(p1[0])
            vertices.add(p1[1])
            vertices.add(p1[2])

            // Pentagon Vertex i+1
            vertices.add(p2[0])
            vertices.add(p2[1])
            vertices.add(p2[2])

            // Farbe
            for (j in 0 until 3) {
                colors.add(color[0])
                colors.add(color[1])
                colors.add(color[2])
                colors.add(color[3])
            }
        }

        // 5 untere Dreiecke (bottom -> pentagon[i+1] -> pentagon[i])
        for (i in 0 until 5) {
            val color = faceColors[i + 5]
            val p1 = pentagonVertices[i]
            val p2 = pentagonVertices[(i + 1) % 5]

            // Bottom
            vertices.add(bottomVertex[0])
            vertices.add(bottomVertex[1])
            vertices.add(bottomVertex[2])

            // Pentagon Vertex i+1 (umgekehrte Reihenfolge für korrekte Normale)
            vertices.add(p2[0])
            vertices.add(p2[1])
            vertices.add(p2[2])

            // Pentagon Vertex i
            vertices.add(p1[0])
            vertices.add(p1[1])
            vertices.add(p1[2])

            // Farbe
            for (j in 0 until 3) {
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

        // D10 zeichnen
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
    }
}
