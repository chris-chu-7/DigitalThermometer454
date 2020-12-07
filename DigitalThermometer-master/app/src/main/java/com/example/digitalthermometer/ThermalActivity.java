package com.example.digitalthermometer;

import androidx.appcompat.app.AppCompatActivity;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.TemperatureUnit;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.live.streaming.ThermalImageStreamListener;



import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;


import com.flir.thermalsdk.image.Point;
//import com.flir.thermalsdk.the
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.flir.thermalsdk.image.palettes.Palette;
import com.flir.thermalsdk.image.palettes.PaletteManager;
import com.flir.thermalsdk.live.Camera;
import com.google.mlkit.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThermalActivity extends AppCompatActivity {

    private static final String TAG = "ThermalActivity";
    private ImageView visualImageView;
    private ImageView thermalImageView;


    private ThermalCamera camera;
    private Camera cameras;
    private MeasurementEngine engine;
    private boolean engineBusy = false;
    private int engineBreakCounter = 0;
    private List<Face> faces;
    private Rect visualAreaOfInterest;
    private Rect thermalAreaOfInterest;
    TextView word;
   // private final CameraHandle  StreamDataListener streamDataListener;

    public StreamDataListener streamDataListener;


    public interface StreamDataListener {
        void streamTempData(double tempAtCenter);
    }




    public int max(int i, int j){
        return (i > j) ? i : j;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thermal);

        // UI Objects
        visualImageView = findViewById(R.id.visual_image_view);
        thermalImageView = findViewById(R.id.thermal_image_view);

        // Paint Settings
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);


        // Measurement Engine and Face Area
        engine = new MeasurementEngine();
        visualAreaOfInterest = null;
        thermalAreaOfInterest = null;
        AtomicBoolean bitmapFinished = new AtomicBoolean(false);

        // Listener
        CameraListener listener = (visualImage, thermalImage) -> {
            if(engineBusy) {
                return;
            }

            new Thread(() -> {
                engineBusy = true;
                int[] cameraMiddle = new int[2];

                if(engineBreakCounter % 10 == 0) {
                    faces = engine.findFaces(visualImage);
                    if(faces != null && faces.size() != 0) {
                        visualAreaOfInterest = faces.get(0).getBoundingBox();

                        float hScale = ((float) thermalImage.getWidth())/((float) visualImage.getWidth());
                        float vScale = ((float) thermalImage.getHeight())/((float) visualImage.getHeight());

                        //sets up the area of interest from the left to the right.
                        thermalAreaOfInterest = new Rect();
                        thermalAreaOfInterest.left = (int) (hScale * (float) visualAreaOfInterest.left);
                        thermalAreaOfInterest.right = (int) (hScale * (float) visualAreaOfInterest.right);
                        thermalAreaOfInterest.top = (int) (vScale * (float) visualAreaOfInterest.top);
                        thermalAreaOfInterest.bottom = (int) (vScale * (float) visualAreaOfInterest.bottom);

                        if((thermalAreaOfInterest.right + thermalAreaOfInterest.left) / 2 != 0 && (thermalAreaOfInterest.top + thermalAreaOfInterest.bottom) / 2 != 0){
                            cameraMiddle[0] = max(cameraMiddle[0], (thermalAreaOfInterest.right + thermalAreaOfInterest.left) / 2);
                            cameraMiddle[1] = max(cameraMiddle[1], (thermalAreaOfInterest.top + thermalAreaOfInterest.bottom) / 2);
                        }

                    } else {
                        visualAreaOfInterest = null;
                        thermalAreaOfInterest = null;
                    }
                }

                if(visualAreaOfInterest != null && thermalAreaOfInterest != null) {
                    Canvas visualCanvas = new Canvas(visualImage);
                    visualCanvas.drawRect(visualAreaOfInterest, paint);

                    Canvas thermalCanvas = new Canvas(thermalImage);
                    thermalCanvas.drawRect(thermalAreaOfInterest, paint);
                }

                runOnUiThread(() -> {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    byte[] byteArray = byteArrayOutputStream .toByteArray();
                    visualImageView.setImageBitmap(visualImage);
                    thermalImageView.setImageBitmap(thermalImage);
                    thermalImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    word = (TextView) findViewById(R.id.test_view);

                    Point pt = new Point(10, 10);
                    //double temp = thermalImage.getValueAt(pt);
                    //ThermalImage realImage = new ThermalImage();
                   // if(bitmapFinished.get()){
                     //   bitmapFinished.set(false);
                        if(encoded != null || encoded.equals("")){

                            int pixel = thermalImage.getPixel(thermalImage.getWidth() / 2, thermalImage.getHeight() / 2);
                            String toString = Integer.toString(pixel);
                            Color myColor = new Color();
                            //ThermalImage bit = new ThermalImage(thermalImageView);
                            int redValue = Color.red(pixel);
                            int blueValue = Color.blue(pixel);
                            int greenValue = Color.green(pixel);
                            word.setText(redValue + " " + blueValue + " " + greenValue);

                        } else {
                            word.setText("null");
                        }
                    //}



                });

                engineBreakCounter++;
                engineBusy = false;
            }).start();
        };

        // Camera
        camera = new ThermalCamera(getApplicationContext(), listener);
    }

    /**
     * Called whenever there is a new Thermal Image available, should be used in conjunction with {@link Camera.Consumer}
     */

    /*
    private final ThermalImageStreamListener thermalImageStreamListener = new ThermalImageStreamListener() {
        @Override
        public void onImageReceived() {
            //Will be called on a non-ui thread
            Log.d(TAG, "onImageReceived(), we got another ThermalImage");
            withImage(this, handleIncomingImage);
        }
    };*/

    @Deprecated
    private void withImage(ThermalImageStreamListener listener, Camera.Consumer<ThermalImage> functionToRun) {
        cameras.withImage(listener, functionToRun);
    }

    private void withImage(Camera.Consumer<ThermalImage> functionToRun){
        cameras.withImage(functionToRun);
    }

    private final ThermalImageStreamListener thermalImageStreamListener = new ThermalImageStreamListener() {
        @Override
        public void onImageReceived() {
            Log.d(TAG, "onImageReceived(), we got another ThermalImage");
            withImage(handleIncomingImage);
        }
    };

    public void startStream(StreamDataListener listener){
        this.streamDataListener = listener;
        cameras.subscribeStream(thermalImageStreamListener);
    }

    public void stopStream(){

    }

    public void start(View view) {
        word = (TextView) findViewById(R.id.test_view);
        //word.setText("Hey this thing is starting now shake ya booty");



        camera.start();
    }

    public void stop(View view) {
        camera.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.stop();
    }

    @Override
    protected void onStop() {
        word = (TextView) findViewById(R.id.test_view);
        word.setText("Hey this thing is stopping now shake ya hips");
        super.onStop();
        camera.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        engine.stop();
    }

    private final Camera.Consumer<ThermalImage> handleIncomingImage = new Camera.Consumer<ThermalImage>() {
        @Override
        public void accept(ThermalImage thermalImage) {
            word = (TextView) findViewById(R.id.test_view);
            word.setText("Image Consuming...");

            try{


            } catch (Exception e){
                
            }
        }
    };

    /**
     * Shows a {@link Toast} on the UI thread.
     * @param text The message to show
     */
    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ThermalActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
