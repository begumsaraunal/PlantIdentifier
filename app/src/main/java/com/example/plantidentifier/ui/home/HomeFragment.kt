package com.example.plantidentifier.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.plantidentifier.R
import com.example.plantidentifier.databinding.FragmentHomeBinding
import com.example.plantidentifier.utils.toFile
import com.example.plantidentifier.utils.normalizeJpeg
import java.io.File
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val galleryLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {
                try {
                    val file = uri.toFile(requireContext())
                    openAnalysis(file)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.image_process_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private var cameraImageFile: File? = null
    private var selectedOrgan = "unknown"

    private val cameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->

            val file = cameraImageFile

            if (success && file != null && file.exists()) {
                try {
                    openAnalysis(file.normalizeJpeg(requireContext()))
                } catch (e: Exception) {
                    file.delete()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.image_process_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                file?.delete()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.photo_cancelled),
                    Toast.LENGTH_SHORT
                ).show()
            }

            cameraImageFile = null
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCameraInternal()
            else Toast.makeText(requireContext(), R.string.camera_permission_required, Toast.LENGTH_LONG).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(
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

        binding.btnTakePhoto.setOnClickListener {
            chooseOrgan { openCamera() }
        }

        binding.btnChoosePhoto.setOnClickListener {
            chooseOrgan { galleryLauncher.launch("image/*") }
        }

        binding.homeToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_language) {
                chooseLanguage()
                true
            } else {
                false
            }
        }
    }

    private fun chooseLanguage() {
        val options = arrayOf(
            getString(R.string.language_system),
            getString(R.string.language_english),
            getString(R.string.language_turkish)
        )
        val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val checked = when {
            current.startsWith("en") -> 1
            current.startsWith("tr") -> 2
            else -> 0
        }

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_PlantIdentifier_Dialog
        )
            .setTitle(R.string.language_title)
            .setSingleChoiceItems(options, checked) { dialog, which ->
                val locales = when (which) {
                    1 -> LocaleListCompat.forLanguageTags("en")
                    2 -> LocaleListCompat.forLanguageTags("tr")
                    else -> LocaleListCompat.getEmptyLocaleList()
                }
                AppCompatDelegate.setApplicationLocales(locales)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun chooseOrgan(onSelected: () -> Unit) {
        val values = arrayOf("leaf", "flower", "fruit", "bark", "habit", "unknown")
        val dialog = BottomSheetDialog(requireContext())
        val content = layoutInflater.inflate(R.layout.bottom_sheet_organ, null)
        dialog.setContentView(content)

        val buttonIds = intArrayOf(
            R.id.btnOrganLeaf,
            R.id.btnOrganFlower,
            R.id.btnOrganFruit,
            R.id.btnOrganBark,
            R.id.btnOrganHabit,
            R.id.btnOrganUnknown
        )

        buttonIds.forEachIndexed { index, buttonId ->
            content.findViewById<View>(buttonId).setOnClickListener {
                selectedOrgan = values[index]
                dialog.dismiss()
                onSelected()
            }
        }

        dialog.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            openCameraInternal()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCameraInternal() {

        val file = File(
            requireContext().cacheDir,
            "camera_capture_${System.currentTimeMillis()}.jpg"
        )

        cameraImageFile = file

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )

        cameraLauncher.launch(uri)
    }

    private fun openAnalysis(file: File) {

        findNavController().navigate(
            R.id.action_homeFragment_to_analysisFragment,
            bundleOf(
                "imagePath" to file.absolutePath,
                "organ" to selectedOrgan
            )
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
