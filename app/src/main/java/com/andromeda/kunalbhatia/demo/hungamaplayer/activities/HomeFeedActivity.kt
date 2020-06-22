package com.andromeda.kunalbhatia.demo.hungamaplayer.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andromeda.kunalbhatia.demo.hungamaplayer.R
import com.andromeda.kunalbhatia.demo.hungamaplayer.HomePostAdapter
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.NetState
import com.andromeda.kunalbhatia.demo.hungamaplayer.models.PostsResponse
import com.andromeda.kunalbhatia.demo.hungamaplayer.viewmodel.HomePostViewModel
import kotlinx.android.synthetic.main.activity_home_feed.*

class HomeFeedActivity : AppCompatActivity() {
    companion object { val TAG: String = HomeFeedActivity::class.java.simpleName}

    private lateinit var homePostViewModel: HomePostViewModel
    private var layoutManager: LinearLayoutManager? = null
    private var adapter: HomePostAdapter? = null
    private val handler = Handler(Looper.getMainLooper())
    private var paginationLimit: Int = 9
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_feed)
        swipe_to_refresh.setOnRefreshListener {
            paginationLimit = 9
            swipe_to_refresh?.isRefreshing = false
            rv_feed?.resetValues()
            rv_feed?.pausePlayer()
            refreshData()
        }

        homePostViewModel = ViewModelProviders.of(this).get(HomePostViewModel::class.java)
        //observe home feed loading progressbar
        homePostViewModel.observeOnStateUpdated().observe(this, Observer<NetState> {
            progressbar_id.visibility = if (it == NetState.LOADING) View.VISIBLE else View.GONE
        })
        //observe home feed post
        homePostViewModel.observePostData().observe(this, Observer {
           bindToPosts(it)
        })
        //load home feed post
        homePostViewModel.loadHomePostFeeds("10", "0")
        prepareDataSet()
        fab_create_post.setOnClickListener {
            val intent = Intent(this@HomeFeedActivity, CreatePostActivity::class.java)
            startActivity(intent)
        }
    }

    // initialize home feed adapter && pagination

    private fun prepareDataSet() {
        layoutManager = LinearLayoutManager(baseContext, RecyclerView.VERTICAL, false)
        layoutManager?.isItemPrefetchEnabled = true
        layoutManager?.initialPrefetchItemCount = 6
        rv_feed.layoutManager = layoutManager
        adapter = HomePostAdapter(this@HomeFeedActivity)
        rv_feed.adapter = adapter
        rv_feed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Log.d(TAG, "onScrolled called ...")
                if (layoutManager != null) {
                    val visibleItemCount = layoutManager!!.getChildCount()
                    val totalItemCount = layoutManager!!.getItemCount()
                    val pastVisibleItems = layoutManager!!.findFirstVisibleItemPosition()
                    if (pastVisibleItems + visibleItemCount >= totalItemCount && totalItemCount > paginationLimit) {
                        Log.d(TAG, "onScrolled called ...Loading data")
                        val postData = homePostViewModel.getPostData()
                        postData?._next?.offset?.let {
                            homePostViewModel.loadHomePostFeeds("10", it.toString())
                        }
                    }
                }

            }
        })
    }

    // setting home feed post

    private fun bindToPosts(response: PostsResponse?) {
        Log.d(TAG, "bindToPosts called...")
        if (response != null && !response.data.isNullOrEmpty()) {
            adapter?.setItemsList(response.data)
            rv_feed?.setMediaObjects(adapter?.getItemList())
            adapter?.let {
                if (it.itemCount <= 10) {
                    handler.postDelayed({
                        if (layoutManager?.findFirstCompletelyVisibleItemPosition() == 0) {
                            rv_feed?.setPlayPosition(-1)
                        }
                        rv_feed?.playVideo(false)
                    }, 500)
                }
            }
        } else {
            rv_feed?.setMediaObjects(ArrayList())
            adapter?.setItemsList(ArrayList())
        }
    }

    private fun refreshData() {
        rv_feed?.setMediaObjects(ArrayList())
        adapter?.setItemsList(ArrayList())
        homePostViewModel.loadHomePostFeeds( "10", "0")
    }

    override fun onResume() {
        super.onResume()
        rv_feed.addEventListener()
    }

    override fun onPause() {
        super.onPause()
        rv_feed.removeEventListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        rv_feed.releasePlayer()
    }

}
