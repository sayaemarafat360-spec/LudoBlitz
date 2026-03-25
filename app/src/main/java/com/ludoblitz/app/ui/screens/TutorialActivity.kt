package com.ludoblitz.app.ui.screens

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.ludoblitz.app.R
import com.ludoblitz.app.databinding.ActivityTutorialBinding
import com.ludoblitz.app.data.local.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tutorial Activity - Interactive animated onboarding
 */
@AndroidEntryPoint
class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding
    
    @Inject
    lateinit var preferenceManager: PreferenceManager
    
    private var currentPage = 0
    
    private val tutorialSteps = listOf(
        TutorialStep(
            title = "Welcome to Ludo Blitz!",
            description = "The most exciting Ludo game with beautiful graphics and amazing features. Let's learn how to play!",
            animationRes = "welcome.json",
            iconRes = R.drawable.ic_logo
        ),
        TutorialStep(
            title = "Roll the Dice",
            description = "Tap the dice to roll. Each player takes turns rolling the dice to move their tokens.",
            animationRes = "dice_roll.json",
            iconRes = R.drawable.ic_dice
        ),
        TutorialStep(
            title = "Roll a 6 to Start",
            description = "In classic mode, you need to roll a 6 to bring your token out of the base. Rolling a 6 also gives you an extra turn!",
            animationRes = "six.json",
            iconRes = R.drawable.ic_dice_6
        ),
        TutorialStep(
            title = "Move Your Tokens",
            description = "Move your tokens around the board based on your dice roll. Get all 4 tokens home to win!",
            animationRes = "move.json",
            iconRes = R.drawable.ic_play
        ),
        TutorialStep(
            title = "Capture Opponents",
            description = "Land on an opponent's token to capture it and send it back to their base. You get bonus points and an extra turn!",
            animationRes = "capture.json",
            iconRes = R.drawable.ic_bot
        ),
        TutorialStep(
            title = "Safe Zones",
            description = "Tokens on safe zones (marked with stars) cannot be captured. Use them strategically!",
            animationRes = "safe.json",
            iconRes = R.drawable.ic_stats
        ),
        TutorialStep(
            title = "Daily Rewards",
            description = "Come back every day for daily rewards and spin the wheel for bonus prizes!",
            animationRes = "reward.json",
            iconRes = R.drawable.ic_gift
        ),
        TutorialStep(
            title = "Ready to Play!",
            description = "You're all set! Choose your game mode and start playing. Good luck!",
            animationRes = "ready.json",
            iconRes = R.drawable.ic_logo
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
        setupClickListeners()
        setupAnimations()
    }

    private fun setupViewPager() {
        val adapter = TutorialAdapter(tutorialSteps)
        binding.viewPager.adapter = adapter
        
        // Connect tab indicator
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                updateButtons()
                playPageAnimation(position)
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnSkip.setOnClickListener {
            finishTutorial()
        }
        
        binding.btnNext.setOnClickListener {
            if (currentPage < tutorialSteps.size - 1) {
                binding.viewPager.currentItem = currentPage + 1
            } else {
                finishTutorial()
            }
        }
        
        binding.btnBack.setOnClickListener {
            if (currentPage > 0) {
                binding.viewPager.currentItem = currentPage - 1
            }
        }
        
        binding.btnGetStarted.setOnClickListener {
            finishTutorial()
        }
    }

    private fun setupAnimations() {
        // Initial animation
        binding.root.post {
            playEntranceAnimation()
        }
    }

    private fun updateButtons() {
        when (currentPage) {
            0 -> {
                binding.btnBack.visibility = View.GONE
                binding.btnNext.visibility = View.VISIBLE
                binding.btnGetStarted.visibility = View.GONE
            }
            tutorialSteps.size - 1 -> {
                binding.btnBack.visibility = View.VISIBLE
                binding.btnNext.visibility = View.GONE
                binding.btnGetStarted.visibility = View.VISIBLE
            }
            else -> {
                binding.btnBack.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
                binding.btnGetStarted.visibility = View.GONE
            }
        }
    }

    private fun playEntranceAnimation() {
        val slideUp = ObjectAnimator.ofFloat(binding.contentContainer, "translationY", 100f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(binding.contentContainer, "alpha", 0f, 1f)
        
        AnimatorSet().apply {
            playTogether(slideUp, fadeIn)
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun playPageAnimation(position: Int) {
        // Animate content on page change
        val step = tutorialSteps[position]
        
        // Animate icon
        binding.ivIcon.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                binding.ivIcon.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start()
            }
            .start()
    }

    private fun finishTutorial() {
        lifecycleScope.launch {
            preferenceManager.setTutorialCompleted(true)
        }
        
        // Play exit animation
        binding.contentContainer.animate()
            .translationY(-100f)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
            .start()
    }

    override fun onBackPressed() {
        // Don't allow back during tutorial
        // Or allow to skip
        finishTutorial()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, TutorialActivity::class.java)
    }
}

data class TutorialStep(
    val title: String,
    val description: String,
    val animationRes: String,
    val iconRes: Int
)

class TutorialAdapter(
    private val steps: List<TutorialStep>
) : androidx.recyclerview.widget.RecyclerView.Adapter<TutorialAdapter.TutorialViewHolder>() {

    class TutorialViewHolder(
        private val binding: com.ludoblitz.app.databinding.ItemTutorialStepBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(step: TutorialStep) {
            binding.tvTitle.text = step.title
            binding.tvDescription.text = step.description
            binding.ivIcon.setImageResource(step.iconRes)
            
            // Load Lottie animation if available
            // binding.lottieAnimation.setAnimation(step.animationRes)
            // binding.lottieAnimation.playAnimation()
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TutorialViewHolder {
        val binding = com.ludoblitz.app.databinding.ItemTutorialStepBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TutorialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        holder.bind(steps[position])
    }

    override fun getItemCount() = steps.size
}
