package com.example.dompetkeluarga


import android.app.Activity.RESULT_OK
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.example.dompetkeluarga.databinding.FragmentAddDataBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddDataFragment : Fragment() {
    private var _binding: FragmentAddDataBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance() // Initialize FirebaseAuth
    private var photoUri: Uri? = null
    private var isDatePickerShown = false

    // Register the activity result launcher for camera capture
    private val cameraResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.extras?.get("data")?.let { imageBitmap ->
                val bitmap = imageBitmap as Bitmap
                val compressedBitmap = compressImage(bitmap)
                photoUri = saveBitmapToTempFile(compressedBitmap)
                binding.imgCamera.setImageBitmap(compressedBitmap)
            }
        }
    }

    // Register the activity result launcher for storage image selection
    private val storageResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let {
                photoUri = it
                binding.imgCamera.setImageURI(photoUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddDataBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        setupExitButton()
        setupDropdowns()
        setupDatePicker()
        setupCurrencyFormatter()
        setupUploadButton()
        binding.btnSave.setOnClickListener { saveDataToFirebase() }
    }

    private fun setupExitButton() {
        binding.btnExit.setOnClickListener {
            findNavController().navigate(R.id.action_addDataFragment_to_homeFragment_user)
        }
    }

    private fun setupDropdowns() {
        val dropdownOptions = listOf("Pendapatan", "Pengeluaran")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, dropdownOptions)
        binding.aCPkeluarga.setAdapter(adapter)

        val kategoriOptions = listOf("Bonus", "Hiburan", "Gajian", "Tagihan", "Lain-lain")
        val kategoriAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, kategoriOptions)
        binding.acKategori.setAdapter(kategoriAdapter)
    }

    private fun setupDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        binding.edtTanggal.setOnClickListener {
            if (!isDatePickerShown) {
                datePicker.show(childFragmentManager, "DATE_PICKER")
                isDatePickerShown = true
            }
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault()) // Format: day/month/year
            binding.edtTanggal.setText(sdf.format(Date(selection)))
            isDatePickerShown = false
        }

        datePicker.addOnDismissListener {
            isDatePickerShown = false
        }
    }

    private fun setupCurrencyFormatter() {
        val editText = binding.edtJumlahUang
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[Rp,.]".toRegex(), "")

                    try {
                        val parsed = cleanString.toDouble()
                        val formatted = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                            maximumFractionDigits = 0
                        }.format(parsed)

                        current = formatted
                        editText.setText(formatted)
                        editText.setSelection(formatted.length)
                    } catch (e: NumberFormatException) {
                        current = ""
                        Toast.makeText(requireContext(), "Input tidak valid", Toast.LENGTH_SHORT).show()
                    }

                    editText.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupUploadButton() {
        binding.btnUploadImage.setOnClickListener {
            openCameraOrStorageOptions()
        }
    }

    private fun openCameraOrStorageOptions() {
        val options = arrayOf("Camera", "Storage")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCameraCapture()
                    1 -> openStoragePicker()
                }
            }
            .show()
    }

    private fun openCameraCapture() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            cameraResultLauncher.launch(cameraIntent)
        } else {
            Toast.makeText(requireContext(), "Camera tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openStoragePicker() {
        val storageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        storageIntent.type = "image/*"
        storageResultLauncher.launch(storageIntent)
    }

    private fun compressImage(bitmap: Bitmap): Bitmap {
        val maxWidth = 1024
        val maxHeight = 1024

        val width = bitmap.width
        val height = bitmap.height
        val ratio = min(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun saveBitmapToTempFile(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "temp_image.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        }
        return Uri.fromFile(file)
    }

    private fun uploadPhotoToFirebase(uri: Uri?, callback: (String?) -> Unit) {
        uri?.let {
            val storageRef = storage.child("Location")
            val photoRef = storageRef.child("Location_${System.currentTimeMillis()}.jpg")
            photoRef.putFile(it).addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    callback(downloadUri?.toString())
                }
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal mengunggah foto: ${e.message}", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        } ?: run {
            Toast.makeText(requireContext(), "URI foto tidak valid", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    private fun saveDataToFirebase() {

        val type = binding.aCPkeluarga.text.toString()
        val category = binding.acKategori.text.toString()
        val date = binding.edtTanggal.text.toString()
        val amountString = binding.edtJumlahUang.text.toString()
        val note = binding.edtCatatan.text.toString()

        if (type.isEmpty() || category.isEmpty() || date.isEmpty() || amountString.isEmpty()) {
            Toast.makeText(requireContext(), "Harap lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val cleanAmountString = amountString.replace("[Rp,.]".toRegex(), "")
        val amount = cleanAmountString.toDoubleOrNull()

        if (amount == null) {
            Toast.makeText(requireContext(), "Jumlah uang tidak valid", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = auth.currentUser?.uid ?: return
        database.child("Users").child(userId).get().addOnSuccessListener { snapshot ->
            val userName = snapshot.child("nama").getValue(String::class.java) ?: "Unknown User"

            val data = mutableMapOf<String, Any>(
                "type" to type,
                "category" to category,
                "date" to date,
                "amount" to amount,
                "note" to note,
                "userName" to userName,
                "userId" to userId
            )

            // Upload the photo and then save data to Firebase
            uploadPhotoToFirebase(photoUri) { photoUrl ->
                if (photoUrl != null) {
                    data["imageLocation"] = photoUrl // Add the photo URL to the data map
                }

                // Generate a unique ID for each transaction using push()
                val transactionRef = database.child("transactions").push()
                data["id"] = transactionRef.key ?: "unknown_id" // Add the unique ID to the data map

                transactionRef.setValue(data).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigate(R.id.action_addDataFragment_to_homeFragment_user)
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Gagal menyimpan data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}