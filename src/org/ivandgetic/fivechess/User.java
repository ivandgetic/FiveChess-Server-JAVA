package org.ivandgetic.fivechess;

import java.net.Socket;

/**
 * Created by ivandgetic on 14/7/11.
 */
public class User {
    String name;
    String state;
    Socket socket;

    public User(String name, String state, Socket socket) {
        this.name = name;
        this.state = state;
        this.socket = socket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

}
