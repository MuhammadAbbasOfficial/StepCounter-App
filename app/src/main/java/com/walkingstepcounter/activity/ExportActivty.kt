package com.walkingstepcounter.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.walkingstepcounter.databinding.ActivityExportActivtyBinding
import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.walkingstepcounter.R

class ExportActivty : AppCompatActivity() {

    private lateinit var binding: ActivityExportActivtyBinding

    // Lazy initialization of the permission launcher
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun finish() {
        super.finish()
        /*val options = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_out_left,  // Animation for entering back
            R.anim.slide_in_right // Animation for exiting ExportActivity
        )
        startActivity(intent, options.toBundle())*/
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExportActivtyBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initializePermissionLauncher()



        val steps = intent.getIntExtra("steps", 0) // Default to 0 if not passed
        val distance = intent.getIntExtra("distance", 0)


        Log.d("bundles", "onCreate: steps = $steps , distance = $distance")


        binding.steps.text = steps.toString()

        binding.distance.text = "$distance m"

        binding.snapBtn.setOnClickListener{

            requestStoragePermissionAndTakeScreenshot(binding.cardView)

        }

        binding.facebookBtn.setOnClickListener{
            shareImageToApp("com.facebook.katana", binding.cardView)
        }

        binding.whatsappBtn.setOnClickListener{
            shareImageToApp("com.whatsapp", binding.cardView)
        }

        binding.instagramBtn.setOnClickListener{
            shareImageToApp("com.instagram.android", binding.cardView)
        }

        binding.menuBtn.setOnClickListener{
            shareImageToApp(null, binding.cardView)
        }


        setContentView(binding.root)

    }

    // Function to initialize permission launcher (call this in onCreate)
    fun Activity.initializePermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to request storage permission and take a screenshot
    fun Activity.requestStoragePermissionAndTakeScreenshot(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            takeScreenshotAndSave(view)
        } else {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    takeScreenshotAndSave(view)
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }

    // Function to capture and save the screenshot
    private fun Activity.takeScreenshotAndSave(view: View) {
        val bitmap = captureViewAsBitmap(view)
        saveBitmapToStorage(bitmap, this)
    }

    // Function to convert the view to a bitmap
    private fun captureViewAsBitmap(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    // Function to save the bitmap to storage based on Android version
    private fun saveBitmapToStorage(bitmap: Bitmap, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Save using MediaStore for Android 10 and above
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "StepCounter_$timestamp.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Screenshots")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val uri = context.contentResolver.insert(contentUri, contentValues)

            if (uri != null) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(uri, contentValues, null, null)
                        Toast.makeText(context, "Screenshot saved to gallery", Toast.LENGTH_SHORT).show()
                        Log.d("Saved", "Screenshot saved to $uri")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // For older versions, save to app-specific external storage
            val file = createImageFile(context)
            if (file != null) {
                try {
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.flush()
                        Toast.makeText(context, "Screenshot saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                        Log.d("Saved", "Screenshot saved to ${file.absolutePath}")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    // Function to create an image file in the Pictures directory for pre-Android Q versions
    private fun createImageFile(context: Context): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "StepCounter_$timestamp.jpg"
//        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return if (storageDir != null) {
            File(storageDir, imageFileName)
        } else null
    }


    /*--------------------------------------------------sharing functions--------------------------------------------------------*/
    // Function to share the image to a specific app
    private fun shareImageToApp(packageName: String?, view: View) {

        // Check if the app is installed
        if (packageName != null && !isAppInstalled(packageName)) {
            Toast.makeText(this, "App is not installed", Toast.LENGTH_SHORT).show()
            return
        }


        // First, capture the image as a bitmap
        val bitmap = captureViewAsBitmap1(view)

        // Save the bitmap to a temporary file
        val imageFile = createTemporaryImageFile(bitmap)

        // If the image was successfully saved, proceed to share
        if (imageFile != null) {
            val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", imageFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (packageName != null) {
                    setPackage(packageName)
                }
            }

            // Start the share intent
            startActivity(Intent.createChooser(intent, "Share Image"))
        }
    }

    // Function to capture the view as a bitmap
    private fun captureViewAsBitmap1(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    // Function to save the bitmap as a temporary image file
    private fun createTemporaryImageFile(bitmap: Bitmap): File? {
        return try {
            val imageFile = File.createTempFile("share_image_", ".jpg", cacheDir)
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
            }
            imageFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }






}

