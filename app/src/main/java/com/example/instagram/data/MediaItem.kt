package com.example.instagram.data

import com.example.instaapp.Model.Post

sealed class MediaItem {
    data class PostItem(val post: Post) : MediaItem()
    data class ReelItem(val reel: ReelData) : MediaItem()
}