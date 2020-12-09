import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;

public class ServerWorker extends Thread{

    private final Socket clientSocket;

    public ServerWorker(Socket clientSocket) {
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

    // TODO handle backspaces
    private void handleClientSocket() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if (line.equalsIgnoreCase("quit")) {
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

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if (login.equals("guest") && password.equals("password")) {
                String msg = "Welcome, guest!\r\n";
                outputStream.write(msg.getBytes());
            } else {
                String msg = "Incorrect username/password";
                outputStream.write(msg.getBytes());
            }
        }
    }
}
