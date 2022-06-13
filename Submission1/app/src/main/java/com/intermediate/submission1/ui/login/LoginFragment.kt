package com.intermediate.submission1.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.intermediate.submission1.R
import com.intermediate.submission1.data.Result
import com.intermediate.submission1.data.network.response.LoginResponse
import com.intermediate.submission1.databinding.FragmentLoginBinding
import com.intermediate.submission1.ui.main.MainActivity

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(layoutInflater)
        (activity as MainActivity).removeBackButtonActionBar()
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        playAnimation()
        setupAction()
    }

    private fun playAnimation() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val translation = if(isLandscape) View.TRANSLATION_Y else View.TRANSLATION_X
        val valuesTranslation = if(isLandscape) 50f else 30f
        ObjectAnimator.ofFloat(binding?.imageView, translation, -(valuesTranslation), valuesTranslation).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val titleTextView =
            ObjectAnimator.ofFloat(binding?.titleTextView, View.ALPHA, 1f).setDuration(150)
        val messageTextView =
            ObjectAnimator.ofFloat(binding?.messageTextView, View.ALPHA, 1f).setDuration(150)
        val emailTextView =
            ObjectAnimator.ofFloat(binding?.emailTextView, View.ALPHA, 1f).setDuration(150)
        val emailEditText =
            ObjectAnimator.ofFloat(binding?.emailEditText, View.ALPHA, 1f).setDuration(150)
        val passwordTextView =
            ObjectAnimator.ofFloat(binding?.passwordTextView, View.ALPHA, 1f).setDuration(150)
        val passwordEditText =
            ObjectAnimator.ofFloat(binding?.passwordEditText, View.ALPHA, 1f).setDuration(150)
        val loginButton =
            ObjectAnimator.ofFloat(binding?.loginButton, View.ALPHA, 1f).setDuration(150)
        val signupButton =
            ObjectAnimator.ofFloat(binding?.signupButton, View.ALPHA, 1f).setDuration(150)

        AnimatorSet().apply {
            playSequentially(
                titleTextView,
                messageTextView,
                emailTextView,
                emailEditText,
                passwordTextView,
                passwordEditText,
                loginButton,
                signupButton
            )
            start()
        }
    }

    private fun setupAction() {
        binding?.signupButton?.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_loginFragment_to_signupFragment)
        )

        binding?.loginButton?.setOnClickListener {
            val email = binding?.emailEditText?.text.toString()
            val password = binding?.passwordEditText?.text.toString()
            when {
                email.isEmpty() -> {
                    binding?.emailEditText?.error = getString(R.string.input_email)
                }
                password.isEmpty() -> {
                    binding?.passwordEditText?.error = getString(R.string.input_password)
                }
                else -> {
                    viewModel.login(email, password).observe(viewLifecycleOwner) {
                        loginObserver(it)
                    }
                }
            }
        }
    }

    private fun loginObserver(result: Result<LoginResponse>) {
        when (result) {
            is Result.Loading -> {
                showLoading(true)
            }
            is Result.Success -> {
                showLoading(false)
            }
            is Result.Error -> {
                showLoading(false)
                showMessage(getString(R.string.something_wrong))
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showMessage(message: String) {
        if (message != "") {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.option_menu_guest, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            else -> NavigationUI.onNavDestinationSelected(item, requireView().findNavController()) || super.onOptionsItemSelected(item)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}