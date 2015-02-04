package com.trysurfer.surfer.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trysurfer.surfer.Constants;
import com.trysurfer.surfer.R;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class InterestsFragment extends Fragment {

        private CheckBox cbxOne, cbxTwo, cbxThree, cbxFour,
                cbxFive, cbxSix, cbxSeven, cbxEight, cbxNine;
        private Button btnFinish;
        private ArrayList<CheckBox> alternatives;
        private ArrayList<String> interestsList;
        private String[] alternativesLabels;

        private SharedPreferences mSharedPrefs;
        private SharedPreferences.Editor mEditor;

        public InterestsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_interests, container, false);

            cbxOne = (CheckBox) rootView.findViewById(R.id.alternative1);
            cbxTwo = (CheckBox) rootView.findViewById(R.id.alternative2);
            cbxThree = (CheckBox) rootView.findViewById(R.id.alternative3);
            cbxFour = (CheckBox) rootView.findViewById(R.id.alternative4);
            cbxFive = (CheckBox) rootView.findViewById(R.id.alternative5);
            cbxSix = (CheckBox) rootView.findViewById(R.id.alternative6);
            cbxSeven = (CheckBox) rootView.findViewById(R.id.alternative7);
            cbxEight = (CheckBox) rootView.findViewById(R.id.alternative8);
            cbxNine = (CheckBox) rootView.findViewById(R.id.alternative9);
            alternatives = new ArrayList<CheckBox>();
            alternatives.add(cbxOne);
            alternatives.add(cbxTwo);
            alternatives.add(cbxThree);
            alternatives.add(cbxFour);
            alternatives.add(cbxFive);
            alternatives.add(cbxSix);
            alternatives.add(cbxSeven);
            alternatives.add(cbxEight);
            alternatives.add(cbxNine);

            alternativesLabels = getResources().getStringArray(R.array.interests_array);

            mSharedPrefs = getActivity().getSharedPreferences(
                    Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);


            if(mSharedPrefs.getBoolean(Constants.SHAREDPREFS.USER_INTERESTS_PICKED, false)){
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<String>>(){}.getType();
                String temp = mSharedPrefs.getString(
                        Constants.SHAREDPREFS.USER_INTERESTS_LIST, "");
                interestsList = gson.fromJson(temp
                        , type);
            }


            for(int i = 0; i<9; i++){
                alternatives.get(i).setText(alternativesLabels[i]);
                if((mSharedPrefs.getBoolean(Constants.SHAREDPREFS.USER_INTERESTS_PICKED, false))
                        && interestsList != null){
                    for(int j = 0; j < interestsList.size(); j++){
                        if(alternatives.get(i).getText().equals(interestsList.get(j))){
                            alternatives.get(i).setChecked(true);
                        }
                    }
                }
            }

            btnFinish = (Button) rootView.findViewById(R.id.button_finish);
            btnFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> interestsList = checkWhichBoxesAreChecked();
                    Gson gson = new Gson();
                    mEditor = mSharedPrefs.edit();
                    String jsonList = gson.toJson(interestsList);
                    mEditor.putString(Constants.SHAREDPREFS.USER_INTERESTS_LIST, jsonList);
                    mEditor.putBoolean(Constants.SHAREDPREFS.USER_INTERESTS_PICKED, true);
                    mEditor.commit();

                    getActivity().finish();
                }
            });

            return rootView;
        }

        public ArrayList<String> checkWhichBoxesAreChecked(){
            ArrayList<String> pickedInterests = new ArrayList<String>();
            if(cbxOne.isChecked()){
                pickedInterests.add((String) cbxOne.getText());
            }
            if(cbxTwo.isChecked()){
                pickedInterests.add((String) cbxTwo.getText());
            }
            if(cbxThree.isChecked()){
                pickedInterests.add((String) cbxThree.getText());
            }
            if(cbxFour.isChecked()){
                pickedInterests.add((String) cbxFour.getText());
            }
            if(cbxFive.isChecked()){
                pickedInterests.add((String) cbxFive.getText());
            }
            if(cbxSix.isChecked()){
                pickedInterests.add((String) cbxSix.getText());
            }
            if(cbxSeven.isChecked()){
                pickedInterests.add((String) cbxSeven.getText());
            }
            if(cbxEight.isChecked()){
                pickedInterests.add((String) cbxEight.getText());
            }
            if(cbxNine.isChecked()){
                pickedInterests.add((String) cbxNine.getText());
            }

            for(int i = 0; i<pickedInterests.size(); i++){
                Log.i("SettingsFragment", pickedInterests.get(i));
            }

            return pickedInterests;
            //Toast.makeText(getActivity(), pickedInterests, Toast.LENGTH_SHORT).show();
        }
    }
}
