package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class Path implements HttpHandler {

    public Path() {
    }

    MessageDatabase messageDB = MessageDatabase.getInstance();

    public void handle(HttpExchange exchange) throws IOException {

        String response = "";
        int code = 200;

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            try {
                InputStream inputStream = exchange.getRequestBody();
                String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));
                inputStream.close();

                //Store input into JSONObject.
                JSONObject jsonObject = new JSONObject(text);

                String tour_name = jsonObject.getString("tour_name");
                String tourDescription = jsonObject.getString("tourDescription");

                try {
                    //Save variables into UserPath and set them into database. 
                    UserPath userPath = new UserPath(tour_name, tourDescription);
                    messageDB.setPath(userPath);
                } catch (SQLException e) {
                    code = 400;
                    response = "Couldn't save path";
                }
                code = 200;
                response = "Path set";

            } catch (JSONException e) {
                code = 407;
                response = "Invalid type for a field";
            }

        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            JSONArray responseMessages = new JSONArray();
            JSONObject path = new JSONObject();
            try {
                //Store the message + path from the database into JSONArray which is then returned as a response.
                path = messageDB.getPath();
                if (path.isEmpty()) {
                    code = 204;
                    response = "No messages";
                } else {
                    responseMessages.put(path);
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
}
