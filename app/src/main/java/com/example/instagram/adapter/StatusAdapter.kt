package com.prasoon.whatsappclone.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.R
import com.example.instagram.databinding.ItemStatusBinding
import com.prasoon.whatsappclone.models.UserStatus
import com.squareup.picasso.Picasso
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import java.text.SimpleDateFormat

class StatusAdapter(
    private val context: Context,
    private val userStatuses: ArrayList<UserStatus>
) : RecyclerView.Adapter<StatusAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val userStatus = userStatuses[position]
        Log.i("links", userStatus.statuses.toString())

        val time = userStatus.lastUpdated
        val dateFormat = SimpleDateFormat("hh:mm a")

        val lastStatus = userStatus.statuses.last()
//        Glide.with(context).load(lastStatus.imageUrl).into(holder.binding.circleImageView)
        Picasso.get().load(lastStatus.imageUrl).into(holder.binding.circleImageView)
      //  holder.binding.userName.text = userStatus.name
      //  holder.binding.lastUpdated.text = "Last Updated : ${dateFormat.format(Date(time))}"
        holder.binding.circularStatusView.setPortionsCount(userStatus.statuses.size)
        holder.binding.circularStatusView.setOnClickListener {
            val myStories = ArrayList<MyStory>()
            for (status in userStatus.statuses) {
                myStories.add(MyStory(status.imageUrl))
                Log.i("links", status.imageUrl!!)
            }
            StoryView.Builder((context as AppCompatActivity).supportFragmentManager)
                .setStoriesList(myStories) // Required
                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                .setTitleText(userStatus.name) // Default is Hidden
                .setSubtitleText("") // Default is Hidden
                .setTitleLogoUrl(userStatus.profileImage) // Default is Hidden
                .setStoryClickListeners(object : StoryClickListeners {
                    override fun onDescriptionClickListener(position: Int) {
                        // your action
                    }

                    override fun onTitleIconClickListener(position: Int) {
                        // your action
                    }
                }) // Optional Listeners
                .build() // Must be called before calling show method
                .show()
        }
    }

    override fun getItemCount(): Int {
        return userStatuses.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemStatusBinding = ItemStatusBinding.bind(itemView)
    }
}
