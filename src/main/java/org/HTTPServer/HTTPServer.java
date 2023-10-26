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

                OutputStream outputStream = socket.getOutputStream();
                PrintWriter printWriter = new PrintWriter(outputStream, true);

                String responseBody = null;

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] inputHeaderLine = line.split(" ");
                    if (inputHeaderLine[0].equals("GET")) {
                        String[] temp = inputHeaderLine[1].split("\\?");
                        String[] queryParameters=temp[1].split("&") ;
                        processGETRequest(queryParameters, reader);
                        printWriter.println("HTTP/1.1 200 OK");
                        printWriter.println("Content-Type: application/json");
                        printWriter.println("");
                        printWriter.println("");
                        //printWriter.println("Content-Length: " + responseBody.length());

                    }
                    if (line.contains("post")) {

                    }
                    if (line.isEmpty()) {
                        break;
                    }
                }
                if (responseBody != null) {
                    // Send the response
                    printWriter.println("HTTP/1.1 200 OK");
                    printWriter.println("Content-Type: text/plain");
                    printWriter.println("Content-Length: " + responseBody.length());
                    printWriter.println("");
                    //printWriter.println(responseBody);

                    // Close the socket and the connection
                    socket.close();
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    static void processGETRequest(String[] queryParameters, BufferedReader reader) throws IOException {
        String line;
        for (String queryParameter : queryParameters) System.out.println(queryParameter);
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }
            System.out.println(line);
        }
    }
}
