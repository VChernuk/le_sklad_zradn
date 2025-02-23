package com.kophe.le_sklad_zradn.screens.deliverynotes.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kophe.le_sklad_zradn.R
import com.kophe.le_sklad_zradn.screens.deliverynotes.viewmodel.DeliveryNotesViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladuilib.deliverynotes.adapter.DeliveryNotesAdapter
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class DeliveryNotesListFragment : Fragment() {

    @Inject
    lateinit var loggingUtil: LoggingUtil

    private lateinit var recyclerView: RecyclerView
    private lateinit var deliveryNotesAdapter: DeliveryNotesAdapter

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel: DeliveryNotesViewModel by lazy {
        ViewModelProvider(this, factory)[DeliveryNotesViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_delivery_notes_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewDeliveryNotes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        deliveryNotesAdapter = DeliveryNotesAdapter(emptyList()) { deliveryNote ->
            loggingUtil.log("${loggingTag()} Clicked on delivery note: ${deliveryNote.number}")
        }
        recyclerView.adapter = deliveryNotesAdapter

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.deliveryNotes.observe(viewLifecycleOwner, Observer { deliveryNotes ->
            loggingUtil.log("${loggingTag()} Loaded ${deliveryNotes.size} delivery notes")

            deliveryNotesAdapter = DeliveryNotesAdapter(deliveryNotes) { deliveryNote ->
                loggingUtil.log("${loggingTag()} Clicked on delivery note: ${deliveryNote.number}")
            }
            recyclerView.adapter = deliveryNotesAdapter
        })
    }
}
