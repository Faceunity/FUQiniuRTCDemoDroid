package com.qiniu.droid.rtc.demo.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faceunity.nama.FURenderer;
import com.faceunity.nama.ui.FaceUnityView;
import com.qiniu.droid.rtc.demo.R;

/**
 * Fragment for call control.
 */
public class ControlFragment extends Fragment {
    private View mControlView;
    private ImageButton mDisconnectButton;
    private ImageButton mCameraSwitchButton;
    private ImageButton mToggleMuteButton;
    private ImageButton mToggleBeautyButton;
    private ImageButton mToggleSpeakerButton;
    private ImageButton mToggleVideoButton;
    private ImageButton mLogShownButton;
    private LinearLayout mLogView;
    private TextView mStreamingConfigButton;
    private TextView mForwardJobButton;
    private TextView mLocalTextViewForVideo;
    private TextView mLocalTextViewForAudio;
    private TextView mRemoteTextView;
    private StringBuffer mRemoteLogText;
    private Chronometer mTimer;
    private OnCallEvents mCallEvents;
    private boolean mIsVideoEnabled = true;
    private boolean mIsShowingLog = false;
    private boolean mIsScreenCaptureEnabled = false;
    private boolean mIsAudioOnly = false;
    private FURenderer fuRenderer;
    private TextView mTvFps;

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {
        void onCallHangUp();

        void onCameraSwitch();

        boolean onToggleMic();

        boolean onToggleVideo();

        boolean onToggleSpeaker();

        boolean onToggleBeauty();

        void onCallStreamingConfig();

        void onToggleForwardJob();
    }

    public void setScreenCaptureEnabled(boolean isScreenCaptureEnabled) {
        mIsScreenCaptureEnabled = isScreenCaptureEnabled;
    }

    public void setFuRenderer(FURenderer fuRenderer) {
        this.fuRenderer = fuRenderer;
    }

    public void setAudioOnly(boolean isAudioOnly) {
        mIsAudioOnly = isAudioOnly;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mControlView = inflater.inflate(R.layout.fragment_room, container, false);

        mDisconnectButton = (ImageButton) mControlView.findViewById(R.id.disconnect_button);
        mTvFps = (TextView) mControlView.findViewById(R.id.tv_fps);
        mCameraSwitchButton = (ImageButton) mControlView.findViewById(R.id.camera_switch_button);
        mToggleBeautyButton = (ImageButton) mControlView.findViewById(R.id.beauty_button);
        mToggleMuteButton = (ImageButton) mControlView.findViewById(R.id.microphone_button);
        mToggleSpeakerButton = (ImageButton) mControlView.findViewById(R.id.speaker_button);
        mToggleVideoButton = (ImageButton) mControlView.findViewById(R.id.camera_button);
        mLogShownButton = (ImageButton) mControlView.findViewById(R.id.log_shown_button);
        mLogView = (LinearLayout) mControlView.findViewById(R.id.log_text);
        mStreamingConfigButton = mControlView.findViewById(R.id.streaming_config_button);
        mForwardJobButton = mControlView.findViewById(R.id.forward_job_button);
        mLocalTextViewForVideo = (TextView) mControlView.findViewById(R.id.local_log_text_video);
        mLocalTextViewForVideo.setMovementMethod(ScrollingMovementMethod.getInstance());
        mLocalTextViewForAudio = (TextView) mControlView.findViewById(R.id.local_log_text_audio);
        mLocalTextViewForAudio.setMovementMethod(ScrollingMovementMethod.getInstance());
        mRemoteTextView = (TextView) mControlView.findViewById(R.id.remote_log_text);
        mRemoteTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTimer = (Chronometer) mControlView.findViewById(R.id.timer);

        FaceUnityView faceUnityView = mControlView.findViewById(R.id.faceunity_view);
        if (fuRenderer == null) {
            faceUnityView.setVisibility(View.GONE);
        } else {
            faceUnityView.setModuleManager(fuRenderer);
        }

        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallEvents.onCallHangUp();
            }
        });

        if (!mIsScreenCaptureEnabled && !mIsAudioOnly) {
            mCameraSwitchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallEvents.onCameraSwitch();
                }
            });
        }

        if (!mIsScreenCaptureEnabled && !mIsAudioOnly) {
            mToggleBeautyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean enabled = mCallEvents.onToggleBeauty();
                    mToggleBeautyButton.setImageResource(enabled ? R.mipmap.face_beauty_open : R.mipmap.face_beauty_close);
                }
            });
        }

        mToggleMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean enabled = mCallEvents.onToggleMic();
                mToggleMuteButton.setImageResource(enabled ? R.mipmap.microphone : R.mipmap.microphone_disable);
            }
        });

        if (mIsScreenCaptureEnabled || mIsAudioOnly) {
            mToggleVideoButton.setImageResource(R.mipmap.video_close);
        } else {
            mToggleVideoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean enabled = mCallEvents.onToggleVideo();
                    mToggleVideoButton.setImageResource(enabled ? R.mipmap.video_open : R.mipmap.video_close);
                }
            });
        }

        mToggleSpeakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = mCallEvents.onToggleSpeaker();
                mToggleSpeakerButton.setImageResource(enabled ? R.mipmap.loudspeaker : R.mipmap.loudspeaker_disable);
            }
        });

        mLogShownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogView.setVisibility(mIsShowingLog ? View.INVISIBLE : View.VISIBLE);
                mIsShowingLog = !mIsShowingLog;
            }
        });

        mStreamingConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallEvents.onCallStreamingConfig();
            }
        });

        mForwardJobButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallEvents.onToggleForwardJob();
            }
        });
        return mControlView;
    }

    public void startTimer() {
        mTimer.setBase(SystemClock.elapsedRealtime());
        mTimer.start();
    }

    public void stopTimer() {
        mTimer.stop();
    }

    public void updateLocalVideoLogText(String logText) {
        if (mLogView.getVisibility() == View.VISIBLE) {
            mLocalTextViewForVideo.setText(logText);
        }
    }

    public void updateLocalAudioLogText(String logText) {
        if (mLogView.getVisibility() == View.VISIBLE) {
            mLocalTextViewForAudio.setText(logText);
        }
    }

    public void updateRemoteLogText(String logText) {
        if (mRemoteLogText == null) {
            mRemoteLogText = new StringBuffer();
        }
        if (mLogView != null) {
            mRemoteTextView.setText(mRemoteLogText.append(logText + "\n"));
        }
    }

    public void updateForwardJobText(String forwardJobText) {
        if (mForwardJobButton != null) {
            mForwardJobButton.setText(forwardJobText);
        }
    }

    public void setFps(String fps) {
        if (mTvFps != null) {
            mTvFps.setText("FPS: " + fps);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mIsVideoEnabled) {
            mCameraSwitchButton.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallEvents = (OnCallEvents) activity;
    }
}
