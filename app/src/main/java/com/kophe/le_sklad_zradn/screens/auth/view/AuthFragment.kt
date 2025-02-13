package com.kophe.le_sklad_zradn.screens.auth.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.databinding.FragmentAuthBinding
import com.kophe.le_sklad_zradn.screens.auth.view.AuthFragmentDirections.actionAuthFragmentToMainFragment
import com.kophe.le_sklad_zradn.screens.auth.viewmodel.AuthViewModel
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Main
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class AuthFragment : BaseViewModelFragment<FragmentAuthBinding, AuthViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentAuthBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
        viewModelSubscriptions()
        viewModel.start()
    }

    override fun createViewModel() = ViewModelProvider(this, factory)[AuthViewModel::class.java]

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeRequestStatus()
    }

    private fun viewModelSubscriptions() {
        loggingUtil.log("${loggingTag()} viewModelSubscriptions(...)")
        viewModel.viewModelEvent.observe(this) {
            if (it != Main) return@observe
            showActionBar()
            navigate(actionAuthFragmentToMainFragment())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        loggingUtil.log("${loggingTag()} onConfigurationChanged(...)")
        binding?.viewModel = viewModel
    }

}
