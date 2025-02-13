package com.kophe.le_sklad_zradn.screens.edititem.view

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.Activity.RESULT_OK
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE
import androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.bumptech.glide.Glide
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentEditItemBinding
import com.kophe.le_sklad_zradn.screens.camera.IMAGE_URI
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.edititem.viewmodel.EditItemViewModel
import com.kophe.le_sklad_zradn.screens.scanner.view.SCANNED_CODE_EVENT
import com.kophe.le_sklad_zradn.screens.selectitems.view.SELECTED_ITEMS_KEY
import com.kophe.le_sklad_zradn.util.export.ExportUtil
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.ItemImage
import com.kophe.leskladlib.repository.common.ItemQuantity
import com.kophe.leskladuilib.OnItemSelectedListener
import com.kophe.leskladuilib.SpinnerExtensions.setSpinnerEntries
import com.kophe.leskladuilib.createissuance.adapter.SelectedItemsAdapter
import com.kophe.leskladuilib.edititem.adapter.CommonItemAdapter
import com.kophe.leskladuilib.edititem.adapter.ItemImagesAdapter
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

private const val PERMISSION_CODE = 1001
private const val IMAGE_CHOOSE = 1000

//TODO: setup title according to item or mode
//TODO: Logging
//TODO: screen titles
//TODO: id allowed symbols

abstract class BaseItemFragment :
    BaseViewModelFragment<FragmentEditItemBinding, EditItemViewModel>(),
    OnItemSelectedListener<CommonItem> {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    @Inject
    lateinit var exportUtil: ExportUtil

    protected val historyAdapter by lazy { CommonItemAdapter(this) }
    protected val imagesAdapter by lazy {
        ItemImagesAdapter(object : OnItemSelectedListener<ItemImage> {
            override fun selectItem(item: ItemImage) {
                viewModel.currentItem.images?.toTypedArray()?.let { images ->
                    openImageViewer(images, images.indexOf(item))
                }
            }
        })
    }
    protected val selectedItemsAdapter by lazy {
        SelectedItemsAdapter(object : OnItemSelectedListener<Item> {
            override fun selectItem(item: Item) {
                viewModel.removeSubItem(item)
                updateAdapter()
            }
        }, object : OnItemSelectedListener<Item> {
            override fun selectItem(item: Item) {
                item.quantity?.let {
                    if (it.quantity > 1) item.quantity =
                        ItemQuantity(it.parentId, it.quantity - 1, it.measurement)
                }
                updateAdapter()
            }
        }, object : OnItemSelectedListener<Item> {
            override fun selectItem(item: Item) {
                //TODO: define max
                item.quantity?.let {
                    item.quantity = ItemQuantity(it.parentId, it.quantity + 1, it.measurement)
                }
                updateAdapter()
            }
        })
    }

    abstract fun openImageViewer(images: Array<ItemImage>, index: Int)

    override fun createViewModel() = ViewModelProvider(this, factory)[EditItemViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
        viewModel.start()
        val savedStateHandle =
            findNavController(this).currentBackStackEntry?.savedStateHandle ?: return
        savedStateHandle.getLiveData<Uri?>(IMAGE_URI)
            .observe(this, this::showImageConfirmationDialog)
    }

    private fun showImageConfirmationDialog(uri: Uri?) {
        //          TODO: log
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_photo_confirmation, null)
        val dialog = AlertDialog.Builder(requireContext()).setTitle("Норм?").setCancelable(true)
            .setView(dialogView).setPositiveButton("Норм", DialogInterface.OnClickListener { _, _ ->
                viewModel.uploadImage(uri ?: return@OnClickListener)
            }).setNegativeButton("Ніт") { dialog, _ -> dialog?.dismiss() }.create()
        dialog.show()
        Glide.with(requireActivity()).load(uri)
            .into(dialogView.findViewById(R.id.confirmationImage))
        val color = resources.getColor(com.kophe.leskladuilib.R.color.colorAccent)
        dialog.getButton(BUTTON_NEGATIVE).setTextColor(color)
        dialog.getButton(BUTTON_POSITIVE).setTextColor(color)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentEditItemBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    abstract fun viewModelSubscriptions()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelSubscriptions()
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.locationEntries.observe(viewLifecycleOwner) {
            binding?.itemLocationView?.spinnerLocation?.setSpinnerEntries(it)
        }
        viewModel.categoryEntries.observe(viewLifecycleOwner) {
            binding?.itemCategoryView?.spinnerCategory?.setSpinnerEntries(it)
        }
        viewModel.subcategoryEntries.observe(viewLifecycleOwner) {
            binding?.itemCategoryView?.viewSubcategory?.visibility =
                if (it.isNullOrEmpty()) GONE else VISIBLE
            binding?.itemCategoryView?.spinnerSubCategory?.setSpinnerEntries(it)
        }
        viewModel.sublocationEntries.observe(viewLifecycleOwner) {
            binding?.itemLocationView?.viewSublocation?.visibility =
                if (it.isNullOrEmpty()) GONE else VISIBLE
            binding?.itemLocationView?.spinnerSublocation?.setSpinnerEntries(it)
        }
        viewModel.ownershipTypeEntries.observe(viewLifecycleOwner) {
            binding?.spinnerOwnershipType?.setSpinnerEntries(it)
        }
        binding?.photosView?.imagesRecycler?.adapter = imagesAdapter
        binding?.itemButtons?.buttonClear?.setOnClickListener {
            preClearAction()
            navigateUp()
        }
        binding?.itemIdEditText?.filters = arrayOf(object : InputFilter {
            override fun filter(
                source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int
            ) = source?.toString()
                ?.let { return@let if (it.matches(Regex("[a-zA-Z\\d /-]*"))) it else null } ?: ""
        })
    }

    protected open fun preClearAction() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode != PERMISSION_CODE) return
        if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) chooseImageGallery()
        else showToastMessage("Permission denied")
    }

    private fun chooseImageGallery() {
        val intent = Intent(ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != IMAGE_CHOOSE || resultCode != RESULT_OK) return
        showImageConfirmationDialog(data?.data)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val savedStateHandle =
            findNavController(this).currentBackStackEntry?.savedStateHandle ?: return
        savedStateHandle.getLiveData<String>(SCANNED_CODE_EVENT).observe(viewLifecycleOwner) {
            viewModel.onBarcodeTextChange(it)
            binding?.barcodeEditText?.setText(it)
        }
        findNavController(this).currentBackStackEntry?.savedStateHandle?.getLiveData<List<Item>>(
            SELECTED_ITEMS_KEY
        )?.observe(viewLifecycleOwner) {
            viewModel.addSubItems(it)
            updateAdapter()
        }
    }

    protected fun checkPermissionAndChooseImageGallery() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), READ_MEDIA_IMAGES
            ) == PERMISSION_DENIED
        ) {
            val permissions = arrayOf(READ_MEDIA_IMAGES)
            requestPermissions(permissions, PERMISSION_CODE)
            return
        }
        chooseImageGallery()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

    override fun selectItem(item: CommonItem) {
        viewModel.getIssuanceInfo(item)
    }

    protected fun updateAdapter() {
        viewModel.currentItem.setOptions?.subItems?.let {
            selectedItemsAdapter.items = it.toList()
        }
    }

}
