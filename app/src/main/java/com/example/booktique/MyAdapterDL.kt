package com.example.booktique

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyAdapterDL(private val listaLibri : ArrayList<LibriDaL>) :
    RecyclerView.Adapter<MyAdapterDL.MyViewHolder>() {

    private lateinit var bListener : onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
        fun moveBook(spinner: Spinner, send : ImageButton)
    }

    fun setOnCLickItemListener(listener: onItemClickListener){
        bListener = listener
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){

        val cover : ImageButton = itemView.findViewById(R.id.coverDL)
        val titolo : TextView = itemView.findViewById(R.id.titoloDL)
        val autore : TextView = itemView.findViewById(R.id.autoreDL)

        init {

            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }

            itemView.findViewById<ImageButton>(R.id.sendDL).setOnClickListener {
                listener.moveBook(itemView.findViewById(R.id.spinnerDL),itemView.findViewById(R.id.sendDL))
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.lista_libri_da_leggere,parent,false)
        return MyViewHolder(itemView,bListener)
    }

    override fun getItemCount(): Int {
        return listaLibri.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = listaLibri[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.copertina)
            .into(holder.cover)

        holder.titolo.text = abbreviaInfo(currentItem?.titolo ?: "",25)

        holder.autore.text = abbreviaInfo(currentItem?.autori ?: "",25)
    }

    //fattorizzare
    fun abbreviaInfo(stringa: String, lunghezzaMassima: Int): String {
        return if (stringa.length <= lunghezzaMassima) {
            stringa
        } else {
            val sottostringa = stringa.take(lunghezzaMassima)
            "$sottostringa..."
        }
    }


}