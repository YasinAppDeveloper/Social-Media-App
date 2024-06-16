package com.example.instagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.instaapp.Model.User
import com.example.instagram.databinding.ActivityAccountSettingBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.R
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop

class AccountSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountSettingBinding
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfileRef: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfileRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")
        getUserInfo()

        binding.accountSettingsLogoutbtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.closeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.accountSettingsChangeProfile.setOnClickListener {
            checker = "clicked"

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, AddPostActivity.PICK_IMAGE_REQUEST)
        }

        binding.saveEditedInfo.setOnClickListener {
            if (checker == "clicked") {
                uploadProfileImageandInfo()
            } else {
                updateUserInfoOnly()
            }

        }

    }

    private fun uploadProfileImageandInfo() {

        when {
            imageUri == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT)
                .show()

            TextUtils.isEmpty(binding.accountSettingsFullnameProfile.text.toString()) -> {
                Toast.makeText(this, "Full Name is required", Toast.LENGTH_SHORT).show()
            }

            TextUtils.isEmpty(binding.accountSettingsUsernameProfile.text.toString()) -> {
                Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
            }

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Profile Settings")
                progressDialog.setMessage("Please wait! Updating...")
                progressDialog.show()

                val fileRef = storageProfileRef!!.child(firebaseUser!!.uid + ".png")

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            Toast.makeText(this, "exception:--" + it, Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = binding.accountSettingsFullnameProfile.text.toString()
                        userMap["username"] =
                            binding.accountSettingsUsernameProfile.text.toString().toLowerCase()
                        userMap["bio"] = binding.accountSettingsBioProfile.text.toString()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(this, "Account is updated", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()

                    } else {
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPostActivity.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            var image = data.data
            if (image != null){
                imageUri = image
                binding.accountSettingsImageProfile.setImageURI(imageUri)
            }
        }
    }

    private fun updateUserInfoOnly() {

        when {
            TextUtils.isEmpty(binding.accountSettingsFullnameProfile.text.toString()) -> {
                Toast.makeText(this, "Full Name is required", Toast.LENGTH_SHORT).show()
            }

            TextUtils.isEmpty(binding.accountSettingsUsernameProfile.text.toString()) -> {
                Toast.makeText(this, "username is required", Toast.LENGTH_SHORT).show()
            }

            else -> {
                val userRef: DatabaseReference =
                    FirebaseDatabase.getInstance().reference.child("Users")
                //using hashmap to store values
                val userMap = HashMap<String, Any>()
                userMap["fullname"] = binding.accountSettingsFullnameProfile.text.toString()
                userMap["username"] =
                    binding.accountSettingsUsernameProfile.text.toString().toLowerCase()
                userMap["bio"] = binding.accountSettingsBioProfile.text.toString()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Account is updated", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }
    private fun getUserInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(com.example.instagram.R.drawable.profile).into(binding.accountSettingsImageProfile)
                    binding.accountSettingsFullnameProfile.setText(user.getFullname())
                    binding.accountSettingsUsernameProfile.setText(user.getUsername())
                    binding.accountSettingsBioProfile.setText(user.getBio())

                }
            }
        })
    }

}