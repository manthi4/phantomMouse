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
import android.media.Image;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.model.Model;
import com.example.phantomouse.ml.ProstheticLandmarkModel;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;


import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TFLiteModel {
    private static final String TAG = "TFLITEMODEL";
    private int REQUEST_CODE_PERMISSIONS = 999; // Return code after asking for permission
    private List<String> REQUIRED_PERMISSIONS = Arrays.asList(Manifest.permission.CAMERA); // permission needed


    // {@link previewDisplayView} that displays the camera-preview frames.
    private final PreviewView previewDisplayView;
    // drawView where the landmarks are drawn
    private final SurfaceView drawView;

    private final Activity parentActivity;
    private final TextView logZone;
    private final BleMouse mouse;
    private final Context parentContext;
    private Camera camera;
    private Executor cameraExecutor;

    public Boolean showLandmarks;
    public Boolean showBBox;
    public float sensitivity = 10;
    public float controle_left_scale = 1f/3;
    public float controle_right_scale = 2f/3;
    public float controle_down_scale =  3f/4;
    public float controle_up_scale = 1f/3;

    private Boolean paused = false;

    private ListenableFuture cameraProviderFuture;


    public TFLiteModel(PreviewView previewDisplayView, SurfaceView drawingView, View view, Context context, Activity activity, TextView logZone, BleMouse mouse) {

        this.mouse = mouse;
        this.parentActivity = activity;
        this.previewDisplayView = previewDisplayView;
        this.drawView = drawingView;
        this.logZone = logZone;
        this.parentContext = context;
        this.cameraExecutor = Executors.newSingleThreadExecutor();

        this.showLandmarks = false;
        this.showBBox = true;

        setupPreviewDisplayView(view);
        drawView.setZOrderOnTop(true);
        drawView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // Request camera permissions
        checkAndRequestPermissions();
        startCamera();



    }

//    Check all permissions are granted - use for Camera permission in this example.
    private void checkAndRequestPermissions(){
        boolean flag = false;
        for(String it : REQUIRED_PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(this.parentContext, it) != PackageManager.PERMISSION_GRANTED){
                flag = true;
            }
        }
        if (flag) {
            ActivityCompat.requestPermissions(
                    this.parentActivity, (String[]) REQUIRED_PERMISSIONS.toArray(), REQUEST_CODE_PERMISSIONS
            );
        }
    }

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
//        Size viewSize = computeViewSize(width, height);
//    }

//    protected Size computeViewSize(int width, int height) {
//        return new Size(width, height);
//    }

    public void onResume() {
        // Request camera permissions
        checkAndRequestPermissions();
        startCamera();
        previewDisplayView.setVisibility(View.VISIBLE);
        drawView.setVisibility(View.VISIBLE);
    }

    public void onPause(){
        paused = true;
        // // Hide preview display until we re-open the camera again.
        previewDisplayView.setVisibility(View.GONE);
        drawView.setVisibility(View.GONE);
        try {
            ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
            cameraProvider.unbindAll();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("paused");

//        ImageAnalyzer.
    }

    public void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.parentContext);


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
                            (LifecycleOwner) parentActivity, cameraSelector, preview, imageAnalyzer
                    );

                    // Attach the preview to preview view, aka View Finder
                    preview.setSurfaceProvider(previewDisplayView.getSurfaceProvider());

                } catch (Exception exc) {
                    Log.e(TAG, "Use case binding failed", exc);
                    exc.printStackTrace();
                }
            }
            },ContextCompat.getMainExecutor(this.parentContext));
    }

    private class ImageAnalyzer implements ImageAnalysis.Analyzer{

        private Model.Options options;
        private ProstheticLandmarkModel handTrackingModel;
        private Paint red;
        private Paint rpaint;
        private Paint black;
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

            black = new Paint();
            black.setStrokeWidth(7f);



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
            if (paused){
                return;
            }
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

//            System.out.println("HTIMAGE ARR SHAPE: " + htImageArr.length);

            int[] dims = {1, 224, 224, 3};
            TensorBuffer htBuf = TensorBuffer.createFixedSize(dims, DataType.FLOAT32);
            htBuf.loadArray(htImageArr);

            //val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

//            System.out.println("SIZE: " + tfImage.getWidth() + " " + tfImage.getHeight() + " " + tfImage.getDataType());
//            System.out.println("COL " + tfImage.getColorSpaceType());
//            System.out.println("shape " +  tfImage.getTensorBuffer().toString());
            //System.out.println("shape " + inputFeature0.buffer.toString())

            //inputFeature0.loadBuffer(tfImage)

            // Runs hand tracking model inference and gets results
            ProstheticLandmarkModel.Outputs outputs = handTrackingModel.process(htBuf);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//            System.out.println("Output Shape: " + Arrays.toString(outputFeature0.getShape()));

            //convert output tensor into landmarks
            float[] outputFloatArr = outputFeature0.getFloatArray();
//            System.out.println("float arr shape: " + outputFloatArr.length);

            int ind = 0;
            for (int i = 0; i < outputFloatArr.length; i += 2){
                items.add(new Landmark( ind,
                        outputFloatArr[i],
                        outputFloatArr[i+1]
                        )
                );
                ind++;

//                System.out.println("Landmark: " + ind + " | x:" + outputFeature0.getFloatValue(i) + ", y:" + outputFeature0.getFloatValue(i+1));

            }
            //items.add(Landmark(ind, outputFeature1.getFloatValue(0), 0.toFloat(), 0.toFloat()))
            //items.add(Landmark(ind+1, outputFeature2.getFloatValue(0), 0.toFloat(), 0.toFloat()))

            /**DRAW TO SCREEN**/
            if (paused){
                return;
            }
//            landmarkRenderer.drawLandmarks(items)

//            var bitmap = Bitmap.createBitmap(drawView.width, drawView.height, Bitmap.Config.RGB_565)
            SurfaceHolder surfaceHolder = drawView.getHolder();
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawColor(Color.TRANSPARENT);


            float radius = 20f;
            float w = drawView.getWidth();//canvas.width
            float h = drawView.getHeight();//canvas.height
//            System.out.println("cw, ch: " + w + " " + h);
            float minx = w;
            float miny = h;
            float maxx = 0f;
            float maxy = 0f;
//            float prevminx = w;
            float x;
            float y;

            /// 0, 0 is the top left corner
            for (Landmark item : items)
            {
                if (item.x == 0.0){
                    continue;
                }
                x = (float) (w - (item.x / 224.0) * w);
                y = (float) (item.y / 224.0) * h;

                minx = Math.min(minx, x);
                miny = Math.min(miny, y);
                maxx = Math.max(maxx, x);
                maxy = Math.max(maxy, y);
                if(showLandmarks){
                    canvas.drawCircle(x,y, radius, red);
                }
            }

            int centerx = (int) ((maxx-minx)/2 +minx);
            int centery = (int) ((maxy-miny)/2 + miny);
            //draw bbox
            canvas.drawRect(minx, miny, maxx, maxy, rpaint);
            canvas.drawCircle(centerx, centery, 20f, rpaint);

            //mouse controle lines
            float controle_left = w * controle_left_scale;
            float controle_right = w * controle_right_scale;
            float controle_down = h * controle_down_scale;
            float controle_up = h * controle_up_scale;

            //hlines
            canvas.drawLine(0, controle_up,  w, controle_up, black);
            canvas.drawLine(0, controle_down,  w, controle_down,black);
            //vlines
            canvas.drawLine(controle_left, 0,  controle_left, h, black);
            canvas.drawLine(controle_right, 0,  controle_right, h, black);

            //blMouse controles
            float deltax = 0;
            float deltay = 0;
            boolean command = false;

            if (centerx < controle_left){// less than means to the left
                command = true;
                deltax = (centerx - controle_left)*sensitivity; // negative goes left
            }
            if (centerx > controle_right){// // greater than mean to the right
                command = true;
                deltax = (centerx - controle_right)*sensitivity; // positive goes right
            }
            if (centery > controle_down){// greater than means below
                command = true;
                deltax = -1*(centery - controle_down)*sensitivity; // negative goes down?
            }
            if (centery < controle_up){// // greater than mean to the right
                command = true;
                deltax = -1*(centery - controle_up)*sensitivity; // positive goes up?
            }
            if(command){
                mouse.moveCommand((int)deltax, (int)deltay);
            }
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
