package com.example.instaapp.Model

data class Post(
     var postid: String = "",
     var postimage: String = "",
     var publisher: String = "",
     var caption: String = "",
     var time: Long? = 0

)  {
    constructor():this("","","","",0)
}

