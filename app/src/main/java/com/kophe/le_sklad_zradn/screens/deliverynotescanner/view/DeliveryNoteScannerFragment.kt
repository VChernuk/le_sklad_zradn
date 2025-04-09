package com.kophe.le_sklad_zradn.screens.deliverynotescanner.view

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_BACK
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.google.zxing.BarcodeFormat.CODABAR
import com.google.zxing.BarcodeFormat.CODE_39
import com.google.zxing.BarcodeFormat.QR_CODE
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentDeliveryNoteScannerBinding
import com.kophe.le_sklad_zradn.screens.common.BaseFragment
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.Item
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject


const val DELIVERYNOTE_ITEMS_KEY = "deliverynote_items_key"

class DeliveryNoteScannerFragment : BaseFragment<FragmentDeliveryNoteScannerBinding>() {

    @Inject
    lateinit var loggingUtil: LoggingUtil

    private val items = mutableListOf<Item>()
    private var beepManager: BeepManager? = null
    private var lastText: String? = null
    private val callback = BarcodeCallback { result ->
        lastText = result.text
        binding?.barcodeView?.setStatusText(result.text)
        beepManager?.playBeepSoundAndVibrate()
        pause()
        findNavController(this@DeliveryNoteScannerFragment).previousBackStackEntry?.savedStateHandle?.set(
            DELIVERYNOTE_ITEMS_KEY, result.text
        )
        navigateUp()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentDeliveryNoteScannerBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermission()
        val formats = listOf(CODE_39, CODABAR, QR_CODE)
        binding?.barcodeView?.decoderFactory = DefaultDecoderFactory(formats)
        binding?.barcodeView?.initializeFromIntent(Intent())
        binding?.barcodeView?.decodeContinuous(callback)
        beepManager = BeepManager(activity)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
//        val savedStateHandle = findNavController(this).currentBackStackEntry?.savedStateHandle
//        savedStateHandle?.getLiveData<List<Item>>(SELECTED_ITEMS_KEY)?.observe(viewLifecycleOwner) {
//            items.addAll(it)
//            setupResult()
//            navigateUp()
//        }
//        savedStateHandle?.getLiveData<Item>(CREATED_ITEM_KEY)?.observe(viewLifecycleOwner) {
//            showToastMessage("Додано: ${it.title}")
//            items.add(it)
//        }
    }

    override fun onResume() {
        super.onResume()
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == ACTION_UP && keyCode == KEYCODE_BACK) {
                setupResult()
                navigateUp()
                return@OnKeyListener true
            }
            false
        })
        resume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.empty_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home, R.id.deliverynoteScannerDone -> {
            setupResult()
            navigateUp()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun setupResult() {
        findNavController(this@DeliveryNoteScannerFragment).previousBackStackEntry?.savedStateHandle?.set(
            DELIVERYNOTE_ITEMS_KEY, items.firstOrNull()
        )
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    private fun pause() {
        binding?.barcodeView?.pause()
    }

    private fun resume() {
        binding?.barcodeView?.resume()
    }

    private fun checkIfCameraPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission(
                context ?: return, CAMERA
            ) == PERMISSION_GRANTED
        ) return
        showAlertDialogMessage(
            "This application needs to access the camera to process barcodes", "Grant permission"
        ) { checkCameraPermission() }
    }

    private fun checkCameraPermission() = try {
        val requiredPermissions = arrayOf(CAMERA)
        requestPermissions(requiredPermissions, 0)
    } catch (e: IllegalArgumentException) {
        checkIfCameraPermissionIsGranted()
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
