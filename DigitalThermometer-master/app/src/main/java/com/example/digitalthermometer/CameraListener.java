package com.example.digitalthermometer;

/*
 * A listener interface that receives the camera's video stream.
 */

import android.graphics.Bitmap;

public interface CameraListener {
    public void receive(Bitmap visualImage, Bitmap thermalImage);
}
