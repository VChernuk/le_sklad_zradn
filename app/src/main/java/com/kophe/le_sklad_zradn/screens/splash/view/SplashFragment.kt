package com.kophe.le_sklad_zradn.screens.splash.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.BuildConfig
import com.kophe.le_sklad_zradn.databinding.FragmentSplashBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Auth
import com.kophe.le_sklad_zradn.screens.splash.view.SplashFragmentDirections.actionSplashFragmentToAuthFragment
import com.kophe.le_sklad_zradn.screens.splash.view.SplashFragmentDirections.actionSplashFragmentToMainFragment
import com.kophe.le_sklad_zradn.screens.splash.viewmodel.SplashViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladuilib.fadeOut
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

class SplashFragment : BaseViewModelFragment<FragmentSplashBinding, SplashViewModel>() {

    @Inject
    lateinit var loggingUtil: LoggingUtil

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSplashBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        hideActionBar()
        viewModel.start()
    }

    override fun createViewModel() = ViewModelProvider(this, factory)[SplashViewModel::class.java]

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelSubscriptions()
    }

    private fun viewModelSubscriptions() {
        viewModel.viewModelEvent.observe(viewLifecycleOwner) {
            loggingUtil.log("${loggingTag()} view model event: $it")
            CoroutineScope(Dispatchers.Main).launch {
                val delayTime = BuildConfig.VERSION_CODE + 1200L
                delay(delayTime)
                when (it) {
                    ViewModelEvent.Main -> {
                        loggingUtil.log("${loggingTag()} main event")
                        binding?.splashLogo?.fadeOut()
                        delay(80)
                        showActionBar()
                        navigate(actionSplashFragmentToMainFragment())
                    }

                    Auth -> {
                        binding?.splashLogo?.fadeOut()
                        delay(90)
                        navigate(actionSplashFragmentToAuthFragment())
                    }

                    else -> { /* just ignore */
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        loggingUtil.log("${loggingTag()} onConfigurationChanged $newConfig")
        view?.postInvalidate()
    }

}
