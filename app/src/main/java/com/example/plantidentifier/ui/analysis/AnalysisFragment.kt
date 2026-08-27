package com.example.plantidentifier.ui.analysis

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.plantidentifier.R
import com.example.plantidentifier.databinding.FragmentAnalysisBinding
import com.example.plantidentifier.viewmodel.PlantViewModel
import java.io.File

class AnalysisFragment : Fragment() {

    private var _binding: FragmentAnalysisBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlantViewModel by activityViewModels()

    private var imagePath: String? = null
    private var organ: String = "leaf"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalysisBinding.inflate(
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

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.setPadding(root.paddingLeft, systemBars.top + 24, root.paddingRight, systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

        imagePath = arguments?.getString("imagePath")
        organ = arguments?.getString("organ") ?: "leaf"

        if (imagePath == null) {
            findNavController().popBackStack()
            return
        }

        val imageFile = File(imagePath!!)

        if (!imageFile.exists()) {
            findNavController().popBackStack()
            return
        }

        binding.imgPlant.setImageURI(
            Uri.fromFile(imageFile)
        )

        startImageAnimation()
        observeViewModel()

        viewModel.identifyPlant(imageFile, organ)

        binding.btnRetry.setOnClickListener {
            viewModel.identifyPlant(imageFile, organ)
        }
    }

    private fun startImageAnimation() {
        binding.imageCard.alpha = 0f
        binding.imageCard.scaleX = 0.96f
        binding.imageCard.scaleY = 0.96f

        binding.imageCard.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(450)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun observeViewModel() {

        viewModel.uiState.observe(viewLifecycleOwner) { state ->

            when {
                state.isLoading -> {
                    binding.progressAnalysis.visibility = View.VISIBLE
                    binding.btnRetry.visibility = View.GONE

                    binding.tvAnalysisTitle.text = getString(com.example.plantidentifier.R.string.analysis_title)

                    binding.tvAnalysisSubtitle.text =
                        getString(com.example.plantidentifier.R.string.analysis_subtitle)

                    binding.tvAnalysisStatus.text =
                        getString(com.example.plantidentifier.R.string.analysis_status)

                    binding.tvAnalysisHint.text =
                        getString(com.example.plantidentifier.R.string.analysis_hint)
                }

                state.error != null -> {
                    binding.progressAnalysis.visibility = View.GONE
                    binding.btnRetry.visibility = View.VISIBLE

                    binding.tvAnalysisTitle.text =
                        getString(R.string.analysis_error_title)

                    binding.tvAnalysisSubtitle.text =
                        getString(state.errorMessageRes ?: R.string.plant_generic_error)

                    binding.tvAnalysisStatus.text =
                        getString(R.string.identification_failed)

                    binding.tvAnalysisHint.text =
                        getString(R.string.try_another_photo)
                }

                state.scientificName.isNotEmpty() -> {

                    binding.progressAnalysis.visibility = View.GONE
                    binding.btnRetry.visibility = View.GONE

                    val path = imagePath

                    if (path != null) {
                        findNavController().navigate(
                            R.id.action_analysisFragment_to_resultFragment,
                            bundleOf("imagePath" to path)
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
