package com.kophe.le_sklad_zradn.screens.createdeliverynote.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentCreateDeliveryNoteBinding //FragmentCreateDeliveryNoteBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Done
import com.kophe.le_sklad_zradn.screens.createdeliverynote.view.CreateDeliveryNoteFragmentDirections.actionCreateDeliveryNoteFragmentToSelectItemsFragment
import com.kophe.le_sklad_zradn.screens.createdeliverynote.viewmodel.CreateDeliveryNoteViewModel
import com.kophe.le_sklad_zradn.screens.selectitems.view.SELECTED_ITEMS_KEY
import com.kophe.le_sklad_zradn.util.export.ExportUtil
import com.kophe.le_sklad_zradn.util.extensions.timestampToFormattedDate
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.ItemQuantity
import com.kophe.leskladuilib.OnItemSelectedListener
import com.kophe.leskladuilib.SpinnerExtensions.setSpinnerValue
import com.kophe.leskladuilib.createdeliverynote.adapter.SelectedItemsAdapter
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class CreateDeliveryNoteFragment :
    BaseViewModelFragment<FragmentCreateDeliveryNoteBinding, CreateDeliveryNoteViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    @Inject
    lateinit var exportUtil: ExportUtil

    private val adapter by lazy {
        SelectedItemsAdapter(object : OnItemSelectedListener<Item> {
            override fun selectItem(item: Item) {
                viewModel.selectedItems.removeAll { it.firestoreId == item.firestoreId && it.titleString() == item.titleString() }
                viewModel.updateSubmitAvailability()
                updateAdapterItems()
                setupTitle()
            }
        }, object : OnItemSelectedListener<Item> {
            override fun selectItem(item: Item) {
                item.quantity?.let {
                    if (it.quantity > 1) item.quantity =
                        ItemQuantity(it.parentId, it.quantity - 1, it.measurement)
                }
                updateAdapterItems()
            }
        }, object : OnItemSelectedListener<Item> {
            override fun selectItem(item: Item) {
                //TODO: define max
                item.quantity?.let {
                    item.quantity = ItemQuantity(it.parentId, it.quantity + 1, it.measurement)
                }
                updateAdapterItems()
            }
        })
    }
    private lateinit var handleBackNavigation: () -> Unit

    override fun createViewModel() =
        ViewModelProvider(this, factory)[CreateDeliveryNoteViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
    }

    private fun viewModelSubscriptions() {
        viewModel.viewModelEvent.observe(viewLifecycleOwner) {
            if (it == Done) {
                NavHostFragment.findNavController(this@CreateDeliveryNoteFragment).previousBackStackEntry?.savedStateHandle?.set(
                    "UPDATE", true
                )
                showToastMessage("Видача сформована")
                handleBackNavigation.invoke()
            }
        }
        observeRequestStatus()
    }

    override fun onResume() {
        super.onResume()
        setupTitle()
        binding?.spinnerLocation?.setSpinnerValue(viewModel.selectedLocation?.title)
        binding?.spinnerSublocation?.setSpinnerValue(viewModel.selectedSublocation?.title)
        setupBackKeyDialogHandler(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentCreateDeliveryNoteBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            val args = CreateDeliveryNoteFragmentArgs.fromBundle(bundle)
            loggingUtil.log("screen started in CREATE mode")
            binding?.buttonCreate?.setOnClickListener {
                navigate(
                    actionCreateDeliveryNoteFragmentToSelectItemsFragment(null)
                )
            }
            binding?.buttonApply?.setOnClickListener {
                viewModel.submit()
            }
            args.items?.let {
                viewModel.selectedItems.addAll(it)
                it.filter { item -> item.quantity != null && item.firestoreId != null }
                    .forEach { item ->
                        viewModel.originalQuantities[item.firestoreId!!] = item.quantity!!.quantity
                    }
            } ?: run {
                handleBackNavigation = { navigateUp() }
                return@let
            }
            viewModel.updateSubmitAvailability()
            updateAdapterItems()
            setupTitle()
            handleBackNavigation = {
                navigateUp()
                navigateUp()
                navigateUp()
            }
        }
        setHasOptionsMenu(true)
        binding?.deliverynoteItemsList?.adapter = adapter
        viewModelSubscriptions()
        viewModel.start()
        viewModel.sublocationEntries.observe(viewLifecycleOwner) {
            binding?.spinnerSublocation?.visibility = if (it.isNullOrEmpty()) GONE else VISIBLE
            binding?.labelSublocation?.visibility = if (it.isNullOrEmpty()) GONE else VISIBLE
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        NavHostFragment.findNavController(this).currentBackStackEntry?.savedStateHandle?.getLiveData<List<Item>>(
            SELECTED_ITEMS_KEY
        )?.observe(viewLifecycleOwner) {
            viewModel.selectedItems.addAll(it)
            it.filter { item -> item.quantity != null && item.firestoreId != null }
                .forEach { item ->
                    viewModel.originalQuantities[item.firestoreId!!] = item.quantity!!.quantity
                }
            viewModel.updateSubmitAvailability()
            updateAdapterItems()
            setupTitle()
        }
    }

    private fun setupTitle() {
        val selectedItemsCount = viewModel.selectedItems.size
        baseActivity?.setTitle(
            "Рух майна ${
                System.currentTimeMillis().timestampToFormattedDate()
            } ($selectedItemsCount обрано)"
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.empty_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { // If it's the home button
            handleBackPressed(true) // Call the method to handle the back button press
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

    private fun updateAdapterItems() {
        adapter.items = viewModel.selectedItems.toList().sortedBy { it.title }
    }

}
