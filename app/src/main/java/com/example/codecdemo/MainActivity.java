package com.example.codecdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.fouraxis.codecdemo.R;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    private AudioRecord mRecorder;
    private AudioTrack mPlayer;
    private static final  int BITRATE = 8000;
    private int minBuffSize;
    private boolean isRecording;
    private ExecutorService service;
    private JNIWrapper jniWrapper;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = Executors.newFixedThreadPool(1);
        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        getAudioPermission();
        jniWrapper = new JNIWrapper();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    private void getAudioPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    private void setupPlayer() {
        minBuffSize = AudioTrack.getMinBufferSize(BITRATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if(minBuffSize < 8192)
            minBuffSize = 8192;
        mPlayer = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                BITRATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuffSize,
                AudioTrack.MODE_STREAM);
    }

    private void setupRecorder() {
        minBuffSize = AudioRecord.getMinBufferSize(BITRATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, BITRATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuffSize);
    }

    private void releaseHW(){
        if(mRecorder != null){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        if(mPlayer != null){
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.shutdownNow();
        stop();
        jniWrapper.destroy();
    }

    private void stop(){
        if(isRecording){
            isRecording = false;
            audioManager.setMode(AudioManager.MODE_NORMAL);
            releaseHW();
        }
    }

    private void start(){
        if(!isRecording) {
            isRecording = true;
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            service.submit(new EchoServer());
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.play){
            start();
        }
        else if(v.getId() == R.id.stop){
           stop();
        }
    }

    private class EchoServer implements Runnable{

        @Override
        public void run() {
            setupRecorder();
            setupPlayer();
            mRecorder.startRecording();
            mPlayer.play();
            short[] buffer = new short[160];
            int readLen = 0;
            while(isRecording){
                readLen = mRecorder.read(buffer,0,buffer.length);
                if(readLen > 0){
                    byte[] out = jniWrapper.encodeOpus(Arrays.copyOfRange(buffer,0,readLen));
                    Log.e("read - out ","" + readLen + " - " + out.length);
//                    mPlayer.write(out,0,out.length);
                    short[] pcm = jniWrapper.decodeOpus(out,out.length);
                    Log.e("pcm ","" + pcm.length);
                    mPlayer.write(pcm,0,pcm.length);
                }
            }
        }
    }
}
