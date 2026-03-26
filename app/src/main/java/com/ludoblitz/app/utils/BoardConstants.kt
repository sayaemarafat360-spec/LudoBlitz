package com.ludoblitz.app.utils

import com.ludoblitz.app.data.models.PlayerColor

/**
 * Board constants for Ludo game
 * The board has a main track of 52 positions plus home columns for each player
 */
object BoardConstants {
    // Total positions on the main track
    const val MAIN_TRACK_SIZE = 52

    // Home column size (path to center for each player)
    const val HOME_COLUMN_SIZE = 5

    // Starting positions for each player on the main track
    // These are the positions where tokens enter the board
    val START_POSITIONS = mapOf(
        PlayerColor.RED to 0,
        PlayerColor.GREEN to 13,
        PlayerColor.YELLOW to 26,
        PlayerColor.BLUE to 39
    )

    // Safe zone positions on the main track
    // These are positions where tokens cannot be captured
    val SAFE_POSITIONS = setOf(
        0, 8, 13, 21, 26, 34, 39, 47 // Starting positions and middle of each arm
    )

    // Home entry positions (where tokens enter their home column)
    val HOME_ENTRY_POSITIONS = mapOf(
        PlayerColor.RED to 50,    // Just before reaching start
        PlayerColor.GREEN to 11,  // Just before reaching start
        PlayerColor.YELLOW to 24, // Just before reaching start
        PlayerColor.BLUE to 37    // Just before reaching start
    )

    // Number of tokens per player
    const val TOKENS_PER_PLAYER = 4

    // Dice max value
    const val DICE_MAX = 6
    const val DICE_MIN = 1

    // Consecutive sixes limit
    const val MAX_CONSECUTIVE_SIXES = 3

    // Capture bonus moves
    const val CAPTURE_BONUS_MOVES = 20

    // Token steps to complete (main track + home column)
    const val STEPS_TO_COMPLETE = MAIN_TRACK_SIZE + HOME_COLUMN_SIZE

    // Main track size constant
    const val MAIN_TRACK_SIZE_INT = MAIN_TRACK_SIZE
}

/**
 * Calculate the actual board position for a token
 * Each player has their own perspective of the board
 */
fun calculateBoardPosition(
    playerColor: PlayerColor,
    stepsFromStart: Int
): Int {
    if (stepsFromStart < 0) return -1 // In home base
    if (stepsFromStart >= BoardConstants.STEPS_TO_COMPLETE) return -2 // Finished

    val startPos = BoardConstants.START_POSITIONS[playerColor] ?: 0

    // If in home column
    if (stepsFromStart >= BoardConstants.MAIN_TRACK_SIZE) {
        return -3 // In home column (special handling needed)
    }

    // Calculate position on main track
    return (startPos + stepsFromStart) % BoardConstants.MAIN_TRACK_SIZE
}

/**
 * Check if a position is a safe zone
 */
fun isSafePosition(position: Int): Boolean {
    return BoardConstants.SAFE_POSITIONS.contains(position)
}

/**
 * Get the home column position for a player
 */
fun getHomeColumnPosition(
    playerColor: PlayerColor,
    stepsFromStart: Int
): Int? {
    if (stepsFromStart < BoardConstants.MAIN_TRACK_SIZE) return null
    return stepsFromStart - BoardConstants.MAIN_TRACK_SIZE
}

/**
 * Calculate distance between two positions on the track
 */
fun calculateDistance(from: Int, to: Int): Int {
    if (from <= to) return to - from
    return BoardConstants.MAIN_TRACK_SIZE - from + to
}

/**
 * Get the starting position for a player's tokens
 */
fun getStartPosition(playerColor: PlayerColor): Int {
    return BoardConstants.START_POSITIONS[playerColor] ?: 0
}

/**
 * Check if a token can enter home column
 */
