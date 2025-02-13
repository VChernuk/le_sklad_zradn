package com.kophe.le_sklad_zradn.screens.admin.createcategory.view

import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentCreateCategoryBinding
import com.kophe.leskladuilib.admin.createcategory.adapter.SubcategoriesAdapter
import com.kophe.le_sklad_zradn.screens.admin.createcategory.viewmodel.CreateCategoryViewModel
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import com.kophe.leskladlib.repository.common.Subcategory
import com.kophe.leskladuilib.OnItemSelectedListener
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject


class CreateCategoryFragment :
    BaseViewModelFragment<FragmentCreateCategoryBinding, CreateCategoryViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    private val adapter by lazy {
        SubcategoriesAdapter(object : OnItemSelectedListener<CommonItem> {
            override fun selectItem(item: CommonItem) = showEditSublocationDialog(item)
        }, object : OnItemSelectedListener<CommonItem> {
            override fun selectItem(item: CommonItem) = removeItem(item)
        }, true)
    }

    private fun removeItem(item: CommonItem) {
        viewModel.subItems.removeAll { it.title == item.title && it.id == item.firestoreId }
        updateAdapterItems()
        setupTitle()
    }

    override fun createViewModel() =
        ViewModelProvider(this, factory)[CreateCategoryViewModel::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
    }

    private fun viewModelSubscriptions() {
        viewModel.viewModelEvent.observe(viewLifecycleOwner) {
            if (it == ViewModelEvent.Done) {
                showToastMessage("Категорія сформована")
                navigateUp()
            }
        }
        observeRequestStatus()
    }

    override fun onResume() {
        super.onResume()
        setupTitle()
        setupBackKeyDialogHandler(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentCreateCategoryBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding?.issuanceItemsList?.adapter = adapter
        viewModelSubscriptions()
        viewModel.start()
        binding?.addSublocation?.setOnClickListener { showSublocationDialog() }
        updateAdapterItems()
        arguments?.let { bundle ->
            val args = CreateCategoryFragmentArgs.fromBundle(
                bundle
            )
            args.category?.let { category ->
                viewModel.category = category
                binding?.locationTitle?.setText(category.title)
                updateAdapterItems()
            }
        }
    }

    private fun setupTitle() {
        baseActivity?.setTitle("Категорія")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

    private fun updateAdapterItems() {
        adapter.items = viewModel.subItems.toList().map { CommonItem(it.title, it.id) }
    }

    private fun showEditSublocationDialog(item: CommonItem) {
        var text = item.title ?: return
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.edit_text_dialog, null)
        val input = dialogView.findViewById<EditText>(com.kophe.le_sklad_zradn.R.id.input)
        input.setText(text)
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                loggingUtil.log("after text changed")
                text = s?.toString() ?: ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loggingUtil.log("on text changed")
            }
        })
        val dialog = AlertDialog.Builder(requireContext()).setTitle("редагувати підкатегорію")
            .setCancelable(true).setView(dialogView).setPositiveButton("зберегти") { _, _ ->
                viewModel.subItems.removeAll { it.title == item.title && it.id == item.firestoreId }
                viewModel.subItems.add(Subcategory(text, item.firestoreId ?: "", 42))
                updateAdapterItems()
            }.setNegativeButton("відміна") { dialog, _ -> dialog?.dismiss() }.create()
        dialog.show()

        val color = resources.getColor(com.kophe.leskladuilib.R.color.colorAccent)
        dialog.getButton(BUTTON_NEGATIVE).setTextColor(color)
        dialog.getButton(BUTTON_POSITIVE).setTextColor(color)
    }

    private fun showSublocationDialog() {
        var text = ""
        val dialogView = requireActivity().layoutInflater.inflate(
            R.layout.edit_text_dialog, null
        )
        val input = dialogView.findViewById<EditText>(R.id.input)
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                loggingUtil.log("after text changed")
                text = s?.toString() ?: ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loggingUtil.log("on text changed")
            }
        })
        val dialog =
            AlertDialog.Builder(requireContext()).setTitle("нова підлокація").setCancelable(true)
                .setView(dialogView).setPositiveButton("Створити") { _, _ ->
                    viewModel.subItems.add(Subcategory(text, "", 42))
                    updateAdapterItems()
                }.setNegativeButton("Відміна") { dialog, _ -> dialog?.dismiss() }.create()
        dialog.show()

        val color = resources.getColor(com.kophe.leskladuilib.R.color.colorAccent)
        dialog.getButton(BUTTON_NEGATIVE).setTextColor(color)
        dialog.getButton(BUTTON_POSITIVE).setTextColor(color)
    }
}
