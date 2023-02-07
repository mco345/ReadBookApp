package com.example.bookreviewapp.fragment.my_page

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.bookreviewapp.DetailActivity
import com.example.bookreviewapp.MainActivity
import com.example.bookreviewapp.R
import com.example.bookreviewapp.adapter.BookAdapter
import com.example.bookreviewapp.adapter.LikeBookAdapter
import com.example.bookreviewapp.databinding.FragmentReadBookBinding

class ReadBookFragment : Fragment() {
    private lateinit var binding: FragmentReadBookBinding
    private lateinit var adapter: LikeBookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReadBookBinding.inflate(layoutInflater)
        return binding.root


    }

    companion object {
        private const val TAG = "ReadBookFragment"
    }
}
