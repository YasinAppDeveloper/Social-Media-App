package com.example.instagram.data

data class ReelData(
    val videoId: String? = null,
     val publisherId: String = "",
    val videoUrl: String? = null,
     val caption: String? = null,
     val timestamp: Long? = null,

)  {
    constructor() : this("", "", "", "", 0)
}
