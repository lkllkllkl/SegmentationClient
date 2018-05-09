package com.example.lkllkllkl.imagesegmentation.editor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.example.lkllkllkl.imagesegmentation.R
import com.example.lkllkllkl.imagesegmentation.data.BackgroundEntity
import com.example.lkllkllkl.imagesegmentation.data.SegmentationImageEntity
import com.example.lkllkllkl.imagesegmentation.home.HomeActivity.Companion.EXTRA_SEGMENTATION_IMG
import com.example.lkllkllkl.imagesegmentation.utils.GlideApp
import com.example.lkllkllkl.imagesegmentation.utils.UriUtils
import com.example.lkllkllkl.photoedit.PhotoEditor
import kotlinx.android.synthetic.main.activity_editor.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class EditorActivity : AppCompatActivity() {

    private lateinit var photoEditor: PhotoEditor
    private val loadingDialog by lazy {
        val view = LayoutInflater.from(this).inflate(R.layout.home_dialog_loading, null, false)
        view.findViewById<TextView>(R.id.tv_loading).setText(R.string.editor_saving)
        val dialog = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()
        dialog.window.setLayout(resources.getDimensionPixelOffset(
                R.dimen.editor_bg_header_width),
                WindowManager.LayoutParams.MATCH_PARENT)

        dialog
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        initPhotoEditor()

        editorIvBack.setOnClickListener { finish() }
        editorIvEraser.setOnClickListener {
            if (photoEditor.brushDrawableMode) {
                editorIvEraser.setImageResource(R.drawable.ic_eraser_gray_24dp)
                photoEditor.setBrushDrawingMode(false)
            } else {
                editorIvEraser.setImageResource(R.drawable.ic_eraser_enable_24dp)
                photoEditor.brushEraser()
            }
        }
        editorIvUndo.setOnClickListener { photoEditor.undo() }
        editorIvRedo.setOnClickListener { photoEditor.redo() }
        editorIvBackground.setOnClickListener { triggerBGRVVisibility() }
        editorIvSave.setOnClickListener { saveImg() }

        initBackGroundRv()
        initSegmentationRv()

    }

    private fun initPhotoEditor() {
        val sourceIv = photoEditorView.source
        sourceIv.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val lp = sourceIv.layoutParams
        lp.height = RelativeLayout.LayoutParams.MATCH_PARENT
        sourceIv.layoutParams = lp

        photoEditor = PhotoEditor
                .Builder(this, photoEditorView)
                .setPinchTextScalable(true)
                .build()
    }

    private val saveListener = object : PhotoEditor.OnSaveListener {
        override fun onFailure(exception: Exception) {

            loadingDialog.dismiss()
            Toast.makeText(this@EditorActivity, "save failure", Toast.LENGTH_SHORT).show()
        }

        override fun onSuccess(imagePath: String) {
            loadingDialog.dismiss()
            Toast.makeText(this@EditorActivity, "save img to $imagePath", Toast.LENGTH_SHORT).show()
            MediaScannerConnection.scanFile(this@EditorActivity, arrayOf(imagePath), null, null)
        }

    }


    private val simpleDateFommat by lazy { SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault()) }
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(RC_WRITE_EXTERAL_STORAGE_PEM)
    private fun saveImg() {
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            val photoDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "segmentation")
            if (photoDir.exists().not()) {
                photoDir.mkdirs()
            }
            val imgName = simpleDateFommat.format(System.currentTimeMillis()) + "_seg.png"
            val imgPath = File(photoDir, imgName).absolutePath
            loadingDialog.show()
            photoEditor.saveImage(imgPath, saveListener)
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.camera_and_write_external_rationale),
                    RC_WRITE_EXTERAL_STORAGE_PEM,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

    }

    private fun triggerBGRVVisibility() {
        editorRvBackground.visibility =
                if (editorRvBackground.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private val divider by lazy {
        val d = DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL)
        ContextCompat.getDrawable(this, R.drawable.rec_divider)?.let {
            d.setDrawable(it)
        }
        d
    }

    private fun initSegmentationRv() {
        val segmentations: List<SegmentationImageEntity> = intent.getParcelableArrayListExtra(EXTRA_SEGMENTATION_IMG)
                ?: listOf(SegmentationImageEntity(url = "http://g.hiphotos.baidu.com/image/h%3D300/sign=5313080a0e087bf462ec51e9c2d2575e/37d3d539b6003af37401eb21392ac65c1038b633.jpg"))
        val adapter = SegmentationAdapter(R.layout.editor_item_segmentation, segmentations)
        val brushView = photoEditorView.brushDrawingView
        adapter.setOnItemClickListener { _, view, position ->
            GlideApp.with(view)
                    .load(segmentations[position].url)
                    .placeholder(R.drawable.ic_place_holder_gray_24dp)
                    .error(R.drawable.ic_error_gray_24dp)
                    .centerInside()
                    .into(brushView)
        }

        editorRvSegmentation.adapter = adapter
        editorRvSegmentation.addItemDecoration(divider)
        editorRvSegmentation.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun initBackGroundRv() {
        val adapter = BackgroundAdapter(R.layout.editor_item_background, backgroundEntities)
        val headView = ImageView(this)
        val lp = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.editor_bg_header_width),
                LinearLayout.LayoutParams.MATCH_PARENT)
        lp.gravity = Gravity.CENTER
        headView.layoutParams = lp
        headView.setImageResource(R.drawable.ic_add_gray_24dp)
        headView.setOnClickListener { selectImageFromGallery() }
        adapter.addHeaderView(headView, -1, LinearLayout.HORIZONTAL)

        adapter.setOnItemClickListener { _, _, position ->
            changeBackground(backgroundEntities[position])
        }


        editorRvBackground.adapter = adapter
        editorRvBackground.addItemDecoration(divider)
        editorRvBackground.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun selectImageFromGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, RC_SELECT_PHOTO)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            uri?.let {
                val path = UriUtils.getPathByUri(this, it)
                path?.let { changeBackground(BackgroundEntity(it)) }
            }
        }
    }

    private fun changeBackground(backgroundEntity: BackgroundEntity) {

        val imageView = photoEditorView.source
        val req =
                if (backgroundEntity.res == 0) GlideApp.with(imageView).load(backgroundEntity.path)
                else GlideApp.with(imageView).load(backgroundEntity.res)
        req.placeholder(R.drawable.ic_place_holder_gray_24dp)
                .error(R.drawable.ic_error_gray_24dp)
                .centerCrop()
                .into(imageView)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private val backgroundEntities by lazy {
        val entities = ArrayList<BackgroundEntity>(10)
        entities.add(BackgroundEntity(res = R.drawable.bg1))
        entities.add(BackgroundEntity(res = R.drawable.bg2))
        entities.add(BackgroundEntity(res = R.drawable.bg3))
        entities.add(BackgroundEntity(res = R.drawable.bg4))
        entities.add(BackgroundEntity(res = R.drawable.bg5))
        entities.add(BackgroundEntity(res = R.drawable.bg6))
        entities.add(BackgroundEntity(res = R.drawable.bg7))
        entities.add(BackgroundEntity(res = R.drawable.bg8))
        entities.add(BackgroundEntity(res = R.drawable.bg9))
        entities.add(BackgroundEntity(res = R.drawable.bg10))
        entities
    }


    companion object {
        const val RC_WRITE_EXTERAL_STORAGE_PEM = 0
        const val RC_SELECT_PHOTO = 0
    }
}
