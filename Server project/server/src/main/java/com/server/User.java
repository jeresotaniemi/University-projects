package com.server;

class User {

    private String username;
    private String password;
    private String email;
    private String userNickname;

    public User() {

    }

    public User(String name, String pass, String mail, String userNickname) {
        this.username = name;
        this.password = pass;
        this.email = mail;
        this.userNickname = userNickname;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getUserNickname() {
        return userNickname;
    }
}
