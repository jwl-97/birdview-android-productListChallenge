package com.jiwoolee.productlistchallenge

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.jiwoolee.productlistchallenge.retrofit.IMyService
import com.jiwoolee.productlistchallenge.retrofit.ProductData
import com.jiwoolee.productlistchallenge.retrofit.RetrofitClient
import com.jiwoolee.productlistchallenge.retrofit.SharedPreferenceManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.Serializable
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), OnItemClickListener {
    private var disposable: CompositeDisposable? = CompositeDisposable() //retrofit 통신
    private var retrofitClient = RetrofitClient.getInstance()
    private var iMyService: IMyService? = (retrofitClient as Retrofit).create(IMyService::class.java)

    private lateinit var recyclerView: RecyclerView
    private var adapter: RecyclerviewAdapter? = null
    private lateinit var searchView: SearchView

    private var defaultSkinType: String = "oily"
    private var pageCount: Int = 1

    private var isLastItem: Boolean = false
    private var isFirst: Boolean = false
    private var isQuery: Boolean = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this

        initToolbar()
        initData()

        setRecyclerview()
        setSpinner()
    }

    private fun initData(){
        isFirst = true
        isQuery = false

        recyclerView = findViewById(R.id.rec_product_list)
        searchView = findViewById(R.id.searchview)
        searchView.clearFocus()
    }

    /*
    툴바 & searchview
     */
    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { //검색 submit 시
                isQuery = true
                adapter!!.removeItem()                  //쿼리한 내용으로 리스트를 갱신시킨다
                queryProduct(defaultSkinType, query!!)

                SharedPreferenceManager.setBoolean(mContext, "noShowProgress", true) //프로그래스바 보이지 않게
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return false
            }
        })

        val closeButton = searchView.findViewById<ImageView>(R.id.search_close_btn) //searchview의 x 버튼
        closeButton.setOnClickListener {
            isQuery = false
            pageCount = 1
            adapter!!.removeItem()                      //리스트를 다시 받아와 갱신시킨다
            getProductList(defaultSkinType, pageCount)

            SharedPreferenceManager.setBoolean(mContext, "noShowProgress", false) //프로그래스바 보이게
            setSearchviewClear()
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun setSearchviewClear(){
        searchView.setQuery("", false) //searchview의 text칸을 비워준다.
        searchView.clearFocus()
        if(tv_noSearchData.visibility == View.VISIBLE){
            tv_noSearchData.visibility = View.GONE
        }
    }

    /*
    리사이클러뷰
     */
    private fun setRecyclerview() {
        initRecyclerview()
        Handler().postDelayed(Runnable {
            getProductList(defaultSkinType, pageCount) //데이터 가져오기
        }, 100)
    }

    private fun initRecyclerview() {
        recyclerView.layoutManager = devideRecyclerviewLayoutForType()

        adapter = RecyclerviewAdapter(this)
        recyclerView.adapter = adapter
        setRecyclerviewListener(recyclerView)
    }

    private fun setRecyclerviewListener(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val linearLayoutManager: LinearLayoutManager? = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLastItem) {
                    val totalItemCount: Int = linearLayoutManager!!.itemCount
                    val lastVisible: Int = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                    isLastItem = (totalItemCount > 0) && (lastVisible >= totalItemCount - 1) // 마지막 아이템인지 판단
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, scrollState: Int) {
                super.onScrollStateChanged(recyclerView, scrollState)
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isLastItem && !isQuery) { //스크롤이 멈춰있고 마지막 아이템일 때
                    pageCount++
                    recyclerView.isNestedScrollingEnabled = false //리사이클러뷰 스크롤 막기

                    Handler().postDelayed(Runnable {
                        getProductList(defaultSkinType, pageCount)//다음 페이지의 아이템 로드해 오기
                    }, 100)

                    isLastItem = false
                    recyclerView.isNestedScrollingEnabled = true //리사이클러뷰 스크롤 풀기
                }
            }
        })
    }

    private fun devideRecyclerviewLayoutForType(): GridLayoutManager {
        val manager = GridLayoutManager(this, 2)
        manager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                when (adapter!!.getItemViewType(position)) {
                    TYPE_ITEM -> 1  //아이템일 경우 한 줄 당 2개 씩
                    else -> 2       //footer는 한 줄 당 1개 씩
                }
        }
        return manager
    }

    /*
    retrofit 통신 && 파싱
     */
    private fun getProductList(skin_type: String, page: Int) { //피부타입, 페이지에 따라 제품 리스트 가져오기
        disposable!!.add(
            iMyService!!.pagingList(skin_type, page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(1, TimeUnit.SECONDS)
                .retry()
                .doOnError {
                    Log.e("ljwError","getProductList_doOnError")
                }
                .subscribe { response ->
                    val listArray: JSONArray = JSONObject(response).getJSONArray("body")
//                    Log.d("ljwLog-list", listArray.toString())
                    parsingProductList(listArray)
                })
        adapter!!.notifyDataSetChanged()
    }

    private fun queryProduct(skin_type: String, search: String) { //쿼리한 내용에 따라 제품 리스트 가져오기
        disposable!!.add(iMyService!!.queryProduct(skin_type, search)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(1, TimeUnit.SECONDS)
            .retry()
            .doOnError {
                Log.e("ljwError","getProductList_doOnError")
            }
            .subscribe { response ->
                try{
                    val listArray: JSONArray = JSONObject(response).getJSONArray("body")
                    parsingProductList(listArray)
                }catch (e : JSONException){ //"ㄴㅇ", "ㅇㅇㅇ" 등 으로 쿼리시 "데이터가 비어있습니다!"며 JSONException 발생시 제품이 없다
                    tv_noSearchData.visibility = View.VISIBLE
                }
            }
        )
        adapter!!.notifyDataSetChanged()
    }

    private fun parsingProductList(listArray: JSONArray) = try {
        for (i in 0 until listArray.length()) {
            val data = ProductData("", "", "")
            val listObject = listArray.getJSONObject(i)
            data.thumbnailImage = listObject.getString("thumbnail_image")
            data.productTitle = listObject.getString("title")
            data.productPrice = makeCommaForNumber(Integer.parseInt(listObject.getString("price").toString().replace(",", ""))) + "원"
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

    /*
    스피너
     */
    private fun setSpinner() {
        val myAdapter = ArrayAdapter(mContext, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.spinner_array))
        spinner_skintype.adapter = myAdapter
        spinner_skintype.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        if (!isFirst) {
                            sortBySkinType("oily")
                        } else {
                            isFirst = false
                        }
                    }
                    1 -> sortBySkinType("dry")
                    2 -> sortBySkinType("sensitive")
                }
                isQuery = false
                spinner_skintype.setSelection(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    private fun sortBySkinType(skin_type: String) {
        Log.d("ljwLog", disposable!!.isDisposed.toString())
        if(!disposable!!.isDisposed){
            disposable?.clear()
        }

        SharedPreferenceManager.setBoolean(mContext, "noShowProgress", false)
        setSearchviewClear()

        pageCount = 1
        defaultSkinType = skin_type
        adapter!!.removeItem()
        getProductList(defaultSkinType, pageCount)
    }

    //recyclerview item 클릭시 ProductDetailActivity로 productData 옮겨주는 listener
    override fun onItemClicked(productData: ProductData) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("productData", productData as Serializable)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_up, R.anim.normal)
    }

    override fun onStop() {
        super.onStop()
        disposable?.clear()
        SharedPreferenceManager.setBoolean(mContext, "noShowProgress", false)
    }
}
