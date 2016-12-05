package io.github.leonhover.iconrainview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void restart(View view) {
        Log.d(TAG, "restart");
        EditText countEdt = (EditText) findViewById(R.id.count_edt);
        EditText launchEdt = (EditText) findViewById(R.id.launch_duration_edt);
        EditText fallGravityEdt = (EditText) findViewById(R.id.fall_gravity_edt);

        int count = TextUtils.isDigitsOnly(countEdt.getText().toString()) ? Integer.valueOf(countEdt.getText().toString()) : -1;
        int launchDuration = TextUtils.isDigitsOnly(launchEdt.getText().toString()) ? Integer.valueOf(launchEdt.getText().toString()) : -1;
        int fallGravity = TextUtils.isDigitsOnly(fallGravityEdt.getText().toString()) ? Integer.valueOf(fallGravityEdt.getText().toString()) : -1;

        IconRainView iconRainView = new IconRainView(this);
        iconRainView.setIcon(R.mipmap.coin);
        iconRainView.setIconCounts(count > 0 ? count : 5);
        iconRainView.setLaunchDuration(launchDuration > 0 ? launchDuration : 300);
        iconRainView.setFallGravity(fallGravity);
        iconRainView.setShadeToGone(true);
        iconRainView.setOnIconRainFallListener(new IconRainView.OnIconRainFallListener() {
            @Override
            public void onRainStart() {
                Log.d(TAG, "onRainStart");
            }

            @Override
            public void onRainFinish() {
                Log.d(TAG, "onRainFinish");
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }

            }
        });

        popupWindow = new PopupWindow(iconRainView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.showAtLocation(findViewById(R.id.text), Gravity.NO_GRAVITY, 0, 0);
        iconRainView.startRainFall();
    }

}
