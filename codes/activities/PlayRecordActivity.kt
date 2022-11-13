package com.msa.audiorecorder.activities

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.msa.audiorecorder.R
import com.msa.audiorecorder.databinding.ActivityPlayRecordBinding
import java.text.DecimalFormat

class PlayRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayRecordBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var delay = 1L
    private val jumpValue = 5000
    private var playbackSpeed = 1.0f

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPlayRecordBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val filePath = intent.getStringExtra("filePath")
        val fileName = intent.getStringExtra("fileName")

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer.apply {
            setDataSource(filePath)
            prepare()
        }

        binding.tvFileName.text = fileName
        binding.tvTrackDuration.setText("${dateFormat(mediaPlayer.duration)}")

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable{
            binding.seekBarAudioPlayer.progress = mediaPlayer.currentPosition
            binding.tvTrackProgress.setText("${dateFormat(mediaPlayer.currentPosition)}")
            handler.postDelayed(runnable, delay)
        }

        playPausePlayer()
        binding.seekBarAudioPlayer.max = mediaPlayer.duration

        mediaPlayer.setOnCompletionListener {
            binding.btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
            handler.removeCallbacks(runnable)
        }

        binding.btnPlay.setOnClickListener{
            playPausePlayer()
        }

        binding.btnForward.setOnClickListener{
            mediaPlayer.seekTo(mediaPlayer.currentPosition + jumpValue)
            binding.seekBarAudioPlayer.progress += jumpValue
        }

        binding.btnBackward.setOnClickListener{
            mediaPlayer.seekTo(mediaPlayer.currentPosition - jumpValue)
            binding.seekBarAudioPlayer.progress -= jumpValue
        }

        // تغيير سرعة التسجيل
        binding.chip.setOnClickListener{
            if(mediaPlayer.isPlaying) {
                if (playbackSpeed != 2f) {
                    playbackSpeed += 0.5f
                } else {
                    playbackSpeed = 0.5f
                }

                mediaPlayer.playbackParams = PlaybackParams().setSpeed(playbackSpeed)
                binding.chip.text = "x $playbackSpeed"
            }
        }

        // تغيير الوقت بإستخدام التحريك ب seekBar
        binding.seekBarAudioPlayer.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, isChange: Boolean) {
                if(isChange){
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

    }

    private fun playPausePlayer(){
        if(! mediaPlayer.isPlaying){
            mediaPlayer.start()
            binding.btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_pause_circle, theme)
            handler.postDelayed(runnable, delay)
        }else{
            mediaPlayer.pause()
            binding.btnPlay.background = ResourcesCompat.getDrawable(resources, R.drawable.ic_play_circle, theme)
            handler.removeCallbacks(runnable)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer.stop()
        mediaPlayer.release()
        handler.removeCallbacks(runnable)
    }

    private fun dateFormat(duration: Int): String{
        val d = duration / 1000
        val s = d % 60
        val m = (d / 60 % 60)
        val h = ((d - m * 60) / 360).toInt()

        val f = DecimalFormat("00")
        var str = "$m:${f.format(s)}"

        if(h > 0){
            str = "$h:$str"
        }
        return str
    }
}