package com.example.radninalog

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CollectionAdapter(private val items: List<CollectionItem>) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.collection_recycler_view, parent, false)
        return CollectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {

        when (holder) {
            is CollectionViewHolder -> {
                holder.bind(position, items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class CollectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val startTime: TextView = view.findViewById(R.id.pocetakRecycler)
        val endTime: TextView = view.findViewById(R.id.krajRecycler)
        val elapsedTime: TextView = view.findViewById(R.id.elapsRecycler)
        val pausedTime : TextView = view.findViewById(R.id.pauzaRecycler)
        val materialsUsed : TextView = view.findViewById(R.id.materialRecycler)
        val toolsUsed : TextView = view.findViewById(R.id.toolRecycler)
        val jobType : TextView = view.findViewById(R.id.jobTypeRecycler)
        val yourJob : TextView = view.findViewById(R.id.jobRecycler)
        val firmName : TextView = view.findViewById(R.id.firmRecycler)
        val fullName : TextView = view.findViewById(R.id.nameRecycler)

        @SuppressLint("SetTextI18n")
        fun bind(
            index : Int,
            collectionItem: CollectionItem
        ){
            startTime.text = collectionItem.startTime
            endTime.text = collectionItem.endTime
            elapsedTime.text = collectionItem.elapsedTime
            pausedTime.text = collectionItem.pausedTime
            materialsUsed.text = collectionItem.materialsUsed
            toolsUsed.text = collectionItem.toolsUsed
            jobType.text = collectionItem.jobType
            yourJob.text = collectionItem.yourJob
            firmName.text = "Firm name: " + collectionItem.firmName
            fullName.text = "Full name: " + collectionItem.fullName
        }
    }

}
