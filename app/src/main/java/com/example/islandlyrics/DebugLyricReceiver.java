package com.example.islandlyrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DebugLyricReceiver extends BroadcastReceiver {
    private static final String TAG = "DebugLyricReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!BuildConfig.DEBUG) return;
        
        if ("com.franco.capsulyric.DEBUG_UPDATE".equals(intent.getAction())) {
            String title = intent.getStringExtra("title");
            String artist = intent.getStringExtra("artist");
            String lyric = intent.getStringExtra("lyric");

            Log.d(TAG, "Debug broadcast received: " + lyric);

            Intent serviceIntent = new Intent(context, LyricService.class);
            serviceIntent.setAction("FORCE_UPDATE_UI");
            serviceIntent.putExtra("title", title);
            serviceIntent.putExtra("artist", artist);
            serviceIntent.putExtra("lyric", lyric);
            context.startService(serviceIntent);
        }
    }
}
