package com.example.ecocampus;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class SecondFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentImageName;
    private LinearLayout llCapturedImages;
    private ArrayList<String> capturedImageNames;

    private EditText etDescription;
    private Button btnCapturePhoto, btnSubmit;
    private int count=1;

    private Bitmap capturedImage;

    public SecondFragment() {
        super(R.layout.fragment_second);  // Inflate the fragment's layout
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        etDescription = getView().findViewById(R.id.etDescription);
        btnCapturePhoto = getView().findViewById(R.id.btnCapturePhoto);
        btnSubmit = getView().findViewById(R.id.btnSubmit);
        llCapturedImages = getView().findViewById(R.id.llCapturedImages);

        capturedImageNames = new ArrayList<>(10);

        // Capture photo when clicked
        btnCapturePhoto.setOnClickListener(v -> capturePhoto());

        // Submit the form when clicked
        btnSubmit.setOnClickListener(v -> {
            String description = etDescription.getText().toString();
            if (!description.isEmpty() && capturedImage != null) {
                Toast.makeText(getActivity(), "Form Submitted!", Toast.LENGTH_SHORT).show();
                // Optionally, perform further actions here, e.g., navigate to another fragment
            } else {
                Toast.makeText(getActivity(), "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void capturePhoto() {
        // Open the camera to capture a photo
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK && data != null) {
            capturedImage = (Bitmap) data.getExtras().get("data");

            // Generate a unique image name (e.g., timestamp or UUID)
            currentImageName = "Captured Image " + (capturedImageNames.size() + 1);
            capturedImageNames.add(currentImageName);

            // Show the image preview dialog
            showImagePreviewDialog();
        }
    }

    private void showImagePreviewDialog() {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.image_preview_dialog, null);
        ImageView imgPreviewDialog = dialogView.findViewById(R.id.imgPreviewDialog);
        Button btnOkPreview = dialogView.findViewById(R.id.btnOkPreview);
        Button btnClosePreview = dialogView.findViewById(R.id.btnClosePreview);

        // Set the captured image to the dialog
        imgPreviewDialog.setImageBitmap(capturedImage);

        // Create the dialog
        Dialog imagePreviewDialog = new Dialog(getContext());
        imagePreviewDialog.setContentView(dialogView);

        // Handle "OK" button click
        btnOkPreview.setOnClickListener(v -> {
            // Add the image name to the form
            addCapturedImageName(currentImageName);
            count++;

            // Dismiss the dialog and allow capturing more images
            imagePreviewDialog.dismiss();

            // Show the capture button again to add more images
            btnCapturePhoto.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);

        });

        // Show the dialog
        imagePreviewDialog.show();
    }

    private void addCapturedImageName(String imageName) {
        // Dynamically add the image filename to the form
        if(count<=10)
        {
        TextView imageTextView = new TextView(getContext());
        imageTextView.setText(imageName);
        llCapturedImages.addView(imageTextView);}
        else {Toast.makeText(getActivity(), "You cannot add more than 10 Images", Toast.LENGTH_SHORT).show();}
    }
}
