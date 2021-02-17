package com.example.fotoaudioprueba

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var graba: Button? = null
    private var para: Button? = null
    private var reproduce: Button? = null
    var mp: MediaPlayer? = null
    private val AUDIO_RECORDER_FILE_EXT_3GP = ".3gp"
    private val AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4"
    private val AUDIO_RECORDER_FOLDER = "AudioRecorder"
    private var recorder: MediaRecorder? = null
    private var currentFormat = 0
    private val output_formats = intArrayOf(MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP)
    private val file_exts = arrayOf(AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView) as ImageView
        /*FOTO*/
        findViewById<Button>(R.id.button).setOnClickListener{
            takePhoto()
        }
        /*REC*/
        graba = findViewById<View>(R.id.button2) as Button
        /*
        findViewById<Button>(R.id.button2).setOnClickListener{
            recordAudio(null)
        }*/
        /*STOP*/
        para= findViewById<Button>(R.id.button3) as Button
        /*
        findViewById<Button>(R.id.button3).setOnClickListener{
            stopAudioRecording(null)
        }*/
        /*PLAY*/
        reproduce=findViewById<Button>(R.id.button4) as Button
        /*findViewById<Button>(R.id.button4).setOnClickListener{
            playAudio(null)
        }*/
        if (mp == null) {
            mp = MediaPlayer()
        }
    }
    private val infoListener = MediaRecorder.OnInfoListener { mr, what, extra -> Toast.makeText(this@MainActivity, "Warning: $what, $extra", Toast.LENGTH_SHORT).show() }
    private var lastFileName: String? = null
    fun recordAudio(v: View?) {
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        //grabacion en mp4
        currentFormat = 0
        recorder?.setOutputFormat(output_formats[currentFormat])
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        lastFileName = getFilename()
        recorder?.setOutputFile(lastFileName)
        recorder?.setOnErrorListener(errorListener)
        recorder?.setOnInfoListener(infoListener)
        try {
            recorder?.prepare()
            recorder?.start()
            graba!!.isEnabled = false
            para!!.isEnabled = true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopAudioRecording(v: View?) {
        if (null != recorder) {
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            recorder = null
            para!!.isEnabled = false
            graba!!.isEnabled = true
            reproduce!!.isEnabled = true
        }
    }

    fun playAudio(v: View?) {
        if (!mp!!.isPlaying) {
            try {
                mp = MediaPlayer()
                mp!!.setDataSource(lastFileName)
                mp!!.prepare()
                mp!!.setOnCompletionListener { reproduce!!.text = "Play" }
                mp!!.start()
                reproduce!!.text = "Pausa"
            } catch (e: IllegalArgumentException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: SecurityException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }
        } else {
            mp!!.pause()
            reproduce!!.text = "Play"
        }
    }

    private val MY_PERMISSIONS_RECORD_AUDIO = 1
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_RECORD_AUDIO -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    recordAudio(null)
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    fun requestAudioPermissions(v: View?) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
            !== PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            !== PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show()

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_RECORD_AUDIO)
            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_RECORD_AUDIO)
            }
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
            === PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            Toast.makeText(this, "Permissions granted to record audio", Toast.LENGTH_LONG).show()
            recordAudio(null)
        }
    }

    private fun getFilename(): String? {
        val filepath = this.getExternalFilesDir(null)?.getAbsolutePath();
        val file = File(filepath, AUDIO_RECORDER_FOLDER)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + "/" + System.currentTimeMillis() + file_exts[currentFormat]
    }

    private val errorListener = MediaRecorder.OnErrorListener { mr, what, extra ->
        graba!!.isEnabled = true
        para!!.isEnabled = false
        Toast.makeText(this@MainActivity, "Error: $what, $extra", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView?.setImageBitmap(imageBitmap)
        }
    }

    val REQUEST_TAKE_PHOTO = 2
    private fun takePhoto() {
        val takeVideoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takeVideoIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_TAKE_PHOTO)
        }
    }
}