package com.example.ecocampus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

public class FirstFragment extends Fragment {

    private Button btnScanQR;
    private TextView tvScanInstructions;
    private FusedLocationProviderClient fusedLocationClient;
    private PreviewView previewView;

    // Define the valid latitude and longitude for location validation (e.g., New York City)
    private static final double VALID_LAT = 26.862775;  // New York City Latitude
    private static final double VALID_LON = 75.810796; // New York City Longitude
    private static final double MAX_DISTANCE_METERS = 100;  // Maximum allowed distance (in meters)

    private static final String VALID_QR_CODE = "CS001";  // Example QR code data

    public FirstFragment() {
        super(R.layout.fragment_first);  // Inflate the fragment's layout
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnScanQR = getView().findViewById(R.id.btnScanQR);
        tvScanInstructions = getView().findViewById(R.id.tvScanInstructions);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        previewView = getView().findViewById(R.id.previewView);

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, 100);
        }

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }

        btnScanQR.setOnClickListener(v -> startQRScanner());
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startQRScanner();
        }

        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permissions granted, no action needed
        }
    }

    private void startQRScanner() {
        // Start the Camera and setup the ImageAnalysis for barcode scanning
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(getActivity());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getActivity()));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Create ImageAnalysis to scan QR Codes
        BarcodeScanner scanner = BarcodeScanning.getClient();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getActivity()), imageProxy -> {
            @OptIn(markerClass = ExperimentalGetImage.class) InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            Barcode barcode = barcodes.get(0);
                            String qrContent = barcode.getDisplayValue();
                            //validateLocation(qrContent);
                            validateQRCodeData(qrContent);
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvScanInstructions.setText("Scan failed. Try again.");
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        });

        cameraProvider.bindToLifecycle(getViewLifecycleOwner(), new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build(), preview, imageAnalysis);
    }
    private void validateQRCodeData(String qrContent) {
        if (!qrContent.equals(VALID_QR_CODE)) {
            tvScanInstructions.setText("Invalid QR Code.");
            return;
        }

        validateLocation();  // If QR Code is valid, check the location
    }
    private void validateLocation() {
        // Simulating location validation
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvScanInstructions.setText("Location permission required.");
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double currentLat = location.getLatitude();
                double currentLon = location.getLongitude();

                if (isLocationValid(currentLat, currentLon)) {
                    // QR Code and Location are valid, load second fragment (Form)
                    loadFormFragment();
                } else {
                    tvScanInstructions.setText("Invalid location.");
                }
            }
        });
    }

    private boolean isLocationValid(double currentLat, double currentLon) {
        // Calculate the distance using Location.distanceBetween() method
        float[] results = new float[1];
        Location.distanceBetween(currentLat, currentLon, VALID_LAT, VALID_LON, results);
        float distanceInMeters = results[0];

        // Check if the current location is within the allowed radius
        return distanceInMeters <= MAX_DISTANCE_METERS;
    }

    private void loadFormFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new SecondFragment());  // Load Form Fragment
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
