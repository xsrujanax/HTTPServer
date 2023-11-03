package org.HTTPServer;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HTTPServer {
    private static String host;
    private static final String BASE_DIRECTORY = "C:\\Users\\Sruja\\GIT\\HTTPServer\\SimpleStorage";
    public static void main(String[] args){
        try{
            ServerSocket serverSocket = new ServerSocket(80);
            System.out.println("Server is listening to port" + 80);

            while(true){
                try{
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected");

                    InputStream inputStream = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    String response = processRequest(reader);

                    OutputStream outputStream = socket.getOutputStream();
                    PrintWriter printWriter = new PrintWriter(outputStream, true);

                    printWriter.println(response);
                    System.out.println(response);
                    printWriter.println("");

                    socket.close();

                } catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String processRequest(BufferedReader reader) throws IOException {
        // return headers + body
        String response ="";
        String responseHeader="";
        String responseBody = "";
        String line=null;
        while((line = reader.readLine()) != null){
            System.out.println("line"+line);
            if(line.contains("GET") || line.contains("POST")){
                String[] extractData = line.split(" ");
                String requestMethod= extractData[0];
                String url = extractData[1];
                responseBody = generateResponseBody(requestMethod,url,reader);
                responseHeader = getResponseHeaders(responseBody);
                break;
            }
            if(line.isEmpty()) {
                break;
            }
        }
        response = responseHeader +responseBody;
        return response;
    }

    public static String generateResponseBody(String requestMethod,String url, BufferedReader reader) throws IOException {
        StringBuilder body = new StringBuilder();
        if(url.startsWith("/get") || url.startsWith("/post")) {
            body.append("{\n");
            String data = "", json = "";
            String args = generateArgs(url);
            String headers = generateHeaders(reader);
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                } else if (line.startsWith("{")) {
                    data = generateData(line);
                    json = generateJSON(line);
                    break;
                }
            }

            body.append(args);
            body.append(data);
            body.append(headers);
            body.append(json);

            String localIP = "";
            String host = getHost();
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                localIP = localhost.getHostAddress();
                System.out.println("Local IP Address: " + localIP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            body.append("\n  \"origin\": \"").append(localIP).append("\",\n");
            body.append("  \"url\": \"http://").append(host).append(url).append("\"");
            body.append("\n}");
            return body.toString();
        }
        else{
            if(requestMethod.equals("GET")){
                body = processGETRequest_FileStorage(url);
            }
            else if(requestMethod.equals("POST")){
                body = processPOSTRequest_FileStorage(url,reader);
            }

            return body.toString();
        }
    }

    private static StringBuilder processPOSTRequest_FileStorage(String url,BufferedReader reader) throws IOException {
        StringBuilder responseBody = new StringBuilder();
        responseBody.append("\n{\n");
        String[] path = url.split("\\?");
        boolean overwrite = true;
        if("/bar".equals(path[0])){
            String content = "";
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println("post"+line);
                if(line.contains("overwrite")){
                    String[] ow= line.split("=");
                    overwrite = Boolean.parseBoolean(ow[1]);
                }
                if (line.startsWith("{")) {
                    content = line;
                    break;
                }

            }

            File file =new File("bar.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\Sruja\\GIT\\HTTPServer\\SimpleStorage\\" + file,overwrite));
            bw.flush();
            bw.write(content);
            bw.close();
            responseBody.append("Content has been saved to a file");
        }
        responseBody.append("\n}");
        return responseBody;
    }



    private static StringBuilder processGETRequest_FileStorage(String url) {
        StringBuilder responseBody = new StringBuilder();
        responseBody.append("{\n");


        String[] path = url.split("\\?");
        String requestedPath = BASE_DIRECTORY + path[0].replace("/","\\");

        if(requestedPath.startsWith(BASE_DIRECTORY)) {
            if ("/".equals(path[0])) {
                //display all files in the directory
                File fileDirectory = new File(requestedPath);
                System.out.println(fileDirectory);
                File[] files = fileDirectory.listFiles();

                if (files != null && files.length > 0) {
                    responseBody.append("   \"files\": [");
                    for (File file : files) {
                        responseBody.append("\"").append(file.getName()).append("\",");
                    }
                    responseBody.setLength(responseBody.length() - 1);
                    responseBody.append("]");
                }
            } else if ("/foo".equals(path[0])) {
                //retrieve the content of the file
                File file = new File(requestedPath + ".txt");
                if (file.exists() && file.isFile()) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            responseBody.append(line).append("\n");
                        }
                        reader.close();
                    } catch (IOException e) {
                        responseBody.append("  Internal error");
                    }
                } else
                    responseBody.append("  HTTP/1.1 404 Not Found\\n\\nFile not found");
            } else
                responseBody.append("  HTTP/1.1 404 Not Found\n\nFile does not exist in the directory");
        }
        else
            responseBody.append("HTTP/1.1 403 Forbidden\n\nAccess to this directory is not allowed");

        responseBody.append("\n}\n");
        return responseBody;
    }

    private static String getResponseHeaders(String responseBody) {
        return "HTTP/1.1 200 OK" + "\n" +
                "Date: " + getDate() + "\n" +
                "Content-Type: application/json" + "\n" +
                "Content-Length: " + responseBody.length() + "\n" +
                "Connection: close" + "\n" +
                "Server: gunicorn/19.9.0" + "\n" +
                "Access-Control-Allow-Origin: *" + "\n" +
                "Access-Control-Allow-Credentials: true" + "\n";
    }

    public static String generateHeaders(BufferedReader reader) throws IOException {
        StringBuilder headers = new StringBuilder("  \"headers\": {\n");

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }
            String[] temp = line.split(":");
            if(line.contains("Host"))
                setHost(temp[1]);
            headers.append("    \"").append(temp[0]).append("\": \"").append(temp[1]).append("\",\n");
        }
        headers.setLength(headers.length()-1);
        headers.append("\n  },");

        return headers.toString();
    }

    public static String generateJSON(String data){

        StringBuilder json = new StringBuilder();
        json.append("\n  \"json\":{");

        data = data.replace("{","");
        data = data.replace("}","");
        String[] jsonData = data.split(",");

        for(String attribute : jsonData )
            json.append("\n    ").append(attribute).append(",");

        json.setLength(json.length()-1);
        json.append("\n  },");

        return json.toString();
    }

    public static String generateData(String dataAttributes){
        StringBuilder data = new StringBuilder();
        data.append("  \"data\":{");

        dataAttributes = dataAttributes.replace("{","");
        dataAttributes = dataAttributes.replace("}","");
        String[] jsonData = dataAttributes.split(",");

        for(String attribute : jsonData )
            data.append(attribute.replace("\"","\\\"")).append(",");

        data.setLength(data.length()-1);
        data.append("},\n");

        return data.toString();
    }

    public static String generateArgs(String url){

        StringBuilder args = new StringBuilder();
        args.append("\n  \"args\":{");

        String[] extractQueryParameters = url.split("\\?");

        if(extractQueryParameters.length>1) {
            String[] queryParameters = extractQueryParameters[1].split("&");

            for (String queryParameter : queryParameters) {
                String[] key_values = queryParameter.split("=");
                args.append("\n    \"").append(key_values[0]).append("\": \"").append(key_values[1]).append("\",");
            }
            args.append("\n  ");
        }
        args.append("},\n");

        return args.toString();

    }

    public static String getDate(){
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formattedDate = dateFormat.format(currentDate);
        formattedDate = formattedDate.replace(".", "");
        return formattedDate;
    }

    static void setHost(String host){
        HTTPServer.host = host;
    }

    static String getHost(){
        return host;
    }
}
