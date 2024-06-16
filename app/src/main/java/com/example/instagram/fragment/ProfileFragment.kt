package com.example.instagram.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Adapter.MyPostAdapter
import com.example.instaapp.Adapter.MyReelAdapter
import com.example.instaapp.Model.Post
import com.example.instaapp.Model.User
import com.example.instagram.AccountSettingActivity
import com.example.instagram.R
import com.example.instagram.ShowUsersActivity
import com.example.instagram.adapter.TabLayoutAdapter
import com.example.instagram.data.ReelData
import com.example.instagram.databinding.ProfileFragmentBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Collections

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {

    private val binding by lazy {
        ProfileFragmentBinding.inflate(layoutInflater)
    }
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var tabLayoutAdapter: TabLayoutAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
        setBottomNavigationViewAppearance(true)

        tabLayoutAdapter = TabLayoutAdapter(requireActivity())
        binding.viewPagerTabLayout.adapter = tabLayoutAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPagerTabLayout) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.grid)
                1 -> tab.setIcon(R.drawable.story_instagram_video_photo_icon)
                2 -> tab.setIcon(R.drawable.save_large_icon)
            }
        }.attach()

        //to call account profile setting activity
        binding.editProfileButton.setOnClickListener {
            val getButtontext = binding.editProfileButton.text.toString()
            startActivity(Intent(requireContext(), AccountSettingActivity::class.java))

        }

        binding.totalFollowers.setOnClickListener {
            val intent = Intent(requireContext(), ShowUsersActivity::class.java)
            intent.putExtra("id", firebaseUser.uid)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }
        binding.totalFollowing.setOnClickListener {
            val intent = Intent(requireContext(), ShowUsersActivity::class.java)
            intent.putExtra("id", firebaseUser.uid)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        getFollowers()
        getFollowing()
        getUserInfo()
        getNoofPosts()

        return binding.root

    }

    private fun checkFollowOrFollowingButtonStatus() {

        val followingRef = firebaseUser.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.child(firebaseUser.uid).exists()) {
                        binding.editProfileButton.text = "Following"
                    } else {
                        binding.editProfileButton.text = "Follow"
                    }
                }
            })
        }
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(firebaseUser.uid)
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
            .child("Follow").child(firebaseUser.uid)
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




    private fun getUserInfo() {
        val usersRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(binding.profileImageProfile)
                    binding.profileToolbarUsername.text = user.getUsername()
                    binding.fullnameInProfile.text = user.getFullname()
                    binding.usernameInProfile.text = user.getUsername()
                    binding.bioProfile.text = user.getBio()

                }
            }
        })
    }

    private fun setBottomNavigationViewAppearance(isVideoFragment: Boolean) {
        if (isVideoFragment) {
            bottomNavigationView.setBackgroundColor(Color.WHITE)
            bottomNavigationView.itemIconTintList = resources.getColorStateList(R.color.black, null)
            bottomNavigationView.itemTextColor = resources.getColorStateList(R.color.black, null)
        } else {
            // Revert to the default appearance
            bottomNavigationView.setBackgroundColor(
                resources.getColor(
                    R.color.default_background_color,
                    null
                )
            )
            bottomNavigationView.itemIconTintList =
                resources.getColorStateList(R.color.default_item_color, null)
            bottomNavigationView.itemTextColor =
                resources.getColorStateList(R.color.default_item_color, null)
        }
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
                    if (post!!.publisher.equals(firebaseUser.uid)) {
                        i = i + 1
                    }
                }
                binding.totalPosts.text = "" + i
            }
        })
    }
}