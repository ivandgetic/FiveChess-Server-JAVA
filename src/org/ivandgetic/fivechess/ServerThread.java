package org.ivandgetic.fivechess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by ivandgetic on 14/7/10.
 */
public class ServerThread implements Runnable {
    private Socket socket = null;
    DataOutputStream out;
    DataInputStream in;
    User user;

    public ServerThread(final Socket socket) throws IOException {
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        String information = in.readUTF();
        String[] separate = information.split(":", 4);
        user = new User(separate[1], "online", socket);
        Main.usersList.add(user);//把socket添加到list
    }

    @Override
    public void run() {
        while (true) try {
            String readline = null;
            readline = in.readUTF();
            String[] separate = readline.split(":", 6);
            if (separate[0].equals("operate")) {
                if (separate[1].equals("getUserList")) {
                    for (User user : Main.usersList) {
                        new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("Message:");
                    }
                    out.writeUTF("operate:clear");
                    for (User user : Main.usersList) {
                        out.writeUTF("information:" + user.getName() + ":" + user.getState() + ":" + user.socket.getRemoteSocketAddress().toString());
                    }
                }
                if (separate[1].equals("invite")) {
                    String nameFrom = separate[2];
                    String nameTo = separate[3];
                    for (final User user : Main.usersList) {
                        if (user.getName().equals(nameTo)) {
                            new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:invitefrom:" + nameFrom);
                            user.setState("invite");
                            Main.sendUsersList();
                        }
                    }
                }
                if (separate[1].equals("agree")) {
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
                            user.setState("gameing");
                            if (chessTo.equals("1")) {
                                new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:agree:" + nameFrom + ":" + nameTo + ":" + chessTo + ":" + "true");
                            } else {
                                new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:agree:" + nameFrom + ":" + nameTo + ":" + chessTo + ":" + "false");

                            }
                        }
                        if (user.getName().equals(nameFrom)) {
                            user.setState("gameing");
                            if (chessFrom.equals("1")) {
                                new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:agree:" + nameFrom + ":" + nameTo + ":" + chessFrom + ":" + "true");
                            } else {
                                new DataOutputStream(user.getSocket().getOutputStream()).writeUTF("operate:agree:" + nameFrom + ":" + nameTo + ":" + chessFrom + ":" + "false");
                            }
                        }
                    }
                    Main.sendUsersList();
                }
                if (separate[1].equals("disagree")) {
                    String nameFrom = separate[2];
                    String nameTo = separate[3];
                    for (final User user : Main.usersList) {
                        if (user.getName().equals(nameTo)) {
                            user.setState("online");
                        }
                        if (user.getName().equals(nameFrom)) {
                            user.setState("online");
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
//            for (Socket socket : Main.socketList) {
//                out = new DataOutputStream(socket.getOutputStream());
//                out.writeUTF("Message:" + separate[1] + ":" + separate[2]);
//            }
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

}
