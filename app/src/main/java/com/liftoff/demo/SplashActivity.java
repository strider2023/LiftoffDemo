package com.liftoff.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by arindamnath on 08/12/15.
 */
public class SplashActivity extends Activity {

    private Animation animFast, animSlow, animVerySlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        animFast = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.float_anim);
        animSlow = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.float_anim_slow);
        animVerySlow = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.float_anim_very_slow);
        findViewById(R.id.splash_image_1).startAnimation(animFast);
        findViewById(R.id.splash_image_2).startAnimation(animSlow);
        findViewById(R.id.splash_text_container).startAnimation(animVerySlow);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, MenuActivity.class));
                finish();
            }
        }, 3500);

    }
}
