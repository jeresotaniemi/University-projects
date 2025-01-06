package com.server;

import com.sun.net.httpserver.*;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

class RegistrationHandler implements HttpHandler {

    private UserAuthenticator userAuthenticator;

    public RegistrationHandler(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }

    @SuppressWarnings("resource")
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getRequestHeaders();
        String contentType = "";
        String response = "";
        int code = 200;
        JSONObject obj = null;

        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                //Check the content type is right.
                if (headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                } else {
                    code = 411;
                    response = "No content type in request";
                }
                if (contentType.equalsIgnoreCase("application/json")) {
                    InputStream inputStream = exchange.getRequestBody();

                    String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));
                    inputStream.close();

                    if (text == null || text.length() == 0) {
                        code = 412;
                        response = "No user credentials";

                    } else {
                        try {
                            //Store registration data sent by user to JSONObject
                            obj = new JSONObject(text);
                        } catch (JSONException e) {
                            code = 413;
                            response = "JSON parse error, faulty user JSON";
                        }

                        if (obj.getString("username").length() == 0 || obj.getString("password").length() == 0) {
                            code = 414;
                            response = "No proper user credentials";
                        } else {
                            //Add user to database.
                            Boolean result = userAuthenticator.addUser(obj.getString("username"),
                                    obj.getString("password"),
                                    obj.getString("email"), obj.getString("userNickname"));
                            if (result == false) {
                                code = 405;
                                response = "User already exists";
                            } else {
                                code = 200;
                                response = "User registered";
                            }
                        }
                    }

                    //Send response code and text back to user.
                    byte[] bytes = response.getBytes("UTF-8");
                    exchange.sendResponseHeaders(code, bytes.length);
                    OutputStream s = exchange.getResponseBody();
                    s.write(bytes);
                    s.close();

                } else {
                    code = 407;
                    response = "Content type is not application/json";
                }

            } else {
                code = 401;
                response = "Not supported, only POST is accepted";
            }

        } catch (Exception e) {
            code = 500;
            response = "Internal server error";
        }

        if (code >= 400) {
            //Send response code and text back to user.
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream stream = exchange.getResponseBody();
            stream.write(response.getBytes());
            stream.close();
        }
    }
}
