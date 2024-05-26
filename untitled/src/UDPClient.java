import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class UDPClient {
    private static final String QUIT = "QUIT";
    private static final String KEYS = "KEYS";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private static final String SERVER_IP = "localhost";
    private static final int PORT = 12345;

    private static DatagramSocket clientSocket;
    private static DatagramPacket sendPacket;
    private static DatagramPacket receivePacket;
    private static InetAddress serverAddress;
    private static byte[] sendData = new byte[1024];
    private static byte[] receiveData = new byte[1024];

    public static void main(String[] args) {
        try {
            clientSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName(SERVER_IP);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("UDP Client started.");
            System.out.println("Input command must be in this format: ");
            System.out.println("      GET <key>");
            System.out.println("      PUT <key> <value>");
            System.out.println("      DELETE <key>");
            System.out.println("      KEYS");
            System.out.println("      QUIT");

            while (true) {
                System.out.print("Enter command: ");
                String command = reader.readLine().toUpperCase();

                if (command.equals(QUIT)) {
                    sendDataPacket(QUIT);
                    break;
                }

                String[] parts = command.split("\\s+");
                switch (parts[0]) {
                    case QUIT:
                        handleQuitRequest();
                        break;
                    case GET:
                    case DELETE:
                        if (parts.length == 2) {
                            handleGetOrDeleteRequest(parts[0], parts[1]);
                        } else {
                            System.out.println("\u001B[31m[" + getCurrentTimeStamp() + "] Invalid " + parts[0] + " command. Format: " + parts[0] + " <key>\u001B[0m");
                        }
                        break;
                    case PUT:
                        if (parts.length != 3) {
                            System.out.println("\u001B[31m" + "["+getCurrentTimeStamp()+ "]" + " Invalid PUT command. Format: PUT <key> <value>" + "\u001B[0m");
                            continue;
                        }
                        else if(parts[1].length() > 10) {
                            System.out.println("\u001B[31m" + getCurrentTimeStamp() + " Error: Key is too long. Maximum length is 10 characters" + "\u001B[0m");

                        }
                        handlePutRequest(parts[1], parts[2]);
                        break;
                    case KEYS:
                        if (parts.length == 1) {
                            handleKeysRequest();
                        } else {
                            System.out.println("\u001B[31m[" + getCurrentTimeStamp() + "] Invalid KEYS command. Format: KEYS\u001B[0m");
                        }
                        break;
                    default:
                        System.out.println("\u001B[31m[" + getCurrentTimeStamp() + "] Command not recognized.\u001B[0m");
                        break;
                }
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendDataPacket(String data) throws IOException {
        sendData = data.getBytes();
        sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, PORT);
        clientSocket.send(sendPacket);
    }

    private static void receiveDataPacket() throws IOException {
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
    }

    private static void handleQuitRequest() throws IOException {
        sendDataPacket(QUIT);
        receiveDataPacket();
        String response = new String(receiveData, 0, receiveData.length);
        sendDataPacket("Response: "+response);
    }

    private static void handlePutRequest(String key, String value) throws IOException {
        String data = PUT + " " + key + " " + value;
        sendDataPacket(data);
        receiveDataPacket();
        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
        if (response.startsWith("Error")) {
            System.out.println("\u001B[31m" + "[" + getCurrentTimeStamp() + "]" + " " + response + "\u001B[0m");
        } else {
            System.out.println("Response: " + response);
            System.out.println("\u001B[32m" + "[" + getCurrentTimeStamp() + "]" + " Command executed successfully" + "\u001B[0m");
        }
    }


    private static void handleGetOrDeleteRequest(String cmd, String key) throws IOException {
        String data = cmd + " " + key;
        sendDataPacket(data);
        receiveDataPacket();
        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
        if (response.startsWith("Error")) {
            System.out.println("\u001B[31m[" + getCurrentTimeStamp() + "] " + response + "\u001B[0m");
        } else {
            System.out.println("Response: " + response);
            System.out.println("\u001B[32m[" + getCurrentTimeStamp() + "] Command executed successfully\u001B[0m");
        }
    }

    private static void handleKeysRequest() throws IOException {
        sendDataPacket(KEYS);
        receiveDataPacket();
        System.out.println("\u001B[32m[" + getCurrentTimeStamp() + "] Keys: " + new String(receivePacket.getData(), 0, receivePacket.getLength()) + "\u001B[0m");
        System.out.println("\u001B[32m[" + getCurrentTimeStamp() + "] Command executed successfully\u001B[0m");
    }

    private static String getCurrentTimeStamp() {
        return java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }
}
