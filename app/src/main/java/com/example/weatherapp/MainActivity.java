package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private TextView cityNameText, temperatureText, humidityText, descriptionText, windText;
    private ImageView weatherIcon;
    private EditText cityNameInput;
    private static final String API_KEY = "8030513b53924726b9961947250107";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityNameText = findViewById(R.id.cityNameText);
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        descriptionText = findViewById(R.id.descriptionText);
        weatherIcon = findViewById(R.id.weatherIcon);
        Button refreshButton = findViewById(R.id.fetchWeatherButton);
        cityNameInput = findViewById(R.id.cityNameInput);

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = cityNameInput.getText().toString();
                if (!cityName.isEmpty()) {
                    FetchWeatherData(cityName);
                } else {
                    cityNameInput.setError("Please enter a city name");
                }
            }
        });

        FetchWeatherData("Mumbai");
    }

    public void FetchWeatherData(String cityName) {
        String url = "https://api.weatherapi.com/v1/current.json?key="
                + API_KEY
                + "&q="
                + cityName;

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String result = response.body().string();
                runOnUiThread(() -> updateUI(result));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void updateUI(String result) {
        if (result != null) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject current = jsonObject.getJSONObject("current");
                JSONObject location = jsonObject.getJSONObject("location");

                double temperature = current.getDouble("temp_c");
                double humidity = current.getDouble("humidity");
                double windSpeed = current.getDouble("wind_kph");
                String description = current.getJSONObject("condition").getString("text");
                String iconUrl = "https:" + current.getJSONObject("condition").getString("icon");

                cityNameText.setText(location.getString("name"));
                temperatureText.setText(String.format("%.0f", temperature));
                humidityText.setText(String.format("%.0f%%", humidity));
                windText.setText(String.format("%.0f km/hr", windSpeed));
                descriptionText.setText(description);

                // Load icon from WeatherAPI with Glide
                Glide.with(this)
                        .load(iconUrl)
                        .into(weatherIcon);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
