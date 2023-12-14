import java.io.*;
import java.net.Socket;

public class Client {

  private Socket socket = null;
  private BufferedReader in = null;
  private BufferedWriter out = null;

  public Client(String address, int port) {
    try {
      socket = new Socket(address, port);
      System.out.println("Connected");

      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      out =
        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      // Get client message from the console
      System.out.print("Enter message to send to the server: ");
      BufferedReader consoleReader = new BufferedReader(
        new InputStreamReader(System.in)
      );
      String clientMessage = consoleReader.readLine();
      sendMessage(clientMessage);

      // Get and print the server response
      String serverResponseAfterMessage = receiveMessage();
      System.out.println("Server: " + serverResponseAfterMessage);

      // Get URL from the console
      System.out.print("Enter URL: ");
      String url = consoleReader.readLine();
      sendMessage(url);

      // Get and print the webpage content
      String webpageContent = receiveMultiLineMessage();
      System.out.println("Received webpage content:\n" + webpageContent);

      // Prompt to save to a local file
      saveWebpageToFile(webpageContent);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        in.close();
        out.close();
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void sendMessage(String message) throws IOException {
    out.write(message + "\n");
    out.flush();
  }

  private String receiveMessage() throws IOException {
    return in.readLine();
  }

  private String receiveMultiLineMessage() throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    String line;
    while ((line = in.readLine()) != null) {
      stringBuilder.append(line).append("\n");
    }
    return stringBuilder.toString();
  }

  private void saveWebpageToFile(String content) {
    try {
      // Prompt the user for the file name to save the webpage content
      System.out.print("Enter the file name to save the webpage content: ");
      String fileName = new BufferedReader(new InputStreamReader(System.in))
        .readLine();

      // Create a FileWriter to write the content to the specified file
      FileWriter fileWriter = new FileWriter(fileName);

      // Write the content to the file
      fileWriter.write(content);
      fileWriter.close();

      System.out.println("Webpage content saved to file: " + fileName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    // local test
    // Client client = new Client("localhost", 60000);
    
    // lan test
    Client client = new Client("192.168.50.255", 60000);
  }
}
