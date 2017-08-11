package it.unibo.lam.roadsosecurity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;


public class Utility {

    public Utility() {}

    public static void playSound(final Context context, final int type) {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                MediaPlayer mediaPlayer;
                int resId = -1;

                switch (type)
                {
                    case 1:
                        resId=R.raw.anomaly_detected;
                        break;
                    case 2:
                        resId=R.raw.anomaly_nearby;
                        break;
                }

                if (resId != -1)
                {
                    mediaPlayer = MediaPlayer.create(context, resId);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setLooping(false);
                    mediaPlayer.start();

                    while (mediaPlayer.isPlaying() == true) { }
                }
            }
        }).start();
    }

    public static int compare(int ax, int ay, int az) {
        ax = Math.abs(ax);
        ay = Math.abs(ay);
        az = Math.abs(az);
        if (ax > ay) {
            if (ax > az) return 0;
        } else if (ay > az) return 1;
        else return 2;

        return -1;
    }

}
