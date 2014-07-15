package org.ivandgetic.fivechess;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by ivandgetic on 14/7/10.
 */
public class Main {
    private static final int SOCKET_PORT = 50000;
    public static ArrayList<User> usersList = new ArrayList<User>();

    public static void main(String[] args) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(SOCKET_PORT);
                    System.out.println("服务器启动！");
                    while (true) {
                        Socket socket = serverSocket.accept();
                        System.out.println(socket.getRemoteSocketAddress() + " is Connected");//在控制台显示
                        new Thread(new ServerThread(socket)).start();
                        sendUsersList();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendUsersList() throws IOException {
        for (User user : Main.usersList) {
            new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:clear");
            for (User useri : Main.usersList) {
                new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("information:" + useri.getName() + ":" + useri.getState() + ":" + useri.socket.getRemoteSocketAddress().toString());
            }
        }
    }
}
