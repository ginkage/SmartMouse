package powl1.smartmouse;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class PadFragment extends Fragment implements OnGestureListener {
    private static final String TAG = PadFragment.class.getSimpleName();

    private OnTouchListener bLeftListener;
    private GestureOverlayView gTouch;
    private boolean mCancelClick;
    private ServiceConnection mConnection;
    private Context mContext;
    private long mDownTimeStamp;
    private boolean mLongPress;
    private MouseServerService mMouseService;
    private float mX;
    private float mY;
    private BroadcastReceiver stateReceiver;
    private Runnable timerTask;

    private final Handler handler = new Handler();

    public PadFragment() {
        stateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateView();
            }
        };
        bLeftListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (mMouseService == null) {
                    Log.e(PadFragment.TAG, "Not connected to Mouse Server");
                } else {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        mMouseService.setButtonLeft(true);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        mMouseService.setButtonLeft(false);
                    }
                    updateView();
                }
                return false;

            }
        };
        timerTask = new Runnable() {
            @Override
            public void run() {
                if (mMouseService != null) {
                    mLongPress = true;
                    mMouseService.setButtonLeft(true);
                }
            }
        };
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mMouseService = ((MouseServerService.MouseServerBinder) service).getService();
                updateView();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mMouseService = null;
                updateView();
            }
        };
    }

    @Override
    public void onStop() {
        if (mConnection != null) {
            mContext.unbindService(mConnection);
        }
        mContext.unregisterReceiver(stateReceiver);
        super.onStop();
    }

    @Override
    public void onStart() {
        mContext = getActivity();
        mContext.bindService(new Intent(mContext, MouseServerService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mContext.registerReceiver(stateReceiver,
                new IntentFilter(MouseServerService.STATE_CHANGED));
        super.onStart();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        rootView.findViewById(R.id.button_left).setOnTouchListener(bLeftListener);
        gTouch = (GestureOverlayView) rootView.findViewById(R.id.gesturePad);
        updateView();
        return rootView;
    }

    private void updateView() {
        if (gTouch != null) {
            if (mMouseService == null) {
                gTouch.setEnabled(false);
                gTouch.setGestureVisible(false);
                gTouch.removeAllOnGestureListeners();
                gTouch.setBackgroundColor(Color.GRAY);
            } else if (mMouseService.getConnectedDevice() == null) {
                gTouch.setEnabled(false);
                gTouch.setGestureVisible(false);
                gTouch.setBackgroundColor(Color.GRAY);
                gTouch.removeAllOnGestureListeners();
                Log.e(TAG, "DISABLE");
            } else {
                gTouch.setEnabled(true);
                gTouch.setGestureVisible(true);
                gTouch.setBackgroundResource(R.drawable.touch);
                gTouch.addOnGestureListener(this);
                Log.e(TAG, "ENABLE");
            }
        }
    }

    @Override
    public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mX = event.getX();
            mY = event.getY();
            mDownTimeStamp = event.getEventTime();
            mCancelClick = false;
            handler.postDelayed(timerTask, 500);
        }
    }

    @Override
    public void onGesture(GestureOverlayView overlay, MotionEvent event) {
        if (event.getAction() == 2 && mMouseService != null) {
            mMouseService.moveXY(event.getX() - mX, event.getY() - mY);
            mX = event.getX();
            mY = event.getY();
            mCancelClick = true;
            handler.removeCallbacks(timerTask);
        }
    }

    @Override
    public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            handler.removeCallbacks(timerTask);
            if (!mCancelClick && event.getEventTime() - mDownTimeStamp < 500) {
                mMouseService.setButtonLeft(true);
                mMouseService.setButtonLeft(false);
            }
            if (mLongPress) {
                mLongPress = false;
                mMouseService.setButtonLeft(false);
            }
        }
    }

    @Override
    public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
    }
}
