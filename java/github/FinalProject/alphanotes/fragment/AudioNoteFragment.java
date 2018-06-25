package github.FinalProject.alphanotes.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.fragment.template.NoteFragment;
import github.FinalProject.alphanotes.model.DatabaseModel;
import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class AudioNoteFragment extends NoteFragment {

    private EditText txtTitle;

    private String audioPath;
    private Recorder recorder;
    private ImageView recordButton;
    private Button playButton;
    private TextView tvSaveHint, tvPath, timeTextView;
    private LinearLayout containerView;
    private int secCount = 0;
    private CountDownTimer countDownTimer;

    public AudioNoteFragment() {
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_audio_note;
    }

    @Override
    public void saveNote(final SaveListener listener) {
        super.saveNote(listener);
        note.title = txtTitle.getText().toString();
        note.body = audioPath;

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
    public void init(View view) {
        txtTitle = view.findViewById(R.id.title_txt);
        recordButton = view.findViewById(R.id.recordButton);
        playButton = view.findViewById(R.id.playButton);
        tvPath = view.findViewById(R.id.tv_path);
        tvSaveHint = view.findViewById(R.id.tv_save_hint);
        containerView = view.findViewById(R.id.containerView);
        timeTextView = view.findViewById(R.id.timeTextView);

        requestPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
        setupRecorder();
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(audioPath)) {

                } else {
                    Toast.makeText(getActivity(), "Audio already Recorded", Toast.LENGTH_SHORT).show();
                }
                recorder.startRecording();
                setTimer();
            }
        });

        view.findViewById(R.id.stopButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    recorder.stopRecording();
                    if (countDownTimer != null)
                        countDownTimer.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                recordButton.post(new Runnable() {
                    @Override
                    public void run() {
                        animateVoice(0);
                    }
                });
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (note != null && !TextUtils.isEmpty(note.body)) {
            playButton.setVisibility(View.VISIBLE);
            containerView.setVisibility(View.GONE);
            tvPath.setText(note.body);
            tvSaveHint.setVisibility(View.GONE);
        } else {
            playButton.setVisibility(View.GONE);
            containerView.setVisibility(View.VISIBLE);
            tvPath.setText(R.string.click_microphone_to_record);
            tvSaveHint.setVisibility(View.VISIBLE);
        }

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioPlayer(note.body);
            }
        });

    }

    public void audioPlayer(String path) {
        //set up MediaPlayer
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requestPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
        }
    }

    private void setupRecorder() {
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override
                    public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                    }
                }), file());
    }

    private void animateVoice(final float maxPeak) {
        recordButton.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }

    @NonNull
    private File file() {
        File file = new File(Environment.getExternalStorageDirectory(), "record_" + System.currentTimeMillis() + ".wav");
        audioPath = file.getPath();
        return file;
    }

    private void setTimer() {
        secCount = 0;
        if (countDownTimer != null)
            countDownTimer.cancel();
        countDownTimer = new CountDownTimer(7000000, 1000) {
            public void onTick(long millisUntilFinished) {
                secCount++;
                int min = secCount / 60;
                int sec = secCount - min * 60;
                timeTextView.setText(String.format(Locale.getDefault(), "%02d : %02d", min, sec));
            }

            public void onFinish() {
            }
        }.start();
    }
}
