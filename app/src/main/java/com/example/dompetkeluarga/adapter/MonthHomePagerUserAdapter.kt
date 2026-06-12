package com.example.dompetkeluarga.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dompetkeluarga.RecyclerViewHomeUserFragment
import com.example.dompetkeluarga.RecylerViewRiwayatLaporanKeuanganAnakFragment

class MonthHomePagerUserAdapter (fragment: Fragment, private val userId: String) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 12 // Number of months

    override fun createFragment(position: Int): Fragment {
        // Pass the month index and userId to the MonthFragment
        return RecyclerViewHomeUserFragment.newInstance(position, userId)
    }
}
