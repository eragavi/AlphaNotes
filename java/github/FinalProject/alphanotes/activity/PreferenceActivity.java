package github.FinalProject.alphanotes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.github.omadahealth.lollipin.lib.PinActivity;
import com.github.omadahealth.lollipin.lib.managers.AppLock;

import github.FinalProject.alphanotes.App;
import github.FinalProject.alphanotes.R;

public class PreferenceActivity extends PinActivity {

    private static final int REQUEST_CODE_ENABLE = 101;

    private Toolbar toolbar;

    private SwitchCompat switchCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
        toolbar = findViewById(R.id.toolbar);
        switchCompat = findViewById(R.id.sw_lock);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        switchCompat.setChecked(App.getInstance().getBoolean("CODE_ENABLED",false));

        switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchCompat.isChecked()){
                    Intent intent = new Intent(PreferenceActivity.this, CustomPinActivity.class);
                    intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
                    startActivityForResult(intent, REQUEST_CODE_ENABLE);
                }else {
                    App.getInstance().putPrefs("CODE_ENABLED", false);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_ENABLE:
                Toast.makeText(this, "PinCode enabled", Toast.LENGTH_SHORT).show();
                App.getInstance().putPrefs("CODE_ENABLED", true);
                break;
        }
    }
}
