package com.jiwoolee.productlistchallenge

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jiwoolee.productlistchallenge.retrofit.ProductData
import kotlinx.android.synthetic.main.activity_product_detail.*

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val productData: ProductData = intent.extras!!.get("productData") as ProductData
        findViewById<TextView>(R.id.tv_detail_product_title).text = productData.productTitle
        findViewById<TextView>(R.id.tv_detail_product_price).text = productData.productPrice

        Glide.with(iv_detail_product_thumbnail.context)
            .load(productData.thumbnailImage)
            .fitCenter()
            .into(iv_detail_product_thumbnail)
    }
}