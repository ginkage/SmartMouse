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

import powl1.smartmouse.MouseServerService.MouseServerBinder;

public class PadFragment extends Fragment implements OnGestureListener {
    private static final String TAG = PadFragment.class.getSimpleName();
    private OnTouchListener bLeftListener;
    private GestureOverlayView gTouch;
    final Handler handler;
    private boolean mCancelClick;
    private ServiceConnection mConnection;
    private Context mContext;
    private long mDownTimeStamp;
    private boolean mLongPress;
    private MouseServerService mMouseService;
    private float mX;
    private float mY;
    private BroadcastReceiver stateReceiver;
    Runnable timerTask;

    public PadFragment() {
        handler = new Handler();
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
                    if (event.getAction() == 0) {
                        mMouseService.setButtonLeft(true);
                    } else if (event.getAction() == 1) {
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
                mMouseService = ((MouseServerBinder) service).getService();
                updateView();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mMouseService = null;
                updateView();
            }
        };
    }

    public void onStop() {
        if (mConnection != null) {
            mContext.unbindService(mConnection);
        }
        mContext.unregisterReceiver(stateReceiver);
        super.onStop();
    }

    public void onStart() {
        mContext = getActivity();
        mContext.bindService(new Intent(mContext, MouseServerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mContext.registerReceiver(stateReceiver, new IntentFilter(MouseServerService.STATE_CHANGED));
        super.onStart();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
        if (event.getAction() == 0) {
            mX = event.getX();
            mY = event.getY();
            mDownTimeStamp = event.getEventTime();
            mCancelClick = false;
            handler.postDelayed(timerTask, 500);
        }
    }

    public void onGesture(GestureOverlayView overlay, MotionEvent event) {
        if (event.getAction() == 2 && mMouseService != null) {
            mMouseService.moveXY(event.getX() - mX, event.getY() - mY);
            mX = event.getX();
            mY = event.getY();
            mCancelClick = true;
            handler.removeCallbacks(timerTask);
        }
    }

    public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
        if (event.getAction() == 1) {
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

    public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
    }
}
