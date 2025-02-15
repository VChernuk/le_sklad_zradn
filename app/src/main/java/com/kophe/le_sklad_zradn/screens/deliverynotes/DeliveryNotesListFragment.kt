//package com.kophe.le_sklad_zradn.screens.deliverynotes
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.kophe.le_sklad_zradn.R
//import com.kophe.leskladlib.loggingTag
//import com.kophe.leskladlib.logging.LoggingUtil
//import com.kophe.leskladuilib.deliverynotes.adapter.DeliveryNotesAdapter
//
//class DeliveryNotesListFragment : Fragment() {
//
//    private lateinit var recyclerView: RecyclerView
//    // Предположим, что у вас есть адаптер DeliveryNoteAdapter и модель DeliveryNote
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_delivery_notes_list, container, false)
//        recyclerView = view.findViewById(R.id.recyclerViewDeliveryNotes)
//        // Настройте RecyclerView, адаптер и данные здесь
//
//        return view
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewDeliveryNotes)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//
//        // Устанавливаем пустой адаптер сразу
//        val deliveryNotesAdapter = DeliveryNotesAdapter(emptyList()) { deliveryNote ->
//            // Обработка клика
//        }
//        recyclerView.adapter = deliveryNotesAdapter
//
//        // Загружаем накладные
//        loadDeliveryNotes()
//    }
//
//    fun loadDeliveryNotes() {
//        loggingUtil.log("${loggingTag()} Загружаем накладные...")
//
//        getDeliveryNotesFromFirestore { deliveryNotes ->
//            loggingUtil.log("${loggingTag()} Загружено ${deliveryNotes.size} накладных")
//
//            deliveryNotesAdapter = DeliveryNotesAdapter(deliveryNotes) { deliveryNote ->
//                loggingUtil.log("${loggingTag()} Клик по накладной: ${deliveryNote.dn_number}")
//            }
//            recyclerView.adapter = deliveryNotesAdapter
//            deliveryNotesAdapter.notifyDataSetChanged()
//        }
//    }
//
//}
//
//}
package com.kophe.le_sklad_zradn.screens.deliverynotes

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.le_sklad_zradn.R
import com.kophe.leskladuilib.deliverynotes.adapter.DeliveryNotesAdapter
import com.kophe.leskladlib.repository.common.DeliveryNote
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class DeliveryNotesListFragment : Fragment() {

    @Inject
    lateinit var loggingUtil: LoggingUtil

    private lateinit var recyclerView: RecyclerView
    private lateinit var deliveryNotesAdapter: DeliveryNotesAdapter

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this) // Внедрение зависимостей
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
            loggingUtil.log("${loggingTag()} Clicl on delivery note: ${deliveryNote.dn_number}")
        }
        recyclerView.adapter = deliveryNotesAdapter

        loggingUtil.log("${loggingTag()} Call loadDeliveryNotes()")
        loadDeliveryNotes()
    }

    private fun loadDeliveryNotes() {
        loggingUtil.log("${loggingTag()} Loading delivery notes...")

        getDeliveryNotesFromFirestore { deliveryNotes: List<DeliveryNote> ->
            loggingUtil.log("${loggingTag()} Loaded ${deliveryNotes.size} delivery notes")

            deliveryNotesAdapter = DeliveryNotesAdapter(deliveryNotes) { deliveryNote ->
                loggingUtil.log("${loggingTag()} Click on delivery note: ${deliveryNote.dn_number}")
            }
            recyclerView.adapter = deliveryNotesAdapter
            deliveryNotesAdapter.notifyDataSetChanged()
        }
    }

    private fun getDeliveryNotesFromFirestore(callback: (List<DeliveryNote>) -> Unit) {
        // TODO: Реализовать получение данных из Firestore
        callback(emptyList())
    }
}
