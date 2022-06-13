package com.intermediate.submission1.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.intermediate.submission1.data.models.Story
import com.intermediate.submission1.databinding.ItemRowStoryBinding
import com.intermediate.submission1.ui.home.HomeFragmentDirections

class ListStoryAdapter : PagingDataAdapter<Story, ListStoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    class ViewHolder(private val binding: ItemRowStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: Story) {
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .apply(RequestOptions().override(550, 550))
                .into(binding.imgItemPhoto)

            binding.imgItemPhoto.transitionName = "image${story.id}"
            binding.tvItemName.transitionName = "name${story.id}"
            binding.tvItemDescription.transitionName = "description${story.id}"

            binding.tvItemName.text = story.name
            binding.tvItemDescription.text = story.description
            itemView.setOnClickListener {
                val toStoryDetailFragment =
                    HomeFragmentDirections.actionHomeFragmentToStoryDetailFragment(story)
                val extras = FragmentNavigatorExtras(
                    binding.imgItemPhoto to "image${story.id}",
                    binding.tvItemName to "name${story.id}",
                    binding.tvItemDescription to "description${story.id}"
                )
                it.findNavController().navigate(toStoryDetailFragment, extras)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemRowStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}