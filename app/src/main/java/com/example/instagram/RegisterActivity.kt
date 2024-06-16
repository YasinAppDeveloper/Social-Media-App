package com.example.instagram

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instagram.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Suppress("OVERRIDE_DEPRECATION")
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private lateinit var mAuth:FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)



        progressDialog= ProgressDialog(this@RegisterActivity)
        progressDialog.setTitle("SignUp")
        progressDialog.setMessage("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        mAuth = FirebaseAuth.getInstance()
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
        }
        binding.signupBtn.setOnClickListener {
            createAccount()
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signInButtonGoogle.setOnClickListener {
           signIn()
        }

    }
    private fun createAccount() {
        val fullName=binding.signupFullname.text.toString()
        val userName=binding.signupUsername.text.toString()
        val email=binding.signupEmail.text.toString()
        val password=binding.signupPassword.text.toString()

        when{
            TextUtils.isEmpty(fullName)-> Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(userName)-> Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show()
           TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            password.length < 6 -> Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show()
            else->
            {
                progressDialog.show()

                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful)
                        {
                            saveUserInfo(fullName,userName,email,progressDialog)
                        }
                        else
                        {
                            val message=task.exception!!.toString()
                            Toast.makeText(this,"Error : $message", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String,progressDialog: ProgressDialog) {
        val currentUserId= FirebaseAuth.getInstance().currentUser!!.uid

        val userMap=HashMap<String,Any>()
        userMap["uid"]=currentUserId
        userMap["fullname"]=fullName
        userMap["username"]=userName.toLowerCase()
        userMap["email"]=email
        userMap["bio"]="Hey! I am using In SocialWave"
        userMap["image"]="https://img.freepik.com/free-psd/3d-illustration-human-avatar-profile_23-2150671142.jpg"

        userRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener {task ->
                if(task.isSuccessful)
                {
                    Toast.makeText(this,"Account has been created", Toast.LENGTH_SHORT).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId)
                        .setValue(true)

                    val intent=Intent(this@RegisterActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val message=task.exception!!.toString()
                    Toast.makeText(this,"Error : $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!.idToken!!)
            } catch (e: ApiException) {
                // Handle sign-in failure
                Toast.makeText(this, "Sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        //dialog.show()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    user?.let {
                        val name = it.displayName
                        val email = it.email
                        val uid = it.uid
                        val image = it.photoUrl.toString()
                        saveUserToFirestore(name, email, uid,image)
                    }
                } else {
                    Toast.makeText(this, "some thing wrong"+task.exception?.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(name: String?, email: String?, uid: String,photo:String) {
       // val userData = User(uid,name.toString(),email.toString(),photo)
        val currentUserId= FirebaseAuth.getInstance().currentUser!!.uid
        val userMap=HashMap<String,Any>()
        userMap["uid"]=uid
        userMap["fullname"]=name.toString()
        userMap["username"]=name!!.toLowerCase()
        userMap["email"]=email.toString()
        userMap["bio"]="Hey! I am using Professional App  Developer 🔥🤫💻"
        userMap["image"]=photo

        userRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener {task ->
                if(task.isSuccessful)
                {
                    Toast.makeText(this,"Account has been created", Toast.LENGTH_SHORT).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId)
                        .setValue(true)

                    val intent=Intent(this@RegisterActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val message=task.exception!!.toString()
                    Toast.makeText(this,"Error : $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }

    }
}