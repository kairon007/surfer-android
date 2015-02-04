package com.trysurfer.surfer.commercial;

/**
 * Created by PRO on 10/9/2014.
 */

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trysurfer.surfer.AppUtil;
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.model.CommercialDAO;

public class CommercialAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    private String serverUrl, mUserId, mUserAuthToken;

    private Context mContext = null;
    private SharedPreferences commercialPrefs, userPrefs;
    private ProgressDialog dialog;
    private SharedPreferences.Editor editor;

    private static final String LOG_TAG = CommercialAsyncTask.class.getName();

    public CommercialAsyncTask(Context context) {
        mContext = context;
        dialog = new ProgressDialog(mContext);
    }

    @Override
    protected void onPreExecute() {
        commercialPrefs = mContext.getSharedPreferences("commercials",
                Context.MODE_PRIVATE);
        userPrefs = mContext.getSharedPreferences(
                "CurrentUser", Context.MODE_PRIVATE);

        mUserId = userPrefs.getString("User_Id", "");
        mUserAuthToken = userPrefs.getString("User_AuthToken", "");

        serverUrl = mContext.getString(R.string.base_url)
                + mContext.getString(R.string.commercial_uri);

        dialog.setMessage("Fetching commercials, please wait.");
        dialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(serverUrl);
        JSONObject holder = new JSONObject();
        JSONObject userObj = new JSONObject();
        String response = null;
        JSONObject json = new JSONObject();

        try {
            try {
                // setup the returned values in case something goes wrong
                json.put("success", false);
                json.put("info", "Something went wrong. Retry!");

                // add the user email and password to the params
                userObj.put("id", mUserId);
                userObj.put("auth_token", mUserAuthToken);
                holder.put("user", userObj);
                StringEntity se = new StringEntity(holder.toString());
                post.setEntity(se);

                // setup the request headers
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-Type", "application/json");

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = client.execute(post, responseHandler);
                json = new JSONObject(response);
            } catch (HttpResponseException e) {
                e.printStackTrace();
                Log.e("ClientProtocol", "" + e);
                json.put("info", "Commercial error. Retry!");
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IO", "" + e);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }
        return json;
    }

    @Override
    protected void onProgressUpdate(Integer... i) {
    }

    @Override
    protected void onPostExecute(JSONObject json) {
        //Log.v(LOG_TAG, "In onPostExecute: ");
        try {
            if (json.getBoolean("success")) {
                //Log.i(LOG_TAG, "success");
                Log.i(LOG_TAG, "" + json);

                JSONArray jsonCommercials = json.getJSONObject("data")
                        .getJSONArray("commercials");
                List<CommercialDAO> commercialList = new Gson().fromJson(
                        jsonCommercials.toString(),
                        new TypeToken<List<CommercialDAO>>() {
                        }.getType());

                String dateFormat = "yyyy-MM-dd'T'HH:mm:SS";
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

                Date newestDate = new Date();
                newestDate = sdf.parse("2014-01-01T1000:00:00");

                for (int i = 0; i < jsonCommercials.length(); i++) {
                    JSONObject tempObj = (JSONObject) jsonCommercials.get(i);
                    String tempStr = tempObj.getString("updated_at");

                    Date tempDate = sdf.parse(tempStr);

                    //Log.v(LOG_TAG, "Current time in check " + tempDate);

                    if (checkIfNewerDate(tempDate, newestDate)) {
                        newestDate = tempDate;
                        //Log.i(LOG_TAG, newestDate + " is new newest in check.");
                    }
                }

                //Log.i(LOG_TAG, "Highest: " + newestDate);

                String storedDateString = commercialPrefs.getString("newest_date",
                        "");

                Date storedDate;
                if (storedDateString.equals("")) {
                    //Log.i(LOG_TAG, "No stored time value, setting default");
                    storedDate = sdf.parse("2014-01-01T1000:00:00");
                    //Log.i(LOG_TAG, "Default time is now: " + storedDate);
                } else {
                    storedDate = sdf.parse(storedDateString);
                    //Log.i(LOG_TAG, "Stored time value: " + storedDate);
                }

                if (checkIfNewerDate(newestDate, storedDate)) {
                    String temp = sdf.format(newestDate);

                    //Log.i(LOG_TAG, "Storing newest date: " + temp);

                    editor = commercialPrefs.edit();
                    editor.putBoolean("commercials_updated", false);
                    editor.putString("newest_date", temp);
                    editor.commit();

                    AppUtil.saveImages(commercialList, mContext);
                }
            }

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            Toast.makeText(mContext, json.getString("info"),
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG)
                    .show();
            e.printStackTrace();
            Log.e(LOG_TAG, "" + e);
        } finally {
            super.onPostExecute(json);
        }
    }

    private boolean checkIfNewerDate(Date newDate, Date oldDate) {
        //Log.i(LOG_TAG, "Checking if " + newDate + " is newer than " + oldDate);

        if (oldDate == null) {
            //Log.i(LOG_TAG, "Old date is null");
            return true;
        }
        if (newDate.after(oldDate)) {
            //Log.i(LOG_TAG, newDate + "is newer than " + oldDate);
            return true;
        }
        //Log.w(LOG_TAG, "Test returned false");
        return false;
    }
}
