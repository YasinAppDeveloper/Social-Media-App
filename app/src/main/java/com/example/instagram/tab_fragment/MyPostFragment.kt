package com.example.instagram.tab_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Adapter.MyPostAdapter
import com.example.instaapp.Model.Post
import com.example.instagram.R
import com.example.instagram.databinding.FragmentMyPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections


class MyPostFragment : Fragment() {
    var myPostAdapter: MyPostAdapter? = null
    var postListSaved: List<Post>? = null
    var postList: List<Post>? = null
    private lateinit var firebaseUser: FirebaseUser
    private val binding by lazy {
        FragmentMyPostBinding.inflate(layoutInflater)
    }
    private var userId:String?= null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        postList = mutableListOf()

        userId = activity?.intent?.getStringExtra("")

        binding.recyclerviewProfileMainUser.setHasFixedSize(true)
        binding.recyclerviewProfileMainUser.layoutManager =  GridLayoutManager(context,3,
            GridLayoutManager.VERTICAL,false)
        postList=ArrayList()
        myPostAdapter= context?.let { MyPostAdapter(it, postList as ArrayList<Post>) }
        binding.recyclerviewProfileMainUser.adapter=myPostAdapter
        myPosts()
        return binding.root
    }
    private fun myPosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                (postList as ArrayList<Post>).clear()
                for (snapshot in p0.children) {
                    val post = snapshot.getValue(Post::class.java)
                    if (post!!.publisher.equals(firebaseUser.uid))
                        (postList as ArrayList<Post>).add(post)
                }
                Collections.reverse(postList)
                myPostAdapter!!.notifyDataSetChanged()
            }
        })
    }
}