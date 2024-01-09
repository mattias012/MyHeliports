package com.example.myheliports

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

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
        getUserName(comment.userId) { userName ->
            holder.commentDateAndUserName?.text = userName
        }

    }

    override fun getItemCount(): Int {
        return commentList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var comment: TextView? = itemView.findViewById<TextView>(R.id.commentByUser)
        var commentDateAndUserName: TextView? = itemView.findViewById(R.id.commentDateAndUserName)
    }
    private fun getUserName(userId: String?, callback: (String) -> Unit) {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("userId", userId).get()
            .addOnSuccessListener { userDocument ->
                //Handle on success
                if (!userDocument.isEmpty) {
                    val document = userDocument.documents[0]
                    // Convert to a User object
                    val user = document.toObject(User::class.java)
                    user?.let {
                        // Call the callback function with the username
                        it.userName?.let { it1 -> callback(it1) }
                    }
                }
            }
    }
}