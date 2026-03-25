package com.ludoblitz.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ludoblitz.app.data.model.BotDifficulty
import com.ludoblitz.app.databinding.HomeFragBinding
import com.ludoblitz.app.ui.viewmodel.MainVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFrag : Fragment() {
    private var _b: HomeFragBinding? = null
    private val b get() = _b!!
    private val vm: MainVM by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = HomeFragBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        vm.user.observe(viewLifecycleOwner) { u ->
            b.tvName.text = u?.displayName ?: "Guest"
            b.tvCoins.text = (u?.coins ?: 0).toString()
            b.tvLevel.text = "Lv ${u?.level ?: 1}"
        }
        b.btnPlay.setOnClickListener { showDiff() }
        b.btnSettings.setOnClickListener { (activity as MainAct).findNavController(R.id.navHost).navigate(R.id.settingsFrag) }
    }

    private fun showDiff() {
        val opts = arrayOf("Easy", "Medium", "Hard", "Expert")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Difficulty")
            .setItems(opts) { _, i ->
                val d = BotDifficulty.values()[i]
                startActivity(Intent(requireContext(), GameAct::class.java).apply {
                    putExtra("players", 2)
                    putExtra("diff", d.name)
                    putExtra("uid", vm.user.value?.id)
                })
            }
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
