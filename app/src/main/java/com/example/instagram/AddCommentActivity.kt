package com.example.instagram

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.instaapp.Adapter.ReelCommentAdapter
import com.example.instaapp.Model.Reel_Comment
import com.example.instaapp.Model.User
import com.example.instagram.databinding.ActivityAddCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Date

class AddCommentActivity : AppCompatActivity() {
    private lateinit var binding :ActivityAddCommentBinding
    private var firebaseUser: FirebaseUser?=null
    private var commentAdapter: ReelCommentAdapter?=null
    private var commentList:MutableList<Reel_Comment>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.commentsToolbar)
        supportActionBar!!.title = "Reel Comments"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.commentsToolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })



        binding.recyclerviewComments.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = LinearLayoutManager(this)
        binding.recyclerviewComments.layoutManager=linearLayoutManager

        commentList=ArrayList()
        commentAdapter= this.let { ReelCommentAdapter(it,commentList as ArrayList<Reel_Comment>) }
        binding.recyclerviewComments.adapter=commentAdapter


        firebaseUser= FirebaseAuth.getInstance().currentUser

        //val add_comment=findViewById<EditText>(R.id.add_comment)
       // val post_comment=findViewById<TextView>(R.id.post_comment)
        val postid = intent.getStringExtra("POST_ID")
        val videoUrl = intent.getStringExtra("url")
        binding.videoComment.setVideoURI(Uri.parse(videoUrl))
        binding.videoComment.setOnPreparedListener { mp ->
           binding.progressBarLoadingCommentsVideo.visibility = View.GONE
            mp.isLooping = true
            binding.videoComment.start()
        }

        getImage()
        readComments(postid!!)
       // getPostImage(postid!!)

            binding.postComment.setOnClickListener {
            if(binding.addComment.text.toString().equals(""))
            {
                Toast.makeText(this,"You can't send an empty comment", Toast.LENGTH_SHORT).show()
            }
            else
            {
                postComment(postid!!)
            }
        }

    }

    private fun postComment(postid:String) {

        val commentRef : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Reel_Comment").child(postid)

        val date = Date()
        val commentMap = HashMap<String, Any>()
        commentMap["publisher"] = firebaseUser!!.uid
        commentMap["comment"] = binding.addComment.text.toString()
        commentMap["time"] = date.time

        commentRef.push().setValue(commentMap)
       // pushNotification(postid)
        binding.addComment.setText("")
        Toast.makeText(this, "posted!!", Toast.LENGTH_LONG).show()
    }

    private fun getImage() {
        val ref : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        ref.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(com.example.instagram.R.drawable.profile).into(binding.userProfileImage)
                }
            }
        })
    }

    private fun pushNotification(postid: String) {

        val ref = FirebaseDatabase.getInstance().reference.child("Notification").child(firebaseUser!!.uid)

        val notifyMap = HashMap<String, Any>()
        notifyMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
        notifyMap["text"] = "commented :"+binding.addComment.text.toString()
        notifyMap["postid"] = postid
        notifyMap["ispost"] = true

        ref.push().setValue(notifyMap)
    }

    private fun readComments(postid: String) {
        val ref: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Reel_Comment").child(postid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {

                commentList?.clear()
                for (snapshot in p0.children) {
                    val cmnt: Reel_Comment? = snapshot.getValue(Reel_Comment::class.java)
                    commentList!!.add(cmnt!!)
                }
                commentAdapter!!.notifyDataSetChanged()
            }
        })
    }

//    private fun getPostImage(postid: String){
//        val postRef = FirebaseDatabase.getInstance()
//            .reference.child("Posts")
//            .child(postid).child("postimage")
//
//        postRef.addValueEventListener(object: ValueEventListener {
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//            override fun onDataChange(p0: DataSnapshot) {
//                if(p0.exists())
//                {
//                    val image = p0.value.toString()
//
//                    Picasso.get().load(image).placeholder(com.example.instagram.R.drawable.profile).into(binding.postImageComment)
//                }
//            }
//        })
//    }

}