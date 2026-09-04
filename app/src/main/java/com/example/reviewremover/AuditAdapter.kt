package com.example.reviewremover

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color

class AuditAdapter(
    private var auditList: List<AuditEntity>,
    // 1. Click callback parameter add karein
    private val onItemClick: (AuditEntity) -> Unit
) : RecyclerView.Adapter<AuditAdapter.AuditViewHolder>() {

    class AuditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReviewId: TextView = itemView.findViewById(R.id.tvReviewId)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvUrl: TextView = itemView.findViewById(R.id.tvReviewUrl)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audit_history, parent, false) // Aapki item layout file ka name
        return AuditViewHolder(view)
    }

    override fun onBindViewHolder(holder: AuditViewHolder, position: Int) {
        val audit = auditList[position]

        holder.tvReviewId.text = audit.extractedReviewId
        holder.tvStatus.text = audit.status
        holder.tvUrl.text = audit.reviewUrl

        // Status Colors Logic
        when (audit.status.uppercase()) {
            "REMOVED" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
            }
            "SUBMITTED" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#FFC107")) // Yellow
            }
            else -> {
                holder.tvStatus.setTextColor(Color.WHITE)
            }
        }

        // 2. Item click listener attach karein
        holder.itemView.setOnClickListener {
            onItemClick(audit)
        }
    }

    override fun getItemCount(): Int = auditList.size

    fun updateData(newList: List<AuditEntity>) {
        this.auditList = newList
        notifyDataSetChanged()
    }
}