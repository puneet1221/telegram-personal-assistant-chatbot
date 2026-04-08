package com.project.personal_assistant.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Lazy
public class WeatherService {
        @Value("${weather.api.key}")
        private String weather_api_key;

        public String getWeather(String city) {
                try {
                        String urlString = "https://api.openweathermap.org/data/2.5/weather?q="
                                        + city + "&appid=" + weather_api_key + "&units=metric";
                        HttpClient httpClient = HttpClient.newHttpClient();

                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create(urlString))
                                        .GET()
                                        .build();
                        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                        log.info(response.body());
                        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
                        if (root.get("cod").getAsInt() == 200) {
                                String cityName = root.get("name").getAsString();
                                double temp = root.getAsJsonObject("main").get("temp").getAsDouble();
                                double feelsLike = root.getAsJsonObject("main").get("feels_like").getAsDouble();
                                int humidity = root.getAsJsonObject("main").get("humidity").getAsInt();
                                String weatherDesc = root.getAsJsonArray("weather")
                                                .get(0).getAsJsonObject()
                                                .get("description").getAsString();
                                return "📍 City: " + cityName +
                                                "\n🌡 Temp: " + temp + "°C" +
                                                "\n🤔 Feels Like: " + feelsLike + "°C" +
                                                "\n💧 Humidity: " + humidity + "%" +
                                                "\n☁️ Condition: " + weatherDesc;
                        } else {
                                String message = root.get("message").getAsString();
                                return message != null ? message : "Something went wrong ❌ ";
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                        return "❌ Error fetching weather";
                }

        }
}
