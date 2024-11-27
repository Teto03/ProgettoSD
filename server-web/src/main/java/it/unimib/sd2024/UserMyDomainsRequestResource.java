package it.unimib.sd2024;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

/**
 * Resource class for handling requests related to user domains.
 */
@Path("domains")
public class UserMyDomainsRequestResource {
  private static final String DATABASE_HOST = "localhost";
  private static final int DATABASE_PORT = 3030;

  /**
   * Retrieves the domains associated with a given user ID.
   *
   * @param userId The ID of the user whose domains are to be retrieved.
   * @return A Response object containing the user's domains or an error message.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserDomains(@QueryParam("userId") String userId) {
    if (userId == null || userId.trim().isEmpty()) {
      return createErrorResponse("User ID is required", Status.BAD_REQUEST);
    }

    String command = "GET domains";
    Response databaseResponse = connectToDatabaseAndHandleResponse(command);

    if (databaseResponse.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
      return createErrorResponse("Database connection error", Status.INTERNAL_SERVER_ERROR);
    }

    String responseContent = databaseResponse.getEntity().toString();
    try (JsonReader jsonReader = Json.createReader(new StringReader(responseContent))) {
      JsonObject responseObject = jsonReader.readObject();
      JsonObject allDocuments = responseObject.getJsonObject("allDocuments");
      JsonArrayBuilder filteredDomainsArrayBuilder = Json.createArrayBuilder();

      allDocuments.keySet().forEach(domainKey -> {
        JsonObject domainData = allDocuments.getJsonObject(domainKey).getJsonObject("data");
        if (userId.equals(domainData.getString("userId"))) {
          JsonObject filteredDomain = Json.createObjectBuilder()
              .add("domainId", domainData.getString("domainId"))
              .add("currentDate", domainData.getString("currentDate"))
              .add("expirationDate", domainData.getString("expirationDate"))
              .build();
          filteredDomainsArrayBuilder.add(filteredDomain);
        }
      });

      return Response.status(Status.OK).entity(filteredDomainsArrayBuilder.build()).build();
    } catch (Exception e) {
      return createErrorResponse("Error processing database response", Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Connects to the database and handles the response for a given command.
   *
   * @param command The command to be sent to the database.
   * @return A Response object containing the database's response.
   */
  private Response connectToDatabaseAndHandleResponse(String command) {
    try (Socket socket = new Socket(DATABASE_HOST, DATABASE_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      out.println(command);
      String response = in.readLine();
      if (response == null) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database connection error").build();
      }
      return Response.ok(response).build();
    } catch (IOException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database connection error").build();
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