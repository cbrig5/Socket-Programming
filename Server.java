import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Server {

  private Socket socket = null;
  private ServerSocket server = null;
  private BufferedReader in = null;
  private BufferedWriter out = null;

  public Server(int port) {
    try {
      server = new ServerSocket(port);
      System.out.println("Server started");
      System.out.println("Waiting for a client ...");
      socket = server.accept();
      System.out.println("Client accepted");

      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out =
        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      // Start an HTTP server in a separate thread
      startHttpServer();

      // Perform a single exchange
      singleExchange();

      // Process URL request and send webpage to the client
      processURLRequest();

      System.out.println("Closing connection");
      socket.close();
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void startHttpServer() {
    // Start HTTP server in a separate thread
    new Thread(() -> {
      try {
        int httpPort = 3001;

        HttpServer httpServer = HttpServer.create(
          new InetSocketAddress(httpPort),
          0
        );
        httpServer.createContext("/", new MyHttpHandler());
        httpServer.setExecutor(null);
        httpServer.start();

        System.out.println(
          "HTTP Server is running on http://localhost:" + httpPort
        );
      } catch (IOException e) {
        e.printStackTrace();
      }
    })
      .start();
  }

  private void singleExchange() {
    try {
      // Server reads a message from the client
      String clientMessage = in.readLine();
      if (clientMessage != null) {
        System.out.println("Received from client: " + clientMessage);

        // Display a prompt for the server response
        System.out.print("respond to client: ");

        // Read the server response from the console
        String serverResponse = new BufferedReader(
          new InputStreamReader(System.in)
        )
          .readLine();

        // Send the response to the client
        out.write("Server says: " + serverResponse + "\n");
        out.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String readWebpageContent(String urlString) {
    StringBuilder content = new StringBuilder();

    try {
      // Disable SSL/TLS certificate validation (for testing only)
      TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs,
            String authType
          ) {}

          public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs,
            String authType
          ) {}
        },
      };

      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      URL url = new URL(urlString);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      try (
        BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream())
        )
      ) {
        String line;
        while ((line = reader.readLine()) != null) {
          content.append(line).append("\n");
        }
      }
      // connection.disconnect();
    } catch (
      IOException | NoSuchAlgorithmException | KeyManagementException e
    ) {
      e.printStackTrace();
    }

    // System.out.println(content.toString());
    return content.toString();
  }

  // handles url given by client
  private void processURLRequest() {
    try {
      String urlRequest = in.readLine();
      if (urlRequest != null) {
        System.out.println("Received URL request from client: " + urlRequest);

        // gets the webpage content
        String webpageContent = readWebpageContent(urlRequest);

        // Send webpage content to the client
        out.write(webpageContent);
        out.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Server server = new Server(60000); // Change port to 60000
  }

  static class MyHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
    }
  }
}
