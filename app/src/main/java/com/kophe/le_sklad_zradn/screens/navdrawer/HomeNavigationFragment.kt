package com.kophe.le_sklad_zradn.screens.navdrawer

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navGraphViewModels
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentHomeNavigationBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class HomeNavigationFragment :
    BaseViewModelFragment<FragmentHomeNavigationBinding, HomeNavigationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun createViewModel() =
        navGraphViewModels<HomeNavigationViewModel>(R.id.main) { factory }.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onActivityCreated(savedInstanceState)
        val binding = FragmentHomeNavigationBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = activity
        weakBinding = WeakReference(binding)
        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding?.viewModel = viewModel
        view?.postInvalidate()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.writeAvailable.observe(viewLifecycleOwner) {
            binding?.adminButton?.isVisible = it == true
        }
    }

}
