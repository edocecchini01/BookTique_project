package com.example.booktique.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.booktique.R
import com.example.booktique.databinding.FragmentRegistrazioneBinding
import com.example.booktique.viewModel.AutenticazioneViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class registrazione : Fragment() {

    lateinit var editTextUser :EditText
    lateinit var editTextEmail :EditText
    lateinit var editTextPassword :EditText
    lateinit var buttonReg :Button
    lateinit var mAuth :FirebaseAuth
    lateinit var cUser : FirebaseUser
    private lateinit var viewModel: AutenticazioneViewModel
    private lateinit var binding: FragmentRegistrazioneBinding

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    interface ClickListener {
        fun onScrittaClicked()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate<FragmentRegistrazioneBinding>(inflater,
            R.layout.fragment_registrazione,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(AutenticazioneViewModel::class.java)

        binding.clickListener = object : ClickListener {
            override fun onScrittaClicked() {
                Navigation.findNavController(view).navigate(R.id.action_registrazione_to_login)
            }
        }
        mAuth = FirebaseAuth.getInstance()

        editTextUser = binding.username
        editTextEmail = binding.email
        editTextPassword = binding.password
        buttonReg = binding.btnRegister

        buttonReg.setOnClickListener {
            val username = editTextUser.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Compila tutti i campi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener() { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Account creato!",
                                Toast.LENGTH_SHORT,
                            ).show()

                            viewModel.registrazione(username, password, email)

                            Navigation.findNavController(view)
                                .navigate(R.id.action_registrazione_to_login)

                        } else {
                            // If sign in fails, display a message to the user.
                            val exception = task.exception
                            if (exception != null) {
                                val errorMessage = exception.localizedMessage
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

            }else {
                Toast.makeText(requireContext(), "Inserisci email e password", Toast.LENGTH_SHORT).show()
            }
        }
}
}