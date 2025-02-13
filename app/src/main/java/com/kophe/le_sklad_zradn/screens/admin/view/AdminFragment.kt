package com.kophe.le_sklad_zradn.screens.admin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.databinding.FragmentAdminBinding
import com.kophe.le_sklad_zradn.screens.admin.view.AdminFragmentDirections.actionAdminFragmentToCategoriesFragment
import com.kophe.le_sklad_zradn.screens.admin.view.AdminFragmentDirections.actionAdminFragmentToLocationsFragment
import com.kophe.le_sklad_zradn.screens.admin.view.AdminFragmentDirections.actionAdminFragmentToScriptsFragment
import com.kophe.le_sklad_zradn.screens.common.BaseFragment
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class AdminFragment : BaseFragment<FragmentAdminBinding>() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var loggingUtil: LoggingUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.scriptsButton?.setOnClickListener { navigate(actionAdminFragmentToScriptsFragment()) }
        binding?.locationsButton?.setOnClickListener {
            navigate(actionAdminFragmentToLocationsFragment())
        }
        binding?.categoriesButton?.setOnClickListener {
            navigate(actionAdminFragmentToCategoriesFragment())
        }
        binding?.adminUsersButton?.isEnabled = false
        //setOnClickListener {
//        navigate(actionAdminFragmentToAdminUsersFragment())
//    }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentAdminBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

}
