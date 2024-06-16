package com.example.instaapp.Adapter

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Model.Post
import com.example.instagram.R
import com.example.instagram.data.ReelData
import com.example.instagram.fragment.PostDetailFragment
import com.squareup.picasso.Picasso
import java.net.URL

class MyReelAdapter(private val mContext: Context, private  val mPost:List<ReelData>): RecyclerView.Adapter<MyReelAdapter.ViewHolder>() {

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var videoView: VideoView
        init
        {
            videoView = itemView.findViewById(R.id.my_reel)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(mContext).inflate(R.layout.myreel_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post=mPost[position]
        val url:Uri = Uri.parse(post.videoUrl)
         holder.videoView.setVideoURI(url)
        holder.videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true // Loop the video
            mediaPlayer.setVolume(0f, 0f) // Mute the video
            holder.videoView.start() // Start playing the video
        }

        holder.videoView.setOnCompletionListener { mediaPlayer ->
            holder.videoView.start() // Restart the video when it completes
        }
    }
}