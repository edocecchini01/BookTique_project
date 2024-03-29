package com.example.booktique.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booktique.dataModel.LibriL
import com.example.booktique.R
import com.google.android.material.textfield.TextInputLayout

class MyAdapterL (private val listaLibri : ArrayList<LibriL>) :
    RecyclerView.Adapter<MyAdapterL.MyViewHolder>() {

    private lateinit var bListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
        fun hideShow(element: LinearLayout, comment : ImageButton)

        fun likeDislike(like: ImageButton, dislike : ImageButton, position: Int)

        fun comment(recensione : TextInputLayout, position: Int)

        fun dettaglioBook(cover: ImageButton, position: Int)

        fun remove(button: ImageButton, position: Int)
    }

    fun setOnCLickItemListener(listener : onItemClickListener){
        bListener = listener
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){

        val cover : ImageButton = itemView.findViewById(R.id.coverL)
        val titolo : TextView = itemView.findViewById(R.id.titoloL)
        val autore : TextView = itemView.findViewById(R.id.autoreL)
        val btnLike : ImageButton = itemView.findViewById(R.id.likeL)
        val btnDislike : ImageButton = itemView.findViewById(R.id.dislikeL)
        val comment : TextInputLayout = itemView.findViewById(R.id.textInputLayout)

        init {

            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }

            itemView.findViewById<ImageButton>(R.id.commentL).setOnClickListener {
                listener.hideShow(itemView.findViewById(R.id.hideLayoutL), itemView.findViewById(R.id.commentL))
                listener.likeDislike(btnLike,btnDislike,absoluteAdapterPosition)
                listener.comment(comment,absoluteAdapterPosition)
            }

            itemView.findViewById<ImageButton>(R.id.coverL).setOnClickListener{
                listener.dettaglioBook(itemView.findViewById(R.id.coverL),bindingAdapterPosition)

            }

            itemView.findViewById<ImageButton>(R.id.deleteL).setOnClickListener {
                listener.remove(itemView.findViewById(R.id.deleteL),bindingAdapterPosition)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.lista_libri_letti,parent,false)
        return MyViewHolder(itemView,bListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = listaLibri[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.copertina)
            .into(holder.cover)
        holder.titolo.text = abbreviaInfo(currentItem?.titolo ?: "",25)

        holder.autore.text = abbreviaInfo(currentItem?.autori ?: "",25)

        if(currentItem?.valutazione == 1)
            holder.btnLike.setImageResource(R.drawable.like_click_icon)
        else if(currentItem?.valutazione == 2)
            holder.btnDislike.setImageResource(R.drawable.dislike_click)

        holder.comment.editText?.setText(currentItem?.recensione ?: "")
    }

    override fun getItemCount(): Int {
        return listaLibri.size
    }

    fun abbreviaInfo(stringa: String, lunghezzaMassima: Int): String {
        return if (stringa.length <= lunghezzaMassima) {
            stringa
        } else {
            val sottostringa = stringa.take(lunghezzaMassima)
            "$sottostringa..."
        }
    }

}