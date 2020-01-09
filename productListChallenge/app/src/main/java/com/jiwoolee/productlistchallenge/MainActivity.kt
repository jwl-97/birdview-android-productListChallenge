package com.jiwoolee.productlistchallenge

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.jiwoolee.productlistchallenge.retrofit.IMyService
import com.jiwoolee.productlistchallenge.retrofit.ProductData
import com.jiwoolee.productlistchallenge.retrofit.RetrofitClient
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.util.HalfSerializer.onComplete
import io.reactivex.internal.util.HalfSerializer.onError
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.Serializable
import java.lang.Exception
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), OnItemClickListener {
    private var disposable: CompositeDisposable? = CompositeDisposable() //retrofit 통신
    private var retrofitClient = RetrofitClient.getInstance()
    private var iMyService: IMyService? =
        (retrofitClient as Retrofit).create(IMyService::class.java)

    private var adapter: RecyclerviewAdapter? = null

    private var defaultSkinType: String = "oily"
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
        getProductList(defaultSkinType, pageCount)
    }

    private fun initRecyclerview(recyclerview: RecyclerView) {
        recyclerview.layoutManager = devideRecyclerviewLayoutForType()

        adapter = RecyclerviewAdapter(this)
        recyclerview.adapter = adapter
    }

    private fun devideRecyclerviewLayoutForType(): GridLayoutManager {
        val manager = GridLayoutManager(this, 2)
        manager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                when (adapter!!.getItemViewType(position)) {
                    TYPE_ITEM -> 1  //아이템일 경우 한 줄 당 2개 씩
                    else -> 2       //header, footer는 한 줄 당 1개 씩
                }
        }
        return manager
    }

    private fun getProductList(skin_type: String, page: Int) {
        disposable!!.add(
            iMyService!!.pagingList(skin_type, page)
                .subscribeOn(Schedulers.io())

                .observeOn(AndroidSchedulers.mainThread())
                .timeout(1, TimeUnit.SECONDS)
                .retry()
                .doOnError {
                    Toast.makeText(this, "doOnError", Toast.LENGTH_SHORT).show()
                }
                .subscribe { response ->
                    val listArray: JSONArray = JSONObject(response).getJSONArray("body")
                    parsingProductList(listArray)
                    disposable?.clear()
                })

    }

    private fun parsingProductList(listArray: JSONArray) = try {
        for (i in 0 until listArray.length()) {
            val data = ProductData("", "", "")
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
        adapter!!.notifyDataSetChanged()
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    //상품 가격 세자리수 콤마
    private fun makeCommaForNumber(input: Int): String {
        val formatter = DecimalFormat("###,###")
        return formatter.format(input)
    }

    //listener
    override fun onItemClicked(productData: ProductData) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("productData", productData as Serializable)
        startActivity(intent)
    }

    private fun setRecyclerViewScrollListener(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager: LinearLayoutManager? =
                    recyclerView.layoutManager as LinearLayoutManager?
                if (!isLastItem) {
                    val totalItemCount: Int = linearLayoutManager!!.itemCount
                    val lastVisible: Int =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition()
                    isLastItem =
                        (totalItemCount > 0) && (lastVisible >= totalItemCount - 1) // 마지막 아이템인지 판단
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, scrollState: Int) {
                super.onScrollStateChanged(recyclerView, scrollState)
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isLastItem) { //스크롤이 멈춰있고 마지막 아이템일 때
                    Handler().postDelayed(Runnable {
                        pageCount++
                        getProductList(defaultSkinType, pageCount) //다음 페이지의 아이템 로드해 오기
                    }, 500)
                    isLastItem = false
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        disposable?.clear()
    }
}