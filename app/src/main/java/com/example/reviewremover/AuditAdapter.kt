package com.example.reviewremover

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AuditAdapter(private var list: List<AuditEntity>) :
    RecyclerView.Adapter<AuditAdapter.AuditViewHolder>() {

    class AuditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReviewId: TextView = itemView.findViewById(R.id.tvReviewId)
        val tvReviewUrl: TextView = itemView.findViewById(R.id.tvReviewUrl)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audit_history, parent, false)
        return AuditViewHolder(view)
    }

    override fun onBindViewHolder(holder: AuditViewHolder, position: Int) {
        val item = list[position]
        holder.tvReviewId.text = "ID: ${item.extractedReviewId}"
        holder.tvReviewUrl.text = item.reviewUrl

        // Status Logic: Simple "Submitted" & "Removed"
        when (item.status) {
            "REMOVED", "REMOVED_FROM_MAPS" -> {
                holder.tvStatus.text = "Status: Removed"
                holder.tvStatus.setTextColor(Color.GREEN)
            }
            else -> {
                // Default submitted / pending status
                holder.tvStatus.text = "Status: Submitted"
                holder.tvStatus.setTextColor(Color.parseColor("#FFC107")) // Yellow
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<AuditEntity>) {
        list = newList
        notifyDataSetChanged()
    }
}