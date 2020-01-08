package com.jiwoolee.productlistchallenge

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.jiwoolee.productlistchallenge.retrofit.IMyService
import com.jiwoolee.productlistchallenge.retrofit.ProductData
import com.jiwoolee.productlistchallenge.retrofit.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.util.HalfSerializer.onNext
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import java.lang.Exception
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private var disposable: CompositeDisposable? = CompositeDisposable() //retrofit 통신
    private var retrofitClient = RetrofitClient.getInstance()
    private var iMyService: IMyService? =
        (retrofitClient as Retrofit).create(IMyService::class.java)

    private var adapter: RecyclerviewAdapter? = null

    private var pageCount: Int = 1
    private var isLastItem = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this

        initToolbar()
        pageCount = 1

        val recyclerView: RecyclerView = findViewById<View>(R.id.rec_product_list) as RecyclerView
        setRecyclerview(recyclerView)
        setRecyclerViewScrollListener(recyclerView)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun setRecyclerview(recyclerview: RecyclerView) {
        initRecyclerview(recyclerview)
        getProductList("oily", pageCount)
    }

    private fun setRecyclerViewScrollListener(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, scrollState: Int) {
                super.onScrollStateChanged(recyclerView, scrollState)
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isLastItem) {
                    Handler().postDelayed(Runnable {
                        pageCount++
                        getProductList("oily", pageCount)
                    }, 500)
                    isLastItem = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager: LinearLayoutManager? =
                    recyclerView.layoutManager as LinearLayoutManager?
                if (!isLastItem) {
                    val totalItemCount: Int = linearLayoutManager!!.itemCount
                    val lastVisible: Int =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition()
                    isLastItem = (totalItemCount > 0) && (lastVisible >= totalItemCount - 1)
                }
            }
        })
    }

    private fun initRecyclerview(recyclerview: RecyclerView) {
        val manager = GridLayoutManager(this, 2)
        manager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (adapter!!.getItemViewType(position) == TYPE_ITEM) {
                    1
                } else {
                    2
                }
        }

        recyclerview.layoutManager = manager

        adapter = RecyclerviewAdapter()
        recyclerview.adapter = adapter
    }


    private fun getProductList(skin_type: String, page: Int) {
        disposable!!.add(iMyService!!.pagingList(skin_type, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .retry()
            .subscribe { response ->
                val listArray: JSONArray = JSONObject(response).getJSONArray("body")
                parsingProductList(listArray)
            }
        )
    }

    private fun parsingProductList(listArray: JSONArray) {
        for (i in 0 until listArray.length()) {
            val data = ProductData()
            val listObject = listArray.getJSONObject(i)
            data.thumbnailImage = listObject.getString("thumbnail_image")
            data.productTitle = listObject.getString("title")
            data.productPrice = makeCommaForNumber(
                Integer.parseInt(
                    listObject.getString("price").toString().replace(",", "")
                )
            ) + "원"
            adapter!!.addItem(data)
        }
        Handler().postDelayed(Runnable {
            adapter!!.notifyDataSetChanged()
        }, 500)
    }

    //상품 가격 세자리수 콤마
    private fun makeCommaForNumber(input: Int): String {
        val formatter = DecimalFormat("###,###")
        return formatter.format(input)
    }

    override fun onStop() {
        super.onStop()
        disposable?.clear()
    }
}