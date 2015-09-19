package com.danilov.aircontrol;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {


    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(22000);
            Socket clientSocket = null;
            while ((clientSocket = serverSocket.accept()) != null) {
                final Socket s = clientSocket;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            InputStream inputStream = s.getInputStream();
                            byte[] bytes = new byte[1024];
                            int read = -1;
                            while ((read = inputStream.read(bytes)) != -1) {
                                //doin nothin
                                System.out.println(new String(bytes, 0, read));
                            }
                            s.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                };
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
