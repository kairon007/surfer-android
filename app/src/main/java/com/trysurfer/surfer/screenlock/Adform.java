package com.trysurfer.surfer.screenlock;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.adform.sdk.interfaces.AdListener;
import com.adform.sdk.utils.managers.InterstitialAdLoader;
import com.adform.sdk.view.CoreAdView;
import com.trysurfer.surfer.R;

/**
 * Created by PRO on 10/13/2014.
 */
public class Adform extends Activity implements AdListener {

    private CoreAdView mAdView;
    private InterstitialAdLoader adLoader;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adform);

    /*
        adLoader  = InterstitialAdLoader.createInstance(getApplicationContext());
        adLoader.setMasterTagId(75935);
        adLoader.setPublisherId(365);
        adLoader.loadAd();
        adLoader.showAd();
    */

        mAdView = (CoreAdView) findViewById(R.id.custom_ad_view);
        mAdView.setMasterTagId(75936);
        mAdView.setPublisherId(365);
        mAdView.setListener(this);
    }

    @Override
    public void onAdLoadSuccess() {
        //
    }

    @Override
    public void onAdLoadFail(String s) {
        RelativeLayout custom_ad_placement = (RelativeLayout) findViewById(R.id.custom_ad_placement);
        //custom_ad_placement.setBackgroundResource(R.drawable.banner_fallback);
    }

    @Override
    public void onDestroy() {
        if (mAdView != null)
            mAdView.destroy();
        if(adLoader != null){
            adLoader.destroy();
        }
        super.onDestroy();
    }
}
