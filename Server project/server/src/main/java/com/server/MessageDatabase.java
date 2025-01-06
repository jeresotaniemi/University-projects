package com.server;

import java.sql.Statement;
import java.util.Base64;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.io.File;
import java.security.SecureRandom;

class MessageDatabase {

    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;
    private Statement queryStatement = null;

    private SecureRandom secureRandom = new SecureRandom();

    private MessageDatabase() {

    }

    public static synchronized MessageDatabase getInstance() {
        if (null == dbInstance) {
            dbInstance = new MessageDatabase();
        }
        return dbInstance;
    }

    public void open(String dbName) throws SQLException {
        File file = new File(dbName);
        boolean fileExists = file.exists() && file.isDirectory();

        String database = "jdbc:sqlite:" + dbName;
        dbConnection = DriverManager.getConnection(database);

        if (fileExists == false) {
            initializeDatabase();
        }
    }

    private boolean initializeDatabase() throws SQLException {

        if (null != dbConnection) {
            //Set database tables data, user and path.
            String createBasicDBData = "create table data (locationName varchar(50) NOT NULL, locationDescription varchar(50) NOT NULL, "
                    + "locationCity varchar(50) NOT NULL, locationCountry varchar(50) NOT NULL, locationStreetAddress varchar(50) NOT NULL, "
                    + "originalPoster varchar(50) NOT NULL, originalPostingTime long NOT NULL, latitude double NOT NULL, longitude double NOT NULL, weather int NOT NULL)";
            String createBasicDBUser = "create table user (username varchar(50) NOT NULL, password varchar(50) NOT NULL, email varchar(50) NOT NULL, userNickname varchar(50) NOT NULL, primary key(username))";
            String createBasicDBPath = "create table path (tour_name varchar(50) NOT NULL, tourDescription varchar(50) NOT NULL)";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createBasicDBData);
            createStatement.executeUpdate(createBasicDBUser);
            createStatement.executeUpdate(createBasicDBPath);
            createStatement.close();

            return true;
        }
        return false;
    }

    public void closeDB() throws SQLException {
        if (null != dbConnection) {
            dbConnection.close();
            dbConnection = null;
        }
    }

    public void setUser(User user) throws SQLException {

        //Salt and hash user password.
        byte bytes[] = new byte[13];
        secureRandom.nextBytes(bytes);
        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes;

        String hashedPassword = Crypt.crypt(user.getPassword(), salt);

        //Set values into user table.
        String setUserString = "insert into user " +
                "VALUES('" + user.getUsername() + "','" + hashedPassword +
                "','" + user.getEmail() + "','" + user.getUserNickname() + "')";
        Statement createStatement;
        createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setUserString);
        createStatement.close();
    }

    public JSONObject getUser(String username) throws SQLException {

        //Get data from 'user' table and store it into JSONObject.
        JSONObject obj = new JSONObject();

        String getUserString = "select username, password, email, userNickname from user where username='" + username
                + "'";

        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getUserString);

        while (rs.next()) {
            obj.put("username", rs.getString("username"));
            obj.put("password", rs.getString("password"));
            obj.put("email", rs.getString("email"));
            obj.put("userNickname", rs.getString("userNickname"));
        }

        return obj;
    }

    public boolean userExists(String username) throws SQLException {

        //Check if the user already exists in the database.
        String getUsernameString = "select username from user where username='" + username + "'";

        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getUsernameString);

        if (rs.next()) {
            return true;
        }

        return false;
    }

    public void setMessage(UserMessage message, String username) throws SQLException {

        //Get username so the userNickname can be save into database.
        JSONObject obj = getUser(username);

        //Set message into 'data' table
        String setMessageString = "insert into data " +
                "VALUES('" + message.getLocationName() + "','" + message.getLocationDescription() +
                "','" + message.getLocationCity() + "','" + message.getLocationCountry() + "','"
                + message.getStreetAddress() +
                "','" + obj.getString("userNickname") + "','" + message.getOriginalPostingTime() + "','"
                + message.getLatitude() + "','" + message.getLongitude() + "','" + message.getWeather() + "')";

        Statement createStatement;
        createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setMessageString);
        createStatement.close();
    }

    public JSONArray getMessages() throws SQLException {

        //Get location data from the 'data' table and store it into JSONObject.
        JSONArray jsonArray = new JSONArray();
        JSONObject object = null;
        String getMessageString = "select locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPoster, originalPostingTime, latitude, longitude, weather from data";
        
        //Set the id for each message, starting from 1.
        int id = 1;

        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessageString);
        while (rs.next()) {
            object = new JSONObject();
            object.put("locationID", id);
            object.put("locationName", rs.getString("locationName"));
            object.put("locationDescription", rs.getString("locationDescription"));
            object.put("locationCity", rs.getString("locationCity"));
            object.put("locationCountry", rs.getString("locationCountry"));
            object.put("locationStreetAddress", rs.getString("locationStreetAddress"));
            object.put("originalPoster", rs.getString("originalPoster"));
            object.put("originalPostingTime", rs.getString("originalPostingTime"));
            if (rs.getDouble("latitude") > 0.0 && rs.getDouble("longitude") > 0.0) {
                object.put("latitude", rs.getDouble("latitude"));
                object.put("longitude", rs.getDouble("longitude"));
                if (rs.getInt("weather") != 999) {
                    object.put("weather", rs.getInt("weather"));
                }
            }
            jsonArray.put(object);
            id++;
        }
        return jsonArray;
    }

    public void setPath(UserPath path) throws SQLException {

        //Set path variables into 'path' table.
        String setPathString = "insert into path " +
                "VALUES('" + path.getTourName() + "','" + path.getTourDescription() + "')";

        Statement createStatement;
        createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setPathString);
        createStatement.close();
    }

    public JSONObject getPath() throws SQLException {

        //Get the whole message with path.
        JSONObject object = null;
        JSONArray array = new JSONArray();
        int id = 1;
        String getPathString = "select tour_name, tourDescription from path";
        String getMessageString = "select locationName, locationDescription, locationCity, locationCountry, locationStreetAddress, originalPoster, originalPostingTime, latitude, longitude, weather from data";

        Statement pathStatement = dbConnection.createStatement();
        Statement msgStatement = dbConnection.createStatement();

        ResultSet rsPath = pathStatement.executeQuery(getPathString);
        ResultSet rsMsg = msgStatement.executeQuery(getMessageString);
        while (rsPath.next()) {
            object = new JSONObject();
            object.put("tour_name", rsPath.getString("tour_name"));
            object.put("tourDescription", rsPath.getString("tourDescription"));

            while (rsMsg.next()) {
                JSONObject locationObject = new JSONObject();
                locationObject.put("locationID", id);
                locationObject.put("locationName", rsMsg.getString("locationName"));
                locationObject.put("locationDescription", rsMsg.getString("locationDescription"));
                locationObject.put("locationCity", rsMsg.getString("locationCity"));
                locationObject.put("locationCountry", rsMsg.getString("locationCountry"));
                locationObject.put("locationStreetAddress", rsMsg.getString("locationStreetAddress"));
                locationObject.put("originalPoster", rsMsg.getString("originalPoster"));
                locationObject.put("originalPostingTime", rsMsg.getString("originalPostingTime"));
                if (rsMsg.getDouble("latitude") > 0.0 && rsMsg.getDouble("longitude") > 0.0) {
                    locationObject.put("latitude", rsMsg.getDouble("latitude"));
                    locationObject.put("longitude", rsMsg.getDouble("longitude"));
                    if (rsMsg.getInt("weather") != 999) {
                        object.put("weather", rsMsg.getInt("weather"));
                    }
                }
                id++;
                array.put(locationObject);
            }
            object.put("locations", array);
        }

        return object;
    }

}
