import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private final int port;
    private final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Chat server starting on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSock = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSock, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // broadcast to all except sender (if needed)
    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler ch : clients) {
            if (ch != sender) {
                ch.send(message);
            }
        }
    }

    public void removeClient(ClientHandler ch) {
        clients.remove(ch);
        System.out.println("Removed client: " + ch.getName());
    }

    public static void main(String[] args) {
        int port = 9000;
        if (args.length > 0) port = Integer.parseInt(args[0]);
        new Server(port).start();
    }

    // Inner class for handling clients
    static class ClientHandler implements Runnable {
        private final Socket sock;
        private final Server server;
        private BufferedReader in;
        private PrintWriter out;
        private String name = "Unknown";

        ClientHandler(Socket sock, Server server) {
            this.sock = sock;
            this.server = server;
        }

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"), true);

                // Step 1: ask for username
                out.println("ENTER_NAME");
                String requestedName = in.readLine();
                if (requestedName == null) {
                    close();
                    return;
                }
                name = sanitize(requestedName);
                out.println("WELCOME " + name);
                server.broadcast(name + " has joined the chat.", this);
                System.out.println(name + " connected from " + sock.getRemoteSocketAddress());

                // Read loop
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.equalsIgnoreCase("/quit") || line.equalsIgnoreCase("/exit")) {
                        break;
                    }
                    if (!line.isEmpty()) {
                        String msg = String.format("%s: %s", name, sanitize(line));
                        server.broadcast(msg, this);
                    }
                }
            } catch (IOException e) {
                // client disconnected unexpectedly
            } finally {
                close();
            }
        }

        // send a line to this client
        public void send(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        private void close() {
            try {
                server.removeClient(this);
                server.broadcast(name + " has left the chat.", this);
                if (sock != null && !sock.isClosed()) sock.close();
            } catch (IOException ignored) {}
            System.out.println("Connection closed for " + name);
        }

        // minimal sanitization to avoid control characters
        private String sanitize(String s) {
            return s.replaceAll("[\\p{Cntrl}&&[^\n\r\t]]", "");
        }
    }
}