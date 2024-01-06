package com.hasanbilgin.artbookkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.hasanbilgin.artbookkotlin.databinding.ReclerviewRowBinding

class ArtAdapter(val artList: ArrayList<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    class ArtHolder(val binding: ReclerviewRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding =
            ReclerviewRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtHolder(binding)
    }


    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.nameTextview.text = artList.get(position).name
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ArtActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("id", artList.get(position).id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }

}