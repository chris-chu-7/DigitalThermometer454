package com.example.digitalthermometer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.mlkit.vision.face.Face;

import java.util.List;

public class ThermalActivity extends AppCompatActivity {

    private ImageView visualImageView;
    private ImageView thermalImageView;


    private ThermalCamera camera;
    private MeasurementEngine engine;
    private boolean engineBusy = false;
    private int engineBreakCounter = 0;
    private List<Face> faces;
    private Rect visualAreaOfInterest;
    private Rect thermalAreaOfInterest;

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

        // Listener
        CameraListener listener = (visualImage, thermalImage) -> {
            if(engineBusy) {
                return;
            }

            new Thread(() -> {
                engineBusy = true;

                if(engineBreakCounter % 10 == 0) {
                    faces = engine.findFaces(visualImage);
                    if(faces != null && faces.size() != 0) {
                        visualAreaOfInterest = faces.get(0).getBoundingBox();

                        float hScale = ((float) thermalImage.getWidth())/((float) visualImage.getWidth());
                        float vScale = ((float) thermalImage.getHeight())/((float) visualImage.getHeight());

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
                    visualImageView.setImageBitmap(visualImage);
                    thermalImageView.setImageBitmap(thermalImage);
                });

                engineBreakCounter++;
                engineBusy = false;
            }).start();
        };

        // Camera
        camera = new ThermalCamera(getApplicationContext(), listener);
    }

    public void start(View view) {
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