fun canEnterHomeColumn(
    playerColor: PlayerColor,
    currentPosition: Int,
    diceValue: Int
): Boolean {
    val homeEntry = BoardConstants.HOME_ENTRY_POSITIONS[playerColor] ?: return false
    val startPos = BoardConstants.START_POSITIONS[playerColor] ?: return false

    // Calculate how many steps from start
    val stepsFromStart = if (currentPosition >= startPos) {
        currentPosition - startPos
    } else {
        BoardConstants.MAIN_TRACK_SIZE - startPos + currentPosition
    }

    // Check if moving by dice value would enter home column
    return stepsFromStart + diceValue >= BoardConstants.MAIN_TRACK_SIZE
}

/**
 * Path definitions for drawing the board
 */
object BoardPath {
    // Main track path coordinates (simplified for 15x15 grid)
    // Each position maps to (row, col) on the board grid
    val MAIN_TRACK_COORDINATES = listOf(
        // Top row (Red side going down)
        Pair(6, 1), Pair(6, 2), Pair(6, 3), Pair(6, 4), Pair(6, 5),
        // Right turn going down
        Pair(5, 6), Pair(4, 6), Pair(3, 6), Pair(2, 6), Pair(1, 6), Pair(0, 6),
        // Top-center going right
        Pair(0, 7), Pair(0, 8),
        // Right side going down
        Pair(1, 8), Pair(2, 8), Pair(3, 8), Pair(4, 8), Pair(5, 8),
        // Right turn going right (Green side)
        Pair(6, 9), Pair(6, 10), Pair(6, 11), Pair(6, 12), Pair(6, 13), Pair(6, 14),
        // Right-center going down
        Pair(7, 14), Pair(8, 14),
        // Bottom going right (Yellow side)
        Pair(8, 13), Pair(8, 12), Pair(8, 11), Pair(8, 10), Pair(8, 9),
        // Right turn going down
        Pair(9, 8), Pair(10, 8), Pair(11, 8), Pair(12, 8), Pair(13, 8), Pair(14, 8),
        // Bottom-center going left
        Pair(14, 7), Pair(14, 6),
        // Left side going up (Blue side)
        Pair(13, 6), Pair(12, 6), Pair(11, 6), Pair(10, 6), Pair(9, 6),
        // Left turn going left
        Pair(8, 5), Pair(8, 4), Pair(8, 3), Pair(8, 2), Pair(8, 1), Pair(8, 0),
        // Left-center going up
        Pair(7, 0), Pair(6, 0)
    )

    // Home column paths for each player
    val HOME_COLUMNS = mapOf(
        PlayerColor.RED to listOf(
            Pair(7, 1), Pair(7, 2), Pair(7, 3), Pair(7, 4), Pair(7, 5)
        ),
        PlayerColor.GREEN to listOf(
            Pair(1, 7), Pair(2, 7), Pair(3, 7), Pair(4, 7), Pair(5, 7)
        ),
        PlayerColor.YELLOW to listOf(
            Pair(7, 13), Pair(7, 12), Pair(7, 11), Pair(7, 10), Pair(7, 9)
        ),
        PlayerColor.BLUE to listOf(
            Pair(13, 7), Pair(12, 7), Pair(11, 7), Pair(10, 7), Pair(9, 7)
        )
    )

    // Home base positions (where tokens start) for each player
    val HOME_BASES = mapOf(
        PlayerColor.RED to listOf(
            Pair(2, 2), Pair(2, 4), Pair(4, 2), Pair(4, 4)
        ),
        PlayerColor.GREEN to listOf(
            Pair(2, 10), Pair(2, 12), Pair(4, 10), Pair(4, 12)
        ),
        PlayerColor.YELLOW to listOf(
            Pair(10, 10), Pair(10, 12), Pair(12, 10), Pair(12, 12)
        ),
        PlayerColor.BLUE to listOf(
            Pair(10, 2), Pair(10, 4), Pair(12, 2), Pair(12, 4)
        )
    )
}
