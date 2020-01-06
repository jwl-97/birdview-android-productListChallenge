package com.jiwoolee.productlistchallenge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jiwoolee.productlistchallenge.retrofit.ProductData
import java.util.ArrayList
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recyclerview_item.view.*

class RecyclerviewAdapter : RecyclerView.Adapter<RecyclerviewAdapter.MyViewHolder>() {
    private val listData = ArrayList<ProductData>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recyclerview_item, viewGroup, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.onBind(listData[position])
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    fun addItem(productData: ProductData) {
        listData.add(productData)
    }

    class MyViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal fun onBind(productData: ProductData) {
            itemView.tv_product_title.text = productData.productTitle
            itemView.tv_product_price.text = productData.productPrice
            Glide.with(itemView.context)
                .load(productData.thumbnailImage)
                .fitCenter()
                .into(itemView.tv_product_thumnail)
        }
    }
}
