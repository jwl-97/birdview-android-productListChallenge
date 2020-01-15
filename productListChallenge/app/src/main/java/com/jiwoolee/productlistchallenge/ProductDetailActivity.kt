package com.jiwoolee.productlistchallenge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jiwoolee.productlistchallenge.retrofit.ProductData
import kotlinx.android.synthetic.main.activity_product_detail.*

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        setData()
    }

    private fun setData(){
        val productData: ProductData = intent.extras!!.get("productData") as ProductData
        tv_detail_product_title.text = productData.productTitle
        tv_detail_product_price.text = productData.productPrice

        Glide.with(iv_detail_product_thumbnail.context)
            .load(productData.thumbnailImage)
            .fitCenter()
            .into(iv_detail_product_thumbnail)

        btn_cancel.setOnClickListener { finish() }
    }
}