package com.hasanbilgin.artbookkotlin


import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore

import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

import com.hasanbilgin.artbookkotlin.databinding.ActivityArtBinding
import java.io.ByteArrayOutputStream
import java.util.Scanner

class ArtActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtBinding

    //galeri açma intenti
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    //izin için
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    //seçilen resim
    var selectedBitMap: Bitmap? = null
    private lateinit var database: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArtBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")) {
            binding.artNameEdittext.setText("")
            binding.artistNameEdittext.setText("")
            binding.yearEdittext.setText("")
            binding.saveButton.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.image)
        } else {
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1) //parametereler dizi şeklinde direk eklenebilir
            var cursor = database.rawQuery("SELECT *FROM arts WHERE id=?", arrayOf(selectedId.toString()))
            val artNameIndex = cursor.getColumnIndex("artname")
            val artistNameIndex = cursor.getColumnIndex("artistname")
            val yearIndex = cursor.getColumnIndex("year")
            val imageIndex = cursor.getColumnIndex("image")
            while (cursor.moveToNext()) {
                binding.artNameEdittext.setText(cursor.getString(artNameIndex))
                binding.artistNameEdittext.setText(cursor.getString(artistNameIndex))
                binding.yearEdittext.setText(cursor.getString(yearIndex))

                val byteArray = cursor.getBlob(imageIndex)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }

        }
    }


    fun saveButtonOnClick(view: View) {

        var artName = binding.artNameEdittext.text.toString()
        var artistName = binding.artistNameEdittext.text.toString()
        var year = binding.yearEdittext.text.toString()
        if (selectedBitMap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitMap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()


            try {
                database.execSQL("CREATE TABLE IF NOT  EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR,artistname VARCHAR, year VARCHAR,image BLOB)")

                val sqlString = "INSERT INTO arts (artname, artistname, year, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArray)
                statement.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val intent = Intent(this@ArtActivity, MainActivity::class.java) //önceki açılan tüm aktivitileri kapatır ve main aktvity geri döner
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

    }

    //boyutunu width ve height küçüklççek
    private fun makeSmallerBitmap(image: Bitmap, maximumSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio: Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) { //Landscape//yatay
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else { //portrait//dikey
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun imageViewOnClick(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { //android 33+ READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) { //izin alma mantığını kullanııya göstereyimmi ? android kendi belirler
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_IMAGES)) { //rationale
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener { //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                } else { //request permission
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery) ///intent
            }
        } else { //android 33+ READ_EXTERNAL_STOREAGE
            //Manifest->androidden
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //izin alma mantığını kullanııya göstereyimmi ? android kendi belirler
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) { //rationale
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener { //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                } else { //request permission
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery) ///intent
            }
        }

    }

    private fun registerLauncher() { //        activityResultLauncher =
        //            registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        //                ActivityResultCallback {
        //
        //                }) //yada

        //        activityResultLauncher =
        //            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        //
        //            }//yada
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    //binding.imageView.setImageURI(imageData)
                    // küçüklte işlemi yapçağımız için bitmapten ilerlicez
                    if (imageData != null) {
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver, imageData)
                                selectedBitMap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitMap)
                            } else { //contentResolver ile this@ArtActivity.contentResolver aynı kullanılabiliyo
                                selectedBitMap = MediaStore.Images.Media.getBitmap(contentResolver, imageData)
                                binding.imageView.setImageBitmap(selectedBitMap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result -> //izin verildiyse
            if (result) {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else { //permission denied
                Toast.makeText(this@ArtActivity, "Permission needed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}