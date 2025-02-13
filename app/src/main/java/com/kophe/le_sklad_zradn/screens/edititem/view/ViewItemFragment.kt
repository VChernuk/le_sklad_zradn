package com.kophe.le_sklad_zradn.screens.edititem.view

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
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentViewItemBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent
import com.kophe.le_sklad_zradn.screens.edititem.view.ViewItemFragmentDirections.actionViewItemFragmentToImageViewerFragment
import com.kophe.le_sklad_zradn.screens.edititem.view.ViewItemFragmentDirections.actionViewItemFragmentToViewIssuanceFragment
import com.kophe.leskladuilib.edititem.adapter.CommonItemAdapter
import com.kophe.leskladuilib.edititem.adapter.ItemImagesAdapter
import com.kophe.le_sklad_zradn.screens.edititem.viewmodel.EditItemViewModel
import com.kophe.le_sklad_zradn.util.export.ExportUtil
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.Issuance
import com.kophe.leskladlib.repository.common.Item
import com.kophe.leskladlib.repository.common.ItemImage
import com.kophe.leskladuilib.OnItemSelectedListener
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

//TODO: recycler width
class ViewItemFragment : BaseViewModelFragment<FragmentViewItemBinding, EditItemViewModel>(),
    OnItemSelectedListener<CommonItem> {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    @Inject
    lateinit var exportUtil: ExportUtil

    private val historyAdapter by lazy { CommonItemAdapter(this) }
    private val imagesAdapter by lazy {
        ItemImagesAdapter(object : OnItemSelectedListener<ItemImage> {
            override fun selectItem(item: ItemImage) {
                val images = viewModel.currentItem.images?.toTypedArray() ?: return
                navigate(
                    actionViewItemFragmentToImageViewerFragment(images, images.indexOf(item))
                )
            }
        })
    }

    override fun createViewModel() = ViewModelProvider(this, factory)[EditItemViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
        viewModel.start()
        viewModel.viewModelEvent.observe(this) {
            (it as? ViewModelEvent.InfoAvailable<Issuance>)?.let { event ->
                navigate(actionViewItemFragmentToViewIssuanceFragment(event.item))
            }
        }
    }

    private fun viewModelSubscriptions() {
        viewModel.images.observe(viewLifecycleOwner) { imagesAdapter.items = it }
        observeRequestStatus()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentViewItemBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    //Set up UI components and handle user input
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelSubscriptions()
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.photosView?.addImagesButton?.visibility = GONE
        binding?.photosView?.addGalleryImagesButton?.visibility = GONE
        setHasOptionsMenu(true)

        arguments?.let { bundle ->
            val args = ViewItemFragmentArgs.fromBundle(bundle)
            val item = args.item
            fillFields(item)

            baseActivity?.setTitle(item.titleString())
        }
        binding?.photosView?.imagesRecycler?.adapter = imagesAdapter
        binding?.historyRecyclerView?.adapter = historyAdapter
    }

    private fun fillFields(item: Item) {
        setupOptionalInfo(item.barcode, binding?.labelBarcode, binding?.barcode)
        setupOptionalInfo(item.id, binding?.labelID, binding?.ID)
        setupOptionalInfo(item.title, binding?.labelTitle, binding?.title)
        setupOptionalInfo(item.location?.title, binding?.labelLocation, binding?.location)
        setupOptionalInfo(item.responsibleUnit?.title, binding?.labelUnit, binding?.unit)
        setupOptionalInfo(item.sn, binding?.labelSn, binding?.sn)
        setupOptionalInfo(item.category?.title, binding?.labelCategory, binding?.category)

        if (item.subcategories.isEmpty()) {
            binding?.subcategory?.visibility = GONE
            binding?.labelSubCategory?.visibility = GONE
        } else {
            binding?.subcategory?.text = item.subcategories.joinToString { it.title }
        }

        setupOptionalInfo(item.notes, binding?.labelComment, binding?.comment)
        setupOptionalInfo(item.ownershipType?.title, binding?.labelOwnership, binding?.ownership)
        setupOptionalInfo(item.sublocation?.title, binding?.labelSublocation, binding?.sublocation)

        if (item.images.isNullOrEmpty()) {
            binding?.photosView?.layoutPhotos?.visibility = GONE
        } else {
            imagesAdapter.items = item.images!!
        }

        if (item.history.isEmpty()) {
            binding?.historyRecyclerView?.visibility = GONE
            binding?.labelHistory?.visibility = GONE
        } else {
            historyAdapter.items = item.history
        }
    }

    private fun setupOptionalInfo(info: String?, title: TextView?, label: TextView?) {
        if (info.isNullOrEmpty()) {
            title?.visibility = GONE
            label?.visibility = GONE
        } else {
            label?.text = info
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.share_txt_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) { // Check the ID of the selected item
            android.R.id.home -> { // If it's the home button
                navigateUp()
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

            else -> super.onOptionsItemSelected(item) // For all other cases, call the superclass method
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
        loggingUtil.log("${loggingTag()} selectItem(...) item: $item")
        viewModel.getIssuanceInfo(item)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

}
