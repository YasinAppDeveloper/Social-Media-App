package com.example.instagram.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instaapp.Adapter.UserAdapter
import com.example.instaapp.Model.User
import com.example.instagram.R
import com.example.instagram.databinding.SearchFragmentBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {
    private val binding by lazy {
        SearchFragmentBinding.inflate(layoutInflater)
    }
    private lateinit var bottomNavigationView: BottomNavigationView
    private var userAdapter: UserAdapter?=null
    private var mUser:MutableList<User>?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mUser = ArrayList()

        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
        setBottomNavigationViewAppearance(true)

        binding.recyclerviewSearch.setHasFixedSize(true)
        binding.recyclerviewSearch.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        //to show a user on search
        userAdapter = requireContext().let { UserAdapter(it, mUser as ArrayList<User>, true)}
        binding.recyclerviewSearch.adapter = userAdapter

        retrieveUser()

        binding.searchitem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.searchitem.text.toString() == "") {
                } else {
                    searchUser(s.toString().toLowerCase())
                }
            }
        })

        return binding.root
    }
    private fun searchUser(input:String) {
        val query = FirebaseDatabase.getInstance().reference
            .child("Users")
            .orderByChild("username")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(datasnapshot: DataSnapshot) {
                mUser?.clear()

                for (snapshot in datasnapshot.children) {
                    //searching all users
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        mUser?.add(user)
                        retrieveUser()
                    }
                }
                userAdapter?.notifyDataSetChanged()
            }
        })
    }
    private fun retrieveUser()
    {
        val usersSearchRef=FirebaseDatabase.getInstance().reference.child("Users")//table name:Users
        usersSearchRef.addValueEventListener(object:ValueEventListener
        {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Could not read from Database", Toast.LENGTH_LONG).show()
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mUser?.clear()
                    for (snapShot in dataSnapshot.children) {
                        val user = snapShot.getValue(User::class.java)

                        val id = FirebaseAuth.getInstance().currentUser!!.uid

                          if (!user?.getUid().equals(id)) {
                              mUser?.add(user!!)
                              binding.searchUserLayout.visibility = View.VISIBLE
                              binding.userSearchProgressBar.visibility = View.GONE
                          }
                    }
                userAdapter?.notifyDataSetChanged()
            }
        })
    }
    private fun setBottomNavigationViewAppearance(isVideoFragment: Boolean) {
        if (isVideoFragment) {
            bottomNavigationView.setBackgroundColor(Color.WHITE)
            bottomNavigationView.itemIconTintList = resources.getColorStateList(R.color.black, null)
            bottomNavigationView.itemTextColor = resources.getColorStateList(R.color.black, null)
        } else {
            // Revert to the default appearance
            bottomNavigationView.setBackgroundColor(resources.getColor(R.color.default_background_color, null))
            bottomNavigationView.itemIconTintList = resources.getColorStateList(R.color.default_item_color, null)
            bottomNavigationView.itemTextColor = resources.getColorStateList(R.color.default_item_color, null)
        }
    }
}