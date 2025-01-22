module pl.edu.pwr.micmar.maildemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires com.google.api.client.auth;
    requires google.api.client;
    requires com.google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.extensions.jetty.auth;
    requires java.net.http;
    requires ai.djl.api;
    requires ai.djl.tokenizers;
    requires javax.mail.api;
    requires org.jsoup;
    requires java.sql;
    requires java.desktop;


    exports pl.edu.pwr.micmar.maildemo.application;
    opens pl.edu.pwr.micmar.maildemo.application to javafx.base, javafx.fxml, javafx.web, javafx.graphics;
}