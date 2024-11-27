package it.unimib.sd2024;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.StringReader;

/**
 * Resource class for handling requests related to user information.
 */
@Path("registrations")
public class UserInfoRequestResource {
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
      return "Error connecting to the database";
    }
  }

  /**
   * Retrieves user information for a given user ID.
   *
   * @param userId The ID of the user whose information is being requested.
   * @return A Response object containing the user's information or an error
   *         message.
   */
  @GET
  @Path("{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserInfo(@PathParam("userId") String userId) {
    try {
      String command = "GET registrations " + userId;
      String response = connectToDatabase(command);

      if ("Error connecting to the database".equals(response)) {
        return createErrorResponse("Database connection error", Status.INTERNAL_SERVER_ERROR);
      } else if ("Document not found".equals(response) || response.isEmpty()) {
        return createErrorResponse("User not found", Status.NOT_FOUND);
      }

      JsonObject jsonResponse = Json.createReader(new StringReader(response)).readObject();
      JsonObject dataObject = jsonResponse.getJsonObject("data");

      if (dataObject == null) {
        return createErrorResponse("User data not found", Status.NOT_FOUND);
      }

      JsonObjectBuilder userInfo = Json.createObjectBuilder()
          .add("email", dataObject.getString("email"))
          .add("name", dataObject.getString("name"))
          .add("surname", dataObject.getString("surname"));

      return Response.status(Status.OK).entity(userInfo.build()).build();
    } catch (Exception e) {
      return createErrorResponse("Unexpected error processing request", Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Creates an error response with a specified message and status.
   *
   * @param message The error message.
   * @param status  The HTTP status code.
   * @return A Response object containing the error message and status.
   */
  private Response createErrorResponse(String message, Status status) {
    JsonObject errorResponse = Json.createObjectBuilder()
        .add("error", message)
        .build();
    return Response.status(status).entity(errorResponse).build();
  }
}