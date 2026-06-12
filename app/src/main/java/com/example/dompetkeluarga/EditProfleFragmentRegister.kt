package com.example.dompetkeluarga

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.dompetkeluarga.databinding.FragmentEditProfileUserBinding
import com.example.dompetkeluarga.databinding.FragmentEditProfleRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.min

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfleFragmentRegister.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfleFragmentRegister : Fragment() {
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var storageRef: StorageReference
    private var _binding: FragmentEditProfleRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var auth: FirebaseAuth


    private val CAMERA_REQUEST_CODE = 101
    private val STORAGE_REQUEST_CODE = 102

    private val getImageResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { imageUri ->
                    profileImageView.setImageURI(imageUri)
                    uploadProfileImage(imageUri)
                }
            }
        }

    private val takePictureResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.extras?.get("data")?.let { imageBitmap ->
                    val imageUri = getImageUriFromBitmap(imageBitmap as Bitmap)
                    profileImageView.setImageURI(imageUri)
                    uploadProfileImage(imageUri)
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebaseRef = FirebaseDatabase.getInstance().getReference("Users")
        storageRef = FirebaseStorage.getInstance().getReference("Images_Profile")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfleRegisterBinding.inflate(inflater, container, false)
        profileImageView = binding.adminProfileImage
        nameTextView = binding.adminNameText
        // Logout Button Listener
        binding.BtnLog.setOnClickListener {
            performLogout()
        }
        binding.RegisterAdmin.setOnClickListener {
            findNavController().navigate(R.id.action_editProfleFragmentRegister_to_registrasiAdminFragment)
        }
        // Fetch user profile data including profile image
        fetchUserProfile()
        binding.editProfileButton.setOnClickListener {
            showImageSourceChooser()
        }
        return binding.root
    }
    private fun showImageSourceChooser() {
        val options = arrayOf("Select from Gallery", "Take a Photo")
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkAndRequestStoragePermission()
                    1 -> checkAndRequestCameraPermission()
                }
            }
            .show()
    }
    private fun checkAndRequestStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            showImagePickerIntent()
        } else {
            requestStoragePermission()
        }
    }
    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(requireContext(), "Storage permission is required to pick an image", Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_REQUEST_CODE
        )
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            showCameraIntent()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA)) {
            Toast.makeText(requireContext(), "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImagePickerIntent()
                } else {
                    Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showCameraIntent()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showImagePickerIntent() {
        val optionsIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImageResult.launch(optionsIntent)
    }

    private fun showCameraIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            takePictureResult.launch(takePictureIntent)
        } else {
            Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "temp_image.jpg")
        val outputStream: OutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return Uri.fromFile(file)
    }
    private fun uploadProfileImage(imageUri: Uri) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val imageRef = storageRef.child("image_profile/${user.uid}/profile_${System.currentTimeMillis()}.jpg")
            val compressedImageUri = compressImage(imageUri)

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            imageRef.putFile(compressedImageUri, metadata)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        Log.d("Upload Success", "Image uploaded successfully: $uri")
                        firebaseRef.child(user.uid).child("profileImage").setValue(uri.toString())
                            .addOnSuccessListener {
                                Glide.with(requireContext()).load(uri).into(profileImageView)
                                Toast.makeText(requireContext(), "Profile picture updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Upload Error", "Error updating profile picture URL in database", e)
                                Toast.makeText(requireContext(), "Failed to update profile picture URL in database", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Upload Error", "Error uploading image", e)
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun compressImage(uri: Uri): Uri {
        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
        val maxWidth = 1024 // Adjust as needed for better performance and quality
        val maxHeight = 1024 // Adjust as needed for better performance and quality

        // Scale bitmap to a maximum size
        val width = bitmap.width
        val height = bitmap.height
        val ratio = min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // Save the scaled bitmap to a temporary file
        val compressedFile = File(requireContext().cacheDir, "compressed_image.jpg")
        val outputStream: OutputStream = FileOutputStream(compressedFile)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        outputStream.close()

        return Uri.fromFile(compressedFile)
    }
    private fun fetchUserProfile() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            firebaseRef.child(user.uid).get().addOnSuccessListener { dataSnapshot ->
                val name = dataSnapshot.child("nama").getValue(String::class.java)
                val profileImageUrl = dataSnapshot.child("profileImage").getValue(String::class.java)

                nameTextView.text = name ?: "Name not set"
                profileImageUrl?.let {
                    Glide.with(requireContext()).load(it).into(profileImageView)
                }
            }
                .addOnFailureListener { e ->
                    Log.e("Fetch Error", "Failed to fetch user profile", e)
                }
        }
    }

    private fun performLogout() {
        // Sign out from Firebase Auth
        auth.signOut()

        // Redirect to LoginActivity
        val intent = Intent(requireContext(), Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            EditProfleFragmentRegister().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}