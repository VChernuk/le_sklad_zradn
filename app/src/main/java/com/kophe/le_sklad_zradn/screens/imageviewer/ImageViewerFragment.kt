package com.kophe.le_sklad_zradn.screens.imageviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.kophe.le_sklad_zradn.databinding.FragmentImageViewerBinding
import com.kophe.le_sklad_zradn.screens.common.BaseFragment
import com.kophe.leskladlib.logging.LoggingUtil
import com.kophe.leskladlib.loggingTag
import com.kophe.leskladuilib.ImagesViewPagerAdapter
import dagger.android.support.AndroidSupportInjection
import java.lang.ref.WeakReference
import javax.inject.Inject

class ImageViewerFragment : BaseFragment<FragmentImageViewerBinding>() {

    @Inject
    lateinit var loggingUtil: LoggingUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        loggingUtil.log("${loggingTag()} onCreate(...)")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loggingUtil.log("${loggingTag()} onCreateView(...)")
        val binding = FragmentImageViewerBinding.inflate(inflater, container, false)
        weakBinding = WeakReference(binding)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            arguments?.let { bundle ->
                val args = ImageViewerFragmentArgs.fromBundle(bundle)
                binding?.selectionPager?.adapter =
                    object : ImagesViewPagerAdapter(it, args.images) {
                        override fun setImage(url: String, imageView: ImageView) {
                            val storageReference = Firebase.storage.getReferenceFromUrl(url)
                            Glide.with(imageView).load(storageReference).into(imageView)
                        }
                    }
                binding?.selectionPager?.setCurrentItem(args.index, true)
            }
        }
    }

}
