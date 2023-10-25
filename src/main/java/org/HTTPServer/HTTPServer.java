package org.HTTPServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer{

    public static void main(String[] args) {

        BufferedReader reader;
        try {
            ServerSocket serverSocket = new ServerSocket(80);
            System.out.println("Server is listening to port" + 80);

            while (true) {
                //Listen to connection from the client and accept it.
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                InputStream inputStream = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                //Send data to the client via client socket- OutputStream.
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter printWriter = new PrintWriter(outputStream, true);

                String responseBody;

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] inputHeaderLine = line.split(" ");
                    if (inputHeaderLine[0].equals("GET")) {
                        String[] temp = inputHeaderLine[1].split("\\?");
                        String[] queryParameters=temp[1].split("&") ;
                        responseBody = processGETRequest(queryParameters, reader);
                    }
                    if (line.contains("post")) {

                    }
                    if (line.isEmpty()) {
                        break;
                    }
                }
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    static String processGETRequest(String[] queryParameters, BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        return line;
    }
}
