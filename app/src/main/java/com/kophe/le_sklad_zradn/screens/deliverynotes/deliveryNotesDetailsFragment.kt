package com.kophe.le_sklad_zradn.screens.deliverynotes

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kophe.le_sklad_zradn.R

class deliveryNotesDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = deliveryNotesDetailsFragment()
    }

    private val viewModel: DeliveryNotesDetailsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_delivery_note_item, container, false)
    }
}