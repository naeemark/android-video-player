package com.andromeda.kunalbhatia.demo.hungamaplayer.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.andromeda.kunalbhatia.demo.hungamaplayer.R
import com.andromeda.kunalbhatia.demo.hungamaplayer.interfaces.CropMedia_Interface
import com.andromeda.kunalbhatia.demo.hungamaplayer.fragments.CropMediaFragment

class CropMediaActivity : AppCompatActivity() {
    companion object{
        var cropMediaInterface: CropMedia_Interface? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("CropMediaActivity", "CropMediaActivity oncreate")
        setContentView(R.layout.activity_crop_media)
        getBundleAndSetupToolbar()
        clickListeners()
    }

    private fun clickListeners() {

    }

    fun getBundleAndSetupToolbar() {
        intent?.extras.let {
            if (it?.getString("STEP", "").equals("CROP", ignoreCase = true)) {
//                prepareSupportToolbarTitle(getString(R.string.crop))
                launchFragment(CropMediaFragment())
            } else if (it?.getString("STEP", "").equals("TRIM", ignoreCase = true)) {
//                prepareSupportToolbarTitle(getString(R.string.trim))
//                launchFragment(TrimVideoFragment())
            }
            else if (it?.getString("STEP", "").equals("PREVIEW POST", ignoreCase = true)) {
//                prepareSupportToolbarTitle(getString(R.string.preview_post))
            }
        }

    }

    fun launchFragment(fragment: androidx.fragment.app.Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        intent.extras?.let {
            fragment.arguments = it
        }
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        handleBackPress()
        finish()
    }

    public fun cropMediaSuccessful(uri: Uri, fileType: String){
        cropMediaInterface?.cropSuccessFull(uri, fileType)
        finish()
    }

    public fun nativeSuccessful(uri: Uri){
        cropMediaInterface?.nativeSuccessFull(uri)
        finish()
    }

    public fun handleBackPress(){
        intent.extras?.let {
            if (it.getString("STEP", "").equals("CROP", ignoreCase = true)) {
                cropMediaInterface?.cropCanceled()
            } else {
                cropMediaInterface?.trimCanceled()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                handleBackPress()
                finish()
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

}