package com.example.dompetkeluarga

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.dompetkeluarga.adapter.Laporan_Keuangan_adapter
import com.example.dompetkeluarga.databinding.FragmentLaporanKeuanganAdminBinding
import com.example.dompetkeluarga.models.Laporan_Keuangan_Anak
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
 * Use the [LaporanKeuanganFragmentAdmin.newInstance] factory method to
 * create an instance of this fragment.
 */
class LaporanKeuanganFragmentAdmin : Fragment() {
    private var _binding: FragmentLaporanKeuanganAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var laporanKeuangan: ArrayList<Laporan_Keuangan_Anak>
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanKeuanganAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseRef = FirebaseDatabase.getInstance().getReference("Users") // Menyesuaikan dengan struktur Firebase
        laporanKeuangan = arrayListOf()

        // Set up RecyclerView
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }

        // Fetch data from Firebase
        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        firebaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding != null) {
                    laporanKeuangan.clear()
                    if (snapshot.exists()) {
                        for (userSnap in snapshot.children) {
                            val userId = userSnap.key ?: continue
                            val nama = userSnap.child("nama").getValue(String::class.java) ?: continue
                            val profileImage = userSnap.child("profileImage").getValue(String::class.java) ?: ""
                            val role = userSnap.child("role").getValue(String::class.java) ?: continue
                            if (role == "User") {
                                val laporan = Laporan_Keuangan_Anak(
                                    userId,
                                    profileImage,
                                    nama
                                )
                                laporanKeuangan.add(laporan)
                                Log.d("Laporan", "User: ${laporan.nama}, ID: ${laporan.id}")
                            }
                        }
                    }

                    val laporanAdapter = Laporan_Keuangan_adapter(
                        requireContext(),
                        laporanKeuangan
                    ) { laporan ->
                        // Handle item click if needed
                    }
                    binding.recyclerView.adapter = laporanAdapter
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