package com.example.digitalthermometer;

/*
 * Wrapper for using the ML Toolkit to find a face in an image.
 */

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;

import java.util.List;

public class MeasurementEngine {
    private List<Face> list;
    private FaceDetector detector;

    public MeasurementEngine() {
        list = null;
        detector = FaceDetection.getClient();
    }

    public List<Face> findFaces(Bitmap image) {
        Task<List<Face>> result = detector.process(InputImage.fromBitmap(image, 0)).addOnSuccessListener(faces -> {
            list = faces;
        }).addOnFailureListener(e -> {
            list = null;
        });;

        return list;
    }

    public void stop() {
        list = null;
        detector.close();
    }
}
