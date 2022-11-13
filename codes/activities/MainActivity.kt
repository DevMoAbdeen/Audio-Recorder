package com.msa.audiorecorder.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.msa.audiorecorder.R
import com.msa.audiorecorder.databinding.ActivityMainBinding
import com.msa.audiorecorder.fragments.BottomDialogFragment
import com.msa.audiorecorder.models.Recordes
import com.msa.audiorecorder.room_db.database.DatabaseClient
import com.msa.audiorecorder.timer.Timer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_CODE = 100

class MainActivity : AppCompatActivity(), Timer.OnTimerTickListener{
    private lateinit var amplitudes: ArrayList<Float>
    lateinit var binding: ActivityMainBinding
    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false
    lateinit var recorder: MediaRecorder

    lateinit var pathRecord: String
    private var dirPath = ""

    private lateinit var timer: Timer
    lateinit var vibrator: Vibrator

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        permissionGranted = ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED

        if(! permissionGranted){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        timer = Timer(this)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        binding.startRecorder.setOnClickListener {
            startRecording()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }

        binding.pauseRecorder.setOnClickListener {
            binding.startRecorder.visibility = View.GONE
            binding.goToMenue.visibility = View.GONE
            binding.resumeRecorder.visibility = View.VISIBLE
            binding.pauseRecorder.visibility = View.GONE
            binding.saveRecord.visibility = View.VISIBLE
            binding.finishRecordDelete.visibility = View.VISIBLE

            pauseRecorder()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }

        binding.resumeRecorder.setOnClickListener {
            binding.startRecorder.visibility = View.GONE
            binding.goToMenue.visibility = View.GONE
            binding.resumeRecorder.visibility = View.GONE
            binding.pauseRecorder.visibility = View.VISIBLE
            binding.saveRecord.visibility = View.VISIBLE
            binding.finishRecordDelete.visibility = View.VISIBLE

            resumeRecorder()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }

        binding.saveRecord.setOnClickListener {
            Recordes.saveOrRename = "saveRecord"

            Recordes.timerRecord = binding.tvTimer.text.toString()
            Recordes.pathRecord = pathRecord
            val bottomDialog = BottomDialogFragment(object: BottomDialogFragment.OnRecordListener{
                override fun updateRecord(index: Int, newName: String) {}
            })
            bottomDialog.show(supportFragmentManager, "saveRecord")

            timer.stop()
            recorder.stop()
            recorder.release()


            binding.tvTimer.setText("00:00.00")
            binding.waveformView.visibility = View.GONE
            amplitudes = binding.waveformView.clear()

            binding.startRecorder.visibility = View.VISIBLE
            binding.goToMenue.visibility = View.VISIBLE
            binding.resumeRecorder.visibility = View.GONE
            binding.pauseRecorder.visibility = View.GONE
            binding.saveRecord.visibility = View.GONE
            binding.finishRecordDelete.visibility = View.GONE
        }

        binding.finishRecordDelete.setOnClickListener {
            recorder.pause()
            timer.pause()
            val alert = AlertDialog.Builder(this)
            alert.setTitle("Not saving !?")
            alert.setMessage("Are you sure you do not want to save the recording !?")
            alert.setIcon(R.drawable.ic_close)
            alert.setCancelable(true)

            alert.setPositiveButton("Yes") { d, i ->
                Toast.makeText(this, "Record Delete", Toast.LENGTH_SHORT).show()
                binding.startRecorder.visibility = View.VISIBLE
                binding.goToMenue.visibility = View.VISIBLE
                binding.resumeRecorder.visibility = View.GONE
                binding.pauseRecorder.visibility = View.GONE
                binding.saveRecord.visibility = View.GONE
                binding.finishRecordDelete.visibility = View.GONE

                try {
                    File(pathRecord).delete()
                }catch (ex: java.lang.Exception){
                    Toast.makeText(this, "${ex.message}", Toast.LENGTH_SHORT).show()
                }
                amplitudes = binding.waveformView.clear()
                binding.waveformView.visibility = View.GONE
                timer.stop()
                binding.tvTimer.setText("00:00.00")
                d.dismiss()
            }

            alert.setNegativeButton("Cancel") { d, i ->
                recorder.resume()
                timer.start()
                d.cancel()
            }
            alert.create().show()
        }

        binding.goToMenue.setOnClickListener {
            startActivity(Intent(this, AllRecordesActivity::class.java))
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE){
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startRecording(){
        if(! permissionGranted){
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        // Start Recording
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"

        val date = SimpleDateFormat("yyyy.MM.dd_HH.mm.SS").format(Date())
        val fileName = "audio_record_$date"
        pathRecord = "$dirPath$fileName.mp3"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(pathRecord)

            try{
                prepare()
                Log.e("msa", "Record is successfull...")
            }catch (ex: Exception){
                Log.e("msaError", "Exception in Line 160: ${ex.message}")
                Toast.makeText(this@MainActivity, "Exception: ${ex.message}", Toast.LENGTH_SHORT).show()
            }

            start()
            binding.waveformView.visibility = View.VISIBLE
        }
        binding.startRecorder.visibility = View.GONE
        binding.goToMenue.visibility = View.GONE
        binding.pauseRecorder.visibility = View.VISIBLE
        binding.resumeRecorder.visibility = View.GONE
        binding.saveRecord.visibility = View.VISIBLE
        binding.finishRecordDelete.visibility = View.VISIBLE
        binding.finishRecordDelete.visibility = View.VISIBLE

        timer.start()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun resumeRecorder(){
        recorder.resume()
        timer.start()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun pauseRecorder(){
        recorder.pause()
        timer.pause()
    }

    override fun onTimerTick(duration: String) {
        binding.tvTimer.setText(duration)
        binding.waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}