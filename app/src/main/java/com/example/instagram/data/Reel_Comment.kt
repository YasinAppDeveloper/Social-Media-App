package com.example.instaapp.Model

class Reel_Comment {
    private var publisher:String=""
    private var comment:String=""
    private var time:Long?=0

    constructor()

    constructor(publisher: String, comment: String,time:Long) {
        this.publisher = publisher
        this.comment = comment
        this.time = time
    }

    fun getPublisher():String{
        return publisher
    }
    fun getComment():String{
        return comment
    }
   fun getTime() : Long{
       return time!!
   }
    fun setPublisher(publisher: String)
    {
        this.publisher=publisher
    }

    fun setComment(comment: String)
    {
        this.comment=comment
    }
    fun setTime(time:Long){
        this.time=time
    }
}