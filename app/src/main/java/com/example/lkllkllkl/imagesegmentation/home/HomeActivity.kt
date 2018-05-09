package com.example.lkllkllkl.imagesegmentation.home

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.example.lkllkllkl.imagesegmentation.R
import com.example.lkllkllkl.imagesegmentation.data.RetrofitUtils
import com.example.lkllkllkl.imagesegmentation.data.SegmentationRepository
import com.example.lkllkllkl.imagesegmentation.utils.UriUtils
import kotlinx.android.synthetic.main.activity_home.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class HomeActivity : AppCompatActivity() {


    private var curImageUri: Uri? = null
    private val segmentationRepository by lazy { SegmentationRepository() }
    private var segUrl: Int = SegmentationRepository.VOC_SEGMENTATION
    private val changeServerDialog by lazy {
        val view = LayoutInflater.from(this).inflate(R.layout.home_dialog_change_server, null, false)
        val homeEtChangeServer = view.findViewById<EditText>(R.id.homeEtChangeServer)
        AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("CONFIRM", { _, _ ->
                    val server = homeEtChangeServer.text.toString()
                    RetrofitUtils.BASE_URL = server
                }).create()
    }
    private val loadingDialog by lazy {
        val dialog = AlertDialog.Builder(this)
                .setView(R.layout.home_dialog_loading)
                .setCancelable(false)
                .create()
        dialog.window.setLayout(resources.getDimensionPixelOffset(
                R.dimen.editor_bg_header_width),
                WindowManager.LayoutParams.MATCH_PARENT)

        dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        homeTvTakePhoto.setOnClickListener { takePhoto() }
        homeTvTakePhoto.setOnLongClickListener {
            changeServerDialog.show()
            true
        }
        homeTvSelFromGallery.setOnClickListener { selectImageFromGallery() }
        homeTvSelFromGallery.setOnLongClickListener {
            segUrl = if (segUrl == SegmentationRepository.VOC_SEGMENTATION) {
                SegmentationRepository.CLOTH_SEGMENTATION
            } else {
                SegmentationRepository.VOC_SEGMENTATION
            }
            Toast.makeText(this, "segmentation mode: $segUrl", Toast.LENGTH_SHORT).show()
            true
        }
    }


    @AfterPermissionGranted(RC_CAMERA_AND_WRITE_EXTERNAL)
    private fun takePhoto() {
        val perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            curImageUri = createImageUri(this)
            val intent = Intent()
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            intent.putExtra(MediaStore.EXTRA_OUTPUT, curImageUri)
            startActivityForResult(intent, RC_TAKE_PHOTO)
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.camera_and_write_external_rationale),
                    RC_CAMERA_AND_WRITE_EXTERNAL,
                    *perms)
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, RC_SELECT_PHOTO)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            RC_TAKE_PHOTO -> onTakePhotoResult(resultCode)
            RC_SELECT_PHOTO -> onSelectPhotoResult(resultCode, data)
        }

        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun onTakePhotoResult(resultCode: Int) {
        curImageUri?.let {
            if (resultCode == Activity.RESULT_OK) {
                segmentImg(it)
            } else {
                delImageUri(this, it)
            }
        }
    }

    private fun onSelectPhotoResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            segmentImg(data.data)
        }
    }


    private fun createImageUri(context: Context): Uri {
        val contentValues = ContentValues()
        return context
                .contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun delImageUri(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null, null)

    }


    private val segCallback by lazy { SegmentationCallback(this) }
    private fun segmentImg(uri: Uri) {
        UriUtils.getPathByUri(this, uri)?.let {
            showLoading(true)
            segmentationRepository.segmentation(it, segUrl, segCallback)
        }
    }

    fun showLoading(show: Boolean) {
        if (show) loadingDialog.show() else loadingDialog.dismiss()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    companion object {
        const val EXTRA_SEGMENTATION_IMG = "extra_segmentation_img"
        const val RC_BASE = 8000
        const val RC_TAKE_PHOTO = RC_BASE + 1
        const val RC_SELECT_PHOTO = RC_BASE + 2
        const val RC_BASE_PERMISSION = 9000
        const val RC_CAMERA_AND_WRITE_EXTERNAL = RC_BASE_PERMISSION + 1
    }
}
