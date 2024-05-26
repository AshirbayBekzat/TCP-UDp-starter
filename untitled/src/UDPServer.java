import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UDPServer {
    private static DatagramSocket serverSocket;
    private static DatagramPacket receivePacket;
    private static List<String> keysList = new ArrayList<>();
    private static List<String> valuesList = new ArrayList<>();

    public static void main(String[] args) {
        try {
            serverSocket = new DatagramSocket();
            int port = serverSocket.getLocalPort();
            System.out.println("UDP Server started. Listening on port " + port);

            while (true) {
                byte[] receiveData = new byte[1024];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String request = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();

                if (request.startsWith("PUT")) {
                    handlePutRequest(request);
                } else if (request.startsWith("GET")) {
                    handleGetRequest(request);
                } else if (request.startsWith("DELETE")) {
                    handleDeleteRequest(request);
                } else if (request.equalsIgnoreCase("KEYS")) {
                    sendResponse(getKeys());
                } else if (request.equalsIgnoreCase("QUIT")) {
                    handleQuitRequest(request);
                    break;
                } else {
                    sendResponse("Invalid command");
                    logMessage("Invalid command");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    private static void handleQuitRequest(String request) throws IOException {
        if(request.equalsIgnoreCase("QUIT")) {
            sendResponse("Connection closed");
            serverSocket.close();
            System.out.println("[" + LocalDateTime.now() + "] Client [" + serverSocket.getInetAddress() + ":" + serverSocket.getPort() + "] Connection Closed");
        }
    }

    private static void handlePutRequest(String request) {
        String[] parts = request.split("\\s+", 3);
        if (parts.length < 3) {
            sendResponse("Invalid PUT command. Format: PUT <key> <value>");
            return;
        }
        String key = parts[1];
        if (key.length() > 10) {
            sendResponse("Error: Key is too long. Maximum length is 10 characters");
            logMessage("Key is too long. Maximum length is 10 characters");
            return;
        }
        String value = parts[2];
        keysList.add(key);
        valuesList.add(value);
        sendResponse("Value stored successfully");
    }


    private static void handleGetRequest(String request) {
        String[] parts = request.split("\\s+", 2);
        if (parts.length != 2) {
            sendResponse("Invalid GET command. Format: GET <key>");
            logMessage("Invalid GET command. Format: GET <key>");
            return;
        }
        String key = parts[1];
        int index = keysList.indexOf(key);
        if (index != -1) {
            String value = valuesList.get(index);
            sendResponse(value);
            logMessage("Command executed successfully");
        } else {
            sendResponse("Error:  does not exist");
            logMessage("Error: This key "+key+" does not exist");
        }
    }


    private static void handleDeleteRequest(String request) {
        String[] parts = request.split("\\s+", 2);
        if (parts.length != 2) {
            sendResponse("Invalid DELETE command. Format: DELETE <key>");
            logMessage("Invalid DELETE command. Format: DELETE <key>");
            return;
        }
        String key = parts[1];
        int index = keysList.indexOf(key);
        if (index != -1) {
            keysList.remove(index);
            valuesList.remove(index);
            sendResponse("Key deleted successfully");
            logMessage("Command executed successfully");
        } else {
            sendResponse("Key not found");
            logMessage("Key not found");
        }
    }

    private static String getKeys() {
        return String.join(", ", keysList);
    }

    private static void sendResponse(String response) {
        try {
            byte[] sendData = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, receivePacket.getAddress(), receivePacket.getPort());
            serverSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logMessage(String message) {
        String now = getCurrentTimeStamp();
        System.out.println("[" + now + "] " + message);
    }

    private static String getCurrentTimeStamp() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }
}
