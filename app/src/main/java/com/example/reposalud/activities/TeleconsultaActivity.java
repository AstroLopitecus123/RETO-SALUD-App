package com.example.reposalud.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.reposalud.R;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class TeleconsultaActivity extends BaseActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private String appId;
    private String channelName;
    private String token;

    private RtcEngine mRtcEngine;
    private boolean isMuted = false;
    private boolean isVideoMuted = false;
    private int dataStreamId = -1;
    private android.os.Handler streamHandler = new android.os.Handler();
    private Runnable streamRunnable;
    private FrameLayout localVideoContainer;
    private FrameLayout remoteVideoContainer;
    private TextView tvCallStatus;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(() -> tvCallStatus.setText("Esperando al doctor..."));
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                tvCallStatus.setVisibility(View.GONE);
                setupRemoteVideo(uid);
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                tvCallStatus.setVisibility(View.VISIBLE);
                tvCallStatus.setText("El doctor salió de la llamada");
                remoteVideoContainer.removeAllViews();
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleconsulta);

        appId = getIntent().getStringExtra("agora_app_id");
        token = getIntent().getStringExtra("agora_token");
        channelName = getIntent().getStringExtra("agora_canal");

        localVideoContainer = findViewById(R.id.localVideoContainer);
        remoteVideoContainer = findViewById(R.id.remoteVideoContainer);
        tvCallStatus = findViewById(R.id.tvCallStatus);

        findViewById(R.id.btnEndCall).setOnClickListener(v -> finish());
        
        FrameLayout btnMic = findViewById(R.id.btnMic);
        ImageView ivMic = findViewById(R.id.ivMic);
        btnMic.setOnClickListener(v -> {
            isMuted = !isMuted;
            if (mRtcEngine != null) mRtcEngine.muteLocalAudioStream(isMuted);
            if (isMuted) {
                btnMic.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D32F2F")));
                ivMic.setImageResource(R.drawable.ic_mic_off_white);
                ivMic.setColorFilter(android.graphics.Color.WHITE);
            } else {
                btnMic.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFFFF")));
                ivMic.setImageResource(R.drawable.ic_mic_white);
                ivMic.setColorFilter(android.graphics.Color.BLACK);
            }
        });

        FrameLayout btnVideo = findViewById(R.id.btnVideo);
        ImageView ivVideo = findViewById(R.id.ivVideo);
        btnVideo.setOnClickListener(v -> {
            isVideoMuted = !isVideoMuted;
            if (mRtcEngine != null) {
                mRtcEngine.muteLocalVideoStream(isVideoMuted);
                mRtcEngine.enableLocalVideo(!isVideoMuted);
            }
            if (isVideoMuted) {
                btnVideo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D32F2F")));
                ivVideo.setImageResource(R.drawable.ic_videocam_off_white);
                ivVideo.setColorFilter(android.graphics.Color.WHITE);
                localVideoContainer.setVisibility(View.GONE);
            } else {
                btnVideo.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFFFF")));
                ivVideo.setImageResource(R.drawable.ic_videocam_white);
                ivVideo.setColorFilter(android.graphics.Color.BLACK);
                localVideoContainer.setVisibility(View.VISIBLE);
            }
        });

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initializeAndJoinChannel();
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeAndJoinChannel();
            } else {
                Toast.makeText(this, "Se requieren permisos para la videollamada", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initializeAndJoinChannel() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);
        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar Agora: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        mRtcEngine.enableVideo();
        setupLocalVideo();

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;

        try {
            io.agora.rtc2.DataStreamConfig streamConfig = new io.agora.rtc2.DataStreamConfig();
            streamConfig.syncWithAudio = false;
            streamConfig.ordered = true;
            dataStreamId = mRtcEngine.createDataStream(streamConfig);
        } catch(Exception e) {
            e.printStackTrace();
        }

        streamRunnable = new Runnable() {
            @Override
            public void run() {
                if (mRtcEngine != null && dataStreamId != -1) {
                    try {
                        android.content.SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                        String nombre = prefs.getString("user_name", "Usuario");
                        String msg = "{\"type\":\"INFO\",\"rol\":\"PACIENTE\",\"nombre\":\"" + nombre + "\"}";
                        mRtcEngine.sendStreamMessage(dataStreamId, msg.getBytes());
                    } catch (Exception e) {}
                }
                streamHandler.postDelayed(this, 3000);
            }
        };
        streamHandler.post(streamRunnable);

        mRtcEngine.joinChannel(token, channelName, 0, options);
    }

    private void setupLocalVideo() {
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);
        localVideoContainer.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
        mRtcEngine.startPreview();
    }

    private void setupRemoteVideo(int uid) {
        remoteVideoContainer.removeAllViews();
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        remoteVideoContainer.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (streamHandler != null && streamRunnable != null) {
            streamHandler.removeCallbacks(streamRunnable);
        }
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }
}

