package com.trysurfer.surfer.user;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trysurfer.surfer.Constants;
import com.trysurfer.surfer.MainActivity;
import com.trysurfer.surfer.R;
import com.trysurfer.surfer.SplashFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class InterestsActivity extends ActionBarActivity {

    private Button btnFinish;
    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        /*
        mSharedPrefs = getApplicationContext().getSharedPreferences(
                Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
        mEditor = mSharedPrefs.edit();

        btnFinish = (Button) findViewById(R.id.btn_interests_finish);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                mEditor.putBoolean("user_interests_picked", true);
                mEditor.commit();

                finish();
            }
        });
        */
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onPause() {
        //Log.v(LOG_TAG, "onPause");

        super.onPause();
    }

    @Override
    public void onDestroy() {
        //Log.v(LOG_TAG, "onDestroy");

        super.onDestroy();
    }

    public static class InterestsFragment extends Fragment {

        private CheckBox cbxOne, cbxTwo, cbxThree, cbxFour,
                cbxFive, cbxSix, cbxSeven, cbxEight, cbxNine;
        private Button btnFinish;
        private ArrayList<CheckBox> alternatives;
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

            for(int i = 0; i<9; i++){
                alternatives.get(i).setText(alternativesLabels[i]);
            }

            mSharedPrefs = getActivity().getSharedPreferences(
                    Constants.SHAREDPREFS.APP_PREFS, Context.MODE_PRIVATE);
            mEditor = mSharedPrefs.edit();

            btnFinish = (Button) rootView.findViewById(R.id.button_finish);
            btnFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> interestsList = checkWhichBoxesAreChecked();
                    Gson gson = new Gson();
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
                Log.i("InterestsFragment", pickedInterests.get(i));
            }

            return pickedInterests;
            //Toast.makeText(getActivity(), pickedInterests, Toast.LENGTH_SHORT).show();
        }
    }
}