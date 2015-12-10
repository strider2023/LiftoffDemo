package com.liftoff.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by arindamnath on 08/12/15.
 */
public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById(R.id.option_riding).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, DriverMapsActivity.class));
            }
        });

        findViewById(R.id.option_need_ride).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, HitchMapsActivity.class));
            }
        });

        findViewById(R.id.option_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MenuActivity.this, SettingsActivity.class));
                Snackbar.make(v, "Feature not yet developed.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
