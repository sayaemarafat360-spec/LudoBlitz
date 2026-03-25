package com.ludoblitz.app.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ludoblitz.app.R
import com.ludoblitz.app.data.model.ShopItem
import com.ludoblitz.app.data.model.ItemType
import com.ludoblitz.app.data.model.Currency
import com.ludoblitz.app.data.local.LocalUserRepository
import com.ludoblitz.app.databinding.ActivityShopBinding
import com.ludoblitz.app.databinding.ItemShopItemBinding
import com.ludoblitz.app.utils.SoundManager
import com.ludoblitz.app.utils.VibrationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shop Activity - Purchase board themes, token styles, dice styles
 */
@AndroidEntryPoint
class ShopActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShopBinding
    
    @Inject
    lateinit var userRepository: LocalUserRepository
    
    @Inject
    lateinit var soundManager: SoundManager
    
    @Inject
    lateinit var vibrationManager: VibrationManager

    private val boardThemes = listOf(
        ShopItem("classic", "Classic", "Traditional wooden board", ItemType.BOARD_THEME, 0, Currency.COINS, "classic"),
        ShopItem("neon", "Neon Glow", "Vibrant neon colors", ItemType.BOARD_THEME, 500, Currency.COINS, "neon"),
        ShopItem("nature", "Nature", "Forest green theme", ItemType.BOARD_THEME, 750, Currency.COINS, "nature"),
        ShopItem("royal", "Royal", "Elegant purple theme", ItemType.BOARD_THEME, 1000, Currency.COINS, "royal"),
        ShopItem("galaxy", "Galaxy", "Space-themed board", ItemType.BOARD_THEME, 1500, Currency.GEMS, "galaxy"),
        ShopItem("fire", "Fire", "Blazing hot theme", ItemType.BOARD_THEME, 2000, Currency.GEMS, "fire")
    )

    private val tokenStyles = listOf(
        ShopItem("default", "Classic", "Standard token design", ItemType.TOKEN_STYLE, 0, Currency.COINS, "default"),
        ShopItem("diamond", "Diamond", "Sparkling gems", ItemType.TOKEN_STYLE, 300, Currency.COINS, "diamond"),
        ShopItem("emoji", "Emoji", "Fun emoji faces", ItemType.TOKEN_STYLE, 500, Currency.COINS, "emoji"),
        ShopItem("neon", "Neon", "Glowing tokens", ItemType.TOKEN_STYLE, 750, Currency.COINS, "neon"),
        ShopItem("crystal", "Crystal", "Crystal clear", ItemType.TOKEN_STYLE, 5, Currency.GEMS, "crystal"),
        ShopItem("golden", "Golden", "Premium gold tokens", ItemType.TOKEN_STYLE, 10, Currency.GEMS, "golden")
    )

    private val diceStyles = listOf(
        ShopItem("default", "Classic", "Standard dice", ItemType.DICE_STYLE, 0, Currency.COINS, "default"),
        ShopItem("golden", "Golden", "Shiny gold dice", ItemType.DICE_STYLE, 400, Currency.COINS, "golden"),
        ShopItem("neon", "Neon", "Glowing dice", ItemType.DICE_STYLE, 600, Currency.COINS, "neon"),
        ShopItem("wooden", "Wooden", "Classic wooden", ItemType.DICE_STYLE, 800, Currency.COINS, "wooden"),
        ShopItem("crystal", "Crystal", "Crystal dice", ItemType.DICE_STYLE, 5, Currency.GEMS, "crystal"),
        ShopItem("animated", "Animated", "Animated dice", ItemType.DICE_STYLE, 15, Currency.GEMS, "animated")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTabs()
        observeUser()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        val adapter = ShopPagerAdapter(this, listOf(boardThemes, tokenStyles, diceStyles))
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Boards"
                1 -> "Tokens"
                2 -> "Dice"
                else -> ""
            }
        }.attach()
    }

    private fun observeUser() {
        lifecycleScope.launch {
            userRepository.getUser().collect { user ->
                binding.tvCoins.text = formatNumber(user.coins)
                binding.tvGems.text = formatNumber(user.gems)
            }
        }
    }

    private fun formatNumber(number: Long): String {
        return when {
            number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
            number >= 1_000 -> String.format("%.1fK", number / 1_000.0)
            else -> number.toString()
        }
    }

    inner class ShopPagerAdapter(
        activity: FragmentActivity,
        private val items: List<List<ShopItem>>
    ) : androidx.viewpager2.adapter.FragmentStateAdapter(activity) {

        override fun getItemCount() = items.size

        override fun createFragment(position: Int): Fragment {
            return ShopFragment.newInstance(items[position])
        }
    }

    class ShopFragment : Fragment() {

        private lateinit var items: List<ShopItem>

        companion object {
            private const val ARG_ITEMS = "items"

            fun newInstance(items: List<ShopItem>): ShopFragment {
                val fragment = ShopFragment()
                val args = Bundle().apply {
                    putParcelableArrayList(ARG_ITEMS, ArrayList(items))
                }
                fragment.arguments = args
                return fragment
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            items = arguments?.getParcelableArrayList(ARG_ITEMS) ?: emptyList()
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return RecyclerView(requireContext()).apply {
                layoutManager = GridLayoutManager(context, 2)
                adapter = ShopItemAdapter(items)
                setPadding(16, 16, 16, 16)
                clipToPadding = false
            }
        }
    }

    inner class ShopItemAdapter(
        private val items: List<ShopItem>
    ) : RecyclerView.Adapter<ShopItemAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemShopItemBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemShopItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            
            holder.binding.apply {
                tvName.text = item.name
                tvDescription.text = item.description
                
                // Set price
                val priceText = if (item.price == 0L) {
                    "FREE"
                } else {
                    "${item.price} ${if (item.currency == Currency.COINS) "coins" else "gems"}"
                }
                tvPrice.text = priceText
                
                // Set price color
                val priceColor = when (item.currency) {
                    Currency.COINS -> getColor(R.color.coin_gold)
                    Currency.GEMS -> getColor(R.color.gem_purple)
                    else -> getColor(R.color.text_primary)
                }
                tvPrice.setTextColor(priceColor)
                
                // Check if owned
                lifecycleScope.launch {
                    val user = userRepository.getUser().first()
                    val isOwned = when (item.type) {
                        ItemType.BOARD_THEME -> user.unlockedBoards.contains(item.id)
                        ItemType.TOKEN_STYLE -> user.unlockedTokens.contains(item.id)
                        ItemType.DICE_STYLE -> user.unlockedDice.contains(item.id)
                        else -> false
                    }
                    
                    val isSelected = when (item.type) {
                        ItemType.BOARD_THEME -> user.selectedBoardTheme == item.id
                        ItemType.TOKEN_STYLE -> user.selectedTokenStyle == item.id
                        ItemType.DICE_STYLE -> user.selectedDiceStyle == item.id
                        else -> false
                    }
                    
                    when {
                        isSelected -> {
                            btnAction.text = "Equipped"
                            btnAction.isEnabled = false
                        }
                        isOwned -> {
                            btnAction.text = "Equip"
                            btnAction.isEnabled = true
                        }
                        else -> {
                            btnAction.text = "Buy"
                            btnAction.isEnabled = true
                        }
                    }
                }
                
                // Preview image placeholder
                ivPreview.setImageResource(R.drawable.ic_logo)
                
                // Click listener
                root.setOnClickListener {
                    showItemPreview(item)
                }
                
                btnAction.setOnClickListener {
                    handleItemClick(item)
                }
            }
        }

        override fun getItemCount() = items.size

        private fun showItemPreview(item: ShopItem) {
            // Show full preview in dialog
        }

        private fun handleItemClick(item: ShopItem) {
            lifecycleScope.launch {
                val user = userRepository.getUser().first()
                
                val isOwned = when (item.type) {
                    ItemType.BOARD_THEME -> user.unlockedBoards.contains(item.id)
                    ItemType.TOKEN_STYLE -> user.unlockedTokens.contains(item.id)
                    ItemType.DICE_STYLE -> user.unlockedDice.contains(item.id)
                    else -> false
                }
                
                if (isOwned) {
                    // Equip
                    equipItem(item)
                } else {
                    // Buy
                    purchaseItem(item)
                }
            }
        }

        private suspend fun equipItem(item: ShopItem) {
            when (item.type) {
                ItemType.BOARD_THEME -> userRepository.selectBoard(item.id)
                ItemType.TOKEN_STYLE -> userRepository.selectToken(item.id)
                ItemType.DICE_STYLE -> userRepository.selectDice(item.id)
                else -> {}
            }
            soundManager.playButtonClick()
            vibrationManager.shortClick()
            notifyDataSetChanged()
        }

        private suspend fun purchaseItem(item: ShopItem) {
            val user = userRepository.getUser().first()
            
            val canAfford = when (item.currency) {
                Currency.COINS -> user.coins >= item.price
                Currency.GEMS -> user.gems >= item.price
                else -> false
            }
            
            if (!canAfford) {
                MaterialAlertDialogBuilder(this@ShopActivity)
                    .setTitle("Insufficient Funds")
                    .setMessage("You don't have enough ${item.currency.name.lowercase()} to purchase this item.")
                    .setPositiveButton("OK", null)
                    .show()
                return
            }
            
            // Confirm purchase
            MaterialAlertDialogBuilder(this@ShopActivity)
                .setTitle("Purchase ${item.name}?")
                .setMessage("This will cost ${item.price} ${item.currency.name.lowercase()}")
                .setPositiveButton("Buy") { _, _ ->
                    lifecycleScope.launch {
                        when (item.currency) {
                            Currency.COINS -> userRepository.removeCoins(item.price)
                            Currency.GEMS -> {
                                val currentGems = userRepository.getUser().first().gems
                                if (currentGems >= item.price) {
                                    userRepository.addGems(-item.price)
                                }
                            }
                            else -> {}
                        }
                        
                        when (item.type) {
                            ItemType.BOARD_THEME -> {
                                userRepository.unlockBoard(item.id)
                                userRepository.selectBoard(item.id)
                            }
                            ItemType.TOKEN_STYLE -> {
                                userRepository.unlockToken(item.id)
                                userRepository.selectToken(item.id)
                            }
                            ItemType.DICE_STYLE -> {
                                userRepository.unlockDice(item.id)
                                userRepository.selectDice(item.id)
                            }
                            else -> {}
                        }
                        
                        soundManager.playCoinCollect(item.price)
                        vibrationManager.mediumClick()
                        notifyDataSetChanged()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, ShopActivity::class.java)
    }
}

// Extension to make Parcelize work
fun Bundle.putParcelableArrayList(key: String, list: ArrayList<out android.os.Parcelable>) {
    putParcelableArrayList(key, list)
}
