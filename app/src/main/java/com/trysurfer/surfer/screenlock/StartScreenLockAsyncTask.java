package com.trysurfer.surfer.screenlock;

/**
 * Created by PRO on 10/9/2014.
 */
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class StartScreenLockAsyncTask extends AsyncTask<Void, Void, Void>{

    private Context mContext;
    private Intent screenLockIntent;

    public StartScreenLockAsyncTask(Context context){
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub

        screenLockIntent = new Intent(mContext.getApplicationContext(),
                //ScreenLockActivity.class
                //AdMobLockScreen.class
                //InterstitialAdMobLockScreen.class
                //Adform.class
                //AdBuddizActivity.class
                MobFoxActivity.class
                );
        screenLockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NO_HISTORY); // ?

        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        // TODO Auto-generated method stub
        mContext.getApplicationContext().startActivity(screenLockIntent);

        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        // TODO Auto-generated method stub
        super.onCancelled();
    }
}

