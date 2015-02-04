package com.trysurfer.surfer.screenlock;

/**
 * Created by PRO on 10/9/2014.
 */
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.model.CommercialDAO;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.widget.ImageView;

public class DisplayInternetImageAsyncTask extends AsyncTask<CommercialDAO, Void, Bitmap> {
    private Context activityContext = null;
    private ImageView mBackground;
    private CommercialDAO chosenCommercial;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor editor;

    private static final String LOG_TAG = DisplayInternetImageAsyncTask.class.getName();

    public DisplayInternetImageAsyncTask(Activity context) {
        activityContext = context;
    }

    @Override
    protected void onPreExecute() {
        mBackground = (ImageView) ((Activity) activityContext)
                .findViewById(R.id.advertiser_background);
        mPreferences = activityContext.getSharedPreferences(
                "current_commercial", Context.MODE_PRIVATE);
        editor = mPreferences.edit();
    }

    @Override
    protected Bitmap doInBackground(CommercialDAO... params) {
        //Log.v(LOG_TAG, "doInBackground..");
        chosenCommercial = params[0];
        Bitmap temp = scaleBitmapImage(chosenCommercial);
        return temp;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        //Log.v(LOG_TAG, "onPostExecute..");
        mBackground.setImageBitmap(result);
        super.onPostExecute(result);
    }

    private Bitmap scaleBitmapImage(CommercialDAO chosenCommercial) {
        //Log.v(LOG_TAG, "scaleBitmapImage");

        try {
            if (chosenCommercial.getPictureBitmap() != null) {
                Bitmap tempBitmap = BitmapFactory.decodeByteArray(
                        chosenCommercial.getPictureBitmap(), 0,
                        chosenCommercial.getPictureBitmap().length);

                DisplayMetrics metrics = new DisplayMetrics();
                ((Activity) activityContext).getWindowManager()
                        .getDefaultDisplay().getMetrics(metrics);

                int height = metrics.heightPixels;
                int width = metrics.widthPixels;

                tempBitmap = getResizedBitmap(tempBitmap,
                        (int) (height * 0.85), width);
                if (tempBitmap != null) {

                    editor.putFloat("shown_commercial_id",
                            chosenCommercial.getId());
                    editor.commit();

                    return tempBitmap;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Need to fix better image resize
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
}
