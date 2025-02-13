package com.kophe.le_sklad_zradn.screens.selectitems.view

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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentSelectItemsBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.InfoAvailable
import com.kophe.le_sklad_zradn.screens.edititem.view.CREATED_ITEM_KEY
import com.kophe.le_sklad_zradn.screens.edititem.view.CreateItemMode.CREATE_ITEM_FOR_ISSUANCE
import com.kophe.le_sklad_zradn.screens.filter.view.FILTER_KEY
import com.kophe.le_sklad_zradn.screens.issuancescanner.view.ISSUANCE_ITEMS_KEY
import com.kophe.le_sklad_zradn.screens.selectitems.view.SelectItemsFragmentDirections.actionSelectItemsFragmentToCreateItemFragment
import com.kophe.le_sklad_zradn.screens.selectitems.view.SelectItemsFragmentDirections.actionSelectItemsFragmentToFilterFragment
import com.kophe.le_sklad_zradn.screens.selectitems.view.SelectItemsFragmentDirections.actionSelectItemsFragmentToIssuanceScannerFragment
import com.kophe.le_sklad_zradn.screens.selectitems.viewmodel.SelectItemsViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.Filter
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladuilib.OnItemSelectedListener
import com.kophe.leskladuilib.selectitems.adapter.SelectItemsAdapter
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

const val SELECTED_ITEMS_KEY = "selected_items_key"

class SelectItemsFragment :
    BaseViewModelFragment<FragmentSelectItemsBinding, SelectItemsViewModel>(),
    OnItemSelectedListener<Item> {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    private val adapter by lazy { SelectItemsAdapter(this) }

    override fun createViewModel() =
        ViewModelProvider(this, factory)[SelectItemsViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
        if (!viewModel.started) viewModel.viewModelEvent.observe(this) { event ->
            (event as? InfoAvailable<Item>)?.let {
                (event.item as? Item)?.let {
                    showToastMessage("Додано $it")
                    adapter.selectedItems.add(it)
                    adapter.notifyDataSetChanged()
                    selectItem(it)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModelSubscriptions()
        val savedStateHandle =
            NavHostFragment.findNavController(this).currentBackStackEntry?.savedStateHandle
                ?: return
        savedStateHandle.getLiveData<Item>(CREATED_ITEM_KEY).observe(viewLifecycleOwner) {
            viewModel.refreshInfo()
            NavHostFragment.findNavController(this@SelectItemsFragment).previousBackStackEntry?.savedStateHandle?.set(
                SELECTED_ITEMS_KEY, listOf(it)
            )
            CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                navigateUp()
            }
        }
        savedStateHandle.getLiveData<Filter>(FILTER_KEY).observe(viewLifecycleOwner) {
            loggingUtil.log("filter: $it")
            binding?.filterInfo?.visibility = if (it.isClear()) GONE else VISIBLE
            if (!it.isClear()) {
                val locationTitle = it.location?.title?.let { location -> "$location/" } ?: ""
                val sublocationTitle =
                    it.sublocation?.title?.let { sublocation -> "$sublocation/" } ?: ""
                val categoryTitle = it.category?.title?.let { category -> "$category/" } ?: ""
                val subcategoryTitle =
                    it.subcategory?.title?.let { subcategory -> "$subcategory/" } ?: ""
                binding?.filterInfoText?.text =
                    "$locationTitle$sublocationTitle$categoryTitle$subcategoryTitle"
            }
            viewModel.currentFilter = it
        }
        savedStateHandle.getLiveData<String>(ISSUANCE_ITEMS_KEY)
            .observe(viewLifecycleOwner) { barcode ->
                viewModel.addItemByBarcode(barcode ?: return@observe)
            }
    }

    private fun viewModelSubscriptions() {
        binding?.itemsList?.adapter = adapter
        viewModel.entries.observe(viewLifecycleOwner) { reloadItems(it) }
        observeRequestStatus()
    }

    private fun reloadItems(items: List<Item>) {
        loggingUtil.log("${loggingTag()} reloadItems(...)")
        adapter.items = items
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentSelectItemsBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.addButton?.setOnClickListener {
            NavHostFragment.findNavController(this@SelectItemsFragment).previousBackStackEntry?.savedStateHandle?.set(
                SELECTED_ITEMS_KEY, adapter.selectedItems
            )
            navigateUp()
        }
        arguments?.let { bundle ->
            val args = SelectItemsFragmentArgs.fromBundle(bundle)
//            args.barcode?.let { barcode ->
//                viewModel.barcode = barcode
//                if (!viewModel.started) viewModel.viewModelEvent.observe(viewLifecycleOwner) { event ->
//                    if (event is EmptyResult) createItemWithBarcode(barcode)
//                }
//                setHasOptionsMenu(false)
//            } ?: run {
//            setHasOptionsMenu(true)
//            }
        }

        setHasOptionsMenu(true)
        binding?.createNewItemButton?.setOnClickListener {
            createItemWithBarcode("")
        }
        viewModel.start()
    }

    private fun createItemWithBarcode(barcode: String) {
        navigate(
            actionSelectItemsFragmentToCreateItemFragment(barcode, CREATE_ITEM_FOR_ISSUANCE)
        )
    }

    override fun selectItem(item: Item) = setupTitle()

    override fun onResume() {
        super.onResume()
        setupTitle()
    }

    private fun setupTitle() {
        val selectedItemsCount = adapter.selectedItems.size
        baseActivity?.setTitle("Додати майно: $selectedItemsCount")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.select_items_menu, menu)
        val myActionMenuItem = menu.findItem(R.id.action_search)
        val searchView = myActionMenuItem?.actionView as SearchView?
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

    private fun setupSearchInfoVisibility() {
        binding?.searchInfo?.visibility = if (viewModel.searchQuery.isEmpty()) GONE else VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.filter -> {
            navigate(actionSelectItemsFragmentToFilterFragment(viewModel.currentFilter))
            true
        }

        R.id.action_scan -> {
            navigate(actionSelectItemsFragmentToIssuanceScannerFragment())
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val searchViewMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchViewMenuItem.actionView as SearchView?
        val searchImgId = resources.getIdentifier("android:id/search_button", null, null)
        val v = searchView?.findViewById(searchImgId) as? ImageView
        v?.setImageResource(R.drawable.search_3824)
        super.onPrepareOptionsMenu(menu)
    }
}
