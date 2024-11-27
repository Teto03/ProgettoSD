package it.unimib.sd2024;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Resource class for handling user registration requests.
 */
@Path("registrations")
public class UserRegistrationRequestResource {
  private static final String DATABASE_HOST = "localhost";
  private static final int DATABASE_PORT = 3030;
  private final Jsonb jsonb = JsonbBuilder.create();

  /**
   * Connects to the database and sends a command.
   * 
   * @param command The command to send to the database.
   * @return The response from the database.
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
   * Retrieves all user emails from the database.
   * 
   * @return A JSON array of all user emails in the database.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllUserEmails() {
    try {
      String command = "GET registrations";
      String response = connectToDatabase(command);
      // Parse the response to JSON object
      JsonObject responseObject = jsonb.fromJson(response, JsonObject.class);
      JsonObject allDocuments = responseObject.getJsonObject("allDocuments");

      // Stream through the documents, extract emails, and join them into a JSON array
      // string
      String emailsJson = allDocuments.values().stream()
          .map(document -> ((JsonObject) document).getJsonObject("data").getString("email"))
          .collect(Json::createArrayBuilder, JsonArrayBuilder::add, JsonArrayBuilder::add)
          .build()
          .toString();

      return Response.ok(emailsJson).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(jsonb.toJson("Error getting all user emails"))
          .build();
    }
  }

  /**
   * Registers a new user with the provided registration request data.
   * 
   * @param userRegistrationRequest The user registration request data.
   * @param request                 The HTTP servlet request.
   * @return A Response object indicating the result of the registration attempt.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response registerUser(UserRegistrationRequest userRegistrationRequest, @Context HttpServletRequest request) {
    String userId = UUID.randomUUID().toString();
    userRegistrationRequest.setUserId(userId);

    String registrationJson = jsonb.toJson(userRegistrationRequest);
    String command = "POST registrations " + userId + " " + registrationJson;
    String response = connectToDatabase(command);

    if ("Error connecting to the database".equals(response)) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(jsonb.toJson("Database connection error")).build();
    } else if ("Document added".equals(response)) {
      SessionManager.createSession(request, userId);
      return Response.status(Status.CREATED).entity(jsonb.toJson(userId)).build();
    } else if ("Document with the same ID already exists. Use PUT to update it.".equals(response)) {
      return Response.status(Status.CONFLICT).entity(jsonb.toJson("User with this email already registered")).build();
    } else {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(jsonb.toJson("Error registering user")).build();
    }
  }
}