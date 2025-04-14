package com.kophe.le_sklad_zradn.screens.items.view

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
import android.widget.ImageView
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentItemsBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemMode.CREATE_ITEM
import com.kophe.le_sklad_zradn.screens.filter.view.FILTER_KEY
import com.kophe.le_sklad_zradn.screens.items.view.ItemsFragmentDirections.actionItemsFragmentToCreateItemFragment
import com.kophe.le_sklad_zradn.screens.items.view.ItemsFragmentDirections.actionItemsFragmentToEditItemFragment
import com.kophe.le_sklad_zradn.screens.items.view.ItemsFragmentDirections.actionItemsFragmentToFilterFragment
import com.kophe.le_sklad_zradn.screens.items.view.ItemsFragmentDirections.actionItemsFragmentToViewItemFragment
import com.kophe.le_sklad_zradn.screens.items.viewmodel.ItemsViewModel
import com.kophe.le_sklad_zradn.util.export.ExportUtil
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.Filter
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladuilib.OnItemSelectedListener
import com.kophe.leskladuilib.items.adapter.ItemsAdapter
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject


class ItemsFragment : BaseViewModelFragment<FragmentItemsBinding, ItemsViewModel>(),
    OnItemSelectedListener<Item> {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    @Inject
    lateinit var exportUtil: ExportUtil
    private val adapter by lazy { ItemsAdapter(this) }
    private lateinit var goToItemDetails: (item: Item) -> Unit

    override fun createViewModel() = ViewModelProvider(this, factory)[ItemsViewModel::class.java]
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
        viewModel.start()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelSubscriptions()
        NavHostFragment.findNavController(this).currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            "UPDATE"
        )?.observe(viewLifecycleOwner) {
            //TODO: add delay
            if (it) viewModel.refreshInfo()
        }
        NavHostFragment.findNavController(this).currentBackStackEntry?.savedStateHandle?.getLiveData<Filter>(
            FILTER_KEY
        )?.observe(viewLifecycleOwner) {
            loggingUtil.log("filter: $it")
            binding?.filterInfo?.visibility = if (it.isClear()) GONE else VISIBLE
            if (!it.isClear()) {
                val locationTitle = it.location?.title?.let { location -> "$location/" } ?: ""
                val sublocationTitle =
                    it.sublocation?.title?.let { sublocation -> "$sublocation/" } ?: ""
                val categoryTitle = it.category?.title?.let { category -> "$category/" } ?: ""
                val subcategoryTitle =
                    it.subcategory?.title?.let { subcategory -> "$subcategory/" } ?: ""
                val ownershipTitle = it.ownershipType?.title?.let { type -> "$type/" } ?: ""
                //val deliveryNote = it.deliveryNote?.title?.let { type -> "$type/" } ?: ""
                val responsibleUnitTitle = it.responsibleUnit?.title?.let { unit -> "$unit/" } ?: ""
                binding?.filterInfoText?.text =
                    locationTitle + sublocationTitle + categoryTitle + subcategoryTitle + ownershipTitle + responsibleUnitTitle
            }
            viewModel.currentFilter = it
        }

    }

    private fun viewModelSubscriptions() {
        viewModel.writeAvailable.observe(viewLifecycleOwner) {
            binding?.addItemButton?.isVisible = it == true
            binding?.addItemButton?.setOnClickListener {
                navigate(actionItemsFragmentToCreateItemFragment(null, CREATE_ITEM))
            }
            goToItemDetails =
                if (it) { item -> navigate(actionItemsFragmentToEditItemFragment(item)) }
                else { item -> navigate(actionItemsFragmentToViewItemFragment(item)) }
        }
        binding?.itemsList?.adapter = adapter
        viewModel.entries.observe(viewLifecycleOwner) { reloadItems(it) }
        observeRequestStatus()
    }

    private fun reloadItems(items: List<Item>) {
        loggingUtil.log("${loggingTag()} reloadItems(...)")
        adapter.items = items
        binding?.number?.text = "${items.size} позицій"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentItemsBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun selectItem(item: Item) = goToItemDetails(item)

    override fun onPrepareOptionsMenu(menu: Menu) {
        val searchViewMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchViewMenuItem.actionView as SearchView?
        val searchImgId = resources.getIdentifier("android:id/search_button", null, null)
        val v = searchView?.findViewById(searchImgId) as? ImageView
        v?.setImageResource(R.drawable.search_3824)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.items_menu, menu)
        val myActionMenuItem = menu.findItem(R.id.action_search)
        val searchView: SearchView? = myActionMenuItem?.actionView as SearchView?
        MainScope().launch {
            delay(200)
            if (viewModel.searchQuery.isNotEmpty()) {
                myActionMenuItem.expandActionView()
                searchView?.requestFocus()
                searchView?.performClick()
                searchView?.setQuery(viewModel.searchQuery, true)
                searchView?.requestFocus()
            }
        }
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                loggingUtil.log("on query text change: $newText")
                viewModel.searchQuery = newText ?: ""
                setupSearchInfoVisibility()
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        setupSearchInfoVisibility()
    }

    private fun setupSearchInfoVisibility() {
        binding?.searchInfo?.visibility = if (viewModel.searchQuery.isEmpty()) GONE else VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.shareXLS -> {
            createXLS()
            true
        }

        R.id.filter -> {
            val filter = viewModel.currentFilter
            navigate(actionItemsFragmentToFilterFragment(filter))
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun createXLS() {
        //TODO: move date to fun
        val title = (viewModel.currentFilter?.let { "items_${it.filterDescription}" }
            ?: "all_items") + "_${viewModel.searchQuery}_${
            SimpleDateFormat("dd.MM.yyyy_HH.mm").format(
                Date()
            )
        }"
        val file = exportUtil.exportItemsFile(adapter.items, title)
        shareFile(file)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

}
