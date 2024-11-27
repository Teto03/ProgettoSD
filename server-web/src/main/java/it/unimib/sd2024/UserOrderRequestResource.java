package it.unimib.sd2024;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;

/**
 * Classe risorsa per la gestione degli ordini di domini.
 * Utilizza JAX-RS per esporre API RESTful.
 */
@Path("orders")
public class UserOrderRequestResource {
  // Indirizzo e porta del database, per la connessione via socket
  private static final String DATABASE_HOST = "localhost";
  private static final int DATABASE_PORT = 3030;
  private static int orderCounter = 0;
  // Istanza di Jsonb per la serializzazione e deserializzazione JSON
  private final Jsonb jsonb = JsonbBuilder.create();

  /**
   * Stabilisce una connessione con il database e gestisce la risposta.
   *
   * @param command Il comando da inviare al database.
   * @return Un oggetto Response contenente il risultato dell'operazione.
   */
  private Response connectToDatabaseAndHandleResponse(String command) {
    try (Socket socket = new Socket(DATABASE_HOST, DATABASE_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      // Invia il comando al database
      out.println(command);
      // Legge la risposta
      String response = in.readLine();
      // Gestisce il caso di risposta nulla
      if (response == null) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database connection error").build();
      }
      // Restituisce la risposta in caso di successo
      return Response.ok(response).build();
    } catch (IOException e) {
      // Gestisce eventuali errori di connessione
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Database connection error").build();
    }
  }

  /**
   * Recupera tutti gli ordini dal database.
   *
   * @param userId ID dell'utente per cui filtrare gli ordini, opzionale.
   * @return Un oggetto Response contenente tutti gli ordini.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOrders(@QueryParam("userId") String userId) {
    String command = "GET orders";
    Response databaseResponse = connectToDatabaseAndHandleResponse(command);

    if (databaseResponse.getStatus() == Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
      return createErrorResponse("Database connection error", Status.INTERNAL_SERVER_ERROR);
    }

    String responseContent = databaseResponse.getEntity().toString();
    try (JsonReader jsonReader = Json.createReader(new StringReader(responseContent))) {
      JsonObject responseObject = jsonReader.readObject();
      JsonObject allDocuments = responseObject.getJsonObject("allDocuments");
      JsonArrayBuilder filteredOrdersArrayBuilder = Json.createArrayBuilder();

      allDocuments.keySet().forEach(orderKey -> {
        JsonObject orderData = allDocuments.getJsonObject(orderKey).getJsonObject("data");
        if (userId == null || userId.trim().isEmpty() || userId.equals(orderData.getString("userId"))) {
          JsonObject filteredOrder = Json.createObjectBuilder()
              .add("domainId", orderData.getString("domainId"))
              .add("orderDate", orderData.getString("orderDate"))
              .add("type", orderData.getString("type"))
              .add("price", orderData.getString("price"))
              .build();
          filteredOrdersArrayBuilder.add(filteredOrder);
        }
      });

      return Response.status(Status.OK).entity(filteredOrdersArrayBuilder.build()).build();
    } catch (Exception e) {
      return createErrorResponse("Error processing database response", Status.INTERNAL_SERVER_ERROR);
    }
  }

  private Response createErrorResponse(String message, Status status) {
    JsonObject errorObject = Json.createObjectBuilder()
        .add("error", message)
        .build();
    return Response.status(status).entity(errorObject).build();
  }

  /**
   * Elabora una richiesta per creare un nuovo ordine.
   *
   * @param orderRequest La richiesta contenente le informazioni dell'ordine.
   * @return Un oggetto Response che indica l'esito del tentativo di creazione.
   */

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createOrder(UserOrderRequest orderRequest) {
    // Increment the counter
    orderCounter++;
    String orderRequestJson = jsonb.toJson(orderRequest);
    String command = "POST orders " + orderCounter + " " + orderRequestJson;

    // Connect to the database and handle the response
    return connectToDatabaseAndHandleResponse(command);
  }
}
