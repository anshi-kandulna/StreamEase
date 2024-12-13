package com.example.streamease

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(
    private val videos: List<Video>,
    private val onClick: (String) -> Unit  // Add a click handler to pass the video URL
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    // ViewHolder class to hold references to the views using findViewById
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)

        fun bind(video: Video) {
            // Load the thumbnail image using Glide
            Glide.with(itemView.context)
                .load(video.image)  // Using the 'image' URL for thumbnail
                .into(thumbnailImageView)

            // Set the title (using the user's name)
            titleTextView.text = video.user.name  // Setting the user's name as title

            // Set up the click listener for the item
            itemView.setOnClickListener {
                // When the item is clicked, pass the video URL to the click handler
                onClick(video.video_files.firstOrNull()?.link ?: "") // Pass the first video file URL
            }
        }
    }

    // Create the view holder when the view is created
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    // Bind the data to the views
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    // Return the size of the list
    override fun getItemCount(): Int = videos.size
}
