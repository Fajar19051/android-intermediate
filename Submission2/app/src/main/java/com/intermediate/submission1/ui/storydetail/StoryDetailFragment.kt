package com.intermediate.submission1.ui.storydetail

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.intermediate.submission1.databinding.FragmentStoryDetailBinding
import com.intermediate.submission1.ui.main.MainActivity
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class StoryDetailFragment : Fragment() {
    private var _binding: FragmentStoryDetailBinding? = null
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStoryDetailBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showBottomNavigationBar(false)
    }

    private fun setupView() {
        val story = StoryDetailFragmentArgs.fromBundle(arguments as Bundle).story
        val df0: DateFormat = DateFormat.getTimeInstance()
        (activity as MainActivity).setActionBarTitle(story.name)
        getDateTime(story.createdAt)?.let { df0.format(it) }?.let {
            (activity as MainActivity).setActionBarSubtitle(it)
        }

        binding?.imgStoryPhoto?.transitionName = "image${story.id}"
        binding?.tvStoryDescription?.transitionName = "description${story.id}"

        Glide.with(this)
            .load(story.photoUrl)
            //.apply(RequestOptions().override(550, 550))
            .into(binding?.imgStoryPhoto as ImageView)
        binding?.tvStoryDescription?.text = story.description
    }

    private fun getDateTime(string: String): Date? {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(string)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).showBottomNavigationBar(true)
        (activity as MainActivity).setActionBarSubtitle()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}