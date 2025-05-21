package com.kophe.le_sklad_zradn.screens.filter.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.kophe.le_sklad_zradn.databinding.FragmentFilterBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Done
import com.kophe.le_sklad_zradn.screens.filter.viewmodel.FilterViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladuilib.SpinnerExtensions.setSpinnerEntries
import com.kophe.leskladuilib.SpinnerExtensions.setSpinnerValue
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

const val FILTER_KEY = "filter_key"

class FilterFragment : BaseViewModelFragment<FragmentFilterBinding, FilterViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
        viewModelSubscriptions()
        viewModel.start()
    }

    override fun createViewModel() = ViewModelProvider(this, factory)[FilterViewModel::class.java]

    private fun viewModelSubscriptions() {
        viewModel.viewModelEvent.observe(this) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                when (it) {
                    Done -> {
                        NavHostFragment.findNavController(this@FilterFragment).previousBackStackEntry?.savedStateHandle?.set(
                            FILTER_KEY, viewModel.currentFilter
                        )
                        navigateUp()
                    }

                    else -> { /* just ignore */
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentFilterBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeRequestStatus()
        viewModel.locationEntries.observe(viewLifecycleOwner) {
            binding?.filtersView?.spinnerLocation?.setSpinnerEntries(it)
        }
        viewModel.sublocationEntries.observe(viewLifecycleOwner) {
            binding?.filtersView?.spinnerSublocation?.setSpinnerEntries(it)
        }
        viewModel.categoryEntries.observe(viewLifecycleOwner) {
            binding?.filtersView?.spinnerCategory?.setSpinnerEntries(it)
        }
        viewModel.subcategoryEntries.observe(viewLifecycleOwner) {
            binding?.filtersView?.spinnerSubCategory?.setSpinnerEntries(it)
        }
        viewModel.ownershipTypeEntries.observe(viewLifecycleOwner) {
            binding?.filtersView?.spinnerOwnershipType?.setSpinnerEntries(it)
        }
        viewModel.deliveryNoteEntries.observe(viewLifecycleOwner) {
            binding?.filtersView?.spinnerDeliveryNoteNumber?.setSpinnerEntries(it)
        }
        arguments?.let { bundle ->
            val args = FilterFragmentArgs.fromBundle(bundle)
            args.currentFilter?.let { filter ->
                viewModel.onCurrentFilterAvailable(filter)
                filter.location?.let {
                    viewModel.locationEntries.observe(viewLifecycleOwner) {
                        binding?.filtersView?.spinnerLocation?.setSpinnerValue(viewModel.currentFilter.location?.title)
                    }
                    filter.sublocation?.let {
                        viewModel.sublocationEntries.observe(viewLifecycleOwner) {
                            binding?.filtersView?.spinnerSublocation?.setSpinnerValue(viewModel.currentFilter.sublocation?.title)
                        }
                    }
                }
                filter.category?.let {
                    viewModel.categoryEntries.observe(viewLifecycleOwner) {
                        binding?.filtersView?.spinnerCategory?.setSpinnerValue(viewModel.currentFilter.category?.title)
                        viewModel.categorySelectionListener.onItemSelected(viewModel.currentFilter.category?.title)
                    }
                    filter.subcategory?.let {
                        viewModel.subcategoryEntries.observe(viewLifecycleOwner) {
                            binding?.filtersView?.spinnerSubCategory?.setSpinnerValue(viewModel.currentFilter.subcategory?.title)
                        }
                    }
                }
                filter.ownershipType?.let {
                    viewModel.ownershipTypeEntries.observe(viewLifecycleOwner) {
                        binding?.filtersView?.spinnerOwnershipType?.setSpinnerValue(viewModel.currentFilter.ownershipType?.title)
                    }
                }

            }
        }
    }
}
