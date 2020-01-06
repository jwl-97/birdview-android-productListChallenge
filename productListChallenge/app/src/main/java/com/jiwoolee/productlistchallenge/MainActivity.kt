package com.jiwoolee.productlistchallenge

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jiwoolee.productlistchallenge.retrofit.IMyService
import com.jiwoolee.productlistchallenge.retrofit.ProductData
import com.jiwoolee.productlistchallenge.retrofit.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {
    private var disposable: CompositeDisposable? = CompositeDisposable() //retrofit 통신
    private var retrofitClient = RetrofitClient.getInstance()
    private var iMyService: IMyService? =
        (retrofitClient as Retrofit).create(IMyService::class.java)

    private var adapter: RecyclerviewAdapter? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this

        initToolbar()

        val recyclerView = findViewById<View>(R.id.rec_product_list) as RecyclerView
        setRecyclerview(recyclerView)

//        val deviceWidth = resources.displayMetrics.widthPixels
//        Log.d("ljwLog", deviceWidth)
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun setRecyclerview(recyclerview: RecyclerView) {
        initRecyclerview(recyclerview)
        getProductList()
        adapter!!.notifyDataSetChanged()
    }

    private fun initRecyclerview(recyclerview: RecyclerView) {
        recyclerview.layoutManager = GridLayoutManager(this, 2)

        adapter = RecyclerviewAdapter()
        recyclerview.adapter = adapter
    }

    private fun getProductList() {
        disposable!!.add(iMyService!!.pagingList("oily")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { response ->
                val listArray: JSONArray = JSONObject(response).getJSONArray("body")
                parsingProductList(listArray)
            }
        )
    }

    private fun parsingProductList(listArray: JSONArray) {
        try {
            for (i in 0 until listArray.length()) {
                val data = ProductData()
                val listObject = listArray.getJSONObject(i)
                data.thumbnailImage = listObject.getString("thumbnail_image")
                data.productTitle = listObject.getString("title")
                data.productPrice = listObject.getString("price")

                adapter!!.addItem(data)
            }
            adapter!!.notifyDataSetChanged()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        disposable?.clear()
    }
}
