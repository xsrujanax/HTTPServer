package org.HTTPServer;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ConcurrentModificationException;
import java.util.Scanner;

public class HTTPClient {
    public static void main(String[] args){
        try{
            Socket socket = new Socket("localhost", 80);

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream,true);

            Scanner scanner = new Scanner(System.in);
            String text;
            do{
                System.out.println("Enter text: ");
                text = scanner.nextLine();
                printWriter.println(text);

                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String time = reader.readLine();
                System.out.println(time);
            } while(!text.equals("bye"));

            socket.close();
        }
        catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        }
        catch (IOException e){
            System.err.println(e.getMessage());
        }
    }
}
