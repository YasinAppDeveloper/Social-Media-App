package com.example.instagram.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.instagram.tab_fragment.MyPostFragment
import com.example.instagram.tab_fragment.MyReelFragment
import com.example.instagram.tab_fragment.SavedPostFragment
import com.example.instagram.tab_fragment.UserPostFragment
import com.example.instagram.tab_fragment.UserReelFragment
import com.example.instagram.tab_fragment.UserSavedPostFragment

class TabLayoutAdapterSecond(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
      return when(position){
           0 -> UserPostFragment()
           1 -> UserReelFragment()
           else -> UserSavedPostFragment()
       }
    }
}