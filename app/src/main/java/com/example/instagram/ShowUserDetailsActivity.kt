package com.example.instagram

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.instaapp.Model.Post
import com.example.instaapp.Model.User
import com.example.instagram.adapter.TabLayoutAdapterSecond
import com.example.instagram.databinding.ActivityShowUserDetailsBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ShowUserDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShowUserDetailsBinding
    private lateinit var firebaseUser: FirebaseUser
    var postList: List<Post>? = null
    private lateinit var tabLayoutAdapter: TabLayoutAdapterSecond
    private var profileId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        profileId = intent.getStringExtra("id")

        postList = mutableListOf()



        binding.backButtonUserDetails.setOnClickListener {
            onBackPressed()
        }

        // tabLayout code editor
        tabLayoutAdapter = TabLayoutAdapterSecond(this)
        binding.viewPagerTabLayout.adapter = tabLayoutAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPagerTabLayout) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.grid)
                1 -> tab.setIcon(R.drawable.story_instagram_video_photo_icon)
                2 -> tab.setIcon(R.drawable.save_large_icon)
            }
        }.attach()


        binding.totalFollowers.setOnClickListener {
            val intent = Intent(this, ShowUsersActivity::class.java)
            intent.putExtra("id", firebaseUser.uid)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }
        binding.totalFollowing.setOnClickListener {
            val intent = Intent(this, ShowUsersActivity::class.java)
            intent.putExtra("id", firebaseUser.uid)
            intent.putExtra("title", "following")
            startActivity(intent)
        }


        binding.editProfileFollowInUfollow.setOnClickListener {
            if (binding.editProfileFollowInUfollow.text.toString() == "Follow") {
                profileId.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(profileId!!)
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                profileId.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(profileId!!)
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                //  pushNotification(user.getUid())
                                            }
                                        }
                                }
                            }
                        }
                }
            } else {
                if (binding.editProfileFollowInUfollow.text.toString() == "Following") {
                    profileId.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId!!)
                            .removeValue()
                            .addOnCompleteListener { task -> //reversing following action
                                if (task.isSuccessful) {
                                    profileId.let { it1 ->
                                        FirebaseDatabase.getInstance().reference
                                            .child("Follow").child(profileId!!)
                                            .child("Followers").child(it1.toString())
                                            .removeValue().addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                }
                                            }
                                    }
                                }
                            }
                    }
                }
            }
        }

        getUserInfo()
        checkFollowOrFollowingButtonStatus()
        getFollowers()
        getFollowing()
        getNoofPosts()

    }

    private fun getUserInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId!!)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.profileImageProfileReciveUser)
                    binding.profileToolbarUsername.text = user.getUsername()
                    binding.fullnameInProfileReciveUsername.text = user.getFullname()
                    binding.usernameInProfileProfileuserName.text = user.getUsername()
                    binding.bioProfileReceiveUserBio.text = user.getBio()

                }
            }
        })
    }

    private fun checkFollowOrFollowingButtonStatus() {

        val followingRef = profileId.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(profileId!!).exists()) {
                        binding.editProfileFollowInUfollow.text = "Following"
                    } else {
                        binding.editProfileFollowInUfollow.text = "Follow"
                    }
                }
            })
        }
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId!!)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.totalFollowers.text = snapshot.childrenCount.toString()
                }
            }
        })
    }

    private fun getFollowing() {
        val followingsRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId!!)
            .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    binding.totalFollowing.text = snapshot.childrenCount.toString()
                }
            }
        })
    }

    private fun getNoofPosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                var i: Int = 0
                for (snapshot in p0.children) {
                    val post = snapshot.getValue(Post::class.java)
                    if (post!!.publisher.equals(profileId)) {
                        i = i + 1
                    }
                }
                binding.totalPosts.text = "" + i
            }
        })
    }

}