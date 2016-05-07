package org.ivandgetic.fivechess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.*;

/**
 * Created by ivandgetic on 14/7/10.
 */
public class ServerThread implements Runnable {
    private Socket socket = null;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;
    User user;
    private Connection c;
    private Statement stmt;

    public ServerThread(final Socket socket) throws IOException {
        this.socket = socket;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        while (true) try {
            processRead(dataInputStream.readUTF());
        } catch (SocketException e) {
            Main.usersList.remove(user);
            System.out.println(socket.getRemoteSocketAddress() + " is Disconnected");
            try {
                Main.sendUsersList();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            break;
        } catch (EOFException e) {
            Main.usersList.remove(user);
            System.out.println(socket.getRemoteSocketAddress() + " is Disconnected");
            try {
                Main.sendUsersList();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            break;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPlayer(String name) throws IOException {
        new DataOutputStream(socket.getOutputStream()).writeUTF("Login:right");
        user = new User(name, "在线", socket);
        Main.usersList.add(user);
        Main.sendUsersList();
    }

    private void checkPlayerWindUp() throws SQLException {
        stmt.close();
        c.commit();
        c.close();
    }

    private void signin(String name, String password) {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:FiveChessDB.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM information;");
            while (rs.next()) {
                if (name.equals(rs.getString("name"))) {
                    if (password.equals(rs.getString("password"))) {
                        addPlayer(name);
                        rs.close();
                        checkPlayerWindUp();
                    } else {
                        dataOutputStream.writeUTF("Login:wrong");
                        rs.close();
                        checkPlayerWindUp();
                    }
                    return;
                }
            }
            dataOutputStream.writeUTF("Login:wrong");
            checkPlayerWindUp();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    private void register(String name, String password) {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:FiveChessDB.db");
            c.setAutoCommit(false);
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM information;");
            while (rs.next()) {
                if (name.equals(rs.getString("name"))) {
                    dataOutputStream.writeUTF("Register:wrong");
                    rs.close();
                    checkPlayerWindUp();
                    return;
                }
            }
            dataOutputStream.writeUTF("Register:right");
            String sql = "INSERT INTO information (name,password) " + "VALUES ('" + name + "','" + password + "')";
            addPlayer(name);
            stmt.executeUpdate(sql);
            checkPlayerWindUp();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }


    private void processRead(String readline) throws IOException {
        System.out.println(readline);
        String[] separate = readline.split(":", 6);
        if (separate[0].equals("Login")) {
            signin(separate[1], separate[2]);
        }
        if (separate[0].equals("Register")) {
            register(separate[1], separate[2]);
        }
        if (separate[0].equals("operate")) {
            if (separate[1].equals("getUserList")) {
                for (User user : Main.usersList) {
                    new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("Test:");
                }
                dataOutputStream.writeUTF("operate:clear");
                for (User user : Main.usersList) {
                    dataOutputStream.writeUTF("information:" + user.getName() + ":" + user.getState() + ":" + user.socket.getRemoteSocketAddress().toString());
                }
            } else if (separate[1].equals("invite")) {
                String nameFrom = separate[2];
                String nameTo = separate[3];
                for (final User user : Main.usersList) {
                    if (user.getName().equals(nameTo)) {
                        new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:invitefrom:" + nameFrom);
                        user.setState("邀请中");
                        Main.sendUsersList();
                    }
                }
            } else if (separate[1].equals("agree")) {
                String chessFrom, chessTo;
                if (Math.random() > 0.5) {
                    chessFrom = "1";
                    chessTo = "2";
                } else {
                    chessFrom = "2";
                    chessTo = "1";
                }
                String nameFrom = separate[2];
                String nameTo = separate[3];
                for (final User user : Main.usersList) {
                    if (user.getName().equals(nameTo)) {
                        user.setState("游戏中");
                        if (chessTo.equals("1")) {
                            new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:agree:" + nameFrom + ":" + nameTo + ":" + chessTo + ":" + "true");
                        } else {
                            new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:agree:" + nameFrom + ":" + nameTo + ":" + chessTo + ":" + "false");

                        }
                    } else if (user.getName().equals(nameFrom)) {
                        user.setState("游戏中");
                        if (chessFrom.equals("1")) {
                            new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:agree:" + nameFrom + ":" + nameTo + ":" + chessFrom + ":" + "true");
                        } else {
                            new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:agree:" + nameFrom + ":" + nameTo + ":" + chessFrom + ":" + "false");
                        }
                    }
                }
                Main.sendUsersList();
            } else if (separate[1].equals("disagree")) {
                String nameFrom = separate[2];
                String nameTo = separate[3];
                for (final User user : Main.usersList) {
                    if (user.getName().equals(nameTo)) {
                        user.setState("在线");
                    }
                    if (user.getName().equals(nameFrom)) {
                        user.setState("在线");
                        new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:disagree:" + nameFrom + ":" + nameTo);
                    }
                }
                Main.sendUsersList();
            }
            if (separate[1].equals("state")) {
                for (final User user : Main.usersList) {
                    if (user.getName().equals(separate[2])) {
                        user.setState(separate[3]);
                    }
                }
                Main.sendUsersList();
            }
        }
        if (separate[0].equals("play")) {
            if (separate[1].equals("place")) {
                for (final User user : Main.usersList) {
                    if (user.getName().equals(separate[2])) {
                        new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("play:place:" + separate[3] + ":" + separate[4] + ":" + separate[5]);
                        new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("play:turn:true");
                    }
                }
            }
            if (separate[1].equals("game")) {
                if (separate[2].equals("leave")) {
                    String nameFrom = separate[3];
                    String nameTo = separate[4];
                    for (final User user : Main.usersList) {
                        if (user.getName().equals(separate[4])) {
                            new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("play:game:leave:" + nameFrom + ":" + nameTo);
                        }
                    }
                }
            }
        }
        if (separate[0].equals("Message")) {
            for (User user : Main.usersList) {
                if (user.getName().equals(separate[2])) {
                    new DataOutputStream(user.getSocket().getOutputStream()).writeUTF(readline);
                }
            }

        }
    }

}
