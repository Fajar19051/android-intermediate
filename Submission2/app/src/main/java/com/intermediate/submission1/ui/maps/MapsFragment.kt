package com.intermediate.submission1.ui.maps

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.intermediate.submission1.R
import com.intermediate.submission1.data.Result
import com.intermediate.submission1.data.models.Story
import com.intermediate.submission1.databinding.FragmentMapsBinding
import com.intermediate.submission1.databinding.MarkerInfoWindowBinding
import com.intermediate.submission1.ui.home.HomeViewModel
import com.intermediate.submission1.ui.home.HomeViewModelFactory
import com.intermediate.submission1.ui.main.MainActivity

class MapsFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(requireActivity())
    }
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).showBottomNavigationBar(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMap()
        setupObserver()
    }

    private fun setupObserver() {
        viewModel.user.observe(viewLifecycleOwner) {
            if (it.token.isNotEmpty()) {
                viewModel.getStoriesWithLocation(it.token).observe(viewLifecycleOwner) { result ->
                    if (result != null) {
                        when (result) {
                            is Result.Loading -> {
                                showLoading(true)
                            }
                            is Result.Success -> {
                                showLoading(false)
                                setListStoryMap(result.data)
                            }
                            is Result.Error -> {
                                showLoading(false)
                                showMessage(getString(R.string.something_wrong))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setListStoryMap(storyList: List<Story>) {
        val firstLocation = LatLng(storyList[0].lat ?: 0.0, storyList[0].lon ?: 0.0)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 15f))
        storyList.forEach {
            if (it.lat !== null && it.lon !== null) {
                val curMarker = mMap.addMarker(
                    MarkerOptions().position(LatLng(it.lat, it.lon)).snippet(it.description)
                        .title(it.name)
                )
                curMarker?.tag = it
            }
        }
        mMap.setOnMarkerClickListener { false }
        mMap.setInfoWindowAdapter(context?.let {MarkerInfoWindowAdapter(it) })
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun showLoading(isLoading: Boolean) {
        binding?.progressBar?.isVisible = isLoading
        binding?.map?.isVisible = !isLoading
    }

    private fun showMessage(message: String) {
        if (message != "") {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMapStyle()
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireActivity(),
                        R.raw.map_style
                    )
                )
            if (!success) {
                Log.e(TAG, getString(R.string.style_parsing_failed))
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, getString(R.string.cant_find_style), exception)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val TAG = "MapFragment"
    }
}

class MarkerInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    private lateinit var _binding: MarkerInfoWindowBinding

    override fun getInfoContents(p0: Marker): View? {
        return null
    }
    override fun getInfoWindow(p0: Marker): View {
        _binding = MarkerInfoWindowBinding.inflate(LayoutInflater.from(context))
        val story = p0.tag as Story
        _binding.markerTitle.text = p0.title
        _binding.markerDescription.text = story.description
        Glide.with(context)
            .load(story.photoUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions().override(550, 550))
            .into(_binding.markerPhoto)
        return _binding.root
    }
}