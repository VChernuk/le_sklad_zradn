package com.kophe.le_sklad_zradn.screens.edititem.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.InfoAvailable
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemFragmentDirections.actionCreateItemFragmentToCameraFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemFragmentDirections.actionCreateItemFragmentToImageViewerFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemFragmentDirections.actionCreateItemFragmentToScannerFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemFragmentDirections.actionCreateItemFragmentToSelectItemsFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemMode.CREATE_ITEM
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemMode.CREATE_ITEM_FOR_ISSUANCE
import com.kophe.le_sklad_zradn.screens.edititem.viewmodel.EditItemViewModel
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.ItemImage
import com.kophe.leskladuilib.OnItemSelectedListener
import com.kophe.leskladuilib.SpinnerExtensions.setSpinnerValue

enum class CreateItemMode {
    CREATE_ITEM, CREATE_ITEM_FOR_ISSUANCE
}

const val CREATED_ITEM_KEY = "created_item_key"

class CreateItemFragment : BaseItemFragment(), OnItemSelectedListener<CommonItem> {

    private lateinit var mode: CreateItemMode

    override fun openImageViewer(images: Array<ItemImage>, index: Int) = navigate(
        actionCreateItemFragmentToImageViewerFragment(images, index)
    )

    override fun createViewModel() = ViewModelProvider(this, factory)[EditItemViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loggingUtil.log("${loggingTag()} onCreate(...)")
    }

    override fun viewModelSubscriptions() {
        viewModel.viewModelEvent.observe(viewLifecycleOwner) {
            (it as? InfoAvailable<List<Item>>)?.let { event ->
                showAlertDialogMessage(
                    "Позиції:\n${event.item.map { item -> item.titleString() + "\n" }} потребують переміщення",
                    "Перемістити"
                ) { viewModel.createIssuanceAndCreateItem(event.item) }
            }
            if (it != ViewModelEvent.Done) return@observe
            showToastMessage("Додано")
            findNavController(this@CreateItemFragment).previousBackStackEntry?.savedStateHandle?.set(
                "UPDATE", true
            )
            loggingUtil.log("${loggingTag()} added an item")
            if (mode == CREATE_ITEM_FOR_ISSUANCE) {
                findNavController(this@CreateItemFragment).previousBackStackEntry?.savedStateHandle?.set(
                    CREATED_ITEM_KEY, viewModel.currentItem
                )
            }
            navigateUp()
        }
        viewModel.images.observe(viewLifecycleOwner) { imagesAdapter.items = it }
        observeRequestStatus()
    }

    //Set up UI components and handle user input
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            val args = CreateItemFragmentArgs.fromBundle(bundle)
            args.barcode?.let { viewModel.onBarcodeTextChange(it) }

            binding?.barcodeEditText?.setText(args.barcode)
            mode = args.mode
            binding?.itemButtons?.buttonApply?.setOnClickListener {
                when (mode) {
                    CREATE_ITEM -> viewModel.submit()
                    CREATE_ITEM_FOR_ISSUANCE -> viewModel.createItemForIssuance()
                }
            }
            binding?.labelHistory?.visibility = GONE
            binding?.historyRecyclerView?.visibility = GONE
        }
        binding?.buttonScan?.setOnClickListener {
            navigate(actionCreateItemFragmentToScannerFragment())
        }
        binding?.photosView?.addImagesButton?.setOnClickListener {
            navigate(actionCreateItemFragmentToCameraFragment())
        }
        binding?.photosView?.addGalleryImagesButton?.setOnClickListener {
            checkPermissionAndChooseImageGallery()
        }
        binding?.itemQuantity?.itemQuantityCheckBox?.setOnCheckedChangeListener { _, isChecked ->
            binding?.itemQuantity?.itemQuantityFields?.visibility = if (isChecked) VISIBLE else GONE
        }
        binding?.setView?.root?.visibility = GONE
        binding?.setView?.itemSetCheckBox?.setOnCheckedChangeListener { _, isChecked ->
            binding?.setView?.addSubitemsButton?.visibility = if (isChecked) VISIBLE else GONE
            viewModel.switchSet(isChecked)
            binding?.itemQuantity?.layoutQuantity?.visibility = if (isChecked) GONE else VISIBLE
            binding?.barcodeLayout?.visibility = if (isChecked) GONE else VISIBLE
            binding?.setView?.subitemsRecyclerView?.visibility = if (isChecked) VISIBLE else GONE
            if (isChecked) {
                binding?.setView?.subitemsRecyclerView?.adapter = selectedItemsAdapter
                updateAdapter()
            }
        }

        binding?.setView?.addSubitemsButton?.setOnClickListener {
            navigate(actionCreateItemFragmentToSelectItemsFragment(null))
        }
        binding?.copyBarcode?.setOnClickListener {
            binding?.itemSn?.text = binding?.barcodeEditText?.text
        }
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        setupBackKeyDialogHandler(mode == CREATE_ITEM)
        binding?.itemLocationView?.spinnerLocation?.setSpinnerValue(viewModel.currentItem.location?.title)
        binding?.itemCategoryView?.spinnerCategory?.setSpinnerValue(viewModel.currentItem.category?.title)
        binding?.itemCategoryView?.spinnerSubCategory?.setSpinnerValue(viewModel.currentItem.subcategories.firstOrNull()?.title)
        binding?.itemLocationView?.spinnerSublocation?.setSpinnerValue(viewModel.currentItem.sublocation?.title)
        binding?.spinnerOwnershipType?.setSpinnerValue(viewModel.currentItem.ownershipType?.title)
        binding?.itemQuantity?.itemQuantityFields?.visibility =
            viewModel.currentItem.quantity?.let { VISIBLE } ?: GONE
        binding?.itemQuantity?.itemQuantityCheckBox?.isChecked =
            viewModel.currentItem.quantity?.let { true } ?: false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.empty_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) { // Check the ID of the selected item
            android.R.id.home -> { // If it's the home button
                handleBackPressed(true) // Call the method to handle the back button press
                true
            }

            else -> super.onOptionsItemSelected(item) // For all other cases, call the superclass method
        }
    }

}
