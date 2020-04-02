package com.example.searchnavershoppingexample

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_result.view.*

class ResultListRecyclerViewAdapter(val resultList: ArrayList<ResultItem>) : RecyclerView.Adapter<ResultListRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultListRecyclerViewAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ResultListRecyclerViewAdapter.ViewHolder, position: Int) {
        holder.bind(resultList[position])
    }

    override fun getItemCount(): Int {
        return resultList.size
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title = itemView.title_textview
        val thumbnail = itemView.thumbnail_imageview
        var link: String = ""

        fun bind (data: ResultItem) {
            if (data.image != "내용 없음") {
                Glide.with(view.context).load(data.image)
                    .apply(RequestOptions().override(120, 120))
                    .apply(RequestOptions.fitCenterTransform())
                    .into(itemView.thumbnail_imageview)
            } else {
                thumbnail.setImageResource(R.drawable.ic_image_24dp)
            }

            title.text = data.title
            link = data.link

            itemView.setOnClickListener({
                val url = Uri.parse(link)
                val intent = Intent(Intent.ACTION_VIEW, url)
                view.context.startActivity(intent)
            })
        }
    }
}