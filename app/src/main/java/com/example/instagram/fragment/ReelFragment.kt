package com.example.instagram.fragment

import ReelVideoAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.instagram.R
import com.example.instagram.data.ReelData
import com.example.instagram.databinding.ReelFragmentBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections

class ReelFragment : Fragment() {
    lateinit var viewPager: ViewPager2
    private lateinit var videoAdapter: ReelVideoAdapter
    private lateinit var videoList: MutableList<ReelData>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNavigationView: BottomNavigationView

    private val binding by lazy {
        ReelFragmentBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        videoList = mutableListOf()
        videoAdapter = ReelVideoAdapter(requireContext(), videoList)
        binding.viewPager.adapter = videoAdapter

        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
        setBottomNavigationViewAppearance(true)


        // Hide status bar and toolbar
        activity?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().reference.child("videos")

        loadVideos()

//
//        requireActivity().onBackPressedDispatcher.addCallback(
//            viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    val currentVideoHolder =
//                        (viewPager.getChildAt(0) as? RecyclerView)?.findViewHolderForAdapterPosition(
//                            viewPager.currentItem
//                        ) as? ReelVideoAdapter.VideoViewHolder
//                    currentVideoHolder?.exoPlayer?.playWhenReady = false
//                    isEnabled = false
//                    requireActivity().onBackPressed()
//                }
//            })

        return binding.root
    }

    private fun loadVideos() {
        binding.progressBar.visibility = View.VISIBLE
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                videoList.clear()
                for (dataSnapshot in snapshot.children) {
                    val video = dataSnapshot.getValue(ReelData::class.java)
                    if (video != null) {
                        videoList.add(video)
                    }
                }
                Collections.shuffle(videoList)
                videoAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load videos", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        })
    }
    private fun setBottomNavigationViewAppearance(isVideoFragment: Boolean) {
        if (isVideoFragment) {
            bottomNavigationView.setBackgroundColor(Color.BLACK)
            bottomNavigationView.itemIconTintList = resources.getColorStateList(R.color.white, null)
            bottomNavigationView.itemTextColor = resources.getColorStateList(R.color.white, null)
        } else {
            // Revert to the default appearance
            bottomNavigationView.setBackgroundColor(resources.getColor(R.color.default_background_color, null))
            bottomNavigationView.itemIconTintList = resources.getColorStateList(R.color.default_item_color, null)
            bottomNavigationView.itemTextColor = resources.getColorStateList(R.color.default_item_color, null)
        }
    }

    override fun onPause() {
        super.onPause()
        videoAdapter.releasePlayer()
    }

    override fun onResume() {
        super.onResume()
       videoAdapter.pausePlayer()
    }
}