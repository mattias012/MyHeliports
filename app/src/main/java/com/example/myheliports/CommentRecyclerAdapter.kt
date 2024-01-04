package com.example.myheliports

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommentRecyclerAdapter(private val context: Context, private val commentList: List<Comment>) :
    RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder>() {

    private var layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentRecyclerAdapter.ViewHolder {

        val itemView = layoutInflater.inflate(R.layout.item_comment, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentList[position]

        holder.comment?.text = comment.comment
//        holder.itemView.tag = comment.documentId

    }

    override fun getItemCount(): Int {
        return commentList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var comment: TextView? = itemView.findViewById<TextView>(R.id.commentByUser)

    }
}