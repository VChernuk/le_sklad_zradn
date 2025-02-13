package com.kophe.le_sklad_zradn.screens.deliverynotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.kophe.le_sklad_zradn.R

class DeliveryNotesListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    // Предположим, что у вас есть адаптер DeliveryNoteAdapter и модель DeliveryNote

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_delivery_notes_list, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewDeliveryNotes)
        // Настройте RecyclerView, адаптер и данные здесь
        return view
    }
}
