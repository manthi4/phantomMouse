package com.example.phantomouse;


import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


//import com.google.mediapipe.components.PermissionHelper;


public class Sc2class extends Fragment {
    private static final String TAG = "S2ClassFrag";
//    private MPipe mPipe;
    private  TextView newtext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.screen2, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        newtext = (TextView) getActivity().findViewById(R.id.logMessage);
        logit("hi");

//        mPipe = new MPipe(new SurfaceView(getContext()), getView(), getContext(), getActivity(), newtext); //new SurfaceView(getContext()), getView(), getContext(), getActivity()
    }

    public void logit(String s){
        newtext.setText(s);
    }

    @Override
    public void onResume() {
        super.onResume();
//        mPipe.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        mPipe.onPause();
    }

//    @Override
//    public void onRequestPermissionsResult(
//            int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

}
