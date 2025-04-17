package com.example.brainboard.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainboard.databinding.ActivityFactBinding;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FactActivity extends AppCompatActivity {

    private ActivityFactBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Fetch and display image
        fetchImageFromApi("https://picsum.photos/300");

        // Fetch and display random fact
        fetchFactFromApi("https://uselessfacts.jsph.pl/api/v2/facts/random");
    }

    private void fetchImageFromApi(String imageUrl) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                runOnUiThread(() -> binding.quoteImageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void fetchFactFromApi(String factUrl) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(factUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";

                JSONObject jsonObject = new JSONObject(response);
                String fact = jsonObject.getString("text");

                runOnUiThread(() -> binding.overlayText.setText(fact));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> binding.overlayText.setText("Failed to load fact."));
            }
        });
    }
}
