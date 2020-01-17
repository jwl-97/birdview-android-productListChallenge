package com.jiwoolee.productlistchallenge

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.jiwoolee.productlistchallenge.retrofit.ProductData
import kotlinx.android.synthetic.main.recyclerview_item.view.*
import java.util.*

const val TYPE_ITEM = 1
const val TYPE_FOOTER = 2

class RecyclerviewAdapter(private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<ViewHolder>(),
    Filterable {
    private var listData = ArrayList<ProductData>()
    var unFilteredlist: ArrayList<ProductData> = listData

    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_ITEM -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)
                MyViewHolder(view)
            }
            else -> {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_footer, parent, false)
                FooterViewHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == listData.size) TYPE_FOOTER else TYPE_ITEM
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            val itemViewHolder: MyViewHolder = holder
            itemViewHolder.onBind(listData[position], itemClickListener)
        }
    }

    class MyViewHolder internal constructor(itemView: View) : ViewHolder(itemView) {
        internal fun onBind(productData: ProductData, clickListener: OnItemClickListener) {
            itemView.tv_product_title.text = productData.productTitle
            itemView.tv_product_price.text = productData.productPrice
            Glide.with(itemView.context)
                .load(productData.thumbnailImage)
                .fitCenter()
                .into(itemView.iv_product_thumnail)

            itemView.setOnClickListener { clickListener.onItemClicked(productData) }
        }
    }

    internal class FooterViewHolder(footerView: View?) : ViewHolder(footerView!!)

    fun addItem(productData: ProductData) {
        listData.add(productData)
    }

    fun removeItem(){
        listData.clear()
    }
    override fun getItemCount(): Int {
        return listData.size + 1
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) { //아무것도 입력하지 않았거나 입력내용을 지운 경우
                    listData = unFilteredlist
                } else {
                    val filteredList = ArrayList<ProductData>()
                    for (name in unFilteredlist) {
                        if (name.productTitle.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(name)
                        }
                    }
                    listData = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = listData
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                listData = filterResults.values as ArrayList<ProductData>
                notifyDataSetChanged()
            }
        }
    }
}

interface OnItemClickListener{
    fun onItemClicked(productData: ProductData)
}