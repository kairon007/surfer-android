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

public class DisplayStoredImageAsyncTask extends AsyncTask<CommercialDAO, Void, Bitmap> {
    private Context mContext = null;
    private ImageView mBackground;
    private CommercialDAO chosenCommercial;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private static final String LOG_TAG = DisplayStoredImageAsyncTask.class.getName();

    public DisplayStoredImageAsyncTask(Activity context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        //background = (ImageView) ((Activity) activityContext)
        //		.findViewById(R.id.advertiser_background);
        mBackground = (ImageView) ((Activity) mContext)
                .findViewById(R.id.glow_pad_background);
        mPreferences = mContext.getSharedPreferences(
                "current_commercial", Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
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
                ((Activity) mContext).getWindowManager()
                        .getDefaultDisplay().getMetrics(metrics);

                int height = metrics.heightPixels;
                int width = metrics.widthPixels;

                tempBitmap = getResizedBitmap(tempBitmap,
                        (int) (height * 0.85), width);
                if (tempBitmap != null) {

                    mEditor.putFloat("shown_commercial_id",
                            chosenCommercial.getId());
                    mEditor.commit();

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

	/*
	private CommercialDAO chooseCommercial() {
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
	private List<CommercialDAO> fetchCommercialsFromDb() {
		CommercialsDataSource datasource = new CommercialsDataSource(
				activityContext);
		List<CommercialDAO> commercialList = null;
		try {
			datasource.open();
			commercialList = datasource.getAllCommercials();

			Log.i(LOG_TAG, "" + commercialList);
			Log.i(LOG_TAG, "list size: " + commercialList.size());
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
	*/
}
