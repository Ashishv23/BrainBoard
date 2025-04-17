package com.example.brainboard.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.brainboard.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FactActivity extends AppCompatActivity {

    private TextView overlayTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fact);

        ImageView quoteImageView = findViewById(R.id.quote_image_view);
        overlayTextView = findViewById(R.id.overlay_text);

        // Fetch and display image
        fetchImageFromApi("https://picsum.photos/300", quoteImageView);

        // Fetch and display random fact
        fetchFactFromApi("https://uselessfacts.jsph.pl/api/v2/facts/random");
    }

    private void fetchImageFromApi(String imageUrl, ImageView imageView) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
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

                runOnUiThread(() -> overlayTextView.setText(fact));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> overlayTextView.setText("Failed to load fact."));
            }
        });
    }
}
