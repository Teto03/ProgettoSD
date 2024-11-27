package it.unimib.sd2024;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;

/**
 * Resource class for handling domain purchase and renewal requests.
 */
@Path("domains")
public class UserBuyRequestResource {
  private static final String DATABASE_HOST = "localhost";
  private static final int DATABASE_PORT = 3030;
  private final Jsonb jsonb = JsonbBuilder.create();

  /**
   * Connects to the database and handles the response for a given command.
   *
   * @param command The command to send to the database.
   * @return A Response object containing the database's response or an error
   *         message.
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
   * Processes a request to buy a domain, setting its registration and expiration
   * dates.
   *
   * @param userBuyRequest The request containing the domain purchase information.
   * @return A Response object indicating the outcome of the purchase attempt.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response buyDomain(UserBuyRequest userBuyRequest) {
    LocalDate registrationDate = LocalDate.now();
    LocalDate expirationDate = registrationDate.plusYears(Long.parseLong(userBuyRequest.getDuration()));

    userBuyRequest.setCurrentDate(registrationDate.toString());
    userBuyRequest.setExpirationDate(expirationDate.toString());

    String buyRequestJson = jsonb.toJson(userBuyRequest);
    String command = "POST domains " + userBuyRequest.getDomainId() + " " + buyRequestJson;

    Response response = connectToDatabaseAndHandleResponse(command);
    return response;
  }

  /**
   * Updates the expiration date of an existing domain registration.
   *
   * @param userBuyRequest The request containing the domain and the new duration.
   * @return A Response object indicating the outcome of the update attempt.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateDomainExpiration(UserBuyRequest userBuyRequest) {
    try {
      LocalDate currentDate = LocalDate.parse(userBuyRequest.getCurrentDate());
      LocalDate newExpirationDate = currentDate.plusYears(Long.parseLong(userBuyRequest.getDuration()));

      userBuyRequest.setExpirationDate(newExpirationDate.toString());

      String userUpdateRequestJson = jsonb.toJson(userBuyRequest);
      String command = "PUT domains " + userBuyRequest.getDomainId() + " " + userUpdateRequestJson;
      Response response = connectToDatabaseAndHandleResponse(command);
      if (response.getStatus() == Status.OK.getStatusCode()) {
        JsonObject jsonResponse = Json.createObjectBuilder()
            .add("expirationDate", newExpirationDate.toString())
            .build();
        return Response.ok(jsonResponse).build();
      } else {
        return response;
      }
    } catch (Exception e) {
      return createErrorResponse("Error processing request", Status.INTERNAL_SERVER_ERROR);
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