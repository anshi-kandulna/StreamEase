package com.example.streamease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(
    private val videos: MutableList<Video>,  // Change to MutableList to allow adding items
    private val onClick: (String, Int) -> Unit  // Pass video URL and position
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
    private var isLoading: Boolean = false

    // ViewHolder class to hold references to the views
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)

        fun bind(video: Video, position: Int) {
            // Load the thumbnail image using Glide
            Glide.with(itemView.context)
                .load(video.image)  // Using the 'image' URL for thumbnail
                .into(thumbnailImageView)

            // Set the title (using the user's name)
            titleTextView.text = video.user.name  // Setting the user's name as title

            // Handle click event to pass the video URL and position
            itemView.setOnClickListener {
                onClick(video.url, position)  // Pass the video URL and position
            }
        }
    }

    // ViewHolder for loading spinner
    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    // Create the view holder when the view is created
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
                VideoViewHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // Bind the data to the views
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VideoViewHolder -> {
                holder.bind(videos[position], position)
            }
            is LoadingViewHolder -> {
                // Show or hide progress bar based on the loading state
                holder.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    // Return the size of the list (including the loading item)
    override fun getItemCount(): Int {
        return videos.size + if (isLoading) 1 else 0
    }

    // Determine whether to show the loading spinner or a regular item
    override fun getItemViewType(position: Int): Int {
        return if (position == videos.size && isLoading) {
            VIEW_TYPE_LOADING
        } else {
            VIEW_TYPE_ITEM
        }
    }

    // Method to add more videos (e.g., for pagination or infinite scroll)
    fun addVideos(newVideos: List<Video>) {
        val currentPosition = videos.size
        videos.addAll(newVideos)
        notifyItemRangeInserted(currentPosition, newVideos.size)
    }

    // Show or hide loading spinner
    fun setLoading(loading: Boolean) {
        isLoading = loading
        // Notify the adapter that the loading view should be updated
        notifyItemChanged(videos.size - 1)
    }
}
