import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TCPServer {
    private static final String QUIT = "QUIT";
    private static final String KEYS = "KEYS";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";

    private static HashMap<String, String> keyValStore = new HashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();
            System.out.println("TCP Server started. Port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[" + LocalDateTime.now() + "] Client [" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Connection Successful!");

                Thread clientHandlerThread = new Thread(new ClientHandler(clientSocket));
                clientHandlerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (DataInputStream dataIn = new DataInputStream(clientSocket.getInputStream());
                 DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream())) {

                while (true) {
                    String command = dataIn.readUTF();
                    System.out.println("[" + LocalDateTime.now() + "] Client [" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "]");
                    System.out.println("Received command: " + command);

                    String[] parts = command.split("\\s+");

                    switch (parts[0]) {
                        case QUIT:
                            handleQuitRequest(parts, dataOut);
                            break;
                        case GET:
                            handleGetRequest(parts, dataOut);
                            break;
                        case PUT:
                            handlePutRequest(parts, dataOut);
                            break;
                        case DELETE:
                            handleDeleteRequest(parts, dataOut);
                            break;
                        case KEYS:
                            handleKeysRequest(dataOut);
                            break;
                        default:
                            dataOut.writeUTF("Invalid command");
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleGetRequest(String[] parts, DataOutputStream dataOut) throws IOException {
            if (parts.length < 2) {
                dataOut.writeUTF("Invalid GET command. Format: GET <key>");
                return;
            }
            String key = parts[1];
            if (key.length() > 10) {
                dataOut.writeUTF("Error: Key is too long. Maximum length is 10 characters");
                return;
            }
            String value = keyValStore.getOrDefault(key, "Error: The key " + key + " does not exist");
            dataOut.writeUTF(value);
        }

        private void handleDeleteRequest(String[] parts, DataOutputStream dataOut) throws IOException {
            String key = parts[1];
            if (key.equalsIgnoreCase("*")) {
                keyValStore.clear();
                dataOut.writeUTF("All values were deleted");
            } else {
                if (keyValStore.containsKey(key)) {
                    keyValStore.remove(key);
                    dataOut.writeUTF("Key deleted successfully");
                } else {
                    dataOut.writeUTF("Error: The key " + key + " does not exist");
                }
            }
        }



        private void handlePutRequest(String[] parts, DataOutputStream dataOut) throws IOException {
            if (parts.length < 3) {
                dataOut.writeUTF("Invalid PUT command. Format: PUT <key> <value>");
                return;
            }
            String key = parts[1];
            if (key.length() > 10) {
                dataOut.writeUTF("Error: Key is too long. Maximum length is 10 characters");
                System.out.println(getFormattedDateTime()+"Error: Key is too long. Maximum length is 10 characters");
                return;
            }
            String value = parts[2];
            keyValStore.put(key, value);
            dataOut.writeUTF("Value stored successfully");
        }

        private void handleKeysRequest(DataOutputStream dataOut) throws IOException {
            Set<String> keys = keyValStore.keySet();
            String keysString = String.join(", ", keys);
            dataOut.writeUTF(keysString);
        }
        private void handleQuitRequest(String[] parts, DataOutputStream dataOut) throws IOException {
            if (parts[0].equals(QUIT)) {
                dataOut.writeUTF("Connection closed");
                clientSocket.close();
                System.out.println("[" + LocalDateTime.now() + "] Client [" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] Connection Closed");
            }
        }


        private static String getFormattedDateTime() {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            return "[" + now.format(formatter) + "]";
        }
    }
}
