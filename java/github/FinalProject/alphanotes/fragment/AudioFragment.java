package github.FinalProject.alphanotes.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.fragment.template.NoteFragment;
import github.FinalProject.alphanotes.model.DatabaseModel;


public class AudioFragment extends NoteFragment {

    private EditText titleText;

    private TextView tvPath, timer;

    private Button btnStart, btnStop, playButton, stopButton;

    private MediaRecorder mRecorder;
    private long mStartTime = 0;
    private LinearLayout playerContainer;
    private RelativeLayout containerView;
    private MediaPlayer mediaPlayer;
    private int[] amplitudes = new int[100];
    private int i = 0;
    private Handler mHandler = new Handler();
    private Runnable mTickExecutor = new Runnable() {
        @Override
        public void run() {
            tick();
            mHandler.postDelayed(mTickExecutor, 100);
        }
    };
    private File mOutputFile;

    public AudioFragment() {
    }

    public static AudioFragment newInstance() {
        Bundle args = new Bundle();
        AudioFragment fragment = new AudioFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public int getLayout() {
        return R.layout.fragment_audio;
    }

    @Override
    public void init(View view) {
        requestPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
        titleText = view.findViewById(R.id.title_txt);
        tvPath = view.findViewById(R.id.tv_path);
        btnStart = view.findViewById(R.id.start_button);
        btnStop = view.findViewById(R.id.stop_button);
        playButton = view.findViewById(R.id.playButton);
        stopButton = view.findViewById(R.id.stopButton);
        timer = view.findViewById(R.id.timer);
        containerView = view.findViewById(R.id.containerView);
        playerContainer = view.findViewById(R.id.play_container);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecording(true);
                btnStop.setEnabled(false);
                btnStart.setEnabled(true);
            }
        });


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (note != null && !TextUtils.isEmpty(note.body)) {
            containerView.setVisibility(View.GONE);
            playerContainer.setVisibility(View.VISIBLE);
            tvPath.setText(note.body);
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(note.body);
                mediaPlayer.prepare();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        try {
                            mediaPlayer.stop();
                            mediaPlayer.prepare();
                            playButton.setEnabled(true);
                            stopButton.setEnabled(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            playerContainer.setVisibility(View.GONE);
            containerView.setVisibility(View.VISIBLE);
            tvPath.setText("");
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mediaPlayer.start();
                    playButton.setEnabled(false);
                    stopButton.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                try {
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                playButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
    }


    @Override
    public void saveNote(final SaveListener listener) {
        super.saveNote(listener);
        note.title = titleText.getText().toString();
        if (mOutputFile != null) {
            note.body = mOutputFile.getAbsolutePath();
        }

        new Thread() {
            @Override
            public void run() {
                long id = note.save();
                if (note.id == DatabaseModel.NEW_MODEL_ID) {
                    note.id = id;
                }
                listener.onSave();
                interrupt();
            }
        }.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (mRecorder != null) {
                stopRecording(false);
            }
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            mRecorder.setAudioEncodingBitRate(48000);
        } else {
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setAudioEncodingBitRate(64000);
        }
        mRecorder.setAudioSamplingRate(16000);
        mOutputFile = getOutputFile();
        mOutputFile.getParentFile().mkdirs();
        mRecorder.setOutputFile(mOutputFile.getAbsolutePath());

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartTime = SystemClock.elapsedRealtime();
            mHandler.postDelayed(mTickExecutor, 100);
            Log.d("Voice Recorder", "started recording to " + mOutputFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("Voice Recorder", "prepare() failed " + e.getMessage());
        }
    }

    protected void stopRecording(boolean saveFile) {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mStartTime = 0;
            mHandler.removeCallbacks(mTickExecutor);
            if (!saveFile && mOutputFile != null) {
                mOutputFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private File getOutputFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/RECORDING_"
                + dateFormat.format(new Date())
                + ".m4a");
    }

    private void tick() {
        long time = (mStartTime < 0) ? 0 : (SystemClock.elapsedRealtime() - mStartTime);
        int minutes = (int) (time / 60000);
        int seconds = (int) (time / 1000) % 60;
        int milliseconds = (int) (time / 100) % 10;
        timer.setText(String.format(Locale.getDefault(), "%d:%s.%d", minutes, seconds < 10 ? "0" + seconds : seconds, milliseconds));
        if (mRecorder != null) {
            amplitudes[i] = mRecorder.getMaxAmplitude();
            //Log.d("Voice Recorder","amplitude: "+(amplitudes[i] * 100 / 32767));
            if (i >= amplitudes.length - 1) {
                i = 0;
            } else {
                ++i;
            }
        }
    }

    public static void requestPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
        }
    }


}
