package com.tuffgames.incrementalquest

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.sqrt

class D4 {
    private val vertexBuffer: FloatBuffer
    private val program: Int

    private val vertexCount = 12 // 4 Flächen * 3 Vertices

    // Vertex Shader
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;

        void main() {
            gl_Position = uMVPMatrix * vPosition;
        }
    """.trimIndent()

    // Fragment Shader mit Uniform Color
    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;

        void main() {
            gl_FragColor = uColor;
        }
    """.trimIndent()

    init {
        val vertices = mutableListOf<Float>()

        // D4 als regulärer Tetraeder
        // 4 Vertices definieren einen Tetraeder
        val scale = 1.5f
        val h = sqrt(2.0 / 3.0).toFloat() * scale  // Höhe für regulären Tetraeder

        // Tetraeder-Vertices (zentriert)
        val v0 = floatArrayOf(0f, h, 0f)                    // Oben
        val v1 = floatArrayOf(-scale, -h/2, scale)          // Vorne Links
        val v2 = floatArrayOf(scale, -h/2, scale)           // Vorne Rechts
        val v3 = floatArrayOf(0f, -h/2, -scale * 1.5f)      // Hinten

        // Einfarbiger Tetraeder - Farbe wird beim Zeichnen übergeben
        // Face 0: Vorne (v0, v1, v2)
        addTriangle(vertices, v0, v1, v2)

        // Face 1: Links (v0, v3, v1)
        addTriangle(vertices, v0, v3, v1)

        // Face 2: Rechts (v0, v2, v3)
        addTriangle(vertices, v0, v2, v3)

        // Face 3: Unten (v1, v3, v2)
        addTriangle(vertices, v1, v3, v2)

        // Vertex Buffer initialisieren
        val vb = ByteBuffer.allocateDirect(vertices.size * 4)
        vb.order(ByteOrder.nativeOrder())
        vertexBuffer = vb.asFloatBuffer()
        vertexBuffer.put(vertices.toFloatArray())
        vertexBuffer.position(0)

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
        v1: FloatArray,
        v2: FloatArray,
        v3: FloatArray
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
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun draw(mvpMatrix: FloatArray, color: FloatArray) {
        // Shader-Programm aktivieren
        GLES20.glUseProgram(program)

        // Vertex Position Handle
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        // Color als Uniform übergeben
        val colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        // MVP Matrix Handle
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // D4 zeichnen
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // Cleanup
        GLES20.glDisableVertexAttribArray(positionHandle)
    }
}
