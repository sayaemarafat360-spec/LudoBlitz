package com.ludoblitz.app.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.ludoblitz.app.R
import com.ludoblitz.app.databinding.ActivitySettingsBinding
import com.ludoblitz.app.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Settings Activity - App configuration and preferences
 */
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            // Sound setting
            viewModel.isSoundEnabled.collect { enabled ->
                binding.switchSound.isChecked = enabled
            }
        }

        lifecycleScope.launch {
            // Music setting
            viewModel.isMusicEnabled.collect { enabled ->
                binding.switchMusic.isChecked = enabled
            }
        }

        lifecycleScope.launch {
            // Vibration setting
            viewModel.isVibrationEnabled.collect { enabled ->
                binding.switchVibration.isChecked = enabled
            }
        }

        lifecycleScope.launch {
            // Dark mode setting
            viewModel.isDarkMode.collect { enabled ->
                binding.switchDarkMode.isChecked = enabled
            }
        }

        lifecycleScope.launch {
            // Notifications setting
            viewModel.isNotificationsEnabled.collect { enabled ->
                binding.switchNotifications.isChecked = enabled
            }
        }

        lifecycleScope.launch {
            // Premium status
            viewModel.isPremium.collect { isPremium ->
                if (isPremium) {
                    binding.cardRemoveAds.visibility = View.GONE
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Sound toggle
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setSoundEnabled(isChecked)
        }

        // Music toggle
        binding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMusicEnabled(isChecked)
        }

        // Vibration toggle
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setVibrationEnabled(isChecked)
        }

        // Dark mode toggle
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDarkMode(isChecked)
            // Recreate activity to apply theme
            recreate()
        }

        // Notifications toggle
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotificationsEnabled(isChecked)
        }

        // Sound volume slider
        binding.sliderSoundVolume.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setSoundVolume(value / 100f)
            }
        }

        // Music volume slider
        binding.sliderMusicVolume.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setMusicVolume(value / 100f)
            }
        }

        // Remove ads
        binding.cardRemoveAds.setOnClickListener {
            showRemoveAdsDialog()
        }

        // Restore purchases
        binding.cardRestorePurchases.setOnClickListener {
            viewModel.restorePurchases()
        }

        // Rate app
        binding.cardRateApp.setOnClickListener {
            openPlayStore()
        }

        // Share app
        binding.cardShareApp.setOnClickListener {
            shareApp()
        }

        // Privacy policy
        binding.cardPrivacyPolicy.setOnClickListener {
            openUrl("https://your-privacy-policy-url.com")
        }

        // Terms of service
        binding.cardTermsOfService.setOnClickListener {
            openUrl("https://your-terms-url.com")
        }

        // About
        binding.cardAbout.setOnClickListener {
            showAboutDialog()
        }

        // Reset progress
        binding.cardResetProgress.setOnClickListener {
            showResetConfirmDialog()
        }

        // Language
        binding.cardLanguage.setOnClickListener {
            showLanguageSelector()
        }
    }

    private fun showRemoveAdsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.remove_ads_title)
            .setMessage(R.string.remove_ads_desc)
            .setPositiveButton("$2.99 - Buy") { _, _ ->
                // Initiate purchase
                viewModel.purchaseRemoveAds()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("market://details?id=$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            }
            startActivity(intent)
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out Ludo Blitz! The best Ludo game ever! 🎲\n\nhttps://play.google.com/store/apps/details?id=$packageName")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Ludo Blitz"))
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
        startActivity(intent)
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About Ludo Blitz")
            .setMessage("""
                Ludo Blitz v1.0.0
                
                Roll. Race. Reign Supreme!
                
                The most beautiful Ludo game with amazing features:
                • Local & Online Multiplayer
                • Smart AI Opponents
                • Daily Rewards & Spin Wheel
                • Beautiful Themes
                
                Made with ❤️
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showResetConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Progress?")
            .setMessage("This will reset all your progress, coins, gems, and achievements. This action cannot be undone!")
            .setPositiveButton("Reset") { _, _ ->
                viewModel.resetProgress()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLanguageSelector() {
        val languages = arrayOf("English", "Spanish", "French", "German", "Hindi", "Portuguese")
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                viewModel.setLanguage(languages[which])
            }
            .show()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}
