package com.example.phantomouse;


import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;


//import com.google.mediapipe.components.PermissionHelper;

import java.util.ArrayList;


public class Sc1class extends Fragment {
    private static final String TAG = "S1ClassFrag";
    private TFLiteModel tfLiteModel;
    private  TextView newtext;
    private BleMouse mouse;
    private PreviewView prev;
    private SurfaceView draw;

    public Sc1class(BleMouse mouse, TFLiteModel tfLiteModel){
        super();
        this.mouse = mouse;
        this.tfLiteModel = tfLiteModel;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen1, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        newtext = (TextView) getActivity().findViewById(R.id.logMessage);
        logit("On device logging zone: ");
        prev = getView().findViewById(R.id.previewDisplaySurface);
        draw = getView().findViewById(R.id.drawingSurface);

        tfLiteModel.init(prev, draw, getView(), getContext(), getActivity(), newtext);

        Switch landmarkToggle = getActivity().findViewById(R.id.switch1);
        landmarkToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tfLiteModel.showLandmarks = isChecked;
            }
        });

    }

    public void logit(String s){
        newtext.setText(s);
    }

    @Override
    public void onResume() {
        super.onResume();
//        tfLiteModel.updateViews(prev, draw);
        tfLiteModel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        tfLiteModel.onPause();
    }

//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

}
