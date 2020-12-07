package com.example.digitalthermometer;

/*
 * Wrapper class for the FLIR One thermal camera.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;
import android.view.View;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.WithHint;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.Rectangle;
import com.flir.thermalsdk.image.TemperatureUnit;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.flir.thermalsdk.image.palettes.Palette;
import com.flir.thermalsdk.image.palettes.PaletteManager;
import com.flir.thermalsdk.live.Camera;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.ConnectParameters;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.live.discovery.DiscoveryFactory;
import com.flir.thermalsdk.live.streaming.ThermalImageStreamListener;

import java.io.IOException;

public class ThermalCamera {

    private static final String TAG = "ThermalCamera";
    private final CameraListener videoListener;
    private final DiscoveryEventListener findHardwareListener;
    private Identity hardwareIdentity;
    private Camera camera;
    private ThermalActivity activity;

    private final Context context;

    private boolean hardwareConnected = false;
    private boolean videoRunning = false;
    TextView word;

    public interface StreamDataListener {
        void streamTempData(double tempAtCenter);
    }

    private StreamDataListener streamDataListener;

    public ThermalCamera(Context appContext, CameraListener appListener) {



        // Save Context
        context = appContext;

        // Initialize Thermal SDK
        ThermalSdkAndroid.init(appContext);

        // Set Video Stream Listener
        videoListener = appListener;

        // Set the Stream Data Listener

        // Set Find Hardware Listener
        findHardwareListener = new DiscoveryEventListener() {
            @Override
            public void onCameraFound(Identity identity) {
                hardwareIdentity = identity;
                DiscoveryFactory.getInstance().stop(CommunicationInterface.USB);
                hardwareConnected = true;
            }

            @Override
            public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
                // do nothing
            }
        };
    }

    public void findHardware() {

        if(hardwareConnected) {
            return;
        }

        DiscoveryFactory.getInstance().scan(findHardwareListener, CommunicationInterface.USB);
    }

    public void start() {
        if(videoRunning) {
            return;
        }

        if(!hardwareConnected) {
            findHardware();
        }


        //------------------------------------------//
        // Determine if the FLIR ONE camera is buggy//
        //------------------------------------------//


        if (UsbPermissionHandler.isFlirOne(hardwareIdentity)) {
            (new UsbPermissionHandler()).requestFlirOnePermisson(hardwareIdentity, context, new UsbPermissionHandler.UsbPermissionListener() {
                @Override

                //-----------------------------------------------------//
                // Perform these actions whenever permission is granted//
                //-----------------------------------------------------//

                public void permissionGranted(@NonNull Identity identity) {
                    try {
                        camera = new Camera();

                        camera.connect(hardwareIdentity, errorCode -> {
                            videoRunning = false;
                            camera = null;
                            hardwareConnected = false;
                        }, new ConnectParameters());

                        camera.subscribeStream(() -> camera.withImage(thermalImage -> {
                            thermalImage.getFusion().setFusionMode(FusionMode.VISUAL_ONLY);
                            Bitmap visual = BitmapAndroid.createBitmap(thermalImage.getImage()).getBitMap();
                            thermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
                            Bitmap thermal = BitmapAndroid.createBitmap(thermalImage.getImage()).getBitMap();
                            videoListener.receive(visual, thermal);


                            //-----------------------------------------//
                            // Subscribe the image stream to a listener//
                            //-----------------------------------------//


                            //camera.subscribeStream(thermalImageStreamListener);


                        }));

                        videoRunning = true;
                    } catch(IOException e) {
                        camera = null;
                        videoRunning = false;
                    }
                }

                @Override
                public void permissionDenied(@NonNull Identity identity) {
                    // do nothing
                }

                @Override
                public void error(ErrorType errorType, Identity identity) {
                    // do nothing
                }
            });
        }
    }

    public void stop() {

        if (!videoRunning || camera == null) {
            return;
        }

        if (camera.isGrabbing()) {
            camera.unsubscribeAllStreams();
        }

        camera.disconnect();

        videoRunning = false;
        camera = null;
    }

    //==========================================//
    // initialize thermal image screen listener //
    //==========================================//

    
    private final ThermalImageStreamListener thermalImageStreamListener = new ThermalImageStreamListener() {
        @Override
        public void onImageReceived() {
            Log.d(TAG, "Image Received");
            withImage(handleIncomingImage);

        }
    };

    //=============================================//
    // run the function when the image is received //
    //=============================================//

    public void withImage(Camera.Consumer<ThermalImage> functionToRun){
        camera.withImage(functionToRun);
    }

    //===============================================//
    //Function to process Thermal Image and update UI//
    //===============================================//
    private final Camera.Consumer<ThermalImage> handleIncomingImage = new Camera.Consumer<ThermalImage>(){

        @Override
        public void accept(ThermalImage thermalImage) {
            try{
                Palette palette = PaletteManager.getDefaultPalettes().get(0);
                thermalImage.setPalette(palette);
                thermalImage.setTemperatureUnit(TemperatureUnit.KELVIN);
                thermalImage.getImageParameters().setEmissivity(0.9);
                thermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
                JavaImageBuffer thermalBuffer = thermalImage.getImage();

                //======================//
                // Take the temperature //
                //======================//

                int width = thermalImage.getWidth() - 1;
                int height = thermalImage.getHeight() - 1;

                double[] temps = thermalImage.getValues(new Rectangle(1, 1, width, height));
                double maxTemp = 0.0;
                for(int i = 0; i < temps.length; i++) {
                    if(temps[i] > maxTemp) maxTemp = temps[i];
                }

                //=====================================================//
                // Convert Kelvin to Celsius and stream the temperature//
                //=========================---------------------------=//
                maxTemp -= 273;
                streamDataListener.streamTempData(maxTemp);

            } catch(Exception e){
                Log.e("Flir", e.getMessage());
            }
        }

    };




}