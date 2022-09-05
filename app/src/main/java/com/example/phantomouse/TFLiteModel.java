package com.example.phantomouse;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.model.Model;
import com.example.phantomouse.ml.ProstheticLandmarkModel;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;


import com.google.common.util.concurrent.ListenableFuture;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TFLiteModel {
    private static final String TAG = "TFLITEMODEL";
    private int REQUEST_CODE_PERMISSIONS = 999; // Return code after asking for permission
    private List<String> REQUIRED_PERMISSIONS = Arrays.asList(Manifest.permission.CAMERA); // permission needed

    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private SurfaceTexture previewFrameTexture;
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private final PreviewView previewDisplayView;
    private final SurfaceView drawView;

    private final Activity activity;
    private final TextView logZone;
//    private final BleMouse mouse;
    private final Context parentContext;
    private Camera camera;
    private Executor cameraExecutor;



    public TFLiteModel(PreviewView previewDisplayView, SurfaceView drawingView, View view, Context context, Activity activity, TextView logZone){//}, BleMouse mouse) { //SurfaceView _previewDisplayView, View view, Context context, Activity activity

//        this.mouse = mouse;
        this.activity = activity;
        this.previewDisplayView = previewDisplayView;
        this.drawView = drawingView;
        this.logZone = logZone;
        this.parentContext = context;
        this.cameraExecutor = Executors.newSingleThreadExecutor();

        setupPreviewDisplayView(view);
//        AndroidAssetUtil.initializeNativeAssetManager(context);

        drawView.setZOrderOnTop(true);
        drawView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Request camera permissions
        checkAndRequestPermissions();
        startCamera();



    }

    /**
     * Check all permissions are granted - use for Camera permission in this example.
     */
    private void checkAndRequestPermissions(){

        boolean flag = true;
        for(String it : REQUIRED_PERMISSIONS) {
            if(!(ContextCompat.checkSelfPermission(this.parentContext, it) == PackageManager.PERMISSION_GRANTED)){
                flag = false;
            }
        }

        if (!flag) {
            ActivityCompat.requestPermissions(
                    this.activity, (String[]) REQUIRED_PERMISSIONS.toArray(), REQUEST_CODE_PERMISSIONS
            );
        }


    }


    double prev5x = 0;
    double prev5y = 0;
    double prev5z = 0;
    /*
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
*/

    public void setupPreviewDisplayView(View view) {
//        previewDisplayView.setVisibility(View.GONE);
//        ViewGroup viewGroup = view.findViewById(R.id.preview_display_layout);
//        viewGroup.addView(previewDisplayView);

//        previewDisplayView
//                .getHolder()
//                .addCallback(
//                        new SurfaceHolder.Callback() {
////                            @Override
////                            public void surfaceCreated(SurfaceHolder holder) {
//////                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
////                            }
//
////                            @Override
////                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//////                                onPreviewDisplaySurfaceChanged(holder, format, width, height);
////                            }
//
////                            @Override
////                            public void surfaceDestroyed(SurfaceHolder holder) {
//////                                processor.getVideoSurfaceOutput().setSurface(null);
////                            }
//                        });
    }

//    protected void onPreviewDisplaySurfaceChanged(
//        SurfaceHolder holder, int format, int width, int height) {
//
//        Size viewSize = computeViewSize(width, height);
//
//
//
//    }

    protected Size computeViewSize(int width, int height) {
        return new Size(width, height);
    }

    public void onResume() {

        // Request camera permissions
        checkAndRequestPermissions();
        startCamera();
        previewDisplayView.setVisibility(View.VISIBLE);
        drawView.setVisibility(View.VISIBLE);
    }

    public void onPause() {

        // // Hide preview display until we re-open the camera again.
        previewDisplayView.setVisibility(View.GONE);
        drawView.setVisibility(View.GONE);
    }

    public void startCamera() {
        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this.parentContext);

        cameraProviderFuture.addListener(new Runnable(){

            @Override
            public void run () {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                ProcessCameraProvider cameraProvider;
                try {
                    cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();


                    Preview preview = new Preview.Builder().build();

                    ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                            // This sets the ideal size for the image to be analyse, CameraX will choose the
                            // the most suitable resolution which may not be exactly the same or hold the same
                            // aspect ratio
                            .setTargetResolution(new Size(224, 224))
                            // How the Image Analyser should pipe in input, 1. every frame but drop no frame, or
                            // 2. go to the latest frame and may drop some frame. The default is 2.
                            // STRATEGY_KEEP_ONLY_LATEST. The following line is optional, kept here for clarity
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();
                    imageAnalyzer.setAnalyzer(cameraExecutor, new ImageAnalyzer(parentContext));


                    // Select camera, back is the default. If it is not available, choose front camera
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;



                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();

                    // Bind use cases to camera - try to bind everything at once and CameraX will find
                    // the best combination.
                    camera = cameraProvider.bindToLifecycle(
                            (LifecycleOwner) activity, cameraSelector, preview, imageAnalyzer
                    );

                    // Attach the preview to preview view, aka View Finder
                    preview.setSurfaceProvider(previewDisplayView.getSurfaceProvider());

                } catch (Exception exc) {
                    Log.e(TAG, "Use case binding failed", exc);
                    exc.printStackTrace();
                }
//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }

            }
            },ContextCompat.getMainExecutor(this.parentContext));

    }

    private class ImageAnalyzer implements ImageAnalysis.Analyzer{

        private Model.Options options;
        private ProstheticLandmarkModel handTrackingModel;
        private Paint red;
        private Paint rpaint;
        private YuvToRgbConverter yuvToRgbConverter;
        private Bitmap bitmapBuffer;
        private Matrix rotationMatrix;

        public ImageAnalyzer(Context ctx){
            this.yuvToRgbConverter = new YuvToRgbConverter(ctx);
            CompatibilityList compatList = new CompatibilityList();
            if(compatList.isDelegateSupportedOnThisDevice()) {
                Log.d(TAG, "This device is GPU Compatible ");
                options = new Model.Options.Builder().setDevice(Model.Device.GPU).build();
            }
            else {
                Log.d(TAG, "This device is GPU Incompatible ");
                options = new Model.Options.Builder().setNumThreads(4).build();
            }

            try {
                handTrackingModel = ProstheticLandmarkModel.newInstance(ctx, options);
            }
            catch(IOException e){
                e.printStackTrace();
            }

            red = new Paint();
            red.setStyle(Paint.Style.FILL_AND_STROKE);
            red.setStrokeWidth(10f);
            red.setColor(Color.RED);
            red.setAntiAlias(true);

            rpaint = new Paint();
            rpaint.setStyle(Paint.Style.STROKE);
            rpaint.setStrokeWidth(10f);
            rpaint.setColor(Color.GREEN);
            rpaint.setAntiAlias(true);

        }


        private class Landmark {

            private String coordString;
            int index;
            float x;
            float y;
            Landmark(int index, float x, float y){
                this.index = index;
                this.x = x;
                this.y = y;
                coordString = String.format("X:%.4f Y:%.4f", x,y);
            }

            // For easy logging
            public String toString(){
                return  this.index + " / " + this.coordString;
            }

        }


        @Override
        public void analyze(ImageProxy imageProxy) {

            ArrayList<Landmark> items = new ArrayList<Landmark>();

            // TODO 2: Convert Image to Bitmap then to TensorImage -> then to ByteBuffer for hand tracking
            //also converts to rgb
            //val tfImage = TensorImage.fromBitmap(toBitmap(imageProxy)).buffer
            TensorImage tfImage = new TensorImage(DataType.FLOAT32);
            tfImage.load(toBitmap(imageProxy));

            //val resize_op = ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR)
            ResizeWithCropOrPadOp resize_op = new ResizeWithCropOrPadOp(224,224);
            TensorImage htImage = resize_op.apply(tfImage);
            //norm to 0,1
            float[] htImageArr = tensorImageToNormFloatArray(htImage);

            System.out.println("HTIMAGE ARR SHAPE: " + htImageArr.length);

            int[] dims = {1, 224, 224, 3};
            TensorBuffer htBuf = TensorBuffer.createFixedSize(dims, DataType.FLOAT32);
            htBuf.loadArray(htImageArr);

            //val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

            System.out.println("SIZE: " + tfImage.getWidth() + " " + tfImage.getHeight() + " " + tfImage.getDataType());
            System.out.println("COL " + tfImage.getColorSpaceType());
            System.out.println("shape " +  tfImage.getTensorBuffer().toString());
            //System.out.println("shape " + inputFeature0.buffer.toString())

            //inputFeature0.loadBuffer(tfImage)

            // Runs hand tracking model inference and gets results
            ProstheticLandmarkModel.Outputs outputs = handTrackingModel.process(htBuf);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            System.out.println("Output Shape: " + Arrays.toString(outputFeature0.getShape()));

            //convert output tensor into landmarks
            float[] outputFloatArr = outputFeature0.getFloatArray();
            System.out.println("float arr shape: " + outputFloatArr.length);

            int ind = 0;
            for (int i = 0; i < outputFloatArr.length; i += 2){
                items.add(new Landmark( ind,
                        outputFloatArr[i],
                        outputFloatArr[i+1]
                        )
                );
                ind++;

                System.out.println("Landmark: " + ind + " | " + outputFeature0.getFloatValue(i) + ", " + outputFeature0.getFloatValue(i+1));
            }
            //items.add(Landmark(ind, outputFeature1.getFloatValue(0), 0.toFloat(), 0.toFloat()))
            //items.add(Landmark(ind+1, outputFeature2.getFloatValue(0), 0.toFloat(), 0.toFloat()))

            /**DRAW TO SCREEN**/
//            landmarkRenderer.drawLandmarks(items)



//            var bitmap = Bitmap.createBitmap(drawView.width, drawView.height, Bitmap.Config.RGB_565)
            SurfaceHolder surfaceHolder = drawView.getHolder();
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawColor(Color.TRANSPARENT);


            float radius = 20f;
            int w = drawView.getWidth();//canvas.width
            int h = drawView.getHeight();//canvas.height
            System.out.println("cw, ch: " + w + " " + h);
            float minx = (float) w;
            float miny = (float) h;
            float maxx = 0f;
            float maxy = 0f;
            float prevminx = (float) w;
            for (Landmark item : items)
            {
                float x = (float) (item.x / 224.0) * w;
                float y = (float) (item.y / 224.0) * h;

                //get second smallest for minx to kill outlier
                if(x < minx)
                {
                    prevminx = minx;
                    minx = x;
                }
                miny = Math.min(miny, y);
                maxx = Math.max(maxx, x);
                maxy = Math.max(maxy, y);

                canvas.drawCircle(x,y, radius, red);
            }

            //draw bbox
            canvas.drawRect(prevminx, miny, maxx, maxy, rpaint);
            canvas.drawCircle((maxx-prevminx)/2 +prevminx, (maxy-miny)/2 + miny, 20f, rpaint);

            surfaceHolder.unlockCanvasAndPost(canvas);


            // Close the image,this tells CameraX to feed the next image to the analyzer
            imageProxy.close();
        }

        /**
         * Convert Image Proxy to Bitmap
         */

        @SuppressLint("UnsafeExperimentalUsageError")
        private Bitmap toBitmap(ImageProxy imageProxy){

            Image image;
            if(imageProxy.getImage().equals(null)){
                return null;
            }
            else{
                image = imageProxy.getImage();
            }

            // Initialise Buffer
            if (bitmapBuffer == null) {
                // The image rotation and RGB image buffer are initialized only once
                Log.d(TAG, "Initalise toBitmap()");
                rotationMatrix = new Matrix();
                rotationMatrix.postRotate((float) imageProxy.getImageInfo().getRotationDegrees());
                bitmapBuffer = Bitmap.createBitmap(
                        imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888
                );
            }

            // Pass image to an image analyser
            yuvToRgbConverter.yuvToRgb(image, bitmapBuffer);

            // Create the Bitmap in the correct orientation
            return Bitmap.createBitmap(
                    bitmapBuffer,
                    0,
                    0,
                    bitmapBuffer.getWidth(),
                    bitmapBuffer.getHeight(),
                    rotationMatrix,
                    false
            );
        }

        private float[] tensorImageToNormFloatArray(TensorImage ti){
            float[] ti_arr = ti.getTensorBuffer().getFloatArray();
//            var iter = ti_arr.listIterator()
//            while(iter.hasNext()){
//                val oldVal = iter.next()
//                iter.set(oldVal/255f)
//            }

            return ti_arr;

        }

    }

}
