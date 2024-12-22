package com.example.laba_4;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private EditText etCity;
    private TextView tvWeather;
    private ImageView ivWeatherIcon;  // Добавляем ImageView для иконки
    private Button btnGetWeather;

    private static final String TAG = "WeatherApp";
    private static final String API_KEY = "aa1d30fc38424e0aa3765820242111"; // Замените на ваш API-ключ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCity = findViewById(R.id.editTextCity);
        tvWeather = findViewById(R.id.textViewWeatherDescription);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);  // Инициализируем ImageView
        btnGetWeather = findViewById(R.id.buttonGetWeather);

        btnGetWeather.setOnClickListener(view -> {
            String city = etCity.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show();
            } else if (isInternetAvailable()) {
                new WeatherTask().execute(city);
            } else {
                Toast.makeText(this, "Нет подключения к Интернету", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private class WeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvWeather.setText("Загрузка...");
        }

        @Override
        protected String doInBackground(String... params) {
            String city = params[0];
            try {
                // Строим URL для запроса
                String urlString = "https://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=" + city + "&lang=ru";
                Log.d(TAG, "Запрашиваем URL: " + urlString);

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                Log.d(TAG, "Ответ от API: " + result.toString());
                return result.toString();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка запроса: " + e.getMessage(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    // Разбираем JSON-ответ
                    JSONObject json = new JSONObject(result);
                    JSONObject current = json.getJSONObject("current");
                    JSONObject condition = current.getJSONObject("condition");
                    String description = condition.getString("text");  // Описание погоды
                    String iconUrl = "https:" + condition.getString("icon");  // URL иконки погоды
                    double temp = current.getDouble("temp_c");  // Температура

                    // Загружаем иконку погоды с помощью Glide
                    Glide.with(MainActivity.this)
                            .load(iconUrl)
                            .into(ivWeatherIcon);  // Загружаем иконку в ImageView

                    // Формируем строку с погодой
                    tvWeather.setText("Город: " + etCity.getText().toString() +
                            "\nПогода: " + description +
                            "\nТемпература: " + temp + "°C");

                } catch (Exception e) {
                    Log.e(TAG, "Ошибка обработки JSON: " + e.getMessage(), e);
                    tvWeather.setText("Ошибка обработки данных");
                }
            } else {
                tvWeather.setText("Ошибка запроса");
            }
        }
    }
}
