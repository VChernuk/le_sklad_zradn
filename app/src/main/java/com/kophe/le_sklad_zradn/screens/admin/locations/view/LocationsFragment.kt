package com.kophe.le_sklad_zradn.screens.admin.locations.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.databinding.FragmentLocationsBinding
import com.kophe.le_sklad_zradn.screens.admin.locations.view.LocationsFragmentDirections.actionLocationsFragmentToCreateLocationFragment
import com.kophe.leskladuilib.admin.locations.adapter.LocationsAdapter
import com.kophe.le_sklad_zradn.screens.admin.locations.viewmodel.LocationsViewModel
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.Location
import com.kophe.leskladlib.repository.common.Sublocation
import com.kophe.leskladuilib.OnItemSelectedListener
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class LocationsFragment : BaseViewModelFragment<FragmentLocationsBinding, LocationsViewModel>(),
    OnItemSelectedListener<Location> {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    private lateinit var showItem: (item: Location) -> Unit

    private val adapter by lazy {
        LocationsAdapter(this, object : OnItemSelectedListener<Sublocation> {
            override fun selectItem(item: Sublocation) {

            }
        })
    }

    override fun createViewModel() =
        ViewModelProvider(this, factory)[LocationsViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelSubscriptions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start()
    }

    private fun viewModelSubscriptions() {
        binding?.issuanceList?.adapter = adapter
        viewModel.entries.observe(viewLifecycleOwner) { reloadItems(it) }
        viewModel.writeAvailable.observe(viewLifecycleOwner) {
            showItem = if (it) { item ->
                navigate(actionLocationsFragmentToCreateLocationFragment(item))
            } else { _ ->
                showMessage("Недостатньо прав доступу")
            }
            binding?.addItemButton?.isVisible = it == true
            binding?.addItemButton?.setOnClickListener {
                navigate(actionLocationsFragmentToCreateLocationFragment(null))
            }
        }
        observeRequestStatus()
    }

    private fun reloadItems(items: List<Location>) {
        loggingUtil.log("${loggingTag()} reloadItems(...)")
        adapter.items = items
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentLocationsBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.issuance_menu, menu)
//    }

    override fun selectItem(item: Location) {
        showItem(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

}
