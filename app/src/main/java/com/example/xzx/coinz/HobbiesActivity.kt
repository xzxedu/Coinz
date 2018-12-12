package com.example.xzx.coinz

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.example.xzx.coinz.HobbiesAdapter
import com.example.xzx.coinz.R
import com.example.xzx.coinz.model.Supplier
import kotlinx.android.synthetic.main.activity_hobbies.*
import kotlinx.android.synthetic.main.activity_wallet2.*

class HobbiesActivity : AppCompatActivity() {

    companion object {
        val TAG: String = HobbiesActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hobbies)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        val adapter= HobbiesAdapter(this, Supplier.hobbies)
        recyclerView?.adapter = adapter
    }
}