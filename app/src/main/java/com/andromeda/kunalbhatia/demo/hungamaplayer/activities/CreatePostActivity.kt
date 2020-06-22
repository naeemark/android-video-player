package com.andromeda.kunalbhatia.demo.hungamaplayer.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andromeda.kunalbhatia.demo.hungamaplayer.R
import com.andromeda.kunalbhatia.demo.hungamaplayer.fragments.CreatePostFragment

class CreatePostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)
        launchFragment(CreatePostFragment())
    }

    fun launchFragment(fragment: androidx.fragment.app.Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        intent.extras?.let { fragment.arguments = it }
        fragmentTransaction.replace(R.id.create_post_container, fragment)
        fragmentTransaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.create_post_container)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

}