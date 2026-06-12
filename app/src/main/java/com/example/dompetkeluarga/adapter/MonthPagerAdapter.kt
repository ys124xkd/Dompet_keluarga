package com.example.dompetkeluarga.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dompetkeluarga.RecylerViewRiwayatLaporanKeuanganAnakFragment

class MonthPagerAdapter(fragment: Fragment, private val userId: String) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 12 // Number of months

    override fun createFragment(position: Int): Fragment {
        // Pass the month index and userId to the MonthFragment
        return RecylerViewRiwayatLaporanKeuanganAnakFragment.newInstance(position, userId)
    }
}