package com.example.instagram.tab_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instaapp.Adapter.MyPostAdapter
import com.example.instaapp.Model.Post
import com.example.instagram.R
import com.example.instagram.databinding.FragmentSavedPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SavedPostFragment : Fragment() {
   private val binding by lazy {
       FragmentSavedPostBinding.inflate(layoutInflater)
   }
    var postListSaved: List<Post>? = null
    var myImagesAdapterSavedImg: MyPostAdapter? = null
    var mySavedImg: List<String>? = null
    private lateinit var firebaseUser: FirebaseUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        mySavedImg = mutableListOf()

        binding.recyclerViewSavedPicMainUser.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerViewSavedPicMainUser.layoutManager = linearLayoutManager2

        postListSaved = ArrayList()
        myImagesAdapterSavedImg =
           requireContext().let { MyPostAdapter(it, postListSaved as ArrayList<Post>) }
       binding.recyclerViewSavedPicMainUser.adapter = myImagesAdapterSavedImg

        mySaves()
        return binding.root
    }
    private fun mySaves() {

        mySavedImg = ArrayList()
        val savesRef =
            FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
        savesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (pO in snapshot.children) {
                        (mySavedImg as ArrayList<String>).add(pO.key!!)
                    }

                    readSavedImagesData()//Following is thr function to get the details of the saved posts
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
    private fun readSavedImagesData() {
        val PostsRef = FirebaseDatabase.getInstance().reference.child("Posts")
        PostsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (datasnapshot.exists()) {
                    (postListSaved as ArrayList<Post>).clear()

                    for (snapshot in datasnapshot.children) {
                        val post = snapshot.getValue(Post::class.java)

                        for (key in mySavedImg!!) {
                            if (post!!.postid == key) {
                                (postListSaved as ArrayList<Post>).add(post!!)
                            }
                        }
                    }
                    myImagesAdapterSavedImg!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}