package com.example.dompetkeluarga.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.dompetkeluarga.RecylerHomeAdminFragment

class MonthHomePagerAdminAdapter (fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 12 // Number of months

    override fun createFragment(position: Int): Fragment {
        // Pass the month index and userId to the MonthFragment
        return RecylerHomeAdminFragment.newInstance(position)
    }
}