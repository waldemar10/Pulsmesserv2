package com.example.pulsmesserv2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView



class BPMModel_RecyclerViewAdapter(

    private val bpmList: ArrayList<BPMModel>,
    private val context: Context,
    private val myViewModel: MyViewModel,
    private val updateDeleteButtonCallback: () -> Unit
) : RecyclerView.Adapter<BPMModel_RecyclerViewAdapter.BPMModel_ViewHolder>() {



    class BPMModel_ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val bpm: TextView = itemView.findViewById(R.id.twBpm)
        val date: TextView = itemView.findViewById(R.id.twDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BPMModel_ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.recycler_view_row, parent, false)
        return BPMModel_ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bpmList.size
    }

    override fun onBindViewHolder(holder: BPMModel_ViewHolder, position: Int) {
        val item = bpmList[position]
        holder.bpm.text = item.bpm.toString()
        holder.date.text = item.date


        if (item.selected) {
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.selected))
        } else {
            holder.cardView.setCardBackgroundColor(context.getColor(android.R.color.white))
        }
        // CHECK IF USER SELECTED ITEMS TO REMOVE

        holder.itemView.setOnClickListener {
            item.selected = !item.selected
            bpmList[position].selected = item.selected
            myViewModel.saveToFile("bpm_data.txt", bpmList)
            notifyItemChanged(position)
            updateDeleteButtonCallback()

        }
    }



}
