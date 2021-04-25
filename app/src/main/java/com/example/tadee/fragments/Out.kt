package com.example.tadee.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.tadee.BuildConfig
import com.example.tadee.R
import kotlinx.android.synthetic.main.fragment_out.*
import java.io.File
import java.io.FileOutputStream


class Out : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_out, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fab_close = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        val fab_open = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        var isOpen = false

        share.setOnClickListener(View.OnClickListener {
            if (isOpen) {
                text.startAnimation(fab_close)
                image.startAnimation(fab_close)
                //fab_main.startAnimation(fab_anticlock)
                text.setClickable(false)
                image.setClickable(false)
                isOpen = false
            } else {
                text.startAnimation(fab_open)
                image.startAnimation(fab_open)
                //fab_main.startAnimation(fab_anticlock)
                text.setClickable(true)
                image.setClickable(true)
                isOpen = true
            }
        })

        text.setOnClickListener(View.OnClickListener {
            takeScreenShot(true)
        })

        image.setOnClickListener(View.OnClickListener {
            takeScreenShot(false)
        })
    }

    private fun takeScreenShot(option: Boolean) {
        var b : Bitmap
        if(option){
            var totalHeight = Output.height
            var totalWidth = Output.width
            b = getBitmapFromView(Output, totalHeight, totalWidth)
        }
        else{
            var totalHeight = sendingPower.height
            var totalWidth = sendingPower.width
            val s = getBitmapFromView(sendingPower, totalHeight, totalWidth)
            totalHeight = receivingPower.height
            totalWidth = receivingPower.width
            val r = getBitmapFromView(receivingPower, totalHeight, totalWidth)
            var bitmapArray = arrayListOf<Bitmap>(s, r)
            b = combineImageIntoOne(bitmapArray)!!
        }



       // var bitmapArray = arrayListOf<Bitmap>(text)
        // a.add(bm1);
        // a.add(bm1);
        // a.add(bm1);
       //
        // Cobine Multi Image Into One

        //Save bitmap
        val extr: String = Environment.getExternalStorageDirectory().toString()
        val fileName = "report.jpg"
        val file = File(context?.externalCacheDir, fileName)
        val fout = FileOutputStream(file)
        b.compress(Bitmap.CompressFormat.PNG, 100, fout)
        fout.flush()
        fout.close()
        file.setReadable(true, false)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_STREAM, context?.let {
                    FileProvider.getUriForFile(
                        it,
                        BuildConfig.APPLICATION_ID + ".provider", file
                    )
                })
            type = "image/png"
        }
        startActivity(Intent.createChooser(sendIntent, "Share"))
    }

    fun getBitmapFromView(view: View, totalHeight: Int, totalWidth: Int): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }

    // Example:
// Bitmap bm1=BitmapFactory.decodeResource(getResources(),.drawable.ic_launcher);
// ArrayList<Bitmap> a=new ArrayList<Bitmap>();
// a.add(bm1);
// a.add(bm1);
// a.add(bm1);
// combineImageIntoOne(a);

    // Example:
    // Bitmap bm1=BitmapFactory.decodeResource(getResources(),.drawable.ic_launcher);
    // ArrayList<Bitmap> a=new ArrayList<Bitmap>();
    // a.add(bm1);
    // a.add(bm1);
    // a.add(bm1);
    // combineImageIntoOne(a);
    // Cobine Multi Image Into One
    private fun combineImageIntoOne(bitmap: ArrayList<Bitmap>): Bitmap? {
        var w = 0
        var h = 0
        for (i in 0 until bitmap.size) {
            if (i < bitmap.size - 1) {
                w =
                    if (bitmap[i].width > bitmap[i + 1].width) bitmap[i].width else bitmap[i + 1].width
            }
            h += bitmap[i].height
        }
        val temp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(temp)
        var top = 0
        for (i in 0 until bitmap.size) {
            top = if (i == 0) 0 else top + bitmap[i].height
            canvas.drawBitmap(bitmap[i], 0f, top.toFloat(), null)
        }
        return temp
    }

}