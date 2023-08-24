package com.example.booktique

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booktique.databinding.FragmentScopriGenereBinding
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScopriGenere : Fragment() {
    private lateinit var binding: FragmentScopriGenereBinding
    private lateinit var recyclerView : RecyclerView
    private lateinit var viewModel: ScopriViewModel
    private lateinit var adapter: MyAdapterGenere
    private lateinit var listaLibri: ArrayList<VolumeDet>
    private var param: String? = null
    private var ricerca: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<FragmentScopriGenereBinding>(
            inflater,
            R.layout.fragment_scopri_genere, container, false
        )

        arguments?.let { args ->
            ricerca = requireArguments().getBoolean("ricerca")
            param = requireArguments().getString("genere")
            Log.d("param", param.toString())
            if(param != null){
                if(ricerca)
                    binding.genere.text = "Ricerca: "+ param
                else
                    binding.genere.text = param
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ScopriViewModel::class.java)
        binding.backbuttonGen.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_scopriGenere_to_scopri)
        }

        listaLibri = ArrayList()
        recyclerView = binding.listaLibriScopriGenere
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        adapter = MyAdapterGenere(listaLibri)
        recyclerView.adapter = adapter

        if(!ricerca) {
            val queryparameter = "subject:" + param
            Log.d("TAG", queryparameter)
            viewModel.searchBooks(queryparameter)
            viewModel.genreBooks.observe(viewLifecycleOwner, Observer { newestBooksList ->
                Log.d("TAG", newestBooksList.toString())
                loadBooks(newestBooksList)
            })
        }else{
            viewModel.searchBooks(param!!)
            viewModel.genreBooks.observe(viewLifecycleOwner, Observer { newestBooksList ->
                Log.d("TAG", newestBooksList.toString())
                loadBooks(newestBooksList)
            })
        }

        adapter.setOnCLickItemListener(object: MyAdapterGenere.onItemClickListener {

            override fun dettaglioBook(cover: ImageButton, position: Int) {
                val libro = getLibro(position)

                val navController = findNavController()
                val action = ScopriGenereDirections.actionScopriGenereToDettaglioLibroScopri(
                    libro,
                    "scopriGenere"
                )
                findNavController().navigate(action)
            }
        })
    }

    private fun loadBooks(books: List<VolumeDet>?){
        if (books != null) {
            listaLibri.addAll(books)
            adapter.notifyDataSetChanged()
        }
    }

    private fun getLibro(position: Int): LibriDaL {
        val libro = LibriDaL(
            listaLibri[position].title,
            listaLibri[position].imageLinks.thumbnail ?: "",
            listaLibri[position].authors.toString(),
            listaLibri[position].pageCount ?: 0,
            listaLibri[position].id ?: "",
            listaLibri[position].description,
            listaLibri[position].categories.toString()
        )
        return libro
    }
}