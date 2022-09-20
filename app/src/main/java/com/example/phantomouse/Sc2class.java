package com.example.phantomouse;


import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;



//import com.google.mediapipe.components.PermissionHelper;


public class Sc2class extends Fragment {
    private static final String TAG = "S2ClassFrag";
    private TFLiteModel tfLiteModel;
    private  TextView newtext;
    private PreviewView prev;
    private SurfaceView draw;

    public Sc2class(TFLiteModel tfLiteModel){
        super();
        this.tfLiteModel = tfLiteModel;

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen2, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        newtext = (TextView) getActivity().findViewById(R.id.logMessage);
        logit("On device logging zone: ");
        prev = getView().findViewById(R.id.previewDisplaySurface);
        draw = getView().findViewById(R.id.drawingSurface);

        tfLiteModel.updateViews(prev, draw);
        tfLiteModel.onResume();

        SeekBar seekBar_r = getView().findViewById(R.id.right_contrl);
        seekBar_r.setMax(50);
        seekBar_r.setProgress(25);
        seekBar_r.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tfLiteModel.controle_right_scale = 0.5f + (progress / ((float)seekBar.getMax()*2));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar seekBar_l = getView().findViewById(R.id.left_contrl);
        seekBar_l.setMax(50);
        seekBar_l.setProgress(25);
        seekBar_l.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tfLiteModel.controle_left_scale = 0f + (progress / ((float)seekBar.getMax()*2));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar seekBar_u = getView().findViewById(R.id.up_contrl);
        seekBar_u.setMax(50);
        seekBar_u.setProgress(25);
        seekBar_u.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tfLiteModel.controle_up_scale = 0f + (progress / ((float)seekBar.getMax()*2));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar seekBar_d = getView().findViewById(R.id.down_contrl);
        seekBar_d.setMax(50);
        seekBar_d.setProgress(25);
        seekBar_d.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tfLiteModel.controle_down_scale = 0.5f + (progress / ((float)seekBar.getMax()*2));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }

    public void logit(String s){
        newtext.setText(s);
    }

    @Override
    public void onResume() {
        super.onResume();
        tfLiteModel.updateViews(prev, draw);
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
