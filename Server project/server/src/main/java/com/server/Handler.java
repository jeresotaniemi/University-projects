package com.server;

import com.sun.net.httpserver.*;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

class Handler implements HttpHandler {

    public Handler() {
    }

    MessageDatabase messageDB = MessageDatabase.getInstance();

    @SuppressWarnings("resource")
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String response = "";
        int code = 200;

        double latitude = 0.0;  //set latitude and longitude to 0, should never be a coordinate of visited location
        double longitude = 0.0;
        int weather = 999;      //set weather to 999 since that is impossible weather to have

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            try {
                InputStream inputStream = exchange.getRequestBody();
                String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                inputStream.close();

                //Store user's input message to JSONObject
                JSONObject jsonObject = new JSONObject(text);

                //Get the values from the input message
                String locationName = jsonObject.getString("locationName");
                String locationDescription = jsonObject.getString("locationDescription");
                String locationCity = jsonObject.getString("locationCity");
                String locationCountry = jsonObject.getString("locationCountry");
                String locationStreetAddress = jsonObject.getString("locationStreetAddress");

                if (jsonObject.has("longitude") && jsonObject.has("latitude")) {
                    longitude = jsonObject.getDouble("longitude");
                    latitude = jsonObject.getDouble("latitude");

                    try {
                        if (jsonObject.has("weather")) {
                            weather = getWeather(latitude, longitude);
                        }
                    } catch (IOException e) {
                        code = 400;
                        response = "An error occurred while fetching weather data.";
                    } catch (InterruptedException e) {
                        code = 401;
                        response = "The weather service is currently unavailable";
                    } catch (URISyntaxException e) {
                        code = 402;
                        response = "Invalid latitude or longitude provided";
                    }
                }

                //Get the posting time
                ZonedDateTime originalPostingTime;
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                    originalPostingTime = ZonedDateTime.parse(jsonObject.getString("originalPostingTime"), formatter);
                    try {
                        //Save all the variables into UserMessage so they can be used later in the database.
                        UserMessage userMessage = new UserMessage(locationName, locationDescription, locationCity,
                                originalPostingTime, locationCountry, locationStreetAddress, latitude, longitude,
                                weather);
                        //Get username and then set the message into database.
                        String username = exchange.getPrincipal().getUsername();
                        messageDB.setMessage(userMessage, username);
                    } catch (SQLException e) {
                        code = 403;
                        response = "Couldn't save user message";
                    }

                    response = "Message received";
                    code = 200;

                } catch (DateTimeParseException e) {
                    response = "Text cannot be parsed";
                    code = 404;
                }

            } catch (JSONException e) {
                code = 407;
                response = "Invalid type for a field";
            }

        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            JSONArray responseMessages = new JSONArray();
            try {
                //Store the message from the database into JSONArray which is then returned as a response.
                responseMessages = messageDB.getMessages();
                if (responseMessages.isEmpty()) {
                    code = 204;
                    response = "No messages";
                } else {
                    response = responseMessages.toString();
                    code = 200;
                }
            } catch (SQLException e) {
                code = 403;
                response = "Couldn't get response from database";
            }

        } else {
            response = "Not supported";
            code = 400;
        }

        //Send the response code and text to user.
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
    }

    private int getWeather(double latitude, double longitude)
            throws IOException, InterruptedException, URISyntaxException {
        //String to send to weather server.
        String xmlData = "<coordinates>\n" +
                "<latitude>" + latitude + "</latitude>\n" +
                "<longitude>" + longitude + "</longitude>\n" +
                "</coordinates>";
        //Url address of the server.
        String urlString = "http://localhost:4001/weather";

        URI uri = new URI(urlString);

        //Set connection to the server
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/xml");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        byte[] dataBytes = xmlData.getBytes(StandardCharsets.UTF_8);
        outputStream.write(dataBytes);

        //Read the data from server.
        StringBuilder responseBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }
        connection.disconnect();

        //Exclude the temperature from the data.
        String response = responseBuilder.toString();
        int start = response.indexOf("<temperature>") + "<temperature>".length();
        int end = response.indexOf("</temperature>");

        int temperature = Integer.parseInt(response.substring(start, end));

        return temperature;
    }
}