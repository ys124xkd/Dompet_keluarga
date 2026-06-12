package com.example.dompetkeluarga

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.dompetkeluarga.adapter.MonthHomePagerAdminAdapter
import com.example.dompetkeluarga.adapter.MonthPagerAdapter
import com.example.dompetkeluarga.databinding.FragmentHomeAdminBinding
import com.example.dompetkeluarga.databinding.FragmentHomeUserBinding
import com.example.dompetkeluarga.models.Laporan_Keuangan_Anak
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Home_Fragment_Admin.newInstance] factory method to
 * create an instance of this fragment.
 */
class Home_Fragment_Admin : Fragment() {
    private var _binding: FragmentHomeAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var adminNameText: TextView
    private lateinit var adminProfileImage: ImageView
    private lateinit var firebaseRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeAdminBinding.inflate(inflater, container, false)

        // Initialize UI components
        adminProfileImage = binding.adminProfileImage
        adminNameText = binding.adminNameText
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        // Ensure that userId is passed when creating the adapter
        val adapter = MonthHomePagerAdminAdapter(this)
        viewPager.adapter = adapter

        // Connect the TabLayout with the ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getMonthName(position)
        }.attach()
        binding.addTransactionButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_Fragment_Admin_to_addDataFragmentAdmin)
        }
        binding.btnReport.setOnClickListener {
            findNavController().navigate(R.id.action_home_Fragment_Admin_to_laporanKeuanganFragmentAdmin)
        }
        binding.btnLaporanPendapatan.setOnClickListener {
            findNavController().navigate(R.id.action_home_Fragment_Admin_to_laporanPendapatanFragmentAdmin)
        }
        binding.btnUangSakuAnak.setOnClickListener {
            findNavController().navigate(R.id.action_home_Fragment_Admin_to_laporanUangSakuAnakFragmentAdmin)
        }
        binding.headerContainer.setOnClickListener {
            findNavController().navigate(R.id.action_home_Fragment_Admin_to_editProfleFragmentRegister)
        }

    // Fetch user data and transaction data
        fetchUserData()


        return binding.root
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
                        val adminName = snapshot.child("nama").getValue(String::class.java)
                        val adminImage = snapshot.child("profileImage").getValue(String::class.java)
                        binding.adminNameText.text = adminName ?: "Admin Name"
                        adminImage?.let {
                            Glide.with(requireContext())
                                .load(adminImage)
                                .into(adminProfileImage)
                        }
                    } else {
                        Toast.makeText(requireContext(), "No user data found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "User is not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}