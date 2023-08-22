package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.viewmodel.AuthViewModel

class AuthFragment : Fragment() {

    private val binding = FragmentAuthBinding.inflate(layoutInflater)
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.signInButton.setOnClickListener {
            val login = binding.loginTextField.editText?.text.toString()
            val password = binding.passwordTextField.editText?.text.toString()
            viewModel.saveIdAndToken(login, password)
            findNavController().navigate(R.id.nav_host_fragment)
        }

        return binding.root
    }

//    val inputText = filledTextField.editText?.text.toString()
//
//    filledTextField.editText?.doOnTextChanged { inputText, _, _, _ ->
//        // Respond to input text change
//    }
}