package github.FinalProject.alphanotes.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Date;

import github.FinalProject.alphanotes.BuildConfig;
import github.FinalProject.alphanotes.R;
import github.FinalProject.alphanotes.fragment.template.NoteFragment;
import github.FinalProject.alphanotes.model.DatabaseModel;

import static android.app.Activity.RESULT_OK;

public class CameraNoteFragment extends NoteFragment {

    private static final int IMAGE_CAMERA_REQUEST = 101;
    private TextView titleTxt, tvPath, tvSaveHint;

    private Button btnCamera;

    private ImageView imageView;

    private File filePathImageCamera;

    public CameraNoteFragment() {

    }

    @Override
    public int getLayout() {
        return R.layout.fragment_camera_note;
    }

    @Override
    public void saveNote(final SaveListener listener) {
        super.saveNote(listener);

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String photoName = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
                filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), photoName + ".jpg");
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        filePathImageCamera);
                it.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                getActivity().startActivityForResult(it, IMAGE_CAMERA_REQUEST);
            }
        });

        if (note != null && !TextUtils.isEmpty(note.body)) {
            btnCamera.setVisibility(View.GONE);
            tvPath.setText(note.body);
            imageView.setVisibility(View.VISIBLE);
            tvSaveHint.setVisibility(View.GONE);
            Picasso.get().load(new File(note.body)).into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
            btnCamera.setVisibility(View.VISIBLE);
            tvSaveHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void init(View view) {
        titleTxt = view.findViewById(R.id.title_txt);
        btnCamera = view.findViewById(R.id.btn_camera);
        imageView = view.findViewById(R.id.iv_image);
        tvPath = view.findViewById(R.id.tv_path);
        tvSaveHint = view.findViewById(R.id.tv_save_hint);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    note.body = filePathImageCamera.getAbsolutePath();
                    tvPath.setText(filePathImageCamera.getAbsolutePath());
                }
                break;

        }
    }
}
