package com.trysurfer.surfer.user;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.trysurfer.surfer.R;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class InterestsFragment extends Fragment {

    private CheckBox cbx_one, cbx_two, cbx_three, cbx_four,
            cbx_five, cbx_six, cbx_seven, cbx_eight, cbx_nine;
    private Button btn_finish;

    public InterestsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_interests, container, false);

        CheckBox cbxOne = (CheckBox) rootView.findViewById(R.id.alternative1);
        CheckBox cbxTwo = (CheckBox) rootView.findViewById(R.id.alternative2);
        CheckBox cbxThree = (CheckBox) rootView.findViewById(R.id.alternative3);
        CheckBox cbxFour = (CheckBox) rootView.findViewById(R.id.alternative4);
        CheckBox cbxFive = (CheckBox) rootView.findViewById(R.id.alternative5);
        CheckBox cbxSix = (CheckBox) rootView.findViewById(R.id.alternative6);
        CheckBox cbxSeven = (CheckBox) rootView.findViewById(R.id.alternative7);
        CheckBox cbxEight = (CheckBox) rootView.findViewById(R.id.alternative8);
        CheckBox cbxNine = (CheckBox) rootView.findViewById(R.id.alternative9);

        /*
        Button btnFinish = (Button) rootView.findViewById(R.id.button_finish);

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkWhichBoxesAreChecked(rootView);
            }
        });
        */

        return rootView;
    }

    public void checkWhichBoxesAreChecked(View view){
        String temp = "";
        if(cbx_one.isChecked()){
            temp += "1 is checked, ";
        }
        if(cbx_two.isChecked()){
            temp += "2 is checked, ";
        }
        if(cbx_three.isChecked()){
            temp += "3 is checked, ";
        }
        if(cbx_four.isChecked()){
            temp += "4 is checked, ";
        }
        if(cbx_five.isChecked()){
            temp += "5 is checked, ";
        }
        if(cbx_six.isChecked()){
            temp += "6 is checked, ";
        }
        if(cbx_seven.isChecked()){
            temp += "7 is checked, ";
        }
        if(cbx_eight.isChecked()){
            temp += "8 is checked, ";
        }
        if(cbx_nine.isChecked()){
            temp += "9 is checked, ";
        }
        Log.i("InterestsFragment", temp);
    }
}
