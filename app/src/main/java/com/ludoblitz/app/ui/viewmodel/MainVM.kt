package com.ludoblitz.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludoblitz.app.data.firebase.AuthManager
import com.ludoblitz.app.data.firebase.DatabaseManager
import com.ludoblitz.app.data.local.PreferenceManager
import com.ludoblitz.app.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
    private val auth: AuthManager,
    private val db: DatabaseManager,
    private val prefs: PreferenceManager
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init { checkAuth() }

    private fun checkAuth() {
        viewModelScope.launch {
            _loading.value = true
            auth.userId?.let { uid ->
                _user.value = db.getUser(uid)
            }
            _loading.value = false
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loading.value = true
            auth.signInEmail(email, pass).fold(
                onSuccess = { uid -> _user.value = db.getUser(uid) },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun register(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _loading.value = true
            auth.signUpEmail(email, pass, name).fold(
                onSuccess = { uid ->
                    val u = User(id = uid, email = email, displayName = name)
                    db.saveUser(u)
                    _user.value = u
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun guestLogin() {
        viewModelScope.launch {
            _loading.value = true
            auth.signInGuest().fold(
                onSuccess = { uid ->
                    val u = User(id = uid, displayName = "Guest_${uid.take(6)}", coins = 500)
                    db.saveUser(u)
                    _user.value = u
                },
                onFailure = { _error.value = it.message }
            )
            _loading.value = false
        }
    }

    fun logout() { auth.signOut(); _user.value = null }
    fun clearError() { _error.value = null }
}
