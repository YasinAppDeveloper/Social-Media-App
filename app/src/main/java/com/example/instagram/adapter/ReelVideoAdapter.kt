import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Model.Reel_Comment
import com.example.instaapp.Model.User
import com.example.instagram.AddCommentActivity
import com.example.instagram.R
import com.example.instagram.data.ReelData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

@SuppressLint("MissingInflatedId")
class ReelVideoAdapter(private val context: Context, private val data: List<ReelData>) :
    RecyclerView.Adapter<ReelVideoAdapter.VideoViewHolder>() {
     private lateinit var firebaseUser: FirebaseUser

    // Check if the user has liked the post
    private var commentsList = mutableListOf<Reel_Comment>()

    private var exoPlayer: ExoPlayer? = null

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerView: PlayerView = itemView.findViewById(R.id.exoPlayer)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressReel)
        val playPauseIcon: ImageView = itemView.findViewById(R.id.play_pause_icon)
        val likeButtonReel: ImageView = itemView.findViewById(R.id.reel_like_button)
        val playPauseButton: RelativeLayout = itemView.findViewById(R.id.play_pause_Layout)
        val likeCounterTv: TextView = itemView.findViewById(R.id.like_count_reel_tv)
        val reelMessageButton: ImageView = itemView.findViewById(R.id.reel_message_button)
        val userProfileImage:CircleImageView = itemView.findViewById(R.id.publisher_profile_image_reel)
        val userName:TextView = itemView.findViewById(R.id.userName_reel)
        val videoCaption:TextView=itemView.findViewById(R.id.reelCaption)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reel_video_item, parent, false)
        return VideoViewHolder(view)
    }

    @SuppressLint("UnsafeOptInUsageError", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val currentItem = data[position]
        holder.progressBar.visibility = View.VISIBLE
        holder.videoCaption.text = currentItem.caption
        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(data[position].videoId!!)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        getLikesOfReel(holder.likeCounterTv, likesRef)

         getUserDetails(holder.userProfileImage,holder.userName,currentItem)

            holder.reelMessageButton.setOnClickListener {
                val intent = Intent(context,AddCommentActivity::class.java)
                intent.putExtra("POST_ID",data[position].videoId)
                intent.putExtra("url",data[position].videoUrl)
                context.startActivity(intent)
            }

        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(firebaseUser!!.uid).exists()) {
                    // User has liked the post
                    holder.likeButtonReel.setImageResource(R.drawable.heart_clicked)
                    holder.likeButtonReel.tag = "liked"
                } else {
                    // User has not liked the post
                    holder.likeButtonReel.setImageResource(R.drawable.heart_repo)
                    holder.likeButtonReel.tag = "like"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        try {
            // Set click listener for like button
            holder.likeButtonReel.setOnClickListener {
                if (holder.likeButtonReel.tag == "like") {
                    likesRef.child(firebaseUser.uid).setValue(true)
                    holder.likeButtonReel.setImageResource(R.drawable.heart_clicked)
                    getLikesOfReel(holder.likeCounterTv, likesRef)
                } else {
                    likesRef.child(firebaseUser.uid).removeValue()
                    holder.likeButtonReel.setImageResource(R.drawable.heart_repo)
                    getLikesOfReel(holder.likeCounterTv, likesRef)
                }
            }
        } catch (e: Exception) {

        }


        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                repeatMode = ExoPlayer.REPEAT_MODE_ALL
                holder.progressBar.visibility = View.GONE
            }

        }


        val gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    // Toggle play/pause on double-tap
                    if (exoPlayer?.isPlaying == true) {
                        exoPlayer?.pause()
                        holder.playPauseIcon.setImageResource(R.drawable.ic_play)
                    } else {
                        exoPlayer?.play()
                        holder.playPauseIcon.setImageResource(R.drawable.ic_pause)
                    }
                    holder.playPauseIcon.visibility = View.VISIBLE
                    holder.playPauseIcon.postDelayed({
                        holder.playPauseIcon.visibility = View.GONE
                    }, 1000)
                    return true
                }
            })


        // Set touch listener to handle double-tap events
        holder.playPauseButton.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        holder.playerView.player = exoPlayer
        holder.playerView.controllerShowTimeoutMs = 0
        holder.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        // Disable default touch behavior that hides the controller
        holder.playerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                holder.playerView.showController()
                // Do nothing
            }
            true
        }
        exoPlayer?.setMediaItem(MediaItem.fromUri(currentItem.videoUrl!!))
        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true

        // Update progress bar visibility based on player state
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> holder.progressBar.visibility = View.VISIBLE
                    Player.STATE_READY -> holder.progressBar.visibility = View.GONE
                    Player.STATE_IDLE -> holder.progressBar.visibility = View.GONE
                    Player.STATE_ENDED -> holder.progressBar.visibility = View.GONE
                }
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                holder.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            override fun onPlayerError(error: PlaybackException) {
                holder.progressBar.visibility = View.GONE
            }
        })

    }

    private fun getUserDetails(userProfileImage: CircleImageView, userName: TextView, currentItem: ReelData) {
        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(currentItem.publisherId!!)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(userProfileImage)
                    userName.text = (user.getFullname())
                 //   publisher.text = (user.getUsername())

                }
            }

        })
    }

    private fun getLikesOfReel(likeCounterTv: TextView, likesRef: DatabaseReference) {
        // Add ValueEventListener to count the likes
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Count the number of likes
                val totalLikes = dataSnapshot.childrenCount.toInt()


                likeCounterTv.text = totalLikes.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun getItemCount(): Int = data.size

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
    fun pausePlayer(){
        exoPlayer?.pause()
        exoPlayer = null
    }

}
