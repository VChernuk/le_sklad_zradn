package com.kophe.le_sklad_zradn.screens.admin.backups.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentBackupsBinding
import com.kophe.le_sklad_zradn.screens.admin.backups.viewmodel.BackupsViewModel
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.Location
import com.kophe.leskladuilib.OnItemSelectedListener
import com.kophe.leskladuilib.admin.backups.adapter.BackupsAdapter
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class BackupsFragment : BaseViewModelFragment<FragmentBackupsBinding, BackupsViewModel>(),
    OnItemSelectedListener<Location> {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    private lateinit var showItem: (item: Location) -> Unit

    private val adapter by lazy {
        BackupsAdapter(object : OnItemSelectedListener<String> {
            override fun selectItem(item: String) {
                viewModel.restore(item)
            }
        })
    }

    override fun createViewModel() = ViewModelProvider(this, factory)[BackupsViewModel::class.java]

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
            } else { _ ->
            }
        }
        observeRequestStatus()
    }

    private fun reloadItems(items: List<String>) {
        loggingUtil.log("${loggingTag()} reloadItems(...)")
        adapter.items = items
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentBackupsBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.issuance_menu, menu)
    }

    override fun selectItem(item: Location) = showItem(item)

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

}
