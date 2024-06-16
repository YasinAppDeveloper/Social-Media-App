package com.example.instagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.yalantis.ucrop.UCrop
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageStoryRef: StorageReference? = null
//    companion object {
//        private const val PICK_IMAGE_REQUEST = 2
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_story)

        storageStoryRef = FirebaseStorage.getInstance().reference.child("Story Pictures")

    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    startActivityForResult(intent, AddPostActivity.PICK_IMAGE_REQUEST)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPostActivity.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            imageUri?.let { startCrop(it) }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                if (it != null){
                    uploadImageToFirebase(it)
                } else {
                    Toast.makeText(this, "Some Error Occured!! Try Again", Toast.LENGTH_SHORT).show()
                }

            }



        }
    }
    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "croppedImage.jpg"))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1080, 1080)
            .start(this)
    }
    private fun uploadImageToFirebase(uri:Uri) {
        when
        {
            uri==null ->{
                Toast.makeText(this,"Please select Image", Toast.LENGTH_SHORT).show()
            }

            else->
            {
                val progressDialog= ProgressDialog(this)
                progressDialog.setTitle("Adding Story")
                progressDialog.setMessage("Please wait while your story is added")
                progressDialog.show()


                val fileRef=storageStoryRef!!.child(System.currentTimeMillis().toString()+".jpg")

                val uploadTask: StorageTask<*>
                uploadTask=fileRef.putFile(uri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }

                    return@Continuation fileRef.downloadUrl
                })
                    .addOnCompleteListener ( OnCompleteListener<Uri> {task ->
                        if (task.isSuccessful){

                            val downloadUrl=task.result
                            myUrl=downloadUrl.toString()


                            val ref= FirebaseDatabase.getInstance().reference
                                .child("Story")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)

                            val storyId=(ref.push().key).toString()

                            val timeEnd=System.currentTimeMillis()+ 86400000 //864000 is the millisec conversion for 24hrs//The timeSpan to expire the story

                            val storymap = HashMap<String, Any>()

                            storymap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
                            storymap["timestart"] = ServerValue.TIMESTAMP
                            storymap["timeend"] = timeEnd
                            storymap["imageurl"] = myUrl
                            storymap["storyid"] = storyId

                            ref.child(storyId).updateChildren(storymap)

                            Toast.makeText(this, "Story Added!!", Toast.LENGTH_SHORT)
                                .show()

                            finish()


                            progressDialog.dismiss()

                        }
                        else
                        {
                            progressDialog.dismiss()
                        }


                    })


            }
        }
    }
}