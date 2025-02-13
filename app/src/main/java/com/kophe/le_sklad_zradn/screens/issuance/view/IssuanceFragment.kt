package com.kophe.le_sklad_zradn.screens.issuance.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentIssuanceBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.InfoAvailable
import com.kophe.le_sklad_zradn.screens.issuance.view.IssuanceFragmentDirections.actionIssuanceFragmentToCreateIssuanceFragment
import com.kophe.le_sklad_zradn.screens.issuance.view.IssuanceFragmentDirections.actionIssuanceFragmentToEditItemFragment
import com.kophe.le_sklad_zradn.screens.issuance.view.IssuanceFragmentDirections.actionIssuanceFragmentToViewIssuanceFragment
import com.kophe.le_sklad_zradn.screens.issuance.view.IssuanceFragmentDirections.actionIssuanceFragmentToViewItemFragment
import com.kophe.le_sklad_zradn.screens.issuance.viewmodel.IssuanceViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.Issuance
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladuilib.OnItemSelectedListener
import com.kophe.leskladuilib.issuance.adapter.IssuanceAdapter
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class IssuanceFragment : BaseViewModelFragment<FragmentIssuanceBinding, IssuanceViewModel>(),
    OnItemSelectedListener<Issuance> {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    private lateinit var showItem: (item: Item) -> Unit

    private val adapter by lazy {
        IssuanceAdapter(this, object : OnItemSelectedListener<CommonItem> {
            override fun selectItem(item: CommonItem) {
                viewModel.findItem(item)
            }
        })
    }

    override fun createViewModel() = ViewModelProvider(this, factory)[IssuanceViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
        viewModel.viewModelEvent.observe(this) {
            (it as? InfoAvailable<Item>)?.let { event -> showItem(event.item) }
        }
        viewModel.start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelSubscriptions()
        NavHostFragment.findNavController(this).currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            "UPDATE"
        )?.observe(viewLifecycleOwner) {
            if (it) viewModel.refreshInfo()
        }
    }

    private fun viewModelSubscriptions() {
        binding?.issuanceList?.adapter = adapter
        viewModel.entries.observe(viewLifecycleOwner) { reloadItems(it) }
        viewModel.writeAvailable.observe(viewLifecycleOwner) {
            showItem = if (it) { item ->
                navigate(actionIssuanceFragmentToEditItemFragment(item))
            } else { item ->
                navigate(actionIssuanceFragmentToViewItemFragment(item))
            }
            binding?.addItemButton?.isVisible = it == true
            binding?.addItemButton?.setOnClickListener {
                navigate(actionIssuanceFragmentToCreateIssuanceFragment(null))
            }
        }
        observeRequestStatus()
    }

    private fun reloadItems(items: List<Issuance>) {
        loggingUtil.log("${loggingTag()} reloadItems(...)")
        adapter.items = items
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentIssuanceBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.issuance_menu, menu)
    }

    override fun selectItem(item: Issuance) {
        navigate(actionIssuanceFragmentToViewIssuanceFragment(item))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

}
