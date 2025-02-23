package com.kophe.le_sklad_zradn.screens.deliverynotes.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kophe.le_sklad_zradn.databinding.FragmentCreateDeliveryNoteBinding
import com.kophe.le_sklad_zradn.screens.deliverynotes.viewmodel.CreateDeliveryNoteViewModel
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladlib.repository.common.CommonItem
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class CreateDeliveryNoteFragment : Fragment() {


    @Inject
    lateinit var loggingUtil: LoggingUtil

    private var _binding: FragmentCreateDeliveryNoteBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private val viewModel: CreateDeliveryNoteViewModel by lazy {
        ViewModelProvider(this, factory)[CreateDeliveryNoteViewModel::class.java]
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateDeliveryNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSave.setOnClickListener {
            val number = binding.etNumber.text.toString()
            val date = binding.etDate.text.toString()
            val toLocationId = binding.etToLocationId.text.toString()
            val toSublocationId = binding.etToSublocationId.text.toString()
            val responsiblePerson = binding.etResponsiblePerson.text.toString()
//            val items = listOf() // Добавь функционал выбора товаров
            val items = emptyList<CommonItem>()

            loggingUtil.log("${loggingTag()} Saving Delivery Note: $number")
            viewModel.createDeliveryNote(number, date, toLocationId, toSublocationId, responsiblePerson, items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
