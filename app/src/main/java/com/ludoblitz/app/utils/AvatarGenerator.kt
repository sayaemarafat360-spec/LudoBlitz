package com.ludoblitz.app.utils

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Random Avatar Generator
 * Generates unique, colorful avatars without needing Firebase Storage
 * Promoted as a feature: "Unique AI-Generated Avatars!"
 */
object AvatarGenerator {
    
    // Avatar style presets
    enum class AvatarStyle {
        GEOMETRIC,
        GRADIENT_CIRCLE,
        INITIALS,
        PATTERN,
        ANIMAL,
        ROBOT
    }
    
    // Color palettes for avatars
    private val colorPalettes = listOf(
        listOf("#FF6B6B", "#4ECDC4", "#45B7D1"), // Coral & Teal
        listOf("#667eea", "#764ba2", "#f093fb"), // Purple Dream
        listOf("#11998e", "#38ef7d", "#00d9ff"), // Mint Fresh
        listOf("#fc466b", "#3f5efb", "#c471ed"), // Neon Nights
        listOf("#ff9a9e", "#fecfef", "#fad0c4"), // Soft Pink
        listOf("#a18cd1", "#fbc2eb", "#f6d365"), // Pastel Dream
        listOf("#ff6a00", "#ee0979", "#ff6b6b"), // Sunset Fire
        listOf("#00c6ff", "#0072ff", "#00f2fe"), // Ocean Blue
        listOf("#f857a6", "#ff5858", "#ff9966"), // Warm Glow
        listOf("#43e97b", "#38f9d7", "#4facfe"), // Fresh Nature
        listOf("#fa709a", "#fee140", "#f6d365"), // Candy Pop
        listOf("#667eea", "#764ba2", "#6B8DD6")  // Royal Purple
    )
    
    // Background colors
    private val backgroundColors = listOf(
        "#1A1A2E", "#16213E", "#0F3460", "#1F1F3D",
        "#2D2D44", "#1E1E2F", "#252540", "#1B1B32"
    )
    
    /**
     * Generate a random avatar bitmap
     */
    fun generateAvatar(
        size: Int = 200,
        style: AvatarStyle? = null,
        seed: String? = null,
        displayName: String? = null
    ): Bitmap {
        val random = if (seed != null) Random(seed.hashCode()) else Random.Default
        val selectedStyle = style ?: AvatarStyle.values().random(random)
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        when (selectedStyle) {
            AvatarStyle.GEOMETRIC -> drawGeometricAvatar(canvas, size, random)
            AvatarStyle.GRADIENT_CIRCLE -> drawGradientCircleAvatar(canvas, size, random)
            AvatarStyle.INITIALS -> drawInitialsAvatar(canvas, size, random, displayName)
            AvatarStyle.PATTERN -> drawPatternAvatar(canvas, size, random)
            AvatarStyle.ANIMAL -> drawAnimalAvatar(canvas, size, random)
            AvatarStyle.ROBOT -> drawRobotAvatar(canvas, size, random)
        }
        
        return bitmap
    }
    
    /**
     * Generate geometric style avatar
     */
    private fun drawGeometricAvatar(canvas: Canvas, size: Int, random: Random) {
        val palette = colorPalettes.random(random)
        val bgColor = Color.parseColor(backgroundColors.random(random))
        
        // Background
        canvas.drawColor(bgColor)
        
        // Draw geometric shapes
        val shapes = random.nextInt(3, 7)
        repeat(shapes) {
            val paint = Paint().apply {
                color = Color.parseColor(palette.random(random))
                isAntiAlias = true
                alpha = 200
            }
            
            val shapeType = random.nextInt(3)
            val centerX = random.nextFloat() * size
            val centerY = random.nextFloat() * size
            val shapeSize = random.nextFloat() * (size / 3) + (size / 6)
            
            when (shapeType) {
                0 -> canvas.drawCircle(centerX, centerY, shapeSize, paint)
                1 -> {
                    val rect = RectF(
                        centerX - shapeSize,
                        centerY - shapeSize,
                        centerX + shapeSize,
                        centerY + shapeSize
                    )
                    val angle = random.nextFloat() * 360f
                    canvas.rotate(angle, centerX, centerY)
                    canvas.drawRect(rect, paint)
                    canvas.rotate(-angle, centerX, centerY)
                }
                2 -> drawTriangle(canvas, paint, centerX, centerY, shapeSize, random.nextFloat() * 360f)
            }
        }
        
        // Add accent circle in center
        val centerPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            alpha = 30
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 4f, centerPaint)
    }
    
    /**
     * Generate gradient circle avatar
     */
    private fun drawGradientCircleAvatar(canvas: Canvas, size: Int, random: Random) {
        val palette = colorPalettes.random(random)
        val bgColor = Color.parseColor(backgroundColors.random(random))
        
        // Background
        canvas.drawColor(bgColor)
        
        // Create gradient
        val colors = intArrayOf(
            Color.parseColor(palette[0]),
            Color.parseColor(palette[1]),
            Color.parseColor(palette.getOrElse(2) { palette[0] })
        )
        
        val gradient = LinearGradient(
            0f, 0f, size.toFloat(), size.toFloat(),
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        
        val paint = Paint().apply {
            shader = gradient
            isAntiAlias = true
        }
        
        // Draw main circle
        canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint)
        
        // Add glow effect
        val glowPaint = Paint().apply {
            color = Color.parseColor(palette[0])
            isAntiAlias = true
            alpha = 50
            maskFilter = BlurMaskFilter(size / 8f, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, glowPaint)
        
        // Add inner circle
        val innerPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            alpha = 40
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 5f, innerPaint)
    }
    
    /**
     * Generate initials avatar
     */
    private fun drawInitialsAvatar(canvas: Canvas, size: Int, random: Random, displayName: String?) {
        val palette = colorPalettes.random(random)
        
        // Create gradient background
        val colors = intArrayOf(
            Color.parseColor(palette[0]),
            Color.parseColor(palette[1])
        )
        
        val gradient = LinearGradient(
            0f, 0f, size.toFloat(), size.toFloat(),
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        
        val bgPaint = Paint().apply {
            shader = gradient
            isAntiAlias = true
        }
        
        // Draw background
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)
        
        // Get initials
        val initials = getInitials(displayName ?: "Player${random.nextInt(100)}")
        
        // Draw initials
        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = size / 2.5f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(size / 20f, 0f, 0f, Color.parseColor("#40000000"))
        }
        
        val textHeight = textPaint.descent() - textPaint.ascent()
        val textOffset = textHeight / 2 - textPaint.descent()
        
        canvas.drawText(
            initials,
            size / 2f,
            size / 2f + textOffset,
            textPaint
        )
    }
    
    /**
     * Generate pattern avatar
     */
    private fun drawPatternAvatar(canvas: Canvas, size: Int, random: Random) {
        val palette = colorPalettes.random(random)
        val bgColor = Color.parseColor(backgroundColors.random(random))
        
        // Background
        canvas.drawColor(bgColor)
        
        // Draw pattern
        val patternType = random.nextInt(4)
        
        when (patternType) {
            0 -> drawDotPattern(canvas, size, random, palette)
            1 -> drawLinePattern(canvas, size, random, palette)
            2 -> drawWavePattern(canvas, size, random, palette)
            3 -> drawHexPattern(canvas, size, random, palette)
        }
    }
    
    private fun drawDotPattern(canvas: Canvas, size: Int, random: Random, palette: List<String>) {
        val dotCount = random.nextInt(15, 30)
        repeat(dotCount) {
            val paint = Paint().apply {
                color = Color.parseColor(palette.random(random))
                isAntiAlias = true
                alpha = random.nextInt(150, 255)
            }
            
            val x = random.nextFloat() * size
            val y = random.nextFloat() * size
            val radius = random.nextFloat() * (size / 8f) + (size / 20f)
            
            canvas.drawCircle(x, y, radius, paint)
        }
    }
    
    private fun drawLinePattern(canvas: Canvas, size: Int, random: Random, palette: List<String>) {
        val lineCount = random.nextInt(5, 12)
        repeat(lineCount) {
            val paint = Paint().apply {
                color = Color.parseColor(palette.random(random))
                isAntiAlias = true
                strokeWidth = random.nextFloat() * 20f + 5f
                strokeCap = Paint.Cap.ROUND
                alpha = random.nextInt(150, 255)
            }
            
            val startX = random.nextFloat() * size
            val startY = random.nextFloat() * size
            val endX = random.nextFloat() * size
            val endY = random.nextFloat() * size
            
            canvas.drawLine(startX, startY, endX, endY, paint)
        }
    }
    
    private fun drawWavePattern(canvas: Canvas, size: Int, random: Random, palette: List<String>) {
        val waveCount = random.nextInt(3, 6)
        repeat(waveCount) { waveIndex ->
            val paint = Paint().apply {
                color = Color.parseColor(palette.getOrElse(waveIndex % palette.size) { palette[0] })
                isAntiAlias = true
                strokeWidth = 8f
                style = Paint.Style.STROKE
                alpha = 200
            }
            
            val path = Path()
            val amplitude = size / 8f + random.nextFloat() * (size / 6f)
            val frequency = 2f + random.nextFloat() * 3f
            val yOffset = size * (waveIndex + 1) / (waveCount + 2).toFloat()
            
            path.moveTo(0f, yOffset)
            
            for (x in 0..size step 5) {
                val y = yOffset + sin(x * frequency * Math.PI / size) * amplitude
                path.lineTo(x.toFloat(), y.toFloat())
            }
            
            canvas.drawPath(path, paint)
        }
    }
    
    private fun drawHexPattern(canvas: Canvas, size: Int, random: Random, palette: List<String>) {
        val hexSize = size / 6f
        val hexCount = random.nextInt(4, 8)
        
        repeat(hexCount) {
            val paint = Paint().apply {
                color = Color.parseColor(palette.random(random))
                isAntiAlias = true
                alpha = random.nextInt(150, 255)
            }
            
            val centerX = random.nextFloat() * size
            val centerY = random.nextFloat() * size
            val actualSize = hexSize + random.nextFloat() * hexSize
            
            drawHexagon(canvas, paint, centerX, centerY, actualSize)
        }
    }
    
    private fun drawHexagon(canvas: Canvas, paint: Paint, cx: Float, cy: Float, size: Float) {
        val path = Path()
        for (i in 0 until 6) {
            val angle = Math.PI / 6 + i * Math.PI / 3
            val x = cx + (size * cos(angle)).toFloat()
            val y = cy + (size * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }
    
    /**
     * Generate animal-style avatar (cute geometric animals)
     */
    private fun drawAnimalAvatar(canvas: Canvas, size: Int, random: Random) {
        val palette = colorPalettes.random(random)
        val bgColor = Color.parseColor(backgroundColors.random(random))
        
        // Background
        canvas.drawColor(bgColor)
        
        // Body
        val bodyPaint = Paint().apply {
            color = Color.parseColor(palette[0])
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 3f, bodyPaint)
        
        // Eyes
        val eyePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }
        val pupilPaint = Paint().apply {
            color = Color.parseColor(backgroundColors.random(random))
            isAntiAlias = true
        }
        
        val eyeSize = size / 10f
        val eyeOffset = size / 8f
        
        // Left eye
        canvas.drawCircle(size / 2f - eyeOffset, size / 2f - size / 12f, eyeSize, eyePaint)
        canvas.drawCircle(size / 2f - eyeOffset, size / 2f - size / 12f, eyeSize / 2, pupilPaint)
        
        // Right eye
        canvas.drawCircle(size / 2f + eyeOffset, size / 2f - size / 12f, eyeSize, eyePaint)
        canvas.drawCircle(size / 2f + eyeOffset, size / 2f - size / 12f, eyeSize / 2, pupilPaint)
        
        // Ears (cat style)
        val earPaint = Paint().apply {
            color = Color.parseColor(palette[1])
            isAntiAlias = true
        }
        
        drawTriangle(canvas, earPaint, size / 3f, size / 3f, size / 6f, -30f)
        drawTriangle(canvas, earPaint, size * 2f / 3f, size / 3f, size / 6f, 30f)
        
        // Nose
        val nosePaint = Paint().apply {
            color = Color.parseColor(palette.getOrElse(2) { palette[0] })
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f + size / 10f, size / 20f, nosePaint)
    }
    
    /**
     * Generate robot-style avatar
     */
    private fun drawRobotAvatar(canvas: Canvas, size: Int, random: Random) {
        val palette = colorPalettes.random(random)
        val bgColor = Color.parseColor(backgroundColors.random(random))
        
        // Background
        canvas.drawColor(bgColor)
        
        // Head (rounded square)
        val headPaint = Paint().apply {
            color = Color.parseColor(palette[0])
            isAntiAlias = true
        }
        val headRect = RectF(size / 5f, size / 5f, size * 4f / 5f, size * 4f / 5f)
        canvas.drawRoundRect(headRect, size / 10f, size / 10f, headPaint)
        
        // Face plate
        val facePaint = Paint().apply {
            color = Color.parseColor(palette[1])
            isAntiAlias = true
        }
        val faceRect = RectF(size / 4f, size / 3f, size * 3f / 4f, size * 2f / 3f)
        canvas.drawRoundRect(faceRect, size / 15f, size / 15f, facePaint)
        
        // Eyes (LED style)
        val eyePaint = Paint().apply {
            color = Color.parseColor(palette.getOrElse(2) { "#FFFFFF" })
            isAntiAlias = true
        }
        
        val eyeWidth = size / 7f
        val eyeHeight = size / 12f
        val eyeY = size / 2f - size / 20f
        
        // Left eye
        canvas.drawRoundRect(
            RectF(size / 3f - eyeWidth / 2, eyeY - eyeHeight / 2,
                  size / 3f + eyeWidth / 2, eyeY + eyeHeight / 2),
            eyeHeight / 2, eyeHeight / 2, eyePaint
        )
        
        // Right eye
        canvas.drawRoundRect(
            RectF(size * 2f / 3f - eyeWidth / 2, eyeY - eyeHeight / 2,
                  size * 2f / 3f + eyeWidth / 2, eyeY + eyeHeight / 2),
            eyeHeight / 2, eyeHeight / 2, eyePaint
        )
        
        // Antenna
        val antennaPaint = Paint().apply {
            color = Color.parseColor(palette[1])
            isAntiAlias = true
            strokeWidth = size / 25f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(size / 2f, size / 5f, size / 2f, size / 10f, antennaPaint)
        canvas.drawCircle(size / 2f, size / 12f, size / 20f, eyePaint)
        
        // Mouth (speaker grille)
        val mouthPaint = Paint().apply {
            color = Color.parseColor(palette[0])
            isAntiAlias = true
            alpha = 180
        }
        repeat(3) { i ->
            val y = size * 3f / 5f + i * size / 20f
            canvas.drawLine(size / 3f, y, size * 2f / 3f, y, mouthPaint.apply {
                strokeWidth = size / 40f
            })
        }
    }
    
    /**
     * Helper to draw triangles
     */
    private fun drawTriangle(canvas: Canvas, paint: Paint, cx: Float, cy: Float, size: Float, rotation: Float) {
        val path = Path()
        val angles = floatArrayOf(-90f, 30f, 150f)
        
        canvas.save()
        canvas.rotate(rotation, cx, cy)
        
        angles.forEachIndexed { i, angle ->
            val rad = Math.toRadians(angle.toDouble())
            val x = cx + (size * cos(rad)).toFloat()
            val y = cy + (size * sin(rad)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        
        canvas.drawPath(path, paint)
        canvas.restore()
    }
    
    /**
     * Get initials from name
     */
    private fun getInitials(name: String): String {
        val words = name.trim().split(" ").filter { it.isNotEmpty() }
        return when {
            words.isEmpty() -> "?"
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "${words[0].first()}${words[1].first()}".uppercase()
        }
    }
    
    /**
     * Generate avatar URL-style seed for consistency
     */
    fun generateSeedForUser(userId: String): String {
        return "avatar_${userId}_${System.currentTimeMillis()}"
    }
    
    /**
     * Get random avatar style
     */
    fun getRandomStyle(): AvatarStyle {
        return AvatarStyle.values().random()
    }
}
