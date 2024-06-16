package com.example.instaapp.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Model.Post
import com.example.instaapp.Model.User
import com.example.instagram.PostCommentsActivity
import com.example.instagram.R
import com.example.instagram.ShowUserDetailsActivity
import com.example.instagram.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit


class PostAdapter
    (private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    //code for events
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]
        val postid = post.postid

        Picasso.get().load(post.postimage).into(holder.postImage)
        holder.caption.text = post.caption
        publisherInfo(holder.profileImage, holder.username, holder.publisher, post.publisher)
        isLiked(post.postid, holder.likeButton, holder.postImage)
        isSaved(post.postid, holder.saveButton)
        getCountofLikes(post.postid, holder.likes)
        getComments(post.postid, holder.comments)
        val timeAgoString = getTimeAgo(post.time!!)
        holder.timeStamp.text = timeAgoString
        holder.publisher.setOnClickListener {
            val intent = Intent(mContext,ShowUserDetailsActivity::class.java)
            intent.putExtra("id",post.publisher)
            mContext.startActivity(intent)
        }


        holder.imageViewShare.setOnClickListener {
            shareUrl(mPost[position].postimage,mContext)
        }

        holder.username.setOnClickListener {
            val intent = Intent(mContext,ShowUserDetailsActivity::class.java)
            intent.putExtra("id",post.publisher)
            mContext.startActivity(intent)
        }

        holder.postImage.setOnClickListener {
            if (holder.postImage.tag.toString() == "like") {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.postid)
                    .child(firebaseUser!!.uid)
                    .setValue(true)
            } else {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.postid)
                    .child(firebaseUser!!.uid)
                    .removeValue()
            }

        }

        holder.profileImage.setOnClickListener {
          val intent = Intent(mContext,ShowUserDetailsActivity::class.java)
            intent.putExtra("id",post.publisher)
            mContext.startActivity(intent)
        }

        holder.publisher.setOnClickListener {
            val intent = Intent(mContext,ShowUserDetailsActivity::class.java)
            intent.putExtra("id",post.publisher)
            mContext.startActivity(intent)
        }

        holder.postImage.setOnClickListener {
            val intent = Intent(mContext,ShowUserDetailsActivity::class.java)
            intent.putExtra("id",post.publisher)
            mContext.startActivity(intent)
        }

        holder.likeButton.setOnClickListener {
            if (holder.likeButton.tag.toString() == "like") {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.postid)
                    .child(firebaseUser!!.uid)
                    .setValue(true)
                pushNotification(post.postid, post.publisher)
            } else {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.postid)
                    .child(firebaseUser!!.uid)
                    .removeValue()
            }
        }

        holder.comments.setOnClickListener {

            val intent = Intent(mContext, PostCommentsActivity::class.java).apply {
                putExtra("POST_ID", postid)
            }
            mContext.startActivity(intent)
        }

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", post.postid)
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        holder.commentButton.setOnClickListener {

            val intent = Intent(mContext, PostCommentsActivity::class.java).apply {
                putExtra("POST_ID", postid)
            }
            mContext.startActivity(intent)
        }

        holder.saveButton.setOnClickListener {
            if (holder.saveButton.tag == "Save") {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
                    .child(post.postid).setValue(true)
                Toast.makeText(mContext, "Post Saved", Toast.LENGTH_SHORT).show()

            } else {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
                    .child(post.postid).removeValue()
                Toast.makeText(mContext, "Post Unsaved", Toast.LENGTH_SHORT).show()
            }
        }
    }



    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var likes: TextView
        var comments: TextView
        var username: TextView
        var publisher: TextView
        var caption: TextView
        var timeStamp: TextView
        var imageViewShare:ImageView

        init {
            profileImage = itemView.findViewById(R.id.publisher_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            likes = itemView.findViewById(R.id.likes)
            comments = itemView.findViewById(R.id.comments)
            username = itemView.findViewById(R.id.publisher_user_name_post)
            publisher = itemView.findViewById(R.id.publisher)
            caption = itemView.findViewById(R.id.caption)
            timeStamp = itemView.findViewById(R.id.timePost_home)
            imageViewShare = itemView.findViewById(R.id.sharePostIamgeButton)

        }

    }

    private fun getComments(postid: String, comment: TextView) {

        val commentRef = FirebaseDatabase.getInstance().reference.child("Comment").child(postid)

        commentRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                comment.text = "View all " + datasnapshot.childrenCount.toString() + " comments"
            }
        })
    }
    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp // Milliseconds difference

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val months = days / 30
        val years = months / 12

        return when {
            years > 0 -> "$years years ago"
            months > 0 -> "$months months ago"
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "just now"
        }
    }
    private fun pushNotification(postid: String, userid: String) {

        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(userid)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
        notifyMap["text"] = "♥liked your post♥"
        notifyMap["postid"] = postid
        notifyMap["ispost"] = true

        ref.push().setValue(notifyMap)
    }


    private fun isLiked(postid: String, imageView: ImageView, postedImg: ImageView) {

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val postRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (datasnapshot.child(firebaseUser!!.uid).exists()) {
                    imageView.setImageResource(R.drawable.heart_clicked)
                    postedImg.tag = " liked"
                    imageView.tag = "liked"
                } else {
                    imageView.setImageResource(R.drawable.heart_not_clicked)
                    postedImg.tag = "like"
                    imageView.tag = "like"
                }
            }
        })
    }

    private fun getCountofLikes(postid: String, likesNo: TextView) {

        val postRef = FirebaseDatabase.getInstance().reference.child("Likes").child(postid)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                likesNo.text = datasnapshot.childrenCount.toString() + " likes"
            }
        })
    }

    private fun isSaved(postid: String, imageView: ImageView) {

        val savesRef =
            FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)

        savesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.saved_post_filled)
                    imageView.tag = "Saved"
                } else {
                    imageView.setImageResource(R.drawable.save_post_unfilled)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun publisherInfo(
        profileImage: CircleImageView,
        username: TextView,
        publisher: TextView,
        publisherID: String,
    ) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    username.text = (user.getUsername())
                    publisher.text = (user.getUsername())

                }
            }

        })
    }
    fun shareUrl(url: String, context: Context) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)

        val chooserTitle = "Share URL"
        val chooser = Intent.createChooser(intent, chooserTitle)

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "No app found to handle this action", Toast.LENGTH_SHORT).show()
        }
    }
}