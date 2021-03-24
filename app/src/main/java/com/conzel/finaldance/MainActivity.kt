package com.conzel.finaldance

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Track


class MainActivity : AppCompatActivity() {
    private val CLIENT_ID = "5dc68703cf8e4c3fbe8e9dfd522d52f7"
    private val REDIRECT_URI = "https://google.com"
    public var mSpotifyAppRemote: SpotifyAppRemote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(CLIENT_ID).
                setRedirectUri(REDIRECT_URI).
                showAuthView(true).
                build();

        SpotifyAppRemote.connect(this, connectionParams,
                object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote
                        Log.d("MainActivity", "Connected! Yay!")

                        // Now you can start interacting with App Remote
                        connected()
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e("MainActivity", throwable.message, throwable)

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                })
    }

    private fun connected() {
        // mSpotifyAppRemote?.playerApi?.play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");
        mSpotifyAppRemote!!.playerApi
                .subscribeToPlayerState()
                .setEventCallback { playerState: PlayerState ->
                    val track: Track? = playerState.track
                    if (track != null) {
                        val tv1: TextView = findViewById(R.id.textview_first)
                        tv1.text = track.name.toString() + " by " + track.artist.name
                    }
                }
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
}
