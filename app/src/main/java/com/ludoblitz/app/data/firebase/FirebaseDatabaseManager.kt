package com.ludoblitz.app.data.firebase

import com.google.firebase.database.*
import com.ludoblitz.app.data.model.*
import com.ludoblitz.app.utils.AvatarGenerator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Realtime Database Manager
 * Handles all database operations for online multiplayer
 * 
 * NOTE: No Firebase Storage used - we use AI-generated avatars instead!
 * This saves money (Firebase Storage requires Blaze plan)
 */
@Singleton
class FirebaseDatabaseManager @Inject constructor(
    private val authManager: FirebaseAuthManager
) {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    
    // References
    private val usersRef: DatabaseReference = database.getReference("users")
    private val gamesRef: DatabaseReference = database.getReference("games")
    private val roomsRef: DatabaseReference = database.getReference("rooms")
    private val leaderboardRef: DatabaseReference = database.getReference("leaderboard")
    private val friendRequestsRef: DatabaseReference = database.getReference("friend_requests")
    private val friendsRef: DatabaseReference = database.getReference("friends")
    
    // ==================== USER OPERATIONS ====================
    
    /**
     * Create or update user profile
     * Generates a unique random avatar instead of uploading to storage
     */
    suspend fun saveUserProfile(user: UserProfile): String {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        
        // Generate avatar seed for consistent avatar generation
        val avatarSeed = user.avatarSeed.ifEmpty {
            AvatarGenerator.generateSeedForUser(userId)
        }
        
        val profileWithAvatar = user.copy(
            id = userId,
            avatarSeed = avatarSeed,
            updatedAt = System.currentTimeMillis()
        )
        
        usersRef.child(userId).setValue(profileWithAvatar).await()
        return avatarSeed
    }
    
    /**
     * Create new user profile after registration
     */
    suspend fun createNewUserProfile(
        email: String,
        displayName: String
    ): UserProfile {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        val avatarSeed = AvatarGenerator.generateSeedForUser(userId)
        
        val profile = UserProfile(
            id = userId,
            displayName = displayName,
            email = email,
            avatarSeed = avatarSeed,
            avatarStyle = AvatarGenerator.getRandomStyle().name,
            coins = 500,  // Starting coins
            gems = 5,     // Starting gems
            xp = 0,
            level = 1,
            gamesPlayed = 0,
            gamesWon = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        usersRef.child(userId).setValue(profile).await()
        return profile
    }
    
    /**
     * Get user profile
     */
    suspend fun getUserProfile(userId: String): UserProfile? {
        val snapshot = usersRef.child(userId).get().await()
        return snapshot.getValue(UserProfile::class.java)
    }
    
    /**
     * Get current user profile
     */
    suspend fun getCurrentUserProfile(): UserProfile? {
        val userId = authManager.getCurrentUserId() ?: return null
        return getUserProfile(userId)
    }
    
    /**
     * Observe user profile changes
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(UserProfile::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        usersRef.child(userId).addValueEventListener(listener)
        awaitClose { usersRef.child(userId).removeEventListener(listener) }
    }
    
    /**
     * Update user stats
     */
    suspend fun updateUserStats(
        gamesPlayed: Int? = null,
        gamesWon: Int? = null,
        coins: Long? = null,
        gems: Int? = null,
        xp: Int? = null,
        level: Int? = null
    ) {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        val updates = mutableMapOf<String, Any>()
        
        gamesPlayed?.let { updates["gamesPlayed"] = it }
        gamesWon?.let { updates["gamesWon"] = it }
        coins?.let { updates["coins"] = it }
        gems?.let { updates["gems"] = it }
        xp?.let { updates["xp"] = it }
        level?.let { updates["level"] = it }
        updates["updatedAt"] = System.currentTimeMillis()
        
        usersRef.child(userId).updateChildren(updates).await()
    }
    
    /**
     * Update user avatar style
     */
    suspend fun updateAvatarStyle(style: String) {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        usersRef.child(userId).child("avatarStyle").setValue(style).await()
    }
    
    // ==================== ROOM OPERATIONS ====================
    
    /**
     * Create a new game room
     */
    suspend fun createRoom(
        roomName: String,
        maxPlayers: Int,
        isPrivate: Boolean,
        gameRules: GameRules
    ): GameRoom {
        val hostId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        val roomId = roomsRef.push().key ?: throw Exception("Failed to create room")
        
        // Generate 6-character room code for private rooms
        val roomCode = if (isPrivate) {
            (100000..999999).random().toString()
        } else ""
        
        val room = GameRoom(
            id = roomId,
            name = roomName,
            hostId = hostId,
            maxPlayers = maxPlayers,
            currentPlayers = listOf(hostId),
            isPrivate = isPrivate,
            roomCode = roomCode,
            gameRules = gameRules,
            status = RoomStatus.WAITING,
            createdAt = System.currentTimeMillis()
        )
        
        roomsRef.child(roomId).setValue(room).await()
        return room
    }
    
    /**
     * Join a game room by ID
     */
    suspend fun joinRoom(roomId: String): GameRoom {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        
        val snapshot = roomsRef.child(roomId).get().await()
        val room = snapshot.getValue(GameRoom::class.java) ?: throw Exception("Room not found")
        
        if (room.currentPlayers.size >= room.maxPlayers) {
            throw Exception("Room is full")
        }
        
        if (room.status != RoomStatus.WAITING) {
            throw Exception("Game already in progress")
        }
        
        if (userId in room.currentPlayers) {
            throw Exception("Already in this room")
        }
        
        val updatedPlayers = room.currentPlayers + userId
        roomsRef.child(roomId).child("currentPlayers").setValue(updatedPlayers).await()
        
        return room.copy(currentPlayers = updatedPlayers)
    }
    
    /**
     * Join room by code (for private rooms)
     */
    suspend fun joinRoomByCode(roomCode: String): GameRoom {
        val snapshot = roomsRef.orderByChild("roomCode").equalTo(roomCode).get().await()
        
        if (!snapshot.exists()) {
            throw Exception("Invalid room code")
        }
        
        val roomSnapshot = snapshot.children.first()
        val room = roomSnapshot.getValue(GameRoom::class.java) ?: throw Exception("Room not found")
        
        return joinRoom(room.id)
    }
    
    /**
     * Leave a game room
     */
    suspend fun leaveRoom(roomId: String) {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        
        val snapshot = roomsRef.child(roomId).get().await()
        val room = snapshot.getValue(GameRoom::class.java) ?: return
        
        val updatedPlayers = room.currentPlayers - userId
        
        if (updatedPlayers.isEmpty()) {
            // Delete room if empty
            roomsRef.child(roomId).removeValue().await()
        } else {
            roomsRef.child(roomId).child("currentPlayers").setValue(updatedPlayers).await()
            
            // If host left, assign new host
            if (room.hostId == userId && updatedPlayers.isNotEmpty()) {
                roomsRef.child(roomId).child("hostId").setValue(updatedPlayers.first()).await()
            }
        }
    }
    
    /**
     * Get room by ID
     */
    suspend fun getRoom(roomId: String): GameRoom? {
        val snapshot = roomsRef.child(roomId).get().await()
        return snapshot.getValue(GameRoom::class.java)
    }
    
    /**
     * Observe room changes
     */
    fun observeRoom(roomId: String): Flow<GameRoom?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(GameRoom::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        roomsRef.child(roomId).addValueEventListener(listener)
        awaitClose { roomsRef.child(roomId).removeEventListener(listener) }
    }
    
    /**
     * Get available public rooms
     */
    fun observeAvailableRooms(): Flow<List<GameRoom>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = snapshot.children.mapNotNull { it.getValue(GameRoom::class.java) }
                    .filter { it.status == RoomStatus.WAITING && !it.isPrivate }
                trySend(rooms)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        roomsRef.orderByChild("status").equalTo(RoomStatus.WAITING.name)
            .addValueEventListener(listener)
        awaitClose { roomsRef.removeEventListener(listener) }
    }
    
    // ==================== GAME OPERATIONS ====================
    
    /**
     * Create a new game from room
     */
    suspend fun createGame(room: GameRoom): OnlineGame {
        val gameId = gamesRef.push().key ?: throw Exception("Failed to create game")
        
        // Create player states with colors
        val playerColors = listOf("RED", "GREEN", "YELLOW", "BLUE")
        val playerStates = room.currentPlayers.mapIndexed { index, playerId ->
            playerId to PlayerGameState(
                userId = playerId,
                color = playerColors.getOrElse(index) { "RED" },
                tokenPositions = listOf(-1, -1, -1, -1), // All tokens in base
                hasFinished = false
            )
        }.toMap()
        
        val game = OnlineGame(
            id = gameId,
            roomId = room.id,
            players = room.currentPlayers,
            playerStates = playerStates,
            currentPlayerIndex = 0,
            status = GameStatus.IN_PROGRESS,
            createdAt = System.currentTimeMillis()
        )
        
        gamesRef.child(gameId).setValue(game).await()
        
        // Update room status
        roomsRef.child(room.id).child("status").setValue(RoomStatus.IN_PROGRESS).await()
        
        return game
    }
    
    /**
     * Update game state
     */
    suspend fun updateGameState(gameId: String, gameState: Map<String, Any>) {
        gamesRef.child(gameId).updateChildren(gameState).await()
    }
    
    /**
     * Update player's turn
     */
    suspend fun updateCurrentPlayer(gameId: String, playerIndex: Int) {
        gamesRef.child(gameId).child("currentPlayerIndex").setValue(playerIndex).await()
    }
    
    /**
     * Update dice roll
     */
    suspend fun updateDiceRoll(gameId: String, value: Int) {
        gamesRef.child(gameId).child("lastDiceValue").setValue(value).await()
    }
    
    /**
     * Update token position
     */
    suspend fun updateTokenPosition(
        gameId: String,
        playerId: String,
        tokenIndex: Int,
        newPosition: Int
    ) {
        val updates = mapOf(
            "playerStates/$playerId/tokenPositions/$tokenIndex" to newPosition
        )
        gamesRef.child(gameId).updateChildren(updates).await()
    }
    
    /**
     * Observe game state
     */
    fun observeGame(gameId: String): Flow<OnlineGame?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(OnlineGame::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        gamesRef.child(gameId).addValueEventListener(listener)
        awaitClose { gamesRef.child(gameId).removeEventListener(listener) }
    }
    
    /**
     * End game and update stats
     */
    suspend fun endGame(gameId: String, winnerId: String, rankings: List<String>) {
        // Update game status
        gamesRef.child(gameId).child("status").setValue(GameStatus.COMPLETED).await()
        gamesRef.child(gameId).child("winnerId").setValue(winnerId).await()
        gamesRef.child(gameId).child("rankings").setValue(rankings).await()
        gamesRef.child(gameId).child("endedAt").setValue(System.currentTimeMillis()).await()
        
        // Update winner stats
        val winnerRef = usersRef.child(winnerId)
        winnerRef.child("gamesWon").get().addOnSuccessListener {
            val currentWins = it.getValue(Int::class.java) ?: 0
            winnerRef.child("gamesWon").setValue(currentWins + 1)
        }
        winnerRef.child("gamesPlayed").get().addOnSuccessListener {
            val current = it.getValue(Int::class.java) ?: 0
            winnerRef.child("gamesPlayed").setValue(current + 1)
        }
        
        // Update all players' games played
        rankings.forEach { playerId ->
            if (playerId != winnerId) {
                usersRef.child(playerId).child("gamesPlayed").get().addOnSuccessListener {
                    val current = it.getValue(Int::class.java) ?: 0
                    usersRef.child(playerId).child("gamesPlayed").setValue(current + 1)
                }
            }
        }
    }
    
    // ==================== LEADERBOARD ====================
    
    /**
     * Get top players
     */
    suspend fun getLeaderboard(limit: Int = 100): List<UserProfile> {
        val snapshot = usersRef.orderByChild("xp").limitToLast(limit).get().await()
        return snapshot.children.mapNotNull { it.getValue(UserProfile::class.java) }.reversed()
    }
    
    /**
     * Observe leaderboard changes
     */
    fun observeLeaderboard(limit: Int = 100): Flow<List<UserProfile>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(UserProfile::class.java) }.reversed()
                trySend(users)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        usersRef.orderByChild("xp").limitToLast(limit).addValueEventListener(listener)
        awaitClose { usersRef.orderByChild("xp").limitToLast(limit).removeEventListener(listener) }
    }
    
    // ==================== FRIENDS ====================
    
    /**
     * Send friend request
     */
    suspend fun sendFriendRequest(toUserId: String): String {
        val fromUserId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        
        // Check if already friends
        val friendsSnapshot = friendsRef.child(fromUserId).child(toUserId).get().await()
        if (friendsSnapshot.exists()) {
            throw Exception("Already friends with this user")
        }
        
        // Check for existing request
        val existingRequest = friendRequestsRef
            .orderByChild("fromUserId")
            .equalTo(fromUserId)
            .get().await()
        
        existingRequest.children.forEach { snapshot ->
            val request = snapshot.getValue(FriendRequest::class.java)
            if (request?.toUserId == toUserId && request.status == FriendRequestStatus.PENDING) {
                throw Exception("Friend request already sent")
            }
        }
        
        val requestId = friendRequestsRef.push().key ?: throw Exception("Failed to create request")
        
        val request = FriendRequest(
            id = requestId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            status = FriendRequestStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        
        friendRequestsRef.child(requestId).setValue(request).await()
        return requestId
    }
    
    /**
     * Accept friend request
     */
    suspend fun acceptFriendRequest(requestId: String) {
        val snapshot = friendRequestsRef.child(requestId).get().await()
        val request = snapshot.getValue(FriendRequest::class.java) ?: throw Exception("Request not found")
        
        // Add to both users' friend lists
        friendsRef.child(request.fromUserId).child(request.toUserId).setValue(true)
        friendsRef.child(request.toUserId).child(request.fromUserId).setValue(true)
        
        // Delete request
        friendRequestsRef.child(requestId).removeValue().await()
    }
    
    /**
     * Decline friend request
     */
    suspend fun declineFriendRequest(requestId: String) {
        friendRequestsRef.child(requestId).removeValue().await()
    }
    
    /**
     * Get friend requests
     */
    suspend fun getFriendRequests(): List<FriendRequest> {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        
        val snapshot = friendRequestsRef.orderByChild("toUserId").equalTo(userId).get().await()
        return snapshot.children.mapNotNull { it.getValue(FriendRequest::class.java) }
    }
    
    /**
     * Observe friend requests
     */
    fun observeFriendRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requests = snapshot.children.mapNotNull { it.getValue(FriendRequest::class.java) }
                    .filter { it.toUserId == userId && it.status == FriendRequestStatus.PENDING }
                trySend(requests)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        friendRequestsRef.orderByChild("toUserId").equalTo(userId).addValueEventListener(listener)
        awaitClose { friendRequestsRef.removeEventListener(listener) }
    }
    
    /**
     * Get friends list
     */
    suspend fun getFriends(): List<String> {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        
        val snapshot = friendsRef.child(userId).get().await()
        return snapshot.children.mapNotNull { it.key }
    }
    
    /**
     * Get friends profiles
     */
    suspend fun getFriendsProfiles(): List<UserProfile> {
        val friendIds = getFriends()
        return friendIds.mapNotNull { getUserProfile(it) }
    }
    
    /**
     * Remove friend
     */
    suspend fun removeFriend(friendId: String) {
        val userId = authManager.getCurrentUserId() ?: throw Exception("Not logged in")
        
        friendsRef.child(userId).child(friendId).removeValue()
        friendsRef.child(friendId).child(userId).removeValue().await()
    }
    
    /**
     * Search users by display name
     */
    suspend fun searchUsers(query: String, limit: Int = 10): List<UserProfile> {
        val snapshot = usersRef.get().await()
        return snapshot.children
            .mapNotNull { it.getValue(UserProfile::class.java) }
            .filter { it.displayName.contains(query, ignoreCase = true) }
            .take(limit)
    }
}

// Data classes for Firebase
data class UserProfile(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarSeed: String = "",      // Seed for avatar generation
    val avatarStyle: String = "",      // AvatarGenerator.AvatarStyle name
    val coins: Long = 500,
    val gems: Int = 5,
    val xp: Int = 0,
    val level: Int = 1,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val currentWinStreak: Int = 0,
    val bestWinStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class GameRoom(
    val id: String = "",
    val name: String = "",
    val hostId: String = "",
    val maxPlayers: Int = 4,
    val currentPlayers: List<String> = emptyList(),
    val isPrivate: Boolean = false,
    val roomCode: String = "",
    val gameRules: GameRules = GameRules(),
    val status: RoomStatus = RoomStatus.WAITING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RoomStatus {
    WAITING, IN_PROGRESS, COMPLETED
}

data class OnlineGame(
    val id: String = "",
    val roomId: String = "",
    val players: List<String> = emptyList(),
    val playerStates: Map<String, PlayerGameState> = emptyMap(),
    val currentPlayerIndex: Int = 0,
    val lastDiceValue: Int = 0,
    val status: GameStatus = GameStatus.WAITING,
    val winnerId: String = "",
    val rankings: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long = 0
)

data class PlayerGameState(
    val userId: String = "",
    val color: String = "RED",
    val tokenPositions: List<Int> = listOf(-1, -1, -1, -1),
    val hasFinished: Boolean = false
)

data class FriendRequest(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class FriendRequestStatus {
    PENDING, ACCEPTED, DECLINED
}
