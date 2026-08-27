package com.example.plantidentifier.ui.result

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.plantidentifier.R
import com.example.plantidentifier.databinding.FragmentResultBinding
import com.example.plantidentifier.viewmodel.PlantViewModel
import java.io.File
import com.example.plantidentifier.utils.formatConfidence

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlantViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(
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
            root.setPadding(root.paddingLeft, systemBars.top + 20, root.paddingRight, systemBars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)

        showPlantImage()
        observeResult()

        binding.btnScanAgain.setOnClickListener {
            arguments?.getString("imagePath")?.let { File(it).delete() }
            findNavController().popBackStack(
                R.id.homeFragment,
                false
            )
        }
    }

    private fun showPlantImage() {

        val imagePath = arguments?.getString("imagePath")
            ?: return

        val imageFile = File(imagePath)

        if (imageFile.exists()) {
            binding.imgPlantResult.setImageURI(
                Uri.fromFile(imageFile)
            )
        }
    }

    private fun observeResult() {

        viewModel.uiState.observe(viewLifecycleOwner) { state ->

            when {
                state.error != null -> {

                    binding.tvCommonName.text =
                        getString(R.string.identification_failed)

                    binding.tvScientificName.text =
                        getString(state.errorMessageRes ?: R.string.try_another_photo)

                    binding.tvConfidence.text = "--"

                    binding.tvFamily.text = "--"
                }

                state.scientificName.isNotEmpty() -> {

                    binding.tvCommonName.text =
                        state.commonName

                    binding.tvScientificName.text =
                        state.scientificName

                    binding.tvConfidence.text = formatConfidence(state.score)

                    binding.tvFamily.text =
                        state.family
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
