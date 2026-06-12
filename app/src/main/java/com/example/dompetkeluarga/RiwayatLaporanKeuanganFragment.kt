package com.example.dompetkeluarga

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dompetkeluarga.adapter.MonthPagerAdapter
import com.example.dompetkeluarga.databinding.FragmentRiwayatLaporanKeuanganBinding
import com.google.android.material.tabs.TabLayoutMediator
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
 * Use the [RiwayatLaporanKeuanganFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RiwayatLaporanKeuanganFragment : Fragment() {
    private var _binding: FragmentRiwayatLaporanKeuanganBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseRef: DatabaseReference
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatLaporanKeuanganBinding.inflate(inflater, container, false)

        binding.backButton.setOnClickListener {
            findNavController().navigate(R.id.action_riwayatLaporanKeuanganFragment_to_laporanKeuanganFragmentAdmin)
            findNavController().popBackStack(R.id.riwayatLaporanKeuanganFragment, true)
        }

        // Ambil userId dari arguments
        userId = arguments?.getString("userId")
        firebaseRef = FirebaseDatabase.getInstance().getReference("Users")

        // Ambil data berdasarkan userId
        userId?.let { fetchUserData(it) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        // Ensure that userId is passed when creating the adapter
        val adapter = MonthPagerAdapter(this, userId ?: "")
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

    private fun fetchUserData(userId: String) {
        firebaseRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nama = snapshot.child("nama").getValue(String::class.java)
                    val profileImage = snapshot.child("profileImage").getValue(String::class.java)

                    // Update UI dengan data pengguna
                    binding.userNameTextDetail.text = nama
                    Glide.with(requireContext()).load(profileImage)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(binding.circleImageViewDetail)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}