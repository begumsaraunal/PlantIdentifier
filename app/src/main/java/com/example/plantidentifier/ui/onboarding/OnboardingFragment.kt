package com.example.plantidentifier.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.plantidentifier.R
import com.example.plantidentifier.databinding.FragmentOnboardingBinding
import kotlin.math.abs

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OnboardingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOnboardingBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OnboardingAdapter()
        binding.viewPager.adapter = adapter
        binding.viewPager.setPageTransformer { page, position ->
            val distance = abs(position).coerceIn(0f, 1f)
            page.alpha = 1f - (distance * 0.25f)
            page.scaleX = 1f - (distance * 0.04f)
            page.scaleY = 1f - (distance * 0.04f)
        }

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    updateDots(position)

                    binding.btnGetStarted.visibility =
                        if (position == 2) View.VISIBLE
                        else View.GONE
                }
            }
        )

        binding.tvSkip.setOnClickListener {
            finishOnboarding()
        }

        binding.btnGetStarted.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun updateDots(position: Int) {

        val active = R.drawable.onboarding_dot_active
        val inactive = R.drawable.onboarding_dot_inactive

        binding.dot1.setBackgroundResource(
            if (position == 0) active else inactive
        )

        binding.dot2.setBackgroundResource(
            if (position == 1) active else inactive
        )

        binding.dot3.setBackgroundResource(
            if (position == 2) active else inactive
        )
    }

    private fun finishOnboarding() {

        requireContext()
            .getSharedPreferences(
                "plant_ai_preferences",
                Context.MODE_PRIVATE
            )
            .edit()
            .putBoolean("hasSeenOnboarding", true)
            .apply()

        // Home is already the graph start destination underneath onboarding
        // on first launch. Pop onboarding so Back cannot reopen it or create
        // duplicate Home destinations.
        if (!findNavController().popBackStack(R.id.homeFragment, false)) {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
