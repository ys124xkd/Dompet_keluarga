package com.example.dompetkeluarga

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dompetkeluarga.adapter.MonthHomePagerUserAdapter
import com.example.dompetkeluarga.databinding.FragmentHomeUserBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment_user : Fragment() {
    private var _binding: FragmentHomeUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRef: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid  // Ensure userId is fetched here
        firebaseRef = FirebaseDatabase.getInstance().getReference("transactions")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeUserBinding.inflate(inflater, container, false)

        // Tombol tambah data
        binding.addTransactionButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_user_to_addDataFragment)
        }
        // Tombol edit profil
        binding.headerContainer.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_user_to_editProfileFragment_user)
        }

        fetchUserData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        // Ensure that userId is passed when creating the adapter
        val adapter = MonthHomePagerUserAdapter(this, userId ?: "")
        viewPager.adapter = adapter

        // Connect the TabLayout with the ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getMonthName(position)
        }.attach()
    }

    private fun getMonthName(position: Int): String {
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return months[position]
    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userName = snapshot.child("nama").getValue(String::class.java)
                        val userImage = snapshot.child("profileImage").getValue(String::class.java)
                        binding.userNameText.text = userName ?: "User Name"
                        userImage?.let {
                            Glide.with(requireContext())
                                .load(it)
                                .into(binding.userProfileImage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
