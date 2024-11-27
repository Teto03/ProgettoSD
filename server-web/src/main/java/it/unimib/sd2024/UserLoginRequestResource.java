package it.unimib.sd2024;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Resource class for handling user login requests.
 */
@Path("login")
public class UserLoginRequestResource {
  private static final String DATABASE_HOST = "localhost";
  private static final int DATABASE_PORT = 3030;

  /**
   * Attempts to log in a user by validating their credentials against the
   * database.
   *
   * @param userLoginRequest The user's login request containing their
   *                         credentials.
   * @param request          The HTTP servlet request.
   * @return A Response object indicating the outcome of the login attempt.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response loginUser(UserLoginRequest userLoginRequest, @Context HttpServletRequest request) {
    String command = "GET registrations " + userLoginRequest.getUserId();
    return connectToDatabaseAndHandleResponse(command, request, userLoginRequest.getUserId());
  }

  /**
   * Connects to the database and handles the response for a given command.
   * If the user is successfully authenticated, a session is created.
   *
   * @param command The command to send to the database.
   * @param request The HTTP servlet request, used for session management.
   * @param userId  The user ID to authenticate.
   * @return A Response object containing the database's response or an error
   *         message.
   */
  private Response connectToDatabaseAndHandleResponse(String command, HttpServletRequest request, String userId) {
    try (Socket socket = new Socket(DATABASE_HOST, DATABASE_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      out.println(command);
      String response = in.readLine();
      if (response == null || response.isEmpty()) {
        return Response.status(Status.UNAUTHORIZED).entity("Invalid userId").build();
      } else {
        SessionManager.createSession(request, userId);
        return Response.status(Status.OK).entity(response).build();
      }
    } catch (IOException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database connection error").build();
    }
  }
}