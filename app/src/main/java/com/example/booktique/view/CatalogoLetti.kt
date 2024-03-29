package com.example.booktique.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booktique.dataModel.LibriL
import com.example.booktique.adapter.MyAdapterL
import com.example.booktique.R
import com.example.booktique.databinding.FragmentCatalogoLettiBinding
import com.example.booktique.viewModel.CatalogoViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CatalogoLetti : Fragment() {
    private lateinit var binding: FragmentCatalogoLettiBinding
    private lateinit var recyclerView : RecyclerView
    private lateinit var viewModel: CatalogoViewModel
    private lateinit var adapter: MyAdapterL
    private lateinit var listaLibri: ArrayList<LibriL>
    private lateinit var select: Spinner
    private val sezioni = arrayListOf("Da Leggere","Letti")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            binding = DataBindingUtil.inflate<FragmentCatalogoLettiBinding>(
                inflater,
                R.layout.fragment_catalogo_letti, container, false
            )

        binding.backbuttonL.setOnClickListener{
            val navController = findNavController()
            navController.navigate(R.id.action_catalogoLetti_to_catalogoHome)
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(CatalogoViewModel::class.java)

        listaLibri = ArrayList()

        recyclerView = binding.listaLibriLeggere
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        adapter = MyAdapterL(listaLibri)
        recyclerView.adapter = adapter


        adapter.setOnCLickItemListener(object : MyAdapterL.onItemClickListener {
            override fun onItemClick(position: Int) {

            }

            override fun hideShow(element: LinearLayout, comment: ImageButton) {
                // Gestione del click per mostrare/nascondere un sezione
                val linearL = element
                val btn = comment
                if(linearL.visibility == View.GONE) {
                    linearL.visibility = View.VISIBLE
                    btn.setBackgroundResource(R.drawable.comment_filled_icon)
                }
                else {
                    linearL.visibility = View.GONE
                    btn.setBackgroundResource(R.drawable.comment_icon)
                }

            }

            override fun likeDislike(like: ImageButton, dislike: ImageButton, position: Int) {
                // Gestione del click sui pulsanti "Mi piace" e "Non mi piace"
                val btnLike = like
                val btnDislike = dislike
                val bookPos = position
                val bookId = getIdPos(bookPos)

                btnLike.setOnClickListener {
                    lifecycleScope.launch {
                        if (listaLibri[position].valutazione == 1) {
                            if (bookId != null) {
                                viewModel.removelike(bookId)
                                btnLike.setImageResource(R.drawable.pollice_icon)
                                listaLibri[position].valutazione = 0
                            }
                        } else {
                            if (bookId != null) {
                                viewModel.like(bookId)
                                btnLike.setImageResource(R.drawable.like_click_icon)
                                btnDislike.setImageResource(R.drawable.pollice_icon)
                                listaLibri[position].valutazione = 1
                            }
                        }
                    }
                }

                btnDislike.setOnClickListener {
                    lifecycleScope.launch {
                        if (listaLibri[position].valutazione == 2) {
                            if (bookId != null) {
                                viewModel.removelike(bookId)
                                btnDislike.setImageResource(R.drawable.pollice_icon)
                                listaLibri[position].valutazione = 0
                            }
                        } else {
                            if (bookId != null) {
                                viewModel.dislike(bookId)
                                btnDislike.setImageResource(R.drawable.dislike_click)
                                btnLike.setImageResource(R.drawable.pollice_icon)
                                listaLibri[position].valutazione = 2
                            }
                        }

                    }
                }
            }

            override fun comment(recensione: TextInputLayout, position: Int) {
                // Gestione dell'inserimento di un commento su un libro letto
                val commento = recensione.editText

                if (commento != null) {
                    commento.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {

                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {

                        }

                        override fun afterTextChanged(s: Editable?) {
                            if(s != null && s.isNotEmpty() && s.last() == '\n'){
                                s.replace(s.length - 1, s.length, "")
                                val review = s.toString()
                                val bookPos = position

                                val bookId = getIdPos(bookPos)
                                if (bookId != null) {
                                    viewModel.comment(review, bookId)

                                }
                            }

                        }


                    })
                }
            }
            override fun dettaglioBook(cover: ImageButton, position: Int) {
                // Gestione del click sulla copertina per visualizzare i dettagli di un libro letto
                lifecycleScope.launch {
                    val libro = async {getLibro(position)}

                    val action =
                        CatalogoLettiDirections.actionCatalogoLettiToLibroLetto(
                            libro.await()!!,
                            "catalogoLetti"
                        )
                    findNavController().navigate(action)
                }
            }

            override fun remove(button: ImageButton, position: Int) {
                // Gestione del click sul pulsante rappresentante una x per rimuovere un libro letto
                val dButton = button
                val bookPos = position
                val bookId = getIdPos(bookPos)
                dButton.setOnClickListener {
                    var dialog: AlertDialog? = null
                    val builder = AlertDialog.Builder(requireContext())
                    val dialogView = layoutInflater.inflate(R.layout.dialog_elimina, null)
                    val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)
                    val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
                    builder.setView(dialogView)

                    btnConfirm.setOnClickListener {
                        if (bookId != null) {
                            viewModel.removeBook(bookId, "letti")
                            Toast.makeText(
                                requireContext(),
                                "Libro eliminato con successo!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else{
                            Toast.makeText(
                                requireContext(),
                                "Errore nell'eliminazione!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        dialog?.dismiss()
                    }

                    btnCancel.setOnClickListener {
                        dialog?.dismiss()
                    }

                    dialog = builder.create()
                    dialog?.show()
                }
            }

        })

        viewModel.checkBookCatalogo()
        viewModel.libriLetti.observe(viewLifecycleOwner, Observer { LettiBooksList ->
                loadBooks(LettiBooksList)
            })

    }

    private fun loadBooks(books: List<LibriL>?){
        // Carica i libri letti nella lista listaLibri
        listaLibri.clear()
        if (books != null) {
            listaLibri.addAll(books)
            adapter.notifyDataSetChanged()
        }
    }

    private fun getIdPos(position : Int): String? {
        // Ottiene l'ID del libro in una deteerminata posizione
        if(listaLibri.isNotEmpty()){
            val bookId = listaLibri[position].id
            return bookId
        }
        return null
    }

    private suspend fun getLibro(position: Int): LibriL? {
        // Ottiene i dettagli di un libro letto dal database Firebase
        val bookId = listaLibri[position].id
        var bookD: LibriL? = null

        if (FirebaseAuth.getInstance().currentUser != null) {
            val cUser = FirebaseAuth.getInstance().currentUser!!
            val database =
                FirebaseDatabase.getInstance("https://booktique-87881-default-rtdb.europe-west1.firebasedatabase.app/")
            val usersRef = database.reference.child("Utenti")
            val childRef = usersRef.child(cUser.uid)
            val catalogoRef = childRef.child("Catalogo")
            val lettiRef = catalogoRef.child("Letti")

            if (bookId != null) {
                val dataSnapshot = lettiRef.child(bookId).get().await()
                bookD = dataSnapshot.getValue(LibriL::class.java)
            }
        }
        return bookD
    }

}