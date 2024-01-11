package com.example.myheliports

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Locale

class UserRecyclerAdapter(private val context: Context, private val userList: List<User>) :
    RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder>() {

    private var layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserRecyclerAdapter.ViewHolder {

        val itemView = layoutInflater.inflate(R.layout.item_listluser, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        holder.titleView?.text = user.userName
        holder.itemView.tag = user.documentId
        holder.itemView.setOnClickListener {
            val documentId = it.tag as String
            //Remember last position in list
            Log.d("!!!", "onBindViewHolder position: $position")
            SharedData.position = position
//            (it.context as StartActivity).showLocationFragment(documentId)
        }

        var  dateString = ""
        if (user.timestamp != null) {
            val date = user.timestamp?.toDate()
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
             dateString = format.format(date)
        }

        holder.joinedViewUser?.text = "Joined $dateString"

        holder.imageView?.setImageResource(R.drawable.avatarrobot)

    }

    override fun getItemCount(): Int {
        return userList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleView: TextView? = itemView.findViewById<TextView>(R.id.titleViewUser)
        var joinedViewUser: TextView? = itemView.findViewById<TextView>(R.id.joinedViewUser)
        var imageView: ImageView = itemView.findViewById<ImageView>(R.id.imageViewUser)
    }
}