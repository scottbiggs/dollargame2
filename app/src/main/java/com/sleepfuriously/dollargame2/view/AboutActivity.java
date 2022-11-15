package com.sleepfuriously.dollargame2.view;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sleepfuriously.dollargame2.R;

/**
 * Displays info about this app
 */
public class AboutActivity extends AppCompatActivity {

    /** The percent of the screen width this Activity will fill */
    private static final float
            PORTRAIT_SCREEN_WIDTH_PERCENT = 0.9f,
            LANDSCAPE_SCREEN_WIDTH_PERCENT = 0.65f;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // fill 90% of the screen in portrait mode, 60% in landscape.
        float screenPercent = PORTRAIT_SCREEN_WIDTH_PERCENT;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenPercent = LANDSCAPE_SCREEN_WIDTH_PERCENT;
        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int newWidth = (int) (metrics.widthPixels * screenPercent);

        setContentView(R.layout.credits_layout);
        getWindow().setLayout(newWidth, ViewGroup.LayoutParams.WRAP_CONTENT);    // needs to happen AFTER setContentView


        Button done_button = findViewById(R.id.done_butt);
        done_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // these enable links in the text strings to work
        TextView aboutTv = findViewById(R.id.inspired_tv);
        aboutTv.setMovementMethod(LinkMovementMethod.getInstance());

        TextView moreTv = findViewById(R.id.more_tv);
        moreTv.setMovementMethod(LinkMovementMethod.getInstance());

        TextView versionTv = findViewById(R.id.version_tv);
        String versionName;

        try {
            versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = getString(R.string.version_error);
        }
        versionTv.setText(versionName);

    }


}
