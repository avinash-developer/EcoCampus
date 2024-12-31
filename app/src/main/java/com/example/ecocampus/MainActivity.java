package com.example.ecocampus;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load the FirstFragment (QR Scan Fragment) by default
        if (savedInstanceState == null) {
            loadFragment(new FirstFragment());
        }
    }

    // Method to load a fragment dynamically
    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);  // Replace the container with the new fragment
        transaction.addToBackStack(null);  // Allow back navigation
        transaction.commit();  // Commit the transaction
    }

    // Optionally, you can handle back navigation here (if needed)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Additional actions before going back, if required
        Toast.makeText(this, "Back pressed", Toast.LENGTH_SHORT).show();
    }
}
