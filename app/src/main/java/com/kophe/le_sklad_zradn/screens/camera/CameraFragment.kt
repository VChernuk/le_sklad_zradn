package com.kophe.le_sklad_zradn.screens.camera

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.Images.Media.RELATIVE_PATH
import android.provider.MediaStore.MediaColumns.DISPLAY_NAME
import android.provider.MediaStore.MediaColumns.MIME_TYPE
import android.util.Size
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.*
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.kophe.le_sklad_zradn.databinding.FragmentCameraBinding
import com.kophe.le_sklad_zradn.screens.common.BaseFragment
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

const val IMAGE_URI = "image_uri"

class CameraFragment : BaseFragment<FragmentCameraBinding>() {

    @Inject
    lateinit var loggingUtil: LoggingUtil
    private var imageCapture: ImageCapture? = null

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCameraBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        binding?.captureButton?.setOnClickListener { takePhoto() }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().setTargetResolution(Size(960, 1280)).build().also {
                it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
            }
            imageCapture = Builder().setTargetResolution(Size(960, 1280)).build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                showDefaultErrorMessage()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) //TODO: sync with uploaded file name
        val contentValues = ContentValues().apply {
            put(DISPLAY_NAME, name)
            put(MIME_TYPE, "image/jpeg")
            if (VERSION.SDK_INT > VERSION_CODES.P) {
                put(RELATIVE_PATH, "Pictures/le_sklad")
            }
        }
        val outputOptions = OutputFileOptions.Builder(
            requireActivity().contentResolver, EXTERNAL_CONTENT_URI, contentValues
        ).build()
        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    showDefaultErrorMessage()
                }

                override fun onImageSaved(output: OutputFileResults) {
                    NavHostFragment.findNavController(this@CameraFragment).previousBackStackEntry?.savedStateHandle?.set(
                        IMAGE_URI, output.savedUri
                    )
                    navigateUp()
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                showToastMessage("Permissions not granted by the user.")
                navigateUp()
            }
        }
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"//TODO: rename
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(CAMERA).apply {
            if (VERSION.SDK_INT <= VERSION_CODES.P) {
                add(WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}
