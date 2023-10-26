package org.HTTPServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
                    System.out.println(line);
                    String[] inputHeaderLine = line.split(" ");
                    if (inputHeaderLine[0].equals("GET")) {
                        String url=inputHeaderLine[1];
                        String[] temp = inputHeaderLine[1].split("\\?");
                        String[] queryParameters=temp[1].split("&") ;
                        responseBody=processGETRequest(queryParameters, reader,url);
                        printWriter.println("HTTP/1.1 200 OK");
                        printWriter.println("Date: "+formattedDate);
                        printWriter.println("Content-Type: application/json");
                        printWriter.println("Content-Length: " + responseBody.length());
                        printWriter.println("Connection: close");
                        printWriter.println("Server: gunicorn/19.9.0");
                        printWriter.println("Access-Control-Allow-Origin: *");
                        printWriter.println("Access-Control-Allow-Credentials: true");
                        printWriter.println("");
                        printWriter.println(responseBody);
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

    static String processGETRequest(String[] queryParameters, BufferedReader reader, String url) throws IOException {
        String localIP="";
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            localIP = localhost.getHostAddress();
            System.out.println("Local IP Address: " + localIP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        StringBuilder responseBody = new StringBuilder("{");
        StringBuilder args = new StringBuilder("\n  \"args\": {\n");
        for (String queryParameter : queryParameters) {
            String[] temp = queryParameter.split("=");
            args.append("    \"").append(temp[0]).append("\": \"").append(temp[1]).append("\",\n");
        }
       args.append("  },\n");

       StringBuilder headers = new StringBuilder("  \"headers\": {\n");
       String host = "";
       String line;
       while ((line = reader.readLine()) != null) {
           if (line.isEmpty()) {
                break;
           }
           String[] temp = line.split(":");
           if(line.contains("Host"))
               host = temp[1];
           headers.append("    \"").append(temp[0]).append("\": \"").append(temp[1]).append("\",\n");
       }
       headers.append("  },");

       responseBody.append(args).append(headers);
       System.out.println(responseBody);
       responseBody.append("\n  \"origin\": \"").append(localIP).append("\",\n");
       responseBody.append("  \"url\": \"http://").append(host).append(url).append("\"");
       responseBody.append("\n}");
       return responseBody.toString();
    }
}
