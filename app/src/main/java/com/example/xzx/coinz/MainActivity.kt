package com.example.xzx.coinz

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.example.xzx.coinz.fragment.MyAccountFragment
import kotlinx.android.synthetic.main.activity_main.*
import com.example.xzx.coinz.R
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener{
            when (it.itemId){
                R.id.navigation_people -> {
                    startActivity<RankActivity>()
                    true
                }
                R.id.navigation_my_account -> {
                    replaceFragment(MyAccountFragment())
                    true
                }
                R.id.start_button ->{
                    startActivity<MapActivity>()
                    true
                }

                else -> false
            }
        }
    }

    @SuppressLint("CommitTransaction")
    private fun replaceFragment(fragment:Fragment){
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.fragment_layout,fragment)
            commit()
        }
    }
}
