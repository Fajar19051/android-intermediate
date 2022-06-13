package com.intermediate.submission1.ui.addstory

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.intermediate.submission1.R
import com.intermediate.submission1.data.Result
import com.intermediate.submission1.data.network.response.AddStoryResponse
import com.intermediate.submission1.databinding.FragmentAddStoryBinding
import com.intermediate.submission1.ui.main.MainActivity
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
    private var location: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isBackCamera: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            Navigation.createNavigateOnClickListener(R.id.action_addStoryFragment_to_cameraFragment)
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setFragmentResultListener("requestKey") { _, bundle ->
            val myFile = bundle.getSerializable("picture") as File
            isBackCamera = bundle.getBoolean("isBackCamera")

            getFile = myFile

            val result = rotateBitmap(BitmapFactory.decodeFile(myFile.path), isBackCamera)
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
        getMyLastLocation()
        setupAction()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showBottomNavigationBar(false)
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
        val result = rotateBitmap(BitmapFactory.decodeFile(getFile?.path),isBackCamera)
        val file = reduceFileImage(result, getFile as File)

        val description =
            binding?.descriptionEditText?.text.toString()
                .toRequestBody("text/plain".toMediaType())
        val lat =
            location?.latitude.toString().toRequestBody("text/plain".toMediaType())
        val lon =
            location?.longitude.toString().toRequestBody("text/plain".toMediaType())
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )

        viewModel.user.observe(viewLifecycleOwner) { user ->
            viewModel.addStory(user.token, imageMultipart, description, lat, lon)
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
                showMessage(getString(R.string.successfully_uploaded), true)
                view?.findNavController()?.navigate(R.id.action_addStoryFragment_to_homeFragment)
            }
            is Result.Error -> {
                showLoading(false)
                showMessage(getString(R.string.something_wrong))
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.CAMERA] == true -> {
                Toast.makeText(activity,R.string.permission_granted, Toast.LENGTH_SHORT).show()
            }
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                getMyLastLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getMyLastLocation()
            }
            else -> {
                Toast.makeText(activity,R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation: Location? ->
                if (lastLocation != null) {
                    location = lastLocation
                } else {
                    Toast.makeText(activity, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showMessage(message: String, isLong: Boolean = false) {
        val toastLength = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        if (message.isNotEmpty()) {
            Toast.makeText(activity, message, toastLength).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as MainActivity).showBottomNavigationBar(true)
    }
}