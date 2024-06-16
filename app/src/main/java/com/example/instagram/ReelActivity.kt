package com.example.instagram

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instagram.data.ReelData
import com.example.instagram.databinding.ReelActivityBinding
import com.example.instagram.fragment.ReelFragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.util.Date
import java.util.UUID
import kotlin.random.Random

@Suppress("PrivatePropertyName")
class ReelActivity : AppCompatActivity() {
    private val binding by lazy {
        ReelActivityBinding.inflate(layoutInflater)
    }
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private var storagePostPictureRef: StorageReference? = null
    private val VIDEO_REQUEST_CODE = 123
    private var videoUrl: Uri? = null
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Posting")
        progressDialog.setMessage("Please wait, we are ..")

        val storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        databaseReference = FirebaseDatabase.getInstance().reference.child("videos")

//        supportFragmentManager.beginTransaction()
//            .replace(R.id.container, ReelFragment())
//            .commit()

        binding.apply {
            selectReel.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setType("video/*")
                startActivityForResult(intent, VIDEO_REQUEST_CODE)
            }
            uploadReelButton.setOnClickListener {
                val caption = binding.writeReelCaption.text.toString()
                if (caption.isEmpty()){
                    Toast.makeText(this@ReelActivity, "Enter the some caption", Toast.LENGTH_SHORT).show()
                } else if (videoUrl == null){
                    Toast.makeText(this@ReelActivity, "select the video for reel", Toast.LENGTH_SHORT).show()
                } else {
                    uploadReel(caption)
                    progressDialog.show()
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                videoUrl = data.data
                binding.reelToBePosted.setVideoURI(videoUrl)
                val mediaController = MediaController(this)
                mediaController.setAnchorView(binding.reelToBePosted)
                binding.reelToBePosted.setMediaController(mediaController)
                binding.reelToBePosted.start()

            }
        }
    }

    private fun uploadReel(cation: String) {
            // progressDialog.show()
            val ref = storageReference.child("videos/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(videoUrl!!)
            uploadTask.addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveVideoMetadata(uri.toString(), cation)
                }
            }.addOnFailureListener { e ->
                //    progressDialog.dismiss()
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    private fun saveVideoMetadata(videoUrl: String, captionFailed: String) {
        val date = Date()
        val videoId = databaseReference.push().key
        val caption = captionFailed
        val timestamp = date.time
        val publisherId = FirebaseAuth.getInstance().currentUser?.uid

        if (videoId != null && publisherId != null) {
            progressDialog.show()
            val videoMetadata = ReelData(videoId, publisherId, videoUrl, caption, timestamp)
            databaseReference.child(videoId).setValue(videoMetadata).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Video metadata saved to database", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Failed to save video metadata to database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        super.onBackPressed()
    }
}