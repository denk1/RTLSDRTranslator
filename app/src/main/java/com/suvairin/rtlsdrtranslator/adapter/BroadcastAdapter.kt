package com.suvairin.rtlsdrtranslator.adapter

import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.suvairin.rtlsdrtranslator.App
import com.suvairin.rtlsdrtranslator.PlayerActivity
import com.suvairin.rtlsdrtranslator.databinding.ItemBroadcastBinding
import com.suvairin.rtlsdrtranslator.model.Broadcast

interface BroadcastActionListener {
    fun onBroadClick( broadcast: Broadcast)
}

class BroadcastAdapter() :
    RecyclerView.Adapter<BroadcastAdapter.BroadcastViewHolder>(), View.OnClickListener    {
    private lateinit var context: Context
    var data: List<Broadcast> = emptyList()
        set(newVaue) {
            field = newVaue
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BroadcastViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBroadcastBinding.inflate(inflater, parent, false)

        binding.titleTextView.setOnClickListener(this)
        //binding.likedImageView.setOnClickListener(this)

        return BroadcastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BroadcastViewHolder, position: Int) {
        val broadcast = data[position]
        val context = holder.itemView.context

        with(holder.binding) {

            holder.binding.titleTextView.text = broadcast.title
            holder.binding.titleTextView.tag = broadcast
        }
    }

    override fun onClick(view: View) {
        val broadcast: Broadcast = view.tag as Broadcast // Получаем из тэга человека
        //val dur = Toast.LENGTH_SHORT
        //Toast.makeText(context, broadcast.title + " " + view.id.toString(), dur).show()
        val intent = Intent(view.context, PlayerActivity::class.java)
        intent.putExtra("title", broadcast.title)
        intent.putExtra("location", broadcast.location.toString())
        view.context.startActivity(intent)
        when (view.id) {



        }
    }

    class BroadcastViewHolder(val binding: ItemBroadcastBinding) : RecyclerView.ViewHolder(binding.root)

}