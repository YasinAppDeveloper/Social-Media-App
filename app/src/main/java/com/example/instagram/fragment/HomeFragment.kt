@file:Suppress("OVERRIDE_DEPRECATION")

package com.example.instagram.fragment

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instaapp.Adapter.PostAdapter
import com.example.instaapp.Model.Post
import com.example.instaapp.Model.User
import com.example.instagram.R
import com.example.instagram.data.ReelData
import com.example.instagram.databinding.HomeFragmentBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.prasoon.whatsappclone.adapters.StatusAdapter
import com.prasoon.whatsappclone.models.Status
import com.prasoon.whatsappclone.models.UserStatus
import com.squareup.picasso.Picasso

import java.util.Collections
import java.util.Date

class HomeFragment : Fragment() {
    private val binding by lazy {
        HomeFragmentBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private lateinit var bottomNavigationView: BottomNavigationView
    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<String>? = null
    private var user: User? = null
    private lateinit var dialog: ProgressDialog
    private lateinit var database: FirebaseDatabase
    private lateinit var statusList: ArrayList<UserStatus>
    private lateinit var statusAdapter: StatusAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        statusList = ArrayList()
        statusAdapter = StatusAdapter(requireActivity(), statusList)
        binding.recyclerViewStory.adapter = statusAdapter

        retrievePosts()

        val linearlayoutManager = LinearLayoutManager(requireContext())
        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().reference.child("videos")
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
        setBottomNavigationViewAppearance(true)

        publisherInfo(binding.storyImage)

        linearlayoutManager.reverseLayout = true
        linearlayoutManager.stackFromEnd = true
        binding.recyclerViewHome.layoutManager = linearlayoutManager
        //For Posts

        postList = ArrayList()
        postAdapter = PostAdapter(requireContext(), postList as ArrayList<Post>)
        binding.recyclerViewHome.adapter = postAdapter

//        feedAdapter = FeedAdapter(requireContext(), items)
//        binding.recyclerViewHome.adapter = feedAdapter
//        binding.recyclerViewHome.layoutManager = LinearLayoutManager(context)
//        feedAdapter.notifyDataSetChanged()


        getAllUser()

        database = FirebaseDatabase.getInstance()
        dialog = ProgressDialog(requireContext())
        dialog.setTitle("Upload Your Story")
        dialog.setMessage("Please wait...")
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        binding.storyImage.setOnClickListener {
            val intent = Intent().apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
            }
            startActivityForResult(intent, 75)
        }

        checkFollowings()
     //   retrievePosts()

        return binding.root
    }

    // get stories
    private fun retrieveStories() {
        //   retrieveStories()
        database.getReference().child("stories").addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    statusList.clear()
                    for (storySnapshot in snapshot.children) {
                        val userStatus = UserStatus().apply {
                            name = storySnapshot.child("name").getValue(String::class.java)
                            profileImage =
                                storySnapshot.child("profileImage").getValue(String::class.java)
                            lastUpdated =
                                storySnapshot.child("lastUpdated").getValue(Long::class.java)!!
                        }
                        Log.i("work_please", userStatus.name.toString())

                        val statuses = ArrayList<Status>()
                        for (statusSnapshot in storySnapshot.child("statuses").children) {
                            val sampleStatus = statusSnapshot.getValue(Status::class.java)
                            sampleStatus?.let {
                                statuses.add(it)
                                binding.progressLayout.visibility = View.VISIBLE
                                binding.loadDataProgressBar.visibility = View.GONE
                                Log.i("hellooo", "${it.imageUrl} ${it.timestamp}")
                            }
                        }
                        userStatus.statuses = statuses
                        statusList.add(userStatus)
                    }
                    statusAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    //to get the following List of logged-in user
    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (followingList as ArrayList<String>).clear() //to get previous data
                    for (snapshot in p0.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }
                    retrieveStories()
                }
            }
        })
    }

    // get User
    private fun getAllUser() {
        val userRef = FirebaseDatabase.getInstance().reference
        userRef.child("Users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                    //   Toast.makeText(requireContext(), ""+user?.getUsername(), Toast.LENGTH_SHORT).show()
                    //   Toast.makeText(requireContext(), ""+user?.getFullname(), Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    // get post
    private fun retrievePosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            @SuppressLint("NotifyDataSetChanged", "SuspiciousIndentation")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    postList?.clear()
                    for (snapshot in p0.children) {
                        val post = snapshot.getValue(Post::class.java)
                        postList!!.add(post!!)
                        binding.loadDataProgressBar.visibility = View.GONE
                        binding.recyclerViewHome.visibility = View.VISIBLE

                    }
                    Collections.shuffle(postList)
                    postAdapter!!.notifyDataSetChanged()
                }
            }

        })
    }

    //   upload status code start
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            data.data?.let { uri ->
                dialog.show()
                val storage = FirebaseStorage.getInstance()
                val date = Date()
                val reference = storage.reference.child("status").child(date.time.toString())
                reference.putFile(uri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnSuccessListener { downloadUri ->
                            val userStatus = UserStatus().apply {
                                profileImage = user?.getImage()
                                lastUpdated = date.time
                            }

                            val obj = hashMapOf(
                                "name" to userStatus.name,
                                "profileImage" to userStatus.profileImage,
                                "lastUpdated" to userStatus.lastUpdated
                            )

                            val status = Status(downloadUri.toString(), userStatus.lastUpdated)

                            database.reference.child("stories")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj as Map<String, Any>)

                            database.reference.child("stories")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .child("statuses")
                                .push()
                                .setValue(status)

                            dialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    // publisher
    private fun publisherInfo(imageView: ImageView) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(imageView)

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

    // get FeedAdapter data
}
