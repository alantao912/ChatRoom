import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread{

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if (cmd.equalsIgnoreCase("quit") || cmd.equals("logoff")) {
                    handleLogoff();
                    break;
                } else if (cmd.equalsIgnoreCase("login")) {
                    handleLogin(outputStream, tokens);
                } else {
                    String msg = "Unknown command: " + cmd + "\r\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }

        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if ((login.equals("Devrim") && password.equals("password")) || (login.equals("Alan") && password.equals("password"))) {
                this.login = login;
                String msg = "Welcome, " + getLogin() + "!\r\n";
                outputStream.write(msg.getBytes());
                System.out.println("User '" + getLogin() + "' logged in successfully.");

                String onlineMsg = "User '" + getLogin() + "' is now online.\r\n";

                List<ServerWorker> workerList = server.getWorkerList();

                // Notify all other users of new login
                for (ServerWorker serverWorker : workerList) {
                    if (!this.equals(serverWorker)) {
                        serverWorker.send(onlineMsg);
                    }
                }

                // Sends current user all other users currently online
                for (ServerWorker serverWorker : workerList) {
                    String msg2 = "User '" + serverWorker.getLogin() + "' is online.\r\n";
                    if (!getLogin().equals(serverWorker.getLogin()) && serverWorker.getLogin() != null) {
                        send(msg2);
                    }
                }
            } else {
                String msg = "Incorrect username/password";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();

        // Notify all other users of logoff
        String offlineMsg = "User '" + getLogin() + "' has logged off.\r\n";
        for (ServerWorker serverWorker : workerList) {
            if (!this.equals(serverWorker)) {
                serverWorker.send(offlineMsg);
            }
        }
        clientSocket.close();
    }

    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
