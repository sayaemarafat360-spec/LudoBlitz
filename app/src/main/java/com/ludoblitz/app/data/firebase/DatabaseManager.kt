package com.ludoblitz.app.data.firebase

import com.google.firebase.database.FirebaseDatabase
import com.ludoblitz.app.data.model.User
import kotlinx.coroutines.tasks.await

class DatabaseManager {
    private val db = FirebaseDatabase.getInstance()
    private val users = db.getReference("users")

    suspend fun saveUser(user: User) { users.child(user.id).setValue(user).await() }
    suspend fun getUser(id: String): User? = users.child(id).get().await().getValue(User::class.java)
    suspend fun updateCoins(id: String, coins: Long) { users.child(id).child("coins").setValue(coins).await() }
}
