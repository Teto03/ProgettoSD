/**
 * Represents the main entry point for the database server application.
 * This class initializes the server, listens for incoming connections,
 * and handles client requests through a dedicated handler.
 */
package it.unimib.sd2024;

import java.net.*;
import java.io.*;

/**
 * Main class where the database starts.
 */
public class Main {
  public static final int PORT = 3030;
  private static Database database;

  /**
   * Starts the server, initializes the database, and listens for incoming
   * connections.
   * For each connection, a new Handler thread is started to process client
   * requests.
   *
   * @throws IOException If an I/O error occurs when opening the socket.
   */
  public static void startServer() throws IOException {
    var server = new ServerSocket(PORT);

    System.out.println("Database listening at localhost:" + PORT);
    database = new Database("Database1");

    try {
      while (true)
        new Handler(server.accept()).start();
    } catch (IOException e) {
      System.err.println(e);
    } finally {
      server.close();
    }
  }

  /**
   * A private static inner class that handles client connections.
   * Each instance of Handler is associated with a single client socket.
   * The run method processes client requests until the connection is closed.
   */
  private static class Handler extends Thread {
    private Socket client;

    /**
     * Constructs a new Handler instance for the specified client socket.
     *
     * @param client The client socket to be handled.
     */
    public Handler(Socket client) {
      this.client = client;
    }

    /**
     * The main execution method for the handler.
     * Reads requests from the client, processes them using a ProtocolHandler,
     * and sends back responses until the client closes the connection.
     */
    public void run() {
      try (var out = new PrintWriter(client.getOutputStream(), true);
          var in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

        String inputLine;

        while ((inputLine = in.readLine()) != null) {
          // Handle the input with a local ProtocolHandler object
          ProtocolHandler protocolHandler = new ProtocolHandler(inputLine, database);
          String response = protocolHandler.handleRequest();
          // System.out.println(response);
          out.println(response); // Send response back to client
        }
      } catch (IOException e) {
        System.err.println(e);
      } finally {
        try {
          client.close();
        } catch (IOException e) {
          System.err.println("Error closing client socket: " + e.getMessage());
        }
      }
    }
  }

  /**
   * The main method to start the server.
   * Calls startServer to initialize the server and begin listening for
   * connections.
   *
   * @param args Command line arguments (not used).
   * @throws IOException If an I/O error occurs when starting the server.
   */
  public static void main(String[] args) throws IOException {
    startServer();
  }
}