package it.unimib.sd2024;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.StringReader;

@Path("domains")
public class UserSearchRequestResource {
  private static final String DATABASE_HOST = "localhost";
  private static final int DATABASE_PORT = 3030;
  private static final ConcurrentHashMap<String, Long> beingBoughtDomains = new ConcurrentHashMap<>();

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

  @GET
  @Path("{domainId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchDomain(@QueryParam("userId") String userId, @PathParam("domainId") String domainId) {
    String command = "GET domains " + domainId;
    String response = connectToDatabase(command);

    if ("Error connecting to the database".equals(response)) {
      return createErrorResponse("Database connection error", Status.INTERNAL_SERVER_ERROR);
    } else if ("Document not found".equals(response) || response.isEmpty()) {
      if (beingBoughtDomains.containsKey(domainId)) {
        JsonObject actionResponse = Json.createObjectBuilder()
            .add("action", "Concurrency error")
            .add("document", domainId)
            .build();
        return Response.status(Status.OK).entity(actionResponse).build();
      } else {
        beingBoughtDomains.put(domainId, System.currentTimeMillis());
        JsonObject actionResponse = Json.createObjectBuilder()
            .add("action", "Buy domain")
            .add("document", domainId)
            .build();
        return Response.status(Status.OK).entity(actionResponse).build();
      }
    }

    try {
      JsonObject jsonObject = Json.createReader(new StringReader(response)).readObject();
      LocalDate expirationDate = LocalDate.parse(jsonObject.getJsonObject("data").getString("expirationDate"));
      if (LocalDate.now().isAfter(expirationDate)) {
        if (beingBoughtDomains.containsKey(domainId)) {
          JsonObject actionResponse = Json.createObjectBuilder()
              .add("action", "Concurrency error")
              .add("document", domainId)
              .build();
          return Response.status(Status.OK).entity(actionResponse).build();
        } else {
          beingBoughtDomains.put(domainId, System.currentTimeMillis());
          JsonObject actionResponse = Json.createObjectBuilder()
              .add("action", "Buy domain")
              .add("document", domainId)
              .build();
          return Response.status(Status.OK).entity(actionResponse).build();
        }
      }

      boolean isUserOwner = userId.equals(jsonObject.getJsonObject("data").getString("userId"));
      JsonObjectBuilder actionResponseBuilder = Json.createObjectBuilder()
          .add("action", isUserOwner ? "Update your domain" : "View owner details");

      if (isUserOwner || !LocalDate.now().isAfter(expirationDate)) {
        actionResponseBuilder.add("document", jsonObject.getJsonObject("data"));
      }

      JsonObject actionResponse = actionResponseBuilder.build();
      return Response.status(Status.OK).entity(actionResponse).build();
    } catch (Exception e) {
      return createErrorResponse("Invalid request", Status.BAD_REQUEST);
    }
  }

  @DELETE
  @Path("release/{domainId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response releaseDomain(@PathParam("domainId") String domainId) {
    beingBoughtDomains.remove(domainId);
    JsonObject actionResponse = Json.createObjectBuilder()
        .add("action", "Domain released")
        .add("document", domainId)
        .build();
    return Response.status(Status.OK).entity(actionResponse).build();
  }

  private Response createErrorResponse(String message, Status status) {
    JsonObject errorResponse = Json.createObjectBuilder()
        .add("error", message)
        .build();
    return Response.status(status).entity(errorResponse).build();
  }
}
