package com.kophe.le_sklad_zradn.screens.main

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.GravityCompat.START
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.databinding.FragmentMainBinding
import com.kophe.le_sklad_zradn.screens.common.BaseViewModelFragment
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Admin
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Done
import com.kophe.le_sklad_zradn.screens.common.ViewModelEvent.Support
import com.kophe.le_sklad_zradn.screens.main.MainFragmentDirections.actionMainFragmentToAdminFragment
import com.kophe.le_sklad_zradn.screens.main.MainFragmentDirections.actionMainFragmentToSplashFragment
import com.kophe.le_sklad_zradn.screens.main.MainFragmentDirections.actionMainFragmentToSupportFragment
import com.kophe.le_sklad_zradn.screens.navdrawer.HomeNavigationViewModel
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class MainFragment : BaseViewModelFragment<FragmentMainBinding, HomeNavigationViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun createViewModel() =
        navGraphViewModels<HomeNavigationViewModel>(R.id.main) { factory }.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        viewModel.viewModelEvent.observe(this) {
            binding?.drawerLayout?.closeDrawer(START)
            when (it) {
                Done -> navigate(actionMainFragmentToSplashFragment())
                Support -> navigate(actionMainFragmentToSupportFragment())
                Admin -> navigate(actionMainFragmentToAdminFragment())
                else -> {}
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding = FragmentMainBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_main) as NavHostFragment
        val navController = navHostFragment.findNavController()
        binding?.bottomNavigationView?.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.itemsFragment || destination.id == R.id.issuanceFragment || destination.id == R.id.deliveryNoteFragment) {
                binding?.bottomNavigationView?.visibility = VISIBLE
            } else {
                binding?.bottomNavigationView?.visibility = GONE
            }
            binding?.drawerLayout?.closeDrawer(START)
        }

        baseActivity?.updNav(
            navController,
            AppBarConfiguration.Builder(R.id.itemsFragment, R.id.issuanceFragment, R.id.deliveryNoteFragment)
                .setDrawerLayout(binding?.drawerLayout).build()
        )

        viewModel.start()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.postInvalidate()
    }

}
