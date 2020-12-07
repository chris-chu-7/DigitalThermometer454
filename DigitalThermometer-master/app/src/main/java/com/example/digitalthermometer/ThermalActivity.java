package com.example.digitalthermometer;

import androidx.appcompat.app.AppCompatActivity;


import com.flir.thermalsdk.image.ThermalImage;



import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;


import com.flir.thermalsdk.image.Point;


import com.flir.thermalsdk.live.Camera;
import com.google.mlkit.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThermalActivity extends AppCompatActivity{

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
    public TextView word;
    public ThermalImage thermalImages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        word = findViewById(R.id.test_view);
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
                    double temp = thermalImages.getValueAt(new Point(10, 10));
                    System.out.println(temp);
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






    public void start(View view) {
        word = findViewById(R.id.test_view);
        word.setText("Starting the process now...");
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
        word = findViewById(R.id.test_view);
        word.setText("Camera Stopping");
        super.onStop();
        camera.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        engine.stop();
    }



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
