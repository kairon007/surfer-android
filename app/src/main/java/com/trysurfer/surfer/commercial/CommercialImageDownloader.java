package com.trysurfer.surfer.commercial;

/**
 * Created by PRO on 10/9/2014.
 */
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.model.CommercialDAO;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class CommercialImageDownloader {

    private static Context mContext;
    private static final String LOG_TAG = CommercialImageDownloader.class
            .getName();

    @SuppressWarnings("unchecked")
    public static void download(List<CommercialDAO> commercialList,
                                Context context) {
        mContext = context;
        BitmapDownloaderTask task = new BitmapDownloaderTask(commercialList,
                mContext);
        task.execute();
    }
}

class BitmapDownloaderTask extends
        AsyncTask<List<CommercialDAO>, Void, List<Bitmap>> {

    private List<CommercialDAO> commercialList;
    private CommercialsDataSource datasource;

    private SharedPreferences commercialPrefs;
    private SharedPreferences.Editor editor;
    private Context mContext;

    private static final String LOG_TAG = BitmapDownloaderTask.class.getName();

    public BitmapDownloaderTask(List<CommercialDAO> list, Context context) {
        commercialList = list;
        datasource = new CommercialsDataSource(context);
        mContext = context;
    }

    @Override
    protected List<Bitmap> doInBackground(List<CommercialDAO>... params) {
        List<Bitmap> bitmapCommercialList = new ArrayList<Bitmap>();
        if (commercialList != null) {
            for (int i = 0; i < commercialList.size(); i++) {
                bitmapCommercialList.add(downloadBitmap(commercialList.get(i)
                        .getPicture()));
            }
            return bitmapCommercialList;
        }
        Log.e(LOG_TAG, "null");
        return null;
    }

    @Override
    protected void onPostExecute(List<Bitmap> bitmapList) {
        //Log.v(LOG_TAG, "onPostExecute");

        if (isCancelled()) {
            bitmapList = null;
            Log.e(LOG_TAG, "iscancelled");
        }

        try {
            datasource.open(false);
            ByteArrayOutputStream baos;
            Bitmap tempBitmap;
            for (int i = 0; i < commercialList.size(); i++) {
                baos = new ByteArrayOutputStream();
                tempBitmap = bitmapList.get(i);
                tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] byteArray = baos.toByteArray();

                commercialList.get(i).setPictureBitmap(byteArray);
                datasource.createCommercial(commercialList.get(i));
                Log.i(LOG_TAG, commercialList.get(i).getId()
                        + " ble lagt til i databasen");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "exception: " + e);
        } finally {
            datasource.close();

            commercialPrefs = mContext.getSharedPreferences("current_commercial",
                    Context.MODE_PRIVATE);
            editor = commercialPrefs.edit();
            editor.putBoolean("commercials_updated", true);

            AppUtil.startService(mContext);
        }
    }

    static Bitmap downloadBitmap(String url) {
        final AndroidHttpClient client = AndroidHttpClient
                .newInstance("Android");
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode
                        + " while retrieving bitmap from " + url);
                Log.e(LOG_TAG, "null");
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    final Bitmap bitmap = BitmapFactory
                            .decodeStream(inputStream);
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or
            // IllegalStateException
            getRequest.abort();
            Log.e("ImageDownloader", "Error while retrieving bitmap from "
                    + url + " : " + e.toString());
        } finally {
            if (client != null) {
                client.close();
            }
        }
        Log.e(LOG_TAG, "null");
        return null;
    }
}