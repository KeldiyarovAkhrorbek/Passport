package uz.projects.passport.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import uz.projects.passport.BuildConfig
import uz.projects.passport.BuildConfig.*
import uz.projects.passport.R
import uz.projects.passport.database.MyDatabase
import uz.projects.passport.databinding.FragmentGivePassportBinding
import uz.projects.passport.entity.Passport
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class GivePassportFragment : Fragment(R.layout.fragment_give_passport) {
    private val binding by viewBinding(FragmentGivePassportBinding::bind)
    private lateinit var myDatabase: MyDatabase
    private lateinit var list: ArrayList<Passport>
    private var fileAbsolutePath = ""
    private lateinit var passport: Passport

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        myDatabase = MyDatabase.getInstance(requireContext())
        list = ArrayList(myDatabase.passportDao().getPassportList())
        binding.apply {
            saveBtn.setOnClickListener {
                if (isValid()) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setMessage(
                        "Ma’lumotlariningiz to’g’ri ekanligiga \n" +
                                "ishonchingiz komilmi?"
                    )
                    builder.setPositiveButton("Ha")
                    { _, _ ->
                        myDatabase.passportDao().addPassport(passport)
                        findNavController().popBackStack()
                    }
                    builder.setNegativeButton("Yo'q") { _, which ->

                    }
                    builder.show()
                }
            }
            image.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Tanlang!")
                builder.setMessage("Kameradan rasm olasizmi yoki Galeriyadan rasm tanlaysizmi?")
                builder.setPositiveButton("Kamera")
                { dialog, which ->
                    onResultCamera()
                }
                builder.setNegativeButton("Galeriya") { dialog, which -> onResultGallery() }
                builder.show()
            }

            edPassportDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                val day = calendar[Calendar.DAY_OF_MONTH]
                val month1 = calendar[Calendar.MONTH] + 1
                val year1 = calendar[Calendar.YEAR] + 1
                val datePickerDialog = DatePickerDialog(requireContext(),
                    android.R.style.Theme_Holo_Light_Dialog,
                    { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                        edPassportDate.setText("$dayOfMonth/$month/$year")
                    }, year1, month1, day
                )
                datePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                datePickerDialog.show()
            }

            edPassportDeadline.setOnClickListener {
                val calendar = Calendar.getInstance()
                val day = calendar[Calendar.DAY_OF_MONTH]
                val month1 = calendar[Calendar.MONTH] + 1
                val year1 = calendar[Calendar.YEAR] + 1
                val datePickerDialog = DatePickerDialog(requireContext(),
                    android.R.style.Theme_Holo_Light_Dialog,
                    { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                        edPassportDeadline.setText("$dayOfMonth/$month/$year")
                    }, year1, month1, day
                )
                datePickerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                datePickerDialog.show()
            }
        }
    }

    private fun isValid(): Boolean {
        binding.apply {
            val name = edName.text.toString().trim()
            val surName = edSurname.text.toString().trim()
            val fio = edFio.text.toString().trim()
            val city = edCity.text.toString().trim()
            val address = edAddress.text.toString().trim()
            val date = edPassportDate.text.toString().trim()
            val lifetime = edPassportDeadline.text.toString().trim()
            val regions = resources.getStringArray(R.array.region)
            val region: String = regions[binding.regionSpinner.selectedItemPosition].trim()
            val genders = resources.getStringArray(R.array.gender)
            val gender: String = genders[binding.genderSpinner.selectedItemPosition].trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Ism kiritilmagan", Toast.LENGTH_SHORT).show()
                return false
            } else if (surName.isEmpty()) {
                Toast.makeText(requireContext(), "Familiya kiritilmagan", Toast.LENGTH_SHORT)
                    .show()
                return false
            } else if (fio.isEmpty()) {
                Toast.makeText(requireContext(), "Sharifi kiritlmagan", Toast.LENGTH_SHORT)
                    .show()
                return false
            } else if (city.isEmpty()) {
                Toast.makeText(requireContext(), "Shahar kiritilmagan", Toast.LENGTH_SHORT)
                    .show()
                return false
            } else if (address.isEmpty()) {
                Toast.makeText(requireContext(), "Yashash joyi kiritilmagan", Toast.LENGTH_SHORT)
                    .show()
                return false
            } else if (date.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Pasport olingan sanasi  kiritilmagan",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return false
            } else if (lifetime.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Pasport tugash sanasi kiritilmagan",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return false
            } else if (fileAbsolutePath.isEmpty()) {
                Toast.makeText(requireContext(), "Rasm kiritilmagan", Toast.LENGTH_SHORT).show()
                return false
            } else if (binding.regionSpinner.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Viloyat tanlanmagan", Toast.LENGTH_SHORT).show()
                return false
            } else if (binding.genderSpinner.selectedItemPosition == 0) {
                Toast.makeText(requireContext(), "Jins tanlanmagan", Toast.LENGTH_SHORT).show()
                return false
            }
            passport = Passport(
                name = name,
                surname = surName,
                fio = fio,
                city = city,
                address = address,
                date = date,
                lifetime = lifetime,
                region = region,
                gender = gender,
                seriesNumber = generate(),
                imagePath = fileAbsolutePath
            )
        }

        return true
    }

    private fun generate(): String {
        val a1 = (65..91).random().toChar()
        val a2 = (65..91).random().toChar()
        val number = (1000000..9999999).random()
        val seriesNumber = "$a1$a2 $number"

        val filterList = list.filter {
            it.seriesNumber == seriesNumber
        }
        return if (filterList.isEmpty()) {
            seriesNumber
        } else {
            generate()
        }
    }

    private fun onResultCamera() {
        Dexter.withContext(requireContext())
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    camera()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        var uri: Uri =
                            Uri.fromParts("package", requireActivity().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } else {
                        response.requestedPermission
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: com.karumi.dexter.listener.PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Camera permission!")
                    builder.setMessage("Kameradan foydalanish uchun ruxsat bering!")
                    builder.setPositiveButton("Ruxsat so'rash")
                    { dialog, which ->
                        p1?.continuePermissionRequest()
                    }
                    builder.setNegativeButton("Ruxsat so'ramaslik") { dialog, which -> p1?.cancelPermissionRequest() }
                    builder.show()

                }

            }).check()
    }


    private fun camera() {
        val photoFile = try {
            createImageFile()
        } catch (e: Exception) {
            null
        }
        photoFile?.also {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID,
                it
            )
            getCameraImage.launch(uri)
        }
    }

    private val getImageContent = registerForActivityResult(ActivityResultContracts.GetContent()) {
        binding.image.setImageURI(it)
        val openInputStream = activity?.contentResolver?.openInputStream(it)
        val m = System.currentTimeMillis()
        val file = File(activity?.filesDir, "$m.jpg")
        val fileOutputStream = FileOutputStream(file)
        openInputStream?.copyTo(fileOutputStream)
        openInputStream?.close()
        fileOutputStream.close()
        fileAbsolutePath = file.absolutePath
        Toast.makeText(requireContext(), file.absolutePath, Toast.LENGTH_SHORT).show()
    }

    private val getCameraImage = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            binding.image.setImageURI(Uri.parse(fileAbsolutePath))
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val m = System.currentTimeMillis()
        val externalFilesDir = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("G21_$m", ".jpg", externalFilesDir)
            .apply {
                fileAbsolutePath = absolutePath
            }
    }

    private fun onResultGallery() {
        Dexter.withContext(requireContext())
            .withPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    getImageContent.launch("image/*")
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    if (response.isPermanentlyDenied) {
                        var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        var uri: Uri =
                            Uri.fromParts("package", requireActivity().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } else {
                        response.requestedPermission
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: com.karumi.dexter.listener.PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Gallery permission!")
                    builder.setMessage("Galleriyadan foydalanish uchun ruxsat bering!")
                    builder.setPositiveButton("Ruxsat so'rash!")
                    { dialog, which ->
                        p1?.continuePermissionRequest()
                    }
                    builder.setNegativeButton("Ruxsat so'ramaslik!") { dialog, which -> p1?.cancelPermissionRequest() }
                    builder.show()

                }

            }).check()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        super.onPrepareOptionsMenu(menu)
    }
}