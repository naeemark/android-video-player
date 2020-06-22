package com.andromeda.kunalbhatia.demo.hungamaplayer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.andromeda.kunalbhatia.demo.hungamaplayer.R
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.SignInResponse
import com.andromeda.kunalbhatia.demo.hungamaplayer.rest.APIs
import com.andromeda.kunalbhatia.demo.hungamaplayer.rest.ApiClient
import kotlinx.android.synthetic.main.activity_main.*

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun login(view: View) {
        callApi()
    }

    private fun callApi() {
        progressBar.visibility = View.VISIBLE
        val api: APIs = ApiClient.getClient()!!.create(APIs::class.java)
        var email = findViewById<EditText>(R.id.etEmail).text.toString()
        var pass = findViewById<EditText>(R.id.etPassword).text.toString()
        if(email.isEmpty()){
            email = "android@gmail.com"
        }
        if(pass.isEmpty()){
            pass = "aaaaaa"
        }

        api.login(email, pass, "idhf8h10h0014841").enqueue(object: Callback<SignInResponse>{
            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Failed " + t.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<SignInResponse>, response: Response<SignInResponse>) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
                ApiClient.myToken = response.body()?.data?.accessToken.toString()
                val intent = Intent (this@MainActivity, HomeFeedActivity::class.java)
                startActivity(intent)
            }
        })
    }
}
