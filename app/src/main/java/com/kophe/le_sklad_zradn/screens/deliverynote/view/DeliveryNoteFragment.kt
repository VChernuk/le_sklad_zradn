package com.kophe.le_sklad_zradn.screens.deliverynote.view

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
import com.kophe.le_sklad_zradn.databinding.FragmentDeliveryNoteBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.InfoAvailable
import com.kophe.le_sklad_zradn.screens.deliverynote.view.DeliveryNoteFragmentDirections.actionDeliveryNoteFragmentToCreateDeliveryNoteFragment
import com.kophe.le_sklad_zradn.screens.deliverynote.view.DeliveryNoteFragmentDirections.actionDeliveryNoteFragmentToEditItemFragment
import com.kophe.le_sklad_zradn.screens.deliverynote.view.DeliveryNoteFragmentDirections.actionDeliveryNoteFragmentToViewDeliveryNoteFragment
import com.kophe.le_sklad_zradn.screens.deliverynote.view.DeliveryNoteFragmentDirections.actionDeliveryNoteFragmentToViewItemFragment
import com.kophe.le_sklad_zradn.screens.deliverynote.viewmodel.DeliveryNoteViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.DeliveryNote
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladuilib.OnItemSelectedListener
import com.kophe.leskladuilib.deliverynote.adapter.DeliveryNoteAdapter
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class DeliveryNoteFragment : BaseViewModelFragment<FragmentDeliveryNoteBinding, DeliveryNoteViewModel>(),
    OnItemSelectedListener<DeliveryNote> {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    private lateinit var showItem: (item: Item) -> Unit

    private val adapter by lazy {
        DeliveryNoteAdapter(this, object : OnItemSelectedListener<CommonItem> {
            override fun selectItem(item: CommonItem) {
                viewModel.findItem(item)
            }
        })
    }

    override fun createViewModel() = ViewModelProvider(this, factory)[DeliveryNoteViewModel::class.java]

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
        binding?.deliverynoteList?.adapter = adapter
        viewModel.entries.observe(viewLifecycleOwner) { reloadItems(it) }
        viewModel.writeAvailable.observe(viewLifecycleOwner) {
            showItem = if (it) { item ->
                navigate(actionDeliveryNoteFragmentToEditItemFragment(item))
            } else { item ->
                navigate(actionDeliveryNoteFragmentToViewItemFragment(item))
            }
            binding?.addItemButton?.isVisible = it == true
            binding?.addItemButton?.setOnClickListener {
                navigate(actionDeliveryNoteFragmentToCreateDeliveryNoteFragment(null))
            }
        }
        observeRequestStatus()
    }

    private fun reloadItems(items: List<DeliveryNote>) {
        loggingUtil.log("${loggingTag()} reloadItems(...)")
        adapter.items = items
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentDeliveryNoteBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.deliverynote_menu, menu)
    }

    override fun selectItem(item: DeliveryNote) {
        navigate(actionDeliveryNoteFragmentToViewDeliveryNoteFragment(item))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

}
