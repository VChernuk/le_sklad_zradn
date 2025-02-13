package com.kophe.le_sklad_zradn.screens.viewissuance

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentViewIssuanceBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.InfoAvailable
import com.kophe.le_sklad_zradn.screens.viewissuance.ViewIssuanceFragmentDirections.actionViewIssuanceFragmentToCreateIssuanceFragment
import com.kophe.le_sklad_zradn.screens.viewissuance.ViewIssuanceFragmentDirections.actionViewIssuanceFragmentToEditItemFragment
import com.kophe.le_sklad_zradn.screens.viewissuance.ViewIssuanceFragmentDirections.actionViewIssuanceFragmentToViewItemFragment
import com.kophe.leskladuilib.viewissuance.adapter.IssuanceCommonItemsAdapter
import com.kophe.le_sklad_zradn.util.export.ExportUtil
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.Issuance
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladuilib.OnItemSelectedListener
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

class ViewIssuanceFragment :
    BaseViewModelFragment<FragmentViewIssuanceBinding, ViewIssuanceViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    @Inject
    lateinit var exportUtil: ExportUtil

    private val adapter by lazy {
        IssuanceCommonItemsAdapter(object : OnItemSelectedListener<CommonItem> {
            override fun selectItem(item: CommonItem) {
                item.firestoreId?.let { viewModel.findItem(it) }
                    ?: run { showDefaultErrorMessage() }
            }
        }, null, false)
    }
    private lateinit var showItem: (item: Item) -> Unit


    override fun createViewModel() =
        ViewModelProvider(this, factory)[ViewIssuanceViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
        viewModel.viewModelEvent.observe(this) {
            (it as? InfoAvailable<*>)?.let { event ->
                (event.item as? Item)?.let { item ->
                    showItem(item)
                    return@observe
                }
                (event.item as? List<Item>)?.let { items ->
                    navigate(actionViewIssuanceFragmentToCreateIssuanceFragment(items.toTypedArray()))
                }
            }
        }
        viewModel.start()
    }

    private fun viewModelSubscriptions() {
        observeRequestStatus()
        viewModel.writeAvailable.observe(viewLifecycleOwner) { writeAvailable ->
            showItem = if (writeAvailable) { item ->
                navigate(actionViewIssuanceFragmentToEditItemFragment(item))
            }
            else { item -> navigate(actionViewIssuanceFragmentToViewItemFragment(item)) }
            if (!writeAvailable) return@observe
//            binding?.buttonReissue?.visibility = VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        setupBackKeyDialogHandler(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentViewIssuanceBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            val args = ViewIssuanceFragmentArgs.fromBundle(bundle)
            if (args.issuance.notes.isEmpty()) binding?.passingNotes?.visibility = GONE
            else binding?.passingNotes?.text = args.issuance.notes
            binding?.callSighTV?.text = args.issuance.receiverCallSign ?: "-"
            binding?.issuanceLocation?.text = args.issuance.to
            viewModel.selectedItems.addAll(args.issuance.items.map { Item(it) })
            updateAdapterItems()
            setupTitle("${args.issuance.date} ${args.issuance.from}")
        }
        setHasOptionsMenu(true)
        binding?.issuanceItemsList?.adapter = adapter
        viewModelSubscriptions()
    }

    private fun setupTitle(msg: String) {
        baseActivity?.setTitle(msg)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.share_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> { // If it's the home button
            handleBackPressed(false) // Call the method to handle the back button press
            true
        }

        R.id.shareXLS -> {
            arguments?.let { bundle ->
                val args = ViewIssuanceFragmentArgs.fromBundle(bundle)
                val issuance = args.issuance
                shareFile(
                    //TODO: move date to fun
                    exportUtil.exportIssuance(
                        issuance, "issuance_${issuance.from}_${issuance.to}_${
                            issuance.date.replace(":", ".")
                        }" + "_${
                            SimpleDateFormat("dd.MM.yyyy_HH.mm").format(
                                Date()
                            )
                        }_export"
                    )
                )
            } ?: run { showDefaultErrorMessage() }
            true
        }

        R.id.share -> {
            arguments?.let { bundle ->
                val args = ViewIssuanceFragmentArgs.fromBundle(bundle)
                val issuance = args.issuance
                shareIssuanceTXT(issuance)
            } ?: run { showDefaultErrorMessage() }
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun shareIssuanceTXT(item: Issuance) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "${item.from} -> ${item.to}\n" + "${item.date} " + "\n${viewModel.selectedItems.map { it.title + "\n" }}".replace(
                "[", ""
            ).replace("]", "").replace(",", "") + "\n\n${item.notes}"
        )
        startActivity(Intent.createChooser(shareIntent, "Надіслати"))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

    private fun updateAdapterItems() {
        adapter.items =
            viewModel.selectedItems.toList().mapNotNull { item1 -> item1.toCommonItem() }
                .sortedBy { it.title }
    }

}
