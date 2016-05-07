package org.ivandgetic.fivechess;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by ivandgetic on 14/7/10.
 */
public class Main {
    public static ArrayList<User> usersList = new ArrayList<User>();

    public static void main(String[] args) {
        createDB();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(50000);
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


    private static void createDB() {
        File dir = new File("FiveChessDB.db");
        if (!dir.exists()) {
            try {
                Class.forName("org.sqlite.JDBC");
                Connection c = DriverManager.getConnection("jdbc:sqlite:FiveChessDB.db");
                Statement stmt = c.createStatement();
                String sql = "CREATE TABLE information (name TEXT NOT NULL,password TEXT NOT NULL)";
                stmt.executeUpdate(sql);
                stmt.close();
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
