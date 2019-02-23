package com.almico.finaldance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class MainActivity extends AppCompatActivity {
    public static final String CLIENT_ID = "5dc68703cf8e4c3fbe8e9dfd522d52f7";
    public static final String REDIRECT_URI =
            "https://google.com";
    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");
                        // TODO: Change to connected
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                        notConnected();
                        // Something went wrong when attempting to connect! Handle errors here
                    }

                });
    }

    private void notConnected() {
        Intent intent = new Intent(this, ConnectionFailure.class);
        startActivity(intent);
    }


    private void connected() {
        Intent intent = new Intent(this, SongSelection.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
