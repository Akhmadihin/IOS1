package com.example.kubiki;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TokenExchangeTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String code = params[0];
        String tokenResponse = null;

        try {
            // URL для обмена code на токен
            URL url = new URL("https://oauth2.googleapis.com/token");

            // Параметры запроса
            String postData = "code=" + code +
                    "&client_id=318037731232-pqq3f9udrtinp4d2g8p7d5o4fc184fo5.apps.googleusercontent.com" +
                    "&redirect_uri=com.example.kubiki://callback" + // Должен совпадать с redirect_uri из запроса
                    "&grant_type=authorization_code";

            // Настройка HTTP-запроса
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            // Отправка данных
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(postData);
            wr.flush();
            wr.close();

            // Чтение ответа
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            tokenResponse = response.toString();
            Log.d("OAuth", "Token Response: " + tokenResponse);

        } catch (Exception e) {
            Log.e("OAuth", "Error exchanging token: " + e.getMessage());
        }
        return tokenResponse;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            // Здесь можно распарсить JSON и получить access_token
            Log.d("OAuth", "Token received: " + result);
        }
    }
}
