package com.conzel.finaldance

import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.spotify.android.appremote.api.PlayerApi

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    var audioManager: AudioManager? = null
    var previousAudio = -1

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        audioManager = requireActivity().getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager;
        saveCurrentAudio()
    }

    fun saveCurrentAudio() {
        previousAudio = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!!
    }

    fun restoreAudio() {
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, previousAudio, 0)
    }

    fun setAudio(p: Double) {
        if (BuildConfig.DEBUG && !(p in 0.0..1.0)) {
            error("Assertion failed")
        }
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)!!
        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVolume * p).toInt(), 0)
    }

    // In seconds
    var PLAY_TIME = 90_000
    var PAUSE_TIME = 30_000
    var FADEOUT_TIME = 3_000
    val FADEOUT_STEP_TIME = 10

    fun load_settings() {
        val time_play = requireActivity().findViewById<TextView>(R.id.time_play)
        val time_pause = requireActivity().findViewById<TextView>(R.id.time_stop)
        val time_fade = requireActivity().findViewById<TextView>(R.id.time_fade)
        PLAY_TIME = 1000 * time_play.text.toString().toInt()
        PAUSE_TIME = 1000 * time_pause.text.toString().toInt()
        FADEOUT_TIME = 1000 * time_fade.text.toString().toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    fun getPlayerApi(): PlayerApi? {
        val main_activity = activity as MainActivity
        val player_api = main_activity.mSpotifyAppRemote?.playerApi;
        return player_api
    }

    fun skip_next() {
        getPlayerApi()?.skipNext()
        getPlayerApi()?.pause()
        getPlayerApi()?.seekTo(0)
        getPlayerApi()?.pause()
    }

    fun play() {
        getPlayerApi()?.seekTo(0)
        getPlayerApi()?.resume()
    }

    fun pause() {
        getPlayerApi()?.pause()
    }

    fun fadeout_blocking(time_ms: Int) {
        val steps = time_ms / FADEOUT_STEP_TIME
        val delta = previousAudio.toDouble() / steps.toDouble()
        for (i: Int in 0..steps) {
            val next_vol = previousAudio - delta * i
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, next_vol.toInt(), 0)
            Thread.sleep(FADEOUT_STEP_TIME.toLong())
        }
    }

    fun stop_callbacks() {
        playbackHandler.removeCallbacksAndMessages(null)
    }

    val playbackHandler = Handler(Looper.getMainLooper())

    private val playbackTask = object: Runnable {
        override fun run() {
            restoreAudio()
            play()
            playbackHandler.postDelayed(stopTask, PLAY_TIME.toLong())
        }
    }

    lateinit var stopTask: Runnable

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stopTask = object: Runnable {
            override fun run() {
                saveCurrentAudio()
                // fadeout_blocking(FADEOUT_TIME)

                // preparing non-blocking fadeout
                val steps = FADEOUT_TIME / FADEOUT_STEP_TIME
                val delta = previousAudio.toDouble() / steps.toDouble()
                var i = 0
                val fadeoutNonblockingTask = object: Runnable {
                    override fun run() {
                        i += 1
                        // Fadeout case
                        if (i <= steps) {
                            val next_vol = previousAudio - delta * i
                            audioManager?.setStreamVolume(
                                AudioManager.STREAM_MUSIC,
                                next_vol.toInt(),
                                0
                            )
                            playbackHandler.postDelayed(this, FADEOUT_STEP_TIME.toLong())
                        }
                        // Fadeout finished, going over to pause
                        else {
                            pause()
                            skip_next()
                            playbackHandler.postDelayed(playbackTask, PAUSE_TIME.toLong())
                        }
                    }
                }
                fadeoutNonblockingTask.run()
            }
        }

        view.findViewById<Button>(R.id.start_button).setOnClickListener {
            start_playback()
        }

        view.findViewById<Button>(R.id.stop_button).setOnClickListener {
            stop_playback()
        }
    }

    fun start_playback() {
        load_settings()
        stop_callbacks()
        if (previousAudio == -1) {
            saveCurrentAudio()
        } else {
            restoreAudio()
        }
        playbackTask.run()
    }

    fun stop_playback() {
        stop_callbacks()
        pause()
        restoreAudio()
    }
}