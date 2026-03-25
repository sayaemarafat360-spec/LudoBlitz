package com.ludoblitz.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.ludoblitz.app.R
import com.ludoblitz.app.databinding.MainActBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainAct : AppCompatActivity() {
    private lateinit var b: MainActBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = MainActBinding.inflate(layoutInflater)
        setContentView(b.root)
        val nav = findNavController(R.id.navHost)
        b.bottomNav.setupWithNavController(nav)
    }
}
