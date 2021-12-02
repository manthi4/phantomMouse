package com.example.phantomouse;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MPipe {
    private static final String TAG = "MPipe";
    private static final String BINARY_GRAPH_NAME = "hand_tracking_mobile_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
    private static final String INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands";
    private static final int NUM_HANDS = 2;
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    // Flips the camera-preview frames vertically before sending them into FrameProcessor to be
    // processed in a MediaPipe graph, and flips the processed frames back when they are displayed.
    // This is needed because OpenGL represents images assuming the image origin is at the bottom-left
    // corner, whereas MediaPipe in general assumes the image origin is at top-left.
    private static final boolean FLIP_FRAMES_VERTICALLY = true;

    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("opencv_java3");
        System.loadLibrary("mediapipe_jni");
    }

    // Sends camera-preview frames into a MediaPipe graph for processing, and displays the processed
    // frames onto a {@link Surface}.
    protected FrameProcessor processor;
    // Handles camera access via the {@link CameraX} Jetpack support library.
    protected CameraXPreviewHelper cameraHelper;

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private SurfaceTexture previewFrameTexture;
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private final SurfaceView previewDisplayView;

    // Creates and manages an {@link EGLContext}.
    private final EglManager eglManager;
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private ExternalTextureConverter converter;

    private final Activity activity;
    private final TextView logZone;
    private final BleMouse mouse;

    public MPipe(SurfaceView previewDisplayView, View view, Context context, Activity activity, TextView logZone, BleMouse mouse) { //SurfaceView _previewDisplayView, View view, Context context, Activity activity

        this.mouse = mouse;
        this.activity = activity;
        this.previewDisplayView = previewDisplayView;
        this.logZone = logZone;

        setupPreviewDisplayView(view);

        AndroidAssetUtil.initializeNativeAssetManager(context);
        eglManager = new EglManager(null);
        processor =
                new FrameProcessor(
                        context,
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME);
        processor
                .getVideoSurfaceOutput()
                .setFlipY(FLIP_FRAMES_VERTICALLY);
        PermissionHelper.checkAndRequestCameraPermissions(activity);

        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        Map<String, Packet> inputSidePackets = new HashMap<>();
        inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
        processor.setInputSidePackets(inputSidePackets);


        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    Log.v(TAG, "Received multi-hand landmarks packet.");
                    List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks =
                            PacketGetter.getProtoVector(packet, LandmarkProto.NormalizedLandmarkList.parser());
                    activity.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               logZone.setText(
                                       getMultiHandLandmarksDebugString(multiHandLandmarks)
                               );

                           }
                       }
                    );

                }
        );
    }


    double prev5x = 0;
    double prev5y = 0;
    double prev5z = 0;
    private String getMultiHandLandmarksDebugString(List<LandmarkProto.NormalizedLandmarkList> multiHandLandmarks) {
        if (multiHandLandmarks.isEmpty()) {
            return "No hand landmarks";
        }
        String multiHandLandmarksStr = "Number of hands detected: " + multiHandLandmarks.size() + "\n";
        int handIndex = 0;



        for (LandmarkProto.NormalizedLandmarkList landmarks : multiHandLandmarks) {
            multiHandLandmarksStr +=
                    "\t#Hand landmarks for hand[" + handIndex + "]: " + landmarks.getLandmarkCount() + "\n";
            int landmarkIndex = 0;
//            for (LandmarkProto.NormalizedLandmark landmark : landmarks.getLandmarkList()) {
//                multiHandLandmarksStr +=
//                        "\t\tLandmark ["
//                                + landmarkIndex
//                                + "]: ("
//                                + landmark.getX()
//                                + ", "
//                                + landmark.getY()
//                                + ", "
//                                + landmark.getZ()
//                                + ")\n";
//                ++landmarkIndex;
//            }

            //landmark 5
            LandmarkProto.NormalizedLandmark landmark5 = landmarks.getLandmarkList().get(5);
            multiHandLandmarksStr +=
                    "\t\tLandmark ["
                            + 5
                            + "]: \n\tx: "
                            + String.format("%.2f",landmark5.getX())
//                            + landmark5.getX()
                            + "\ty: "
                            + String.format("%.2f",landmark5.getY())
//                            + landmark5.getY()
                            + "\tz: "
                            + String.format("%.2f",landmark5.getZ())
//                            + landmark5.getZ()
                            + ")\n";

            //landmark 5
            LandmarkProto.NormalizedLandmark landmark17 = landmarks.getLandmarkList().get(17);
            multiHandLandmarksStr +=
                    "\t\tLandmark ["
                            + 17
                            + "]: \n\tx: "
                            + String.format("%.2f",landmark17.getX())
//                            + landmark17.getX()
                            + "\ty: "
                            + String.format("%.2f",landmark17.getY())
//                            + landmark17.getY()
                            + "\tz: "
                            + String.format("%.2f",landmark17.getZ())
//                            + landmark17.getZ()
                            + ")\n";


            double xdiff = landmark17.getX() - landmark5.getX();
            double vdiff = landmark5.getY() - landmark17.getY(); //positive means 17 higher than 5

            multiHandLandmarksStr += "\nprevX: " + String.format("%.2f",prev5x) + "\tcurrX: " + String.format("%.2f",landmark5.getX());
            double xdelta = prev5x - landmark5.getX(); //negative if moved right
            prev5x = landmark5.getX();

            double zdelta = prev5z - landmark5.getZ(); //positive if moving away
            prev5z = landmark5.getZ();

            if(vdiff > xdiff){
                mouse.clickCommand(BleMouse.LeftClick);
            }

            if(-1*vdiff > xdiff){
                mouse.clickCommand(BleMouse.RightClick);
            }

            if(Math.abs(vdiff) < xdiff){
                mouse.clickReleaseCommand();
            }

            xdelta = xdelta*-100;
            zdelta = zdelta*((zdelta > 0)?-100:-100);

            multiHandLandmarksStr += "\ndeltax: " + String.format("%.2f",xdelta) + "\tdeltaz: " + String.format("%.2f",zdelta);
            mouse.moveCommand((int) xdelta, (int)zdelta);

            ++handIndex;
        }
        return multiHandLandmarksStr;
    }


    public void setupPreviewDisplayView(View view) {
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = view.findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);

        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                onPreviewDisplaySurfaceChanged(holder, format, width, height);
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }

    protected void onPreviewDisplaySurfaceChanged(
            SurfaceHolder holder, int format, int width, int height) {

        Size viewSize = computeViewSize(width, height);
        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
        boolean isCameraRotated = cameraHelper.isCameraRotated();


        converter.setSurfaceTextureAndAttachToGLContext(
                previewFrameTexture,
                isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
    }

    protected Size computeViewSize(int width, int height) {
        return new Size(width, height);
    }

    public void onResume() {
        converter =
                new ExternalTextureConverter(
                        eglManager.getContext(), 2);
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        if (PermissionHelper.cameraPermissionsGranted(activity)) {
            startCamera();
        }
    }

    public void onPause() {
        converter.close();
        // // Hide preview display until we re-open the camera again.
        previewDisplayView.setVisibility(View.GONE);
    }

    public void startCamera() {
        cameraHelper = new CameraXPreviewHelper();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    //onCameraStarted
                    previewFrameTexture = surfaceTexture;
                    previewDisplayView.setVisibility(View.VISIBLE);
                });
        CameraHelper.CameraFacing cameraFacing = CameraHelper.CameraFacing.FRONT;
        cameraHelper.startCamera(
                activity, cameraFacing, /*previewFrameTexture*/ null, null);
    }

}
