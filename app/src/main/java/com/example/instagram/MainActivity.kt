package com.example.instagram

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.instagram.databinding.ActivityMainBinding
import com.example.instagram.fragment.HomeFragment
import com.example.instagram.fragment.ProfileFragment
import com.example.instagram.fragment.ReelFragment
import com.example.instagram.fragment.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
   private val binding by lazy {
       ActivityMainBinding.inflate(layoutInflater)
   }
    internal var selectedFragment: Fragment?=null
    private lateinit var firebaseUser: FirebaseUser
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.homeFragment -> {
                moveToFragment(HomeFragment())
                return@OnNavigationItemSelectedListener true
            }

            R.id.searchFragment -> {
                moveToFragment(SearchFragment())
                return@OnNavigationItemSelectedListener true
            }

            R.id.addFragment -> {
             //   item.isChecked = false
               // moveToFragment(AddFragment())
                val bottomSheetDialog = BottomSheetDialog(this)
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout)
                bottomSheetDialog.show()

                val addPhoto = bottomSheetDialog.findViewById<LinearLayout>(R.id.addPhoto)
                addPhoto?.setOnClickListener {
                    startActivity(Intent(this@MainActivity, AddPostActivity::class.java))
                }
                val addReel = bottomSheetDialog.findViewById<LinearLayout>(R.id.addReel)
                addReel?.setOnClickListener {
                    startActivity(Intent(this@MainActivity, ReelActivity::class.java))
                    finish()
                    bottomSheetDialog.dismiss()
                }

                return@OnNavigationItemSelectedListener true
            }

            R.id.reelFragment -> {
                moveToFragment(ReelFragment())
                return@OnNavigationItemSelectedListener true
            }

            R.id.profileFragment -> {
                moveToFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)





        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val bottomView:BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        moveToFragment(HomeFragment())

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }

    }
    private fun moveToFragment(fragment:Fragment){
        val fragmentTrans=supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.container,fragment)
        fragmentTrans.commit()
    }

}