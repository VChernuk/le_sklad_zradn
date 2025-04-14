package com.kophe.le_sklad_zradn.screens.edititem.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.navigation.fragment.NavHostFragment
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Done
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.InfoAvailable
import com.kophe.le_sklad_zradn.screens.edititem.view.EditItemFragmentDirections.actionEditItemFragmentToCameraFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.EditItemFragmentDirections.actionEditItemFragmentToCreateIssuanceFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.EditItemFragmentDirections.actionEditItemFragmentToImageViewerFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.EditItemFragmentDirections.actionEditItemFragmentToScannerFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.EditItemFragmentDirections.actionEditItemFragmentToViewIssuanceFragment
import com.kophe.leskladlib.isSet
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.Issuance
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.ItemImage
import com.kophe.leskladuilib.OnItemSelectedListener
import com.kophe.leskladuilib.SpinnerExtensions.setSpinnerEntries
import com.kophe.leskladuilib.SpinnerExtensions.setSpinnerValue

//EditItemFragment class for editing or creating items
//TODO: rename
//TODO: setup title according to item or mode
class EditItemFragment : BaseItemFragment(), OnItemSelectedListener<CommonItem> {

    private var originalItem: Item? = null
    override fun openImageViewer(images: Array<ItemImage>, index: Int) =
        navigate(actionEditItemFragmentToImageViewerFragment(images, index))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.viewModelEvent.observe(this) {
            (it as? InfoAvailable<Issuance>)?.let { event ->
                navigate(actionEditItemFragmentToViewIssuanceFragment(event.item))
            }
        }
        setHasOptionsMenu(true)
    }

    override fun preClearAction() {
        val currentItem = viewModel.currentItem
        originalItem?.let {
            currentItem.barcode = it.barcode
            currentItem.category = it.category
            currentItem.createdDate = it.createdDate
            currentItem.firestoreId = it.firestoreId
            currentItem.history = it.history
            currentItem.id = it.id
            currentItem.images = it.images
            currentItem.responsibleUnit = it.responsibleUnit
            currentItem.location = it.location
            currentItem.notes = it.notes
            currentItem.ownershipType = it.ownershipType
            currentItem.deliveryNote = it.deliveryNote
            currentItem.quantity = it.quantity
            currentItem.setOptions = it.setOptions
            currentItem.sn = it.sn
            currentItem.subcategories = it.subcategories
            currentItem.sublocation = it.sublocation
            currentItem.title = it.title
        }
    }


    override fun viewModelSubscriptions() {
        viewModel.viewModelEvent.observe(viewLifecycleOwner) {
            if (it != Done) return@observe
            NavHostFragment.findNavController(this@EditItemFragment).previousBackStackEntry?.savedStateHandle?.set(
                "UPDATE", true
            )
            showToastMessage("Готово")
            navigateUp()
        }
        viewModel.images.observe(viewLifecycleOwner) { imagesAdapter.items = it }
        observeRequestStatus()
    }


    //Set up UI components and handle user input
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            val args = EditItemFragmentArgs.fromBundle(bundle)
            val item = args.item ?: run {
                showMessage("От лишенько, майна не знайдено")
                navigateUp()
                return@let
            }
            originalItem = item.copy()
            setupEditItemUI(item)
            binding?.itemButtons?.buttonApply?.setOnClickListener { viewModel.saveChanges() }
        }
        binding?.buttonScan?.setOnClickListener {
            navigate(actionEditItemFragmentToScannerFragment())
        }
        binding?.photosView?.addImagesButton?.setOnClickListener {
            navigate(actionEditItemFragmentToCameraFragment())
        }
        binding?.photosView?.addGalleryImagesButton?.setOnClickListener {
            checkPermissionAndChooseImageGallery()
        }
        binding?.copyBarcode?.setOnClickListener {
            binding?.itemSn?.text = binding?.barcodeEditText?.text
        }
//        binding?.setView?.root?.visibility = GONE
    }

    override fun onResume() {
        super.onResume()
        val currentItem = viewModel.currentItem
        binding?.itemLocationView?.spinnerLocation?.setSpinnerValue(currentItem.location?.title)
        binding?.itemQuantity?.itemQuantityFields?.visibility =
            currentItem.quantity?.let { VISIBLE } ?: GONE
        binding?.itemQuantity?.itemQuantityCheckBox?.isChecked = currentItem.quantity != null
    }

    private fun setupEditItemUI(item: Item) {
        loggingUtil.log("${loggingTag()} setupEditItemUI(...) item: $item")
        baseActivity?.setTitle("${item.title ?: ""} ${item.id ?: ""}")
        viewModel.currentItem = item
        binding?.barcodeEditText?.setText(item.barcode)
        binding?.itemIdEditText?.setText(item.id)
        binding?.itemName?.setText(item.title)
        binding?.itemSn?.setText(item.sn)
        binding?.notes?.setText(item.notes)
        item.quantity?.let { q ->
            binding?.itemQuantity?.itemQuantityCheckBox?.isEnabled = false
            binding?.itemQuantity?.itemQuantity?.setText("${q.quantity}")
            binding?.itemQuantity?.itemMeasurement?.setText(q.measurement)
            binding?.itemQuantity?.layoutQuantity?.visibility = VISIBLE
        } ?: run { binding?.itemQuantity?.layoutQuantity?.visibility = GONE }
        viewModel.categoryEntries.observe(viewLifecycleOwner) {
            binding?.itemCategoryView?.spinnerCategory?.setSpinnerValue(item.category?.title)
        }
        viewModel.locationEntries.observe(viewLifecycleOwner) {
            binding?.itemLocationView?.spinnerLocation?.setSpinnerValue(item.location?.title)
            binding?.itemLocationView?.spinnerLocation?.isEnabled = false
            if (item.sublocation == null) {
                binding?.itemLocationView?.spinnerSublocation?.visibility = GONE
                binding?.itemLocationView?.labelSublocation?.visibility = GONE
            } else {
                binding?.itemLocationView?.spinnerSublocation?.setSpinnerEntries(item.location?.sublocations?.map { it.title })
                binding?.itemLocationView?.spinnerSublocation?.setSpinnerValue(item.sublocation?.title)
            }
            binding?.itemLocationView?.spinnerSublocation?.isEnabled = false
        }
        viewModel.subcategoryEntries.observe(viewLifecycleOwner) {
            binding?.itemCategoryView?.spinnerSubCategory?.setSpinnerValue(item.subcategories.firstOrNull()?.title)
        }
        viewModel.ownershipTypeEntries.observe(viewLifecycleOwner) {
            loggingUtil.log("${loggingTag()} will set ownership type to $it")
            binding?.itemOwnershipView?.spinnerOwnershipType?.setSpinnerValue(item.ownershipType?.title)
        }
        viewModel.deliveryNoteEntries.observe(viewLifecycleOwner) {
            loggingUtil.log("${loggingTag()} will set deliveryNote type to $it")
            binding?.itemOwnershipView?.spinnerDeliveryNoteNumber?.setSpinnerValue(item.deliveryNote?.deliveryNoteNumber)
        }
        if (viewModel.currentItem.history.isEmpty()) {
            binding?.historyRecyclerView?.visibility = GONE
            binding?.labelHistory?.visibility = GONE
        } else {
            binding?.historyRecyclerView?.adapter = historyAdapter
            historyAdapter.items = viewModel.currentItem.history
        }
        binding?.buttonScan?.setOnClickListener {
            navigate(actionEditItemFragmentToScannerFragment())
        }
        if (viewModel.currentItem.isSet()) {
            viewModel.switchSet(true)
            binding?.setView?.addSubitemsButton?.visibility = VISIBLE
            binding?.itemCategoryView?.spinnerCategory?.isEnabled = false
            binding?.itemCategoryView?.spinnerSubCategory?.isEnabled = false
            binding?.setView?.itemSetCheckBox?.isChecked = true
            binding?.setView?.subitemsRecyclerView?.adapter = selectedItemsAdapter
            updateAdapter()
        } else {
            binding?.setView?.root?.visibility = GONE
        }
        binding?.setView?.itemSetCheckBox?.isEnabled = false
        viewModel.updateSaveAvailability()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_item_menu, menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) { // Check the ID of the selected item
            android.R.id.home -> { // If it's the home button
                preClearAction()
                handleBackPressed(false) // Call the method to handle the back button press
                true
            }

            R.id.createIssuance -> { // If it's the "Create Issuance" menu item
                navigate(
                    actionEditItemFragmentToCreateIssuanceFragment(arrayOf(viewModel.currentItem))
                )
                true
            }

            R.id.share -> {
                arguments?.let { bundle ->
                    val args = EditItemFragmentArgs.fromBundle(bundle)
                    val item = args.item ?: run {
                        showDefaultErrorMessage()
                        return@let
                    }
                    shareItemTxt(item)
                } ?: run { showDefaultErrorMessage() }
                true
            }

            R.id.delete -> {
                arguments?.let { bundle ->
                    val args = EditItemFragmentArgs.fromBundle(bundle)
                    val item = args.item ?: run {
                        showDefaultErrorMessage()
                        return@let
                    }
                    showAlertDialogMessage(
                        "Видалити ${item.titleString()}?", "Так", viewModel::deleteItem
                    )
                } ?: run { showDefaultErrorMessage() }
                true
            }

            else -> super.onOptionsItemSelected(menuItem) // For all other cases, call the superclass method
        }
    }

    private fun shareItemTxt(item: Item) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, exportUtil.itemToText(item))
        startActivity(Intent.createChooser(shareIntent, "Надіслати"))
    }

    override fun selectItem(item: CommonItem) {
        viewModel.getIssuanceInfo(item)
    }

}
