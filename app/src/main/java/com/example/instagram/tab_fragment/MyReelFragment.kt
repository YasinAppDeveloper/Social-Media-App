package com.example.instagram.tab_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instaapp.Adapter.MyReelAdapter
import com.example.instaapp.Model.Post
import com.example.instagram.R
import com.example.instagram.data.ReelData
import com.example.instagram.databinding.FragmentMyReelBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections


class MyReelFragment : Fragment() {
    var postReelAdapter: MyReelAdapter? = null
    var listReel: List<ReelData>? = null
    private lateinit var firebaseUser: FirebaseUser
    private val binding by lazy {
        FragmentMyReelBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        binding.recyclerViewReelVideoMainUser.setHasFixedSize(true)
        val linearLayoutManager3: LinearLayoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerViewReelVideoMainUser.layoutManager = linearLayoutManager3

        listReel = ArrayList()
        postReelAdapter =
            requireContext().let { MyReelAdapter(it, listReel as ArrayList<ReelData>) }
        binding.recyclerViewReelVideoMainUser.adapter = postReelAdapter

        myReel()

        return binding.root
    }
    private fun myReel() {
        val postRef = FirebaseDatabase.getInstance().reference.child("videos")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                (listReel as ArrayList<ReelData>).clear()
                for (snapshot in p0.children) {
                    val post = snapshot.getValue(ReelData::class.java)
                    if (post!!.publisherId.equals(firebaseUser.uid))
                        (listReel as ArrayList<ReelData>).add(post)
                }
                Collections.reverse(listReel)
                postReelAdapter!!.notifyDataSetChanged()
            }
        })
    }
}