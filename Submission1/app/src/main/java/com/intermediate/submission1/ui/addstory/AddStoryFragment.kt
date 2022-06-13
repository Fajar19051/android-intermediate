package com.intermediate.submission1.ui.addstory

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.intermediate.submission1.R
import com.intermediate.submission1.data.Result
import com.intermediate.submission1.data.network.response.AddStoryResponse
import com.intermediate.submission1.databinding.FragmentAddStoryBinding
import com.intermediate.submission1.utils.reduceFileImage
import com.intermediate.submission1.utils.rotateBitmap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryFragment : Fragment() {
    private var _binding: FragmentAddStoryBinding? = null
    private val binding get() = _binding
    private val viewModel: AddStoryViewModel by viewModels {
        AddStoryViewModelFactory(requireActivity())
    }
    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Navigation.createNavigateOnClickListener(R.id.action_addStoryFragment_to_cameraFragment)
        }
        setFragmentResultListener("requestKey") { _, bundle ->
            val myFile = bundle.getSerializable("picture") as File
            val isBackCamera = bundle.getBoolean("isBackCamera")
            val isFromCamera = bundle.getBoolean("isFromCamera")

            getFile = myFile
            var result = BitmapFactory.decodeFile(myFile.path)
            if(isFromCamera) result = rotateBitmap(result, isBackCamera)
            binding?.previewImageView?.setImageBitmap(result)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddStoryBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAction()
    }

    private fun setupAction() {
        binding?.cameraButton?.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_addStoryFragment_to_cameraFragment)
        )
        binding?.uploadButton?.setOnClickListener {
            val description = binding?.descriptionEditText?.text.toString()
            when {
                description.isEmpty() -> {
                    binding?.descriptionEditText?.error = getString(R.string.input_description)
                }
                getFile == null -> {
                    showMessage(getString(R.string.image_not_found))
                }
                else -> {
                    uploadImage()
                }
            }
        }
    }

    private fun uploadImage() {
        val file = reduceFileImage(getFile as File)

        val description =
            binding?.descriptionEditText?.text.toString()
                .toRequestBody("text/plain".toMediaType())
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )

        viewModel.user.observe(viewLifecycleOwner) { user ->
            viewModel.addStory(user.token, imageMultipart, description,)
                .observe(viewLifecycleOwner) {
                    addStoryObserver(it)
                }
        }
    }

    private fun addStoryObserver(result: Result<AddStoryResponse>) {
        when (result) {
            is Result.Loading -> {
                showLoading(true)
            }
            is Result.Success -> {
                showLoading(false)
                showMessage(getString(R.string.successfully_uploaded),true)
                view?.findNavController()?.navigate(R.id.action_addStoryFragment_to_homeFragment)
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

    private fun showMessage(message: String, isLong: Boolean = false) {
        val toastLength = if(isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        if (message.isNotEmpty()) {
            Toast.makeText(activity, message, toastLength).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}