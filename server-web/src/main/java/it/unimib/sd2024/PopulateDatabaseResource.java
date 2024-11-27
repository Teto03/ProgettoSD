package it.unimib.sd2024;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

/**
 * Resource class for handling requests to create new collections in the
 * database.
 */
@Path("collections")
public class PopulateDatabaseResource {
  private static final String DATABASE_HOST = "localhost";
  private static final int DATABASE_PORT = 3030;

  /**
   * Connects to the database and executes a command.
   *
   * @param command The command to be executed.
   * @return The response from the database as a String.
   */
  private String connectToDatabase(String command) {
    try (Socket socket = new Socket(DATABASE_HOST, DATABASE_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      out.println(command);
      return in.readLine();
    } catch (IOException e) {
      return "Error connecting to the database: " + e.getMessage();
    }
  }

  /**
   * Processes a request to create a new collection in the database.
   *
   * @param jsonString The request containing the collection creation
   *                   information.
   * @return A Response object indicating the outcome of the collection creation
   *         attempt.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createCollection(String jsonString) {
    // Parse the JSON string
    JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
    JsonObject jsonObject = jsonReader.readObject();
    jsonReader.close();

    String collectionName = jsonObject.getString("collectionName");

    String command = "CREATE " + collectionName;
    String response = connectToDatabase(command);

    if ("Collection created".equals(response)) {
      return Response.ok(response).build();
    } else {
      // Assuming the createResponse contains the error message
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
    }
  }
}
