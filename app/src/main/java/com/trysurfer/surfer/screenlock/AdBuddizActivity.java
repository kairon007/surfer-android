package com.trysurfer.surfer.screenlock;

import android.app.Activity;
import android.os.Bundle;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.trysurfer.surfer.R;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

//import AdBuddizActivity SDK
import com.purplebrain.adbuddiz.sdk.AdBuddizDelegate;
import com.purplebrain.adbuddiz.sdk.AdBuddizError;
import com.purplebrain.adbuddiz.sdk.AdBuddizLogLevel;

/**
 * Created by pro on 10.12.2014.
 */
public class AdBuddizActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adbuddiz);

        final Activity activity = this;

        AdBuddiz.setLogLevel(AdBuddizLogLevel.Info);    // log level
        AdBuddiz.setPublisherKey("e45582c7-f32f-4bde-bac3-280f42ee90d4"); // replace with your app publisher key
        AdBuddiz.setTestModeActive();                   // to delete before submitting to store

        AdBuddiz.cacheAds(activity);                    // start caching ads

        findViewById(R.id.button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // To call whenever you want to display an Ad.
                // Parameter is the current activity
                AdBuddiz.showAd(activity);
            }
        });

        // OPTIONAL, to get more info about the SDK behavior
        AdBuddiz.setDelegate(new AdBuddizDelegate() {
            @Override
            public void didCacheAd() {
                Toast.makeText(activity, "didCacheAd", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void didShowAd() {
                Toast.makeText(activity, "didShowAd", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void didFailToShowAd(AdBuddizError error) {
                Toast.makeText(activity, error.name(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void didClick() {
                Toast.makeText(activity, "didClick", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void didHideAd() {
                Toast.makeText(activity, "didHideAd", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdBuddiz.onDestroy(); // to minimize memory footprint
    }

}
