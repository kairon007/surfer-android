package com.trysurfer.surfer.screenlock;

/**
 * Created by PRO on 10/9/2014.
 */
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.commercial.CommercialsDataSource;
import com.trysurfer.surfer.model.CommercialDAO;
import com.trysurfer.surfer.model.TrackerDAO;
import com.trysurfer.surfer.receiver.ScreenLockReceiver;

public class ScreenLockActivityOld extends Activity {
    private boolean flagsSet = false;
    private boolean commercialTrackerAvailable;
    private final int DEFAULT_SEEKBAR_VALUE = 50;
    private int LAST_SEEKBAR_VALUE = DEFAULT_SEEKBAR_VALUE;
    private List<CommercialDAO> commercialList;

    private CommercialDAO chosenCommercial;
    private static Context mContext;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor editor;
    private ImageView background;
    private SeekBar slider;
    private WebView browser;
    private View myView;
    private ViewGroup parent;

    private static final String LOG_TAG = ScreenLockActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");

        updateActivityFlags();
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_screenlock_old);

        background = (ImageView) findViewById(R.id.advertiser_background);

        parent = (ViewGroup) background.getParent();

        mContext = getApplicationContext();
        chosenCommercial = null;
        commercialTrackerAvailable = true;

        mPreferences = mContext.getSharedPreferences("current_commercial",
                Context.MODE_PRIVATE);
        editor = mPreferences.edit();

        ScreenLockReceiver.setMainActivityHandler(this);

        //INTERNET
		/*
		browser = (WebView) findViewById(R.id.webview);
		browser.setVisibility(View.INVISIBLE);
		browser.setBackgroundColor(Color.parseColor("#000000"));
		browser.setWebViewClient(new MyBrowser());
		browser.setOnTouchListener(new View.OnTouchListener() {@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		*/
        //INTERNETNED
		/*
		myView = findViewById(R.id.webview);
		//parent = (ViewGroup) myView.getParent();
		if(parent != null){
			parent.removeView(myView);
		}

		Uri imageUri = Uri.parse("android.resource://com.olsen.surfer/drawable/logo");
		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        bitmap = scaleBitmapImage(bitmap);
		background.setImageBitmap(bitmap);
		*/
        //lauchCommercialImage();
        startAdvertiserSeekbar();
        startPhoneStateListener();
    }

    private Bitmap scaleBitmapImage(Bitmap bitmap) {
        Log.v(LOG_TAG, "scaleBitmapImage");

        try {
            if (bitmap != null) {


                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager()
                        .getDefaultDisplay().getMetrics(metrics);

                int height = metrics.heightPixels;
                int width = metrics.widthPixels;

                Bitmap tempBitmap = getResizedBitmap(bitmap,
                        (int) (height * 0.85), width);
                if (tempBitmap != null) {
					/*
					editor.putFloat("shown_commercial_id",
							chosenCommercial.getId());
					editor.commit();
					*/
                    return tempBitmap;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int newHeight,
                                          int newWidth) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        float scale = (scaleHeight <= scaleWidth) ? scaleHeight : scaleWidth;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    private void lauchCommercialImage(CommercialDAO commercial) {
		/*
		if(AppUtil.networkAvailable(getApplicationContext())){
			// INTERNET


			String commercialPicture = "<center>"
					+ commercial.getPicture()
					+ "</center>";
			System.out.println(commercialPicture);
			browser.loadData(commercialPicture, "text/html", null);
			// browser.loadUrl(chosenCommercial);
			// INTERNET END

			myView = background;
			//parent = (ViewGroup) myView.getParent();
			if(parent != null){
				parent.removeView(myView);
			}
			/*try {

				new DisplayInternetImageAsyncTask(this).execute(commercial);
			} catch (Exception e) {
				e.getStackTrace();
				Log.e(LOG_TAG, "" + e.getLocalizedMessage());
			}
			*/
		/*} else {
			myView = findViewById(R.id.webview);
			//parent = (ViewGroup) myView.getParent();
			if(parent != null){
				parent.removeView(myView);
			}
			try {
				new DisplayStoredImageAsyncTask(this).execute(commercial);
			} catch (Exception e) {
				e.getStackTrace();
				Log.e(LOG_TAG, "" + e.getLocalizedMessage());
			}
		}
		*/
        myView = findViewById(R.id.webview);
        //parent = (ViewGroup) myView.getParent();
        if(parent != null){
            parent.removeView(myView);
        }
        try {
            new DisplayStoredImageAsyncTask(this).execute(commercial);
        } catch (Exception e) {
            e.getStackTrace();
            Log.e(LOG_TAG, "" + e.getLocalizedMessage());
        }
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            browser.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Toast.makeText(mContext, "Oh no! " + description,
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected CommercialDAO chooseCommercial() {
        commercialList = fetchCommercialsFromDb();
        if (commercialList.size() > 0 && commercialList != null) {
            int rng = new Random().nextInt(commercialList.size());
            chosenCommercial = commercialList.get(rng);

            editor.putFloat("shown_commercial_id", chosenCommercial.getId());
            editor.commit();

            return chosenCommercial;
        }
        return null;
    }

    // Fetching stored commercials
    protected List<CommercialDAO> fetchCommercialsFromDb() {
        CommercialsDataSource datasource = new CommercialsDataSource(
                getApplicationContext());
        List<CommercialDAO> commercialList = null;
        try {
            datasource.open(true);
            commercialList = datasource.getAllCommercials();

            //Log.i(LOG_TAG, "" + commercialList);
            //Log.i(LOG_TAG, "list size: " + commercialList.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            datasource.close();
            if (commercialList != null) {
                return commercialList;
            }
        }
        return null;
    }

    private void startAdvertiserSeekbar() {
        //Log.v(LOG_TAG, "startAdvertiserSeekbar");
        try {
            slider = (SeekBar) findViewById(R.id.seekBar1);
            slider.setProgress(DEFAULT_SEEKBAR_VALUE);
            slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    slider.setProgress(DEFAULT_SEEKBAR_VALUE);
                    LAST_SEEKBAR_VALUE = DEFAULT_SEEKBAR_VALUE;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {

                    if (progress >= LAST_SEEKBAR_VALUE
                            && progress < LAST_SEEKBAR_VALUE + 12
                            && progress > 50) {
                        LAST_SEEKBAR_VALUE = progress - 1;
                    } else if (progress <= LAST_SEEKBAR_VALUE
                            && progress > LAST_SEEKBAR_VALUE - 12
                            && progress < 50) {
                        LAST_SEEKBAR_VALUE = progress + 1;
                    }

                    if (fromUser) {
                        if ((progress == LAST_SEEKBAR_VALUE + 1 && progress > 50)
                                || (progress == LAST_SEEKBAR_VALUE - 1 && progress < 50)) {
                            LAST_SEEKBAR_VALUE = progress;
                            advertiserSeekbarAction(progress);
                        } else {
                            slider.setProgress(DEFAULT_SEEKBAR_VALUE);
                            LAST_SEEKBAR_VALUE = DEFAULT_SEEKBAR_VALUE;
                        }
                    }
                }

                private void advertiserSeekbarAction(int progress) {
                    if (progress >= 83) {
                        if (commercialTrackerAvailable) {
                            closeCommercial();
                        }
                    } else if (progress <= 17) {
                        if (commercialTrackerAvailable) {
                            openCommercial();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG,
                    "startAdvertiserSeekbar error: " + e.getLocalizedMessage());
            finish();
        }
    }

    protected void closeCommercial() {
        commercialTrackerAvailable = false;
        TrackerDAO tracker = new TrackerDAO(chosenCommercial.getId(), false,
                true, false);

        AppUtil.sendCommercialTracker(mContext, tracker);

        finish();
    }

    // If user slides to the left
    protected void openCommercial() {
        commercialTrackerAvailable = false;
        TrackerDAO tracker = new TrackerDAO(chosenCommercial.getId(), true,
                false, false);

        AppUtil.sendCommercialTracker(mContext, tracker);

        if (AppUtil.isUrlValid(chosenCommercial.getUrl())) {
            KeyguardManager keyguardManager = (KeyguardManager) mContext
                    .getSystemService(Context.KEYGUARD_SERVICE);

            if (!keyguardManager.inKeyguardRestrictedInputMode()) {
                AppUtil.openCommercialUrl(mContext, chosenCommercial.getUrl());
            } else {
                editor = mPreferences.edit();
                editor.putString("open_commercial_url",
                        chosenCommercial.getUrl());
                editor.putBoolean("open_commercial", true);
                editor.commit();
            }
        }
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume");

        chosenCommercial = chooseCommercial();
        lauchCommercialImage(chosenCommercial);
    }

    @Override
    protected void onPause() {
        Log.v(LOG_TAG, "onPause");

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.v(LOG_TAG, "onStop");

        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy");

        super.onDestroy();
    }

    public void updateActivityFlags() {
        if (!flagsSet) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            flagsSet = true;
        } else if (flagsSet) {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            flagsSet = false;
        }
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss.
        return;
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                // || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)) {
            // this is where I can do my stuff
            return true; // because I handled the event
        }
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            // || (event.getKeyCode() == KeyEvent.KEYCODE_POWER))
            return false;
        }
        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
            // System.out.println("KEYCODE_HOME PRESSED");
            // return true;
            return true;
        }
        return false;
    }

    // Start phone state listener
    private void startPhoneStateListener() {
        StateListener phoneStateListener = new StateListener();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    // StateListener.
    class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i(LOG_TAG, "call Activity off hook");
                    finish();
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    };
}
