package com.example.booktique

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

import com.example.booktique.databinding.FragmentScopriPerTeBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScopriPerTe : Fragment() {
    private lateinit var binding:FragmentScopriPerTeBinding
    private val perTeBooksList = mutableListOf<VolumeDet>()
    private lateinit var viewModel: ScopriPerTeViewModel
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentScopriPerTeBinding>(
            inflater,
            R.layout.fragment_scopri_per_te, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ScopriPerTeViewModel::class.java)
        sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        viewModel.perTeBooksList.observe(viewLifecycleOwner, Observer { PerTeBookList ->
            Log.d("SLIDE","Pertebookssss:$PerTeBookList")
            loadBooks(PerTeBookList)
            slideBook()
        })

        scopriButton()
        lifecycleScope.launch() {
            val go = likeBook()
            if(go == true) {
                val call1 = async { viewModel.authorCall("relevance", 15) }
                val call2 = async { viewModel.genCall("relevance", 15) }
                call1.await()
                call2.await()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        binding.buttonPerte.setBackgroundColor(Color.parseColor("#B46060"))
        binding.buttonPerte.setTextColor(Color.parseColor("#FFF4E0"))
    }

    private fun scopriButton(){
        binding.buttonScopri.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_scopriPerTe_to_scopri)
        }
    }

    private suspend fun likeBook() : Boolean{
        var go = true
        val likeBook = viewModel.likeBook()
        if(likeBook.isEmpty()) {
            binding.imageButton2.visibility = View.GONE
            binding.textView7.text = "Non ci sono abbastanza informazioni, torna quando avrai letto altri libri!"
            binding.linearL.visibility = View.GONE
            go = false
        }
        return go
    }


    private fun slideBook() {
        Log.d("SLIDE","Pertebook:$perTeBooksList")
        var currentIndex = sharedPrefs.getInt("currentIndex", 0)
        if (perTeBooksList != null) {
            if (perTeBooksList.isNotEmpty()){
                binding.imageButton2.visibility = View.VISIBLE
                binding.linearL.visibility = View.VISIBLE
                showBook(currentIndex)

                binding.no.setOnClickListener {
                    Log.d("SLIDENO","size:${perTeBooksList.size}")
                    if (currentIndex < (perTeBooksList.size - 1)){
                        currentIndex++
                        sharedPrefs.edit().putInt("currentIndex", currentIndex).apply()
                        Log.d("SLIDENO","Index: $currentIndex")
                        showBook(currentIndex)

                    }else{
                        binding.imageButton2.visibility = View.GONE
                        binding.textView7.text = "Libri terminati! Torna più tardi"
                        binding.linearL.visibility = View.GONE
                        currentIndex = 0
                        sharedPrefs.edit().putInt("currentIndex", currentIndex).apply()
                    }
                }

                binding.si.setOnClickListener {
                    var dialog: AlertDialog? = null
                    val builder = AlertDialog.Builder(requireContext())
                    val dialogView = layoutInflater.inflate(R.layout.dialog_per_te, null)
                    val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)
                    val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
                    builder.setView(dialogView)

                    btnConfirm.setOnClickListener {
                        aggiungiLibro()
                        currentIndex = sharedPrefs.getInt("currentIndex", 0)
                        showBook(currentIndex)
                        dialog?.dismiss()
                    }

                    btnCancel.setOnClickListener {
                        dialog?.dismiss()
                    }

                    dialog = builder.create()
                    dialog?.show()
                }
            }else{
                binding.imageButton2.visibility = View.GONE
                binding.textView7.text = "Caricamento..."
                binding.linearL.visibility = View.GONE
            }
        }
    }

    fun abbreviaInfo(stringa: String, lunghezzaMassima: Int): String {
        return if (stringa.length <= lunghezzaMassima) {
            stringa
        } else {
            val sottostringa = stringa.take(lunghezzaMassima)
            "$sottostringa..."
        }
    }

    private fun aggiungiLibro(){
        var currentIndex = sharedPrefs.getInt("currentIndex", 0)
        val last = viewModel.aggiungiLibro(currentIndex)
        Log.d("AGGIUNGI LIBRO", "last:$last")
        if (last == false) {
                currentIndex++
                sharedPrefs.edit().putInt("currentIndex", currentIndex).apply()
            Log.d("AGGIUNGI LIBRO", "currentindex:$currentIndex")
            } else{
                binding.imageButton2.visibility = View.GONE
                binding.textView7.text = "Libri terminati! Torna più tardi"
                binding.linearL.visibility = View.GONE
            }
        }

    private fun setupImageButtonClickListener(book: VolumeDet, imageButton: ImageButton) {
        imageButton.setOnClickListener {
            val libro = LibriDaL(
                book.title,
                book.imageLinks.thumbnail ?: "",
                book.authors.toString(),
                book.pageCount ?: 0,
                book.id ?: "",
                book.description,
                book.categories.toString()

            )

            val action =
                ScopriPerTeDirections.actionScopriPerTeToDettaglioLibroScopri(libro, "scopriPerTe")
            findNavController().navigate(action)
        }
    }

    private fun loadBooks(books: List<VolumeDet>?){
        var bookk = books
        bookk = bookk?.distinctBy { it.id }
        if (bookk != null) {
            perTeBooksList.addAll(bookk)
            Log.d("LOADBOOKS","$perTeBooksList")
        }
    }

    private fun showBook(currentIndex : Int){
        val book = perTeBooksList[currentIndex]
        binding.textView7.text = abbreviaInfo(book.title.toString(), 20)

        val imageUrl = book?.imageLinks?.thumbnail
        Log.d("Image", "imageUrl: $imageUrl")

        Glide.with(requireContext())
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.let {
                        // Ottieni la lista delle cause radice dell'eccezione
                        val rootCauses = e.rootCauses
                        for (cause in rootCauses) {
                            // Stampa le informazioni sulla causa dell'errore
                            Log.e("Glide1", "Root cause: ${cause.message}")
                        }
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // L'immagine è stata caricata con successo
                    return false
                }
            })

            .into(binding.imageButton2)
        setupImageButtonClickListener(book, binding.imageButton2)
    }

}