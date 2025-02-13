package com.kophe.le_sklad_zradn.screens.scanner.view

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.BarcodeFormat.*
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.kophe.le_sklad_zradn.databinding.FragmentScannerBinding
import com.kophe.le_sklad_zradn.screens.common.BaseFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemMode
import com.kophe.le_sklad_zradn.screens.scanner.view.ScannerFragmentDirections.actionScannerFragmentToCreateItemFragment
import com.kophe.le_sklad_zradn.screens.scanner.view.ScannerMode.CREATE_ITEM
import com.kophe.le_sklad_zradn.screens.scanner.view.ScannerMode.SCAN_ONLY
import java.lang.ref.WeakReference

const val SCANNED_CODE_EVENT = "scanned_code_event"

class ScannerFragment : BaseFragment<FragmentScannerBinding>() {

    private var barcodeView: DecoratedBarcodeView? = null
    private var beepManager: BeepManager? = null
    private var lastText: String? = null
    private lateinit var scannerMode: ScannerMode

    private val callback = BarcodeCallback { result ->
        lastText = result.text
        barcodeView?.setStatusText(result.text)
        beepManager?.playBeepSoundAndVibrate()
        pause()
        when (scannerMode) {
            CREATE_ITEM -> navigate(
                actionScannerFragmentToCreateItemFragment(result.text, CreateItemMode.CREATE_ITEM)
            )
            SCAN_ONLY -> {
                findNavController(this@ScannerFragment).previousBackStackEntry?.savedStateHandle?.set(
                    SCANNED_CODE_EVENT, result.text
                )
                navigateUp()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentScannerBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            scannerMode = ScannerFragmentArgs.fromBundle(bundle).scannerMode
            when (scannerMode) {
                CREATE_ITEM -> {
                    val manualButton = binding?.buttonCreateManual
                    manualButton?.visibility = VISIBLE
                    manualButton?.setOnClickListener {
                        actionScannerFragmentToCreateItemFragment(null, CreateItemMode.CREATE_ITEM)
                    }
                }
                SCAN_ONLY -> binding?.buttonCreateManual?.visibility = GONE
            }
        }
        checkCameraPermission()
        barcodeView = binding?.barcodeScanner
        val formats = listOf(CODE_39, CODABAR, QR_CODE)
        barcodeView?.barcodeView?.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView?.initializeFromIntent(Intent())
        barcodeView?.decodeContinuous(callback)
        beepManager = BeepManager(activity)
    }

    override fun onResume() {
        super.onResume()
        resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView?.pause()
    }

    private fun pause() {
        barcodeView?.pause()
    }

    private fun resume() {
        barcodeView?.resume()
    }

    private fun checkCameraPermission() = try {
        val requiredPermissions = arrayOf(CAMERA)
        requestPermissions(requiredPermissions, 0)
    } catch (e: IllegalArgumentException) {
        checkIfCameraPermissionIsGranted()
    }

    private fun checkIfCameraPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(
                context ?: return, CAMERA
            ) == PERMISSION_GRANTED
        ) return
        MaterialAlertDialogBuilder(context ?: return).setTitle("Permission required")
            .setMessage("This application needs to access the camera to process barcodes")
            .setPositiveButton("Ok") { _, _ ->
                checkCameraPermission()
            }.setCancelable(false).create().apply {
                setCanceledOnTouchOutside(false)
                show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkIfCameraPermissionIsGranted()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.postInvalidate()
    }

}
