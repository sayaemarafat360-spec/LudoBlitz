package com.ludoblitz.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ludoblitz.app.data.local.PreferenceManager
import com.ludoblitz.app.databinding.SettingsFragBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFrag : Fragment() {
    private var _b: SettingsFragBinding? = null
    private val b get() = _b!!
    @Inject lateinit var prefs: PreferenceManager

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = SettingsFragBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        lifecycleScope.launch {
            b.swSound.isChecked = prefs.soundEnabled.first()
            b.swVibro.isChecked = prefs.vibrationEnabled.first()
            b.swDark.isChecked = prefs.darkMode.first()
        }
        b.swSound.setOnCheckedChangeListener { _, c -> lifecycleScope.launch { prefs.setSound(c) } }
        b.swVibro.setOnCheckedChangeListener { _, c -> lifecycleScope.launch { prefs.setVibration(c) } }
        b.swDark.setOnCheckedChangeListener { _, c ->
            lifecycleScope.launch { prefs.setDarkMode(c) }
            AppCompatDelegate.setDefaultNightMode(if (c) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
