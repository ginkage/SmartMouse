package powl1.smartmouse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startAdvertising();
    }

    protected void onDestroy() {
        stopAdvertising();
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings || super.onOptionsItemSelected(item);
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
