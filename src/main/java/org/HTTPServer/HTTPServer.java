package org.HTTPServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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

                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                String formattedDate = dateFormat.format(currentDate);
                formattedDate = formattedDate.replace(".", "");

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
                        responseBody=processGETRequest(queryParameters, reader);
                        printWriter.println("HTTP/1.1 200 OK");
                        printWriter.println("Date: "+formattedDate);
                        printWriter.println("Content-Type: application/json");
                        printWriter.println("Content-Length: " + responseBody.length());
                        printWriter.println("Connection: close");
                        printWriter.println("Server: gunicorn/19.9.0");
                        printWriter.println("Access-Control-Allow-Origin: *");
                        printWriter.println("Access-Control-Allow-Credentials: true");
                        printWriter.println("");
                        printWriter.println("");


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

    static String processGETRequest(String[] queryParameters, BufferedReader reader) throws IOException {
        String line;
        StringBuilder args = new StringBuilder("\n\t\"args\": {\n");
        for (String queryParameter : queryParameters) {
            String[] temp = queryParameter.split("=");
            args.append("\t\t\"").append(temp[0]).append("\": \"").append(temp[1]).append("\",\n");
        }
       args.append("\t},");

       StringBuilder headers = new StringBuilder("\t\"headers\": {\n");
       System.out.println(args);
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }
            String[] temp = line.split(":");
            headers.append("\t\t\"").append(temp[0]).append("\": \"").append(temp[1]).append("\",\n");
        }
        headers.append("\t},");
        System.out.println(headers);
        return "";
    }
}
