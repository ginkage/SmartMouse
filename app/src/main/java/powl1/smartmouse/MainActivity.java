package powl1.smartmouse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startAdvertising();
    }

    @Override
    protected void onDestroy() {
        stopAdvertising();
        super.onDestroy();
    }

    private void startAdvertising() {
        startService(new Intent(this, MouseAdvertService.class));
        startService(new Intent(this, MouseServerService.class));
    }

    private void stopAdvertising() {
        stopService(new Intent(this, MouseAdvertService.class));
        stopService(new Intent(this, MouseServerService.class));
    }
}
