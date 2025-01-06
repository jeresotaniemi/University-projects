package com.server;

import java.sql.SQLException;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONObject;

class UserAuthenticator extends com.sun.net.httpserver.BasicAuthenticator {

    private MessageDatabase messageDB;

    public UserAuthenticator() {
        super("info");

        messageDB = MessageDatabase.getInstance();
    }

    public boolean checkCredentials(String username, String password) {
        try {
            //Check if the password from database match.
            JSONObject obj = messageDB.getUser(username);
            String hashedPassword = obj.getString("password");

            if (hashedPassword.equals(Crypt.crypt(password, hashedPassword))) {
                return true;
            }
        } catch (SQLException e) {
            //Send response code and text back to user.
        }
        return false;
    }

    public boolean addUser(String userName, String password, String email, String userNickname) {
        try {
            if (!messageDB.userExists(userName)) {
                User newUser = new User(userName, password, email, userNickname);
                messageDB.setUser(newUser);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
