package com.kophe.le_sklad_zradn.screens.admin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.databinding.FragmentScriptsBinding
import com.kophe.le_sklad_zradn.screens.admin.view.ScriptsFragmentDirections.actionScriptsFragmentToBackupsFragment
import com.kophe.le_sklad_zradn.screens.admin.viewmodel.AdminViewModel
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class ScriptsFragment : BaseViewModelFragment<FragmentScriptsBinding, AdminViewModel>() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun createViewModel() = ViewModelProvider(this, factory)[AdminViewModel::class.java]

    @Inject
    lateinit var loggingUtil: LoggingUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelSubscriptions()
        binding?.restoreButton?.setOnClickListener { navigate(actionScriptsFragmentToBackupsFragment()) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentScriptsBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun viewModelSubscriptions() {
        loggingUtil.log("${loggingTag()} viewModelSubscriptions(...)")
        viewModel.viewModelEvent.observe(viewLifecycleOwner) {
            if (it != ViewModelEvent.Main) return@observe
            showActionBar()
            navigateUp()
        }
    }

}
