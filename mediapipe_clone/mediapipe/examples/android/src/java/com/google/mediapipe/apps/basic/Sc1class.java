package com.google.mediapipe.apps.basic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
// import com.google.mediapipe.apps.basic.R;
// import android.R;


import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;


public class Sc1class extends Fragment {
    // PreviewView previewView;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen1, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // previewView = getView().findViewById(R.id.previewView);

        // Request camera permissions

//        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
//        }
    }

    // void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
    //     Preview preview = new Preview.Builder()
    //             .build();

    //     CameraSelector cameraSelector = new CameraSelector.Builder()
    //             .requireLensFacing(CameraSelector.LENS_FACING_BACK)
    //             .build();

    //     preview.setSurfaceProvider(previewView.getSurfaceProvider());

    //     Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    // }


}
