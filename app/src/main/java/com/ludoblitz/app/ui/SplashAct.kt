package com.ludoblitz.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ludoblitz.app.databinding.SplashActBinding
import com.ludoblitz.app.ui.viewmodel.MainVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashAct : AppCompatActivity() {
    private val vm: MainVM by viewModels()
    override fun onCreate(s: Bundle?) {
        installSplashScreen()
        super.onCreate(s)
        SplashActBinding.inflate(layoutInflater)
        vm.user.observe(this) { startActivity(Intent(this, MainAct::class.java)); finish() }
    }
}
