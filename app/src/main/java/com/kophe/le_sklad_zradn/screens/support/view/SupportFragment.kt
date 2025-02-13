package com.kophe.le_sklad_zradn.screens.support.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.kophe.le_sklad_zradn.databinding.FragmentSupportBinding
import com.kophe.le_sklad_zradn.screens.common.BaseFragment
import com.kophe.leskladlib.logging.LoggingUtil
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class SupportFragment : BaseFragment<FragmentSupportBinding>() {

    @Inject
    lateinit var loggingUtil: LoggingUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSupportBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.sendLogs?.setOnClickListener { sendLogs() }
//        binding?.adminButton?.setOnClickListener { navigate(actionSupportFragmentToAdminFragment()) }
    }

    private fun sendLogs() {
        loggingUtil.log("sendLogs()")
        loggingUtil.log("support message: ${binding?.supportMessageEditText?.text.toString()}")
        shareFile(loggingUtil.logFile())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.postInvalidate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            navigateUp()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

}
