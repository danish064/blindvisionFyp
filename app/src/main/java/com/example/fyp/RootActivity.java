package com.example.fyp;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceRequest;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RootActivity extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private int REQUEST_CODE_PERMISSIONS_result = 0;
    //    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    TextView detectedObjectName;
    PreviewView detectionFrame;

    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        setContentView(R.layout.activity_root);
        detectedObjectName = findViewById(R.id.detectedObject);
        detectionFrame = findViewById(R.id.detectionFrame);
        detectedObjectName.setText("Starting detection, value set programmatically");

        if (allPermissionsGranted()) {
            Toast.makeText(this, "All permissions given", Toast.LENGTH_SHORT).show();
            startCamera(); //start camera if permission has been granted by user
        } else {
            Toast.makeText(this, "No camera permission, please allow", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Toast.makeText(this, String.join("Request code: ", String.valueOf(requestCode)), Toast.LENGTH_SHORT).show();
        REQUEST_CODE_PERMISSIONS_result = requestCode;
//        detectedObjectName.setText(requestCode);
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                CharSequence msg = "Permissions granted: " + String.valueOf(requestCode);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                startCamera();
            } else {
                CharSequence msg = "Permissions not granted: " + String.valueOf(requestCode);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
//                this.finish();
            }
        }

//        }
    }

    private void startCamera() {
        Toast.makeText(this, "Starting camera..", Toast.LENGTH_SHORT).show();

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

//        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
//        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);
//
//        // Query if extension is available (optional).
//        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
//            // Enable the extension if available.
//            hdrImageCaptureExtender.enableExtension(cameraSelector);
//        }

        final ImageCapture imageCapture = builder.setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation()).build();

//        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
//        SurfaceTexture surfaceTexture = detectionFrame.getSurfaceTexture();
//        Surface surface = new Surface(surfaceTexture);

        preview.setSurfaceProvider(detectionFrame.getSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);


    }
}
