import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final String name;

    public Client(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }

    public void start() throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

        // Reader thread: receives from server and prints
        Thread receiver = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    // server command to prompt for name
                    if (line.startsWith("ENTER_NAME")) {
                        out.println(name);
                    } else {
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection closed by server.");
            } finally {
                close();
            }
        });
        receiver.start();

        // Sender thread: read console and send to server
        Thread sender = new Thread(() -> {
            Scanner scanner = new Scanner(System.in, "UTF-8");
            try {
                while (true) {
                    if (!scanner.hasNextLine()) break;
                    String msg = scanner.nextLine();
                    if (msg.equalsIgnoreCase("/quit") || msg.equalsIgnoreCase("/exit")) {
                        out.println("/quit");
                        break;
                    }
                    out.println(msg);
                }
            } finally {
                scanner.close();
                close();
            }
        });
        sender.start();

        // join threads
        try {
            sender.join();
            // once sender finishes, receiver should exit soon
            receiver.join();
        } catch (InterruptedException ignored) {}
    }

    private void close() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 9000;
        String name = "Anonymous";

        if (args.length >= 1) name = args[0];
        if (args.length >= 2) host = args[1];
        if (args.length >= 3) port = Integer.parseInt(args[2]);

        new Client(host, port, name).start();
    }
}
