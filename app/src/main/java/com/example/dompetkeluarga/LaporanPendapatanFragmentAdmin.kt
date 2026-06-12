package com.example.dompetkeluarga

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dompetkeluarga.adapter.Laporan_Keuangan_adapter
import com.example.dompetkeluarga.adapter.Pendapatan_admin_adapter
import com.example.dompetkeluarga.databinding.FragmentLaporanKeuanganAdminBinding
import com.example.dompetkeluarga.databinding.FragmentLaporanPendapatanAdminBinding
import com.example.dompetkeluarga.models.Laporan_Keuangan_Anak
import com.example.dompetkeluarga.models.pendapatan_admin
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
 * Use the [LaporanPendapatanFragmentAdmin.newInstance] factory method to
 * create an instance of this fragment.
 */
class LaporanPendapatanFragmentAdmin : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentLaporanPendapatanAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var pendapatanAdmin: ArrayList<pendapatan_admin>
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLaporanPendapatanAdminBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseRef = FirebaseDatabase.getInstance().getReference("transactions_admin")
        pendapatanAdmin = arrayListOf()

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
                    pendapatanAdmin.clear()
                    if (snapshot.exists()) {
                        for (laporanSnap in snapshot.children) {
                            val laporan = laporanSnap.getValue(pendapatan_admin::class.java)
                            laporan?.let { pendapatanAdmin.add(it) }
                        }
                    }

                    // Set adapter after data is loaded
                    val laporanAdapter = Pendapatan_admin_adapter(pendapatanAdmin)
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LaporanPendapatanFragmentAdmin.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LaporanPendapatanFragmentAdmin().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}