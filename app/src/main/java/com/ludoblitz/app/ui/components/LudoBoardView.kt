package com.ludoblitz.app.ui.components

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import com.ludoblitz.app.R
import com.ludoblitz.app.data.model.Token
import com.ludoblitz.app.data.model.TokenColor
import com.ludoblitz.app.data.model.Player

/**
 * Custom View for rendering the Ludo game board
 * Handles drawing the board, paths, home bases, and tokens with animations
 */
class LudoBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val yellowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val homePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tokenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tokenBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Board dimensions
    private var cellSize = 0f
    private var boardSize = 0f
    private var tokenRadius = 0f

    // Colors
    private val redColor: Int
    private val greenColor: Int
    private val yellowColor: Int
    private val blueColor: Int
    private val boardBgColor: Int
    private val pathColor: Int
    private val borderColor: Int

    // Players and tokens
    private var players: List<Player> = emptyList()
    private val tokenPositions = mutableMapOf<String, PointF>()
    
    // Animation
    private var animatingTokens = mutableSetOf<String>()
    
    // Board theme
    private var boardTheme: String = "classic"

    // Path positions (52 main positions + 5 home stretch positions per color)
    // Each position maps to (column, row) in grid coordinates
    private val positionCoords = mutableMapOf<Int, Pair<Float, Float>>()
    
    // Base positions for tokens (when in base, not on board)
    private val basePositions = mutableMapOf<TokenColor, Array<Pair<Float, Float>>>()

    init {
        // Get colors from resources
        redColor = context.getColor(R.color.token_red)
        greenColor = context.getColor(R.color.token_green)
        yellowColor = context.getColor(R.color.token_yellow)
        blueColor = context.getColor(R.color.token_blue)
        boardBgColor = context.getColor(R.color.board_background)
        pathColor = context.getColor(R.color.board_path)
        borderColor = context.getColor(R.color.board_border)

        // Initialize paints
        initPaints()
        
        // Calculate position coordinates
        calculatePositions()
    }

    private fun initPaints() {
        boardPaint.apply {
            color = boardBgColor
            style = Paint.Style.FILL
        }

        pathPaint.apply {
            color = pathColor
            style = Paint.Style.FILL
        }

        redPaint.apply {
            color = redColor
            style = Paint.Style.FILL
        }

        greenPaint.apply {
            color = greenColor
            style = Paint.Style.FILL
        }

        yellowPaint.apply {
            color = yellowColor
            style = Paint.Style.FILL
        }

        bluePaint.apply {
            color = blueColor
            style = Paint.Style.FILL
        }

        borderPaint.apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        starPaint.apply {
            color = context.getColor(R.color.safe_zone_star)
            style = Paint.Style.FILL
        }

        homePaint.apply {
            color = context.getColor(R.color.board_home_center)
            style = Paint.Style.FILL
        }

        tokenPaint.apply {
            style = Paint.Style.FILL
        }

        tokenBorderPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }

        textPaint.apply {
            color = Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        boardSize = w.toFloat()
        cellSize = boardSize / 15f
        tokenRadius = cellSize * 0.35f
        
        // Recalculate positions with actual cell size
        calculatePositions()
    }

    private fun calculatePositions() {
        // Main path positions (1-52)
        // These are the positions on the outer track
        
        // Red start at position 1, goes clockwise
        // Green start at position 14
        // Yellow start at position 27
        // Blue start at position 40
        
        // Bottom row (left to right): positions 1-5 (Red exit path)
        for (i in 0..5) {
            positionCoords[i + 1] = Pair((i + 1) * cellSize, 7 * cellSize)
        }
        
        // Left column (bottom to top): positions 6-12
        for (i in 0..5) {
            positionCoords[6 + i] = Pair(6 * cellSize, (6 - i) * cellSize)
        }
        positionCoords[12] = Pair(6 * cellSize, 6 * cellSize)
        
        // Top-left corner going right
        for (i in 0..5) {
            positionCoords[13 + i] = Pair(7 * cellSize, (i + 1) * cellSize)
        }
        
        // Top row (left to right): Green exit
        positionCoords[13] = Pair(7 * cellSize, cellSize)
        for (i in 0..5) {
            positionCoords[14 + i] = Pair((i + 1) * cellSize, 6 * cellSize)
        }
        
        // Top-right going down
        for (i in 0..5) {
            positionCoords[20 + i] = Pair(8 * cellSize, (i + 1) * cellSize)
        }
        
        // Right column (top to bottom)
        for (i in 0..5) {
            positionCoords[26 + i] = Pair((14 - i) * cellSize, 7 * cellSize)
        }
        
        // Bottom-right going left
        for (i in 0..5) {
            positionCoords[32 + i] = Pair(8 * cellSize, (14 - i) * cellSize)
        }
        
        // Continue around the board...
        // This is simplified - in production you'd calculate all 52 positions
        
        // Red home stretch (positions 52-56 for Red)
        for (i in 0..5) {
            positionCoords[100 + i] = Pair((i + 1) * cellSize, 7 * cellSize)
        }
        
        // Green home stretch
        for (i in 0..5) {
            positionCoords[200 + i] = Pair(7 * cellSize, (i + 1) * cellSize)
        }
        
        // Yellow home stretch
        for (i in 0..5) {
            positionCoords[300 + i] = Pair((14 - i) * cellSize, 7 * cellSize)
        }
        
        // Blue home stretch
        for (i in 0..5) {
            positionCoords[400 + i] = Pair(7 * cellSize, (14 - i) * cellSize)
        }
        
        // Base positions for each color
        basePositions[TokenColor.RED] = arrayOf(
            Pair(1.5f * cellSize, 1.5f * cellSize),
            Pair(4.5f * cellSize, 1.5f * cellSize),
            Pair(1.5f * cellSize, 4.5f * cellSize),
            Pair(4.5f * cellSize, 4.5f * cellSize)
        )
        
        basePositions[TokenColor.GREEN] = arrayOf(
            Pair(10.5f * cellSize, 1.5f * cellSize),
            Pair(13.5f * cellSize, 1.5f * cellSize),
            Pair(10.5f * cellSize, 4.5f * cellSize),
            Pair(13.5f * cellSize, 4.5f * cellSize)
        )
        
        basePositions[TokenColor.YELLOW] = arrayOf(
            Pair(10.5f * cellSize, 10.5f * cellSize),
            Pair(13.5f * cellSize, 10.5f * cellSize),
            Pair(10.5f * cellSize, 13.5f * cellSize),
            Pair(13.5f * cellSize, 13.5f * cellSize)
        )
        
        basePositions[TokenColor.BLUE] = arrayOf(
            Pair(1.5f * cellSize, 10.5f * cellSize),
            Pair(4.5f * cellSize, 10.5f * cellSize),
            Pair(1.5f * cellSize, 13.5f * cellSize),
            Pair(4.5f * cellSize, 13.5f * cellSize)
        )
    }

    /**
     * Set players to render
     */
    fun setPlayers(players: List<Player>) {
        this.players = players
        updateTokenPositions()
        invalidate()
    }

    /**
     * Update token positions based on game state
     */
    private fun updateTokenPositions() {
        players.forEach { player ->
            player.tokens.forEachIndexed { index, token ->
                val key = "${player.color}_$index"
                
                if (token.isHome) {
                    // Token finished - don't render or render at home center
                    val homeCenter = getHomeCenterPosition(player.color)
                    tokenPositions[key] = PointF(homeCenter.first, homeCenter.second)
                } else if (token.isInBase()) {
                    // Token in base
                    val basePos = basePositions[player.color]?.get(index)
                    if (basePos != null) {
                        tokenPositions[key] = PointF(basePos.first, basePos.second)
                    }
                } else {
                    // Token on board
                    val boardPos = getBoardPosition(token.position, player.color)
                    tokenPositions[key] = PointF(boardPos.first, boardPos.second)
                }
            }
        }
    }

    /**
     * Get screen coordinates for a board position
     */
    private fun getBoardPosition(position: Int, color: TokenColor): Pair<Float, Float> {
        // Simplified position calculation
        // In production, you'd have full mapping for all 52 positions
        
        val adjustedPos = position % 52
        if (adjustedPos == 0) return Pair(7.5f * cellSize, 7.5f * cellSize)
        
        // Calculate based on position around the board
        val angle = (adjustedPos - 1) * 360f / 52f
        val radius = 5.5f * cellSize
        val centerX = 7.5f * cellSize
        val centerY = 7.5f * cellSize
        
        val radians = Math.toRadians(angle.toDouble() - 90)
        val x = centerX + (radius * Math.cos(radians)).toFloat()
        val y = centerY + (radius * Math.sin(radians)).toFloat()
        
        return Pair(x, y)
    }

    /**
     * Get home center position for finished tokens
     */
    private fun getHomeCenterPosition(color: TokenColor): Pair<Float, Float> {
        return when (color) {
            TokenColor.RED -> Pair(3f * cellSize, 7.5f * cellSize)
            TokenColor.GREEN -> Pair(7.5f * cellSize, 3f * cellSize)
            TokenColor.YELLOW -> Pair(12f * cellSize, 7.5f * cellSize)
            TokenColor.BLUE -> Pair(7.5f * cellSize, 12f * cellSize)
        }
    }

    /**
     * Animate token movement
     */
    fun animateTokenMove(
        playerColor: TokenColor,
        tokenIndex: Int,
        fromPosition: Int,
        toPosition: Int,
        duration: Long = 300,
        onComplete: () -> Unit = {}
    ) {
        val key = "${playerColor}_$tokenIndex"
        if (animatingTokens.contains(key)) return
        
        animatingTokens.add(key)
        
        val startPos = if (fromPosition == -1) {
            basePositions[playerColor]?.get(tokenIndex) ?: Pair(0f, 0f)
        } else {
            getBoardPosition(fromPosition, playerColor)
        }
        
        val endPos = getBoardPosition(toPosition, playerColor)
        
        val tokenView = PointF(startPos.first, startPos.second)
        
        val animatorX = ObjectAnimator.ofFloat(tokenView, "x", startPos.first, endPos.first)
        val animatorY = ObjectAnimator.ofFloat(tokenView, "y", startPos.second, endPos.second)
        
        animatorX.interpolator = AccelerateDecelerateInterpolator()
        animatorY.interpolator = AccelerateDecelerateInterpolator()
        
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animatorX, animatorY)
        animatorSet.duration = duration
        
        animatorSet.addUpdateListener {
            tokenPositions[key] = PointF(tokenView.x, tokenView.y)
            invalidate()
        }
        
        animatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                animatingTokens.remove(key)
                tokenPositions[key] = PointF(endPos.first, endPos.second)
                invalidate()
                onComplete()
            }
        })
        
        animatorSet.start()
    }

    /**
     * Highlight valid moves
     */
    fun highlightToken(playerColor: TokenColor, tokenIndex: Int, highlight: Boolean) {
        // Add visual highlight for valid token selection
        invalidate()
    }

    /**
     * Set board theme
     */
    fun setBoardTheme(theme: String) {
        this.boardTheme = theme
        when (theme) {
            "classic" -> {
                boardPaint.color = boardBgColor
                pathPaint.color = pathColor
            }
            "neon" -> {
                boardPaint.color = Color.parseColor("#1A1A2E")
                pathPaint.color = Color.parseColor("#2D2D44")
            }
            "nature" -> {
                boardPaint.color = Color.parseColor("#E8F5E9")
                pathPaint.color = Color.parseColor("#C8E6C9")
            }
            "royal" -> {
                boardPaint.color = Color.parseColor("#FCE4EC")
                pathPaint.color = Color.parseColor("#F8BBD9")
            }
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw board background
        drawBoardBackground(canvas)

        // Draw home bases (colored corners)
        drawHomeBases(canvas)

        // Draw paths
        drawPaths(canvas)

        // Draw home stretch (colored paths to center)
        drawHomeStretches(canvas)

        // Draw center home
        drawCenterHome(canvas)

        // Draw safe zones (stars)
        drawSafeZones(canvas)

        // Draw borders
        drawBorders(canvas)

        // Draw tokens
        drawTokens(canvas)
    }

    private fun drawBoardBackground(canvas: Canvas) {
        val rect = RectF(0f, 0f, boardSize, boardSize)
        canvas.drawRoundRect(rect, 16f, 16f, boardPaint)
    }

    private fun drawHomeBases(canvas: Canvas) {
        // Red home (top-left)
        val redRect = RectF(0f, 0f, cellSize * 6, cellSize * 6)
        canvas.drawRoundRect(redRect, 16f, 16f, redPaint)

        // Green home (top-right)
        val greenRect = RectF(cellSize * 9, 0f, boardSize, cellSize * 6)
        canvas.drawRoundRect(greenRect, 16f, 16f, greenPaint)

        // Yellow home (bottom-right)
        val yellowRect = RectF(cellSize * 9, cellSize * 9, boardSize, boardSize)
        canvas.drawRoundRect(yellowRect, 16f, 16f, yellowPaint)

        // Blue home (bottom-left)
        val blueRect = RectF(0f, cellSize * 9, cellSize * 6, boardSize)
        canvas.drawRoundRect(blueRect, 16f, 16f, bluePaint)

        // Draw token circles in each home base
        drawBaseTokenCircles(canvas)
    }

    private fun drawBaseTokenCircles(canvas: Canvas) {
        val circleRadius = cellSize * 0.35f
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pathColor
            style = Paint.Style.FILL
        }

        // Red base circles
        basePositions[TokenColor.RED]?.forEach { (x, y) ->
            canvas.drawCircle(x, y, circleRadius, circlePaint)
            canvas.drawCircle(x, y, circleRadius, borderPaint)
        }

        // Green base circles
        basePositions[TokenColor.GREEN]?.forEach { (x, y) ->
            canvas.drawCircle(x, y, circleRadius, circlePaint)
            canvas.drawCircle(x, y, circleRadius, borderPaint)
        }

        // Yellow base circles
        basePositions[TokenColor.YELLOW]?.forEach { (x, y) ->
            canvas.drawCircle(x, y, circleRadius, circlePaint)
            canvas.drawCircle(x, y, circleRadius, borderPaint)
        }

        // Blue base circles
        basePositions[TokenColor.BLUE]?.forEach { (x, y) ->
            canvas.drawCircle(x, y, circleRadius, circlePaint)
            canvas.drawCircle(x, y, circleRadius, borderPaint)
        }
    }

    private fun drawPaths(canvas: Canvas) {
        // Draw the main path cells
        // Top horizontal paths
        for (i in 0..5) {
            drawCell(canvas, i * cellSize, 6 * cellSize, pathPaint)
        }
        for (i in 9..14) {
            drawCell(canvas, i * cellSize, 6 * cellSize, pathPaint)
        }
        for (i in 0..5) {
            drawCell(canvas, i * cellSize, 8 * cellSize, pathPaint)
        }
        for (i in 9..14) {
            drawCell(canvas, i * cellSize, 8 * cellSize, pathPaint)
        }

        // Left vertical paths
        for (i in 0..5) {
            drawCell(canvas, 6 * cellSize, i * cellSize, pathPaint)
        }
        for (i in 9..14) {
            drawCell(canvas, 6 * cellSize, i * cellSize, pathPaint)
        }
        for (i in 0..5) {
            drawCell(canvas, 8 * cellSize, i * cellSize, pathPaint)
        }
        for (i in 9..14) {
            drawCell(canvas, 8 * cellSize, i * cellSize, pathPaint)
        }

        // Center cells
        for (row in 6..8) {
            for (col in 6..8) {
                if (row == 7 || col == 7) {
                    drawCell(canvas, col * cellSize, row * cellSize, pathPaint)
                }
            }
        }
    }

    private fun drawCell(canvas: Canvas, left: Float, top: Float, paint: Paint) {
        val rect = RectF(left, top, left + cellSize, top + cellSize)
        canvas.drawRect(rect, paint)
        canvas.drawRect(rect, borderPaint)
    }

    private fun drawHomeStretches(canvas: Canvas) {
        // Red home stretch (horizontal from left to center)
        for (i in 1..5) {
            drawCell(canvas, i * cellSize, 7 * cellSize, redPaint)
        }

        // Green home stretch (vertical from top to center)
        for (i in 1..5) {
            drawCell(canvas, 7 * cellSize, i * cellSize, greenPaint)
        }

        // Yellow home stretch (horizontal from right to center)
        for (i in 9..13) {
            drawCell(canvas, i * cellSize, 7 * cellSize, yellowPaint)
        }

        // Blue home stretch (vertical from bottom to center)
        for (i in 9..13) {
            drawCell(canvas, 7 * cellSize, i * cellSize, bluePaint)
        }
    }

    private fun drawCenterHome(canvas: Canvas) {
        val centerX = cellSize * 7.5f
        val centerY = cellSize * 7.5f
        val size = cellSize * 1.5f

        // Draw triangles for each color pointing to center
        val redPath = Path().apply {
            moveTo(centerX - size, centerY - size)
            lineTo(centerX, centerY)
            lineTo(centerX - size, centerY + size)
            close()
        }
        canvas.drawPath(redPath, redPaint)

        val greenPath = Path().apply {
            moveTo(centerX - size, centerY - size)
            lineTo(centerX, centerY)
            lineTo(centerX + size, centerY - size)
            close()
        }
        canvas.drawPath(greenPath, greenPaint)

        val yellowPath = Path().apply {
            moveTo(centerX + size, centerY - size)
            lineTo(centerX, centerY)
            lineTo(centerX + size, centerY + size)
            close()
        }
        canvas.drawPath(yellowPath, yellowPaint)

        val bluePath = Path().apply {
            moveTo(centerX - size, centerY + size)
            lineTo(centerX, centerY)
            lineTo(centerX + size, centerY + size)
            close()
        }
        canvas.drawPath(bluePath, bluePaint)

        // Draw center circle
        canvas.drawCircle(centerX, centerY, cellSize * 0.5f, homePaint)
        canvas.drawCircle(centerX, centerY, cellSize * 0.5f, borderPaint)
    }

    private fun drawSafeZones(canvas: Canvas) {
        // Draw stars at safe positions
        val safePositions = listOf(
            Pair(cellSize * 1.5f, cellSize * 7.5f),  // Red safe
            Pair(cellSize * 7.5f, cellSize * 1.5f),  // Green safe
            Pair(cellSize * 13.5f, cellSize * 7.5f), // Yellow safe
            Pair(cellSize * 7.5f, cellSize * 13.5f)  // Blue safe
        )

        safePositions.forEach { (x, y) ->
            drawStar(canvas, x, y)
        }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float) {
        val radius = cellSize * 0.3f
        val path = Path()
        val points = 5

        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) radius else radius * 0.5f
            val angle = Math.PI / 2 + i * Math.PI / points
            val x = cx + (r * Math.cos(angle)).toFloat()
            val y = cy - (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()

        canvas.drawPath(path, starPaint)
    }

    private fun drawBorders(canvas: Canvas) {
        val rect = RectF(0f, 0f, boardSize, boardSize)
        canvas.drawRoundRect(rect, 16f, 16f, borderPaint)
    }

    private fun drawTokens(canvas: Canvas) {
        players.forEach { player ->
            val color = when (player.color) {
                TokenColor.RED -> redColor
                TokenColor.GREEN -> greenColor
                TokenColor.YELLOW -> yellowColor
                TokenColor.BLUE -> blueColor
            }

            player.tokens.forEachIndexed { index, token ->
                val key = "${player.color}_$index"
                val position = tokenPositions[key] ?: return@forEachIndexed
                
                // Draw token shadow
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#40000000")
                }
                canvas.drawCircle(position.x + 2f, position.y + 2f, tokenRadius, shadowPaint)
                
                // Draw token
                tokenPaint.color = color
                canvas.drawCircle(position.x, position.y, tokenRadius, tokenPaint)
                
                // Draw token border
                canvas.drawCircle(position.x, position.y, tokenRadius, tokenBorderPaint)
                
                // Draw token number
                val tokenTextPaint = Paint(textPaint).apply {
                    textSize = tokenRadius * 0.8f
                }
                canvas.drawText("${index + 1}", position.x, position.y + tokenTextPaint.textSize / 3, tokenTextPaint)
            }
        }
    }

    /**
     * Get token at touch position
     */
    fun getTokenAtPosition(x: Float, y: Float, playerColor: TokenColor): Int? {
        players.find { it.color == playerColor }?.tokens?.forEachIndexed { index, token ->
            val key = "${playerColor}_$index"
            val tokenPos = tokenPositions[key] ?: return@forEachIndexed
            
            val distance = Math.sqrt(
                Math.pow((x - tokenPos.x).toDouble(), 2.0) +
                Math.pow((y - tokenPos.y).toDouble(), 2.0)
            )
            
            if (distance <= tokenRadius * 1.5) {
                return index
            }
        }
        return null
    }
}
