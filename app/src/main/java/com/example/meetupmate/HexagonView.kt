package com.example.meetupmate

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class HexagonView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val path = Path()
    private var bitmap: Bitmap? = null

    init {
        paint.color = Color.BLACK // Example: Black color for the hexagon
        paint.style = Paint.Style.FILL
    }

    // Set the bitmap to be drawn inside the hexagon
    fun setImageBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        invalidate() // Redraw the view with the new bitmap
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = width / 2

        path.reset()
        path.moveTo(radius, 0f)
        path.lineTo(width, height / 4)
        path.lineTo(width, 3 * height / 4)
        path.lineTo(radius, height)
        path.lineTo(0f, 3 * height / 4)
        path.lineTo(0f, height / 4)
        path.close()

        canvas.drawPath(path, paint)

        // Draw the bitmap inside the hexagon
        bitmap?.let {
            val rectF = RectF(0f, 0f, width, height)
            val bitmapShader = BitmapShader(it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            val paint = Paint()
            paint.isAntiAlias = true
            paint.shader = bitmapShader

            val size = if (width < height) width else height
            val left = (width - size) / 2
            val top = (height - size) / 2
            canvas.drawBitmap(it, null, RectF(left, top, left + size, top + size), paint)
        }
    }
}
