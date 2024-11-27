# Progetto Sistemi Distribuiti 2023-2024 - API REST

**Attenzione**: l'unica rappresentazione ammessa è in formato JSON. Pertanto
vengono assunti gli header `Content-Type: application/json` e `Accept:
application/json`.

## / collections

Ogni risorsa ha la sua sezione dedicata con i metodi ammessi. In questo caso si
riferisce alla risorsa `/collections`.

### POST (createCollection)

```
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
```

- **Descrizione**: Crea una nuova collezione nel database utilizzando il nome
  fornito nel corpo della richiesta. Questo endpoint analizza una stringa JSON
  per estrarre il nome della collezione desiderata e invia un comando al
  database per creare effettivamente la collezione. Questa API viene utilizzata
  al caricamento della login page per evitare di dover fare hard coding della
  creazione delle due collezioni `domains` e `registrations` all'interno del
  database.
- **Parametri**: Nessuno.
- **Header**: `Content-Type: application/json`
- **Body richiesta**: Un oggetto JSON che contiene il nome della collezione da
  creare. Esempio: `{"collectionName": "nomeCollezione"}`.
- **Risposta**:
  - In caso di successo, restituisce una conferma che la collezione è stata
    creata.
  - In caso di errore, restituisce un messaggio di errore.
- **Codici di stato restituiti**:
  - `200 OK`: La collezione è stata creata con successo.
  - `500 Internal Server Error`: Errore interno del server, impossibile creare
    la collezione.

## / registrations

Ogni risorsa ha la sua sezione dedicata con i metodi ammessi. In questo caso si
riferisce alla risorsa `/registrations`.

### GET (getAllUserEmails)

```
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
```

- **Descrizione**: Recupera tutte le email degli utenti registrati nel database,
  questo viene fatto per garantire che quando un utente si registra non sia già
  presente un account con la sua mail nel database. Se questo si verifica
  l'utente può effettuare il login con il userId univoco oppure registrarsi con
  un nuovo account.
- **Parametri**: Nessuno.
- **Header**: `Accept: application/json`.
- **Body richiesta**: Nessuno.
- **Risposta**: Un array JSON contenente tutte le email degli utenti registrati
- **Codici di stato restituiti**:
  - `200 OK`: Richiesta eseguita con successo e email degli utenti restituite.
  - `500 Internal Server Error`: Errore interno del server, impossibile
    recuperare le email degli utenti.

### GET (getUserInfo)

```
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
```

- **Descrizione**: Recupera nome, cognome ed email dal database di un utente
  associato ad uno specifico userId, questo viene fatto per poter mostrare le
  informazioni sulla pagina dell'owner di un dominio.
- **Parametri**: `userId` (nell'URL) - L'ID dell'utente di cui recuperare le
  informazioni.
- **Header**: `Accept: application/json`
- **Body richiesta**: Nessuno.
- **Risposta**: Un oggetto JSON contenente le informazioni dell'utente, incluse
  email, nome e cognome.
- **Codici di stato restituiti**:
  - `200 OK`: Richiesta eseguita con successo e informazioni dell'utente
    restituite.
  - `404 Not Found`: Nessun utente trovato corrispondente all'ID fornito.
  - `500 Internal Server Error`: Errore interno del server, impossibile
    elaborare la richiesta.

### POST (registerUser)

```
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
```

- **Descrizione**: Registra un nuovo utente nel sistema utilizzando i dati
  forniti nella richiesta di registrazione.
- **Parametri**: Nessuno.
- **Header**: `Content-Type: application/json`
- **Body richiesta**: Un oggetto JSON contenente i dati di registrazione
  dell'utente, come email, nome, cognome, ecc.
- **Risposta**: Un oggetto JSON contenente l'ID dell'utente appena registrato.
- **Codici di stato restituiti**:
  - `201 Created`: L'utente è stato registrato con successo e la sua ID è
    restituita nella risposta.
  - `409 Conflict`: Un utente con la stessa email esiste già nel sistema.
    Utilizzare PUT per aggiornare le informazioni di un utente esistente.
  - `500 Internal Server Error`: Errore interno del server, impossibile
    registrare l'utente.

## / login

Ogni risorsa ha la sua sezione dedicata con i metodi ammessi. In questo caso si
riferisce alla risorsa `/login`.

### POST (loginUser)

```
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response loginUser(UserLoginRequest userLoginRequest, @Context HttpServletRequest request) {
    String command = "GET registrations " + userLoginRequest.getUserId();
    return connectToDatabaseAndHandleResponse(command, request, userLoginRequest.getUserId());
  }

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
```

- **Descrizione**: Effettua il login di un utente verificando le credenziali
  fornite. Nonostante l'operazione di login possa sembrare un'operazione di
  lettura, per cui sarebbe intuitivo utilizzare il metodo GET, si sceglie di
  utilizzare POST per motivi di sicurezza. Inviare dati sensibili come le
  credenziali di accesso tramite GET esporrebbe tali informazioni nelle URL, nei
  log del server, e potenzialmente in cache, rendendoli vulnerabili a
  intercettazioni. Utilizzando POST, i dati vengono inviati nel corpo della
  richiesta, offrendo un livello di sicurezza maggiore.
- **Parametri**: Nessuno.
- **Header**: `Content-Type: application/json`
- **Body richiesta**: Un oggetto JSON contenente la credenziale di login
  dell'utente `userId`. E' stato scelto di utilizzare lo userId come fosse una
  password o chiave per il singolo utente. Abbiamo usato UUID per generarla
  invece di utilizzare uno userId numeri es. 1, 2, 3, ..., n.
- **Risposta**:
  - In caso di successo, restituisce un messaggio di conferma del login e un
    token di sessione.
  - In caso di credenziali non valide, restituisce un messaggio di errore.
- **Codici di stato restituiti**:
  - `200 OK`: Login effettuato con successo. Restituisce il token di sessione.
  - `401 Unauthorized`: Credenziali non valide. L'utente non è autorizzato.
  - `500 Internal Server Error`: Errore interno del server, impossibile
    completare l'operazione di login.

## / domains

Ogni risorsa ha la sua sezione dedicata con i metodi ammessi. In questo caso si
riferisce alla risorsa `/domains`.

### GET (searchDomain)

```
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
```

- **Descrizione**: Questo endpoint permette di cercare informazioni su un
  dominio specifico utilizzando il suo ID. La logica implementata gestisce
  diversi scenari in base allo stato del dominio e alla relazione con l'utente
  che effettua la richiesta. Se il dominio non è trovato o la risposta è vuota,
  e il dominio è attualmente in fase di acquisto, viene restituito un errore di
  concorrenza. Se il dominio non è in fase di acquisto, viene suggerito di
  acquistarlo. Se il dominio è trovato, si verifica se è scaduto; se sì, e non è
  in fase di acquisto, viene suggerito di acquistarlo. Altrimenti, se l'utente è
  il proprietario del dominio, può aggiornarlo; se non lo è, può visualizzare i
  dettagli del proprietario. Questo endpoint gestisce anche errori di
  connessione al database e richieste non valide.
- **Parametri**:
  - `userId` (QueryParam): L'ID dell'utente che effettua la richiesta, usato per
    determinare se l'utente è il proprietario del dominio.
  - `domainId` (PathParam): L'ID del dominio che si sta cercando.
- **Header**: `Content-Type: application/json` - Indica che la risposta sarà in
  formato JSON.
- **Body richiesta**: Nessuno.
- **Risposta**:
  - Se il dominio non è trovato o è in fase di acquisto, restituisce un'azione
    consigliata ("Concurrency error" o "Buy domain") con il relativo ID del
    dominio.
  - Se il dominio è scaduto, suggerisce di acquistarlo.
  - Se il dominio è attivo, restituisce un'azione ("Update your domain" o "View
    owner details") basata sulla proprietà dell'utente, insieme ai dettagli del
    dominio.
  - In caso di errore di connessione al database, restituisce un messaggio di
    errore specifico.
  - In caso di richiesta non valida, restituisce un errore di richiesta non
    valida.
- **Codici di stato restituiti**:
  - `200 OK`: La ricerca è stata completata con successo e i dettagli del
    dominio (o l'azione consigliata) sono restituiti.
  - `400 Bad Request`: La richiesta non è valida, tipicamente a causa di un
    problema nel parsing della risposta del database.
  - `500 Internal Server Error`: Errore interno del server, come un problema di
    connessione al database.

### GET (getUserDomains)

```
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
```

- **Descrizione**: Questo endpoint recupera tutti i domini associati a un
  determinato utente, basandosi sull'ID utente fornito come parametro della
  query. La funzione verifica prima la validità dell'ID utente e poi procede a
  interrogare il database per ottenere i dettagli dei domini. Se la connessione
  al database fallisce, viene restituito un errore specifico. In caso contrario,
  la funzione filtra i domini appartenenti all'utente e restituisce i dettagli
  rilevanti di ciascun dominio, come l'ID del dominio, la data corrente e la
  data di scadenza. Questo endpoint è utile per gli utenti che desiderano
  visualizzare un elenco dei loro domini registrati e le relative informazioni
  di scadenza.
- **Parametri**:
  - `userId` (QueryParam): L'ID dell'utente che effettua la richiesta.
    Utilizzato per filtrare i domini appartenenti all'utente specificato.
- **Header**: `Content-Type: application/json`
- **Body richiesta**: Nessuno. Le informazioni necessarie sono passate tramite
  il parametro della query.
- **Risposta**:
  - In caso di successo, restituisce un array di oggetti JSON, ognuno
    rappresentante un dominio appartenente all'utente, con dettagli come l'ID
    del dominio, la data corrente e la data di scadenza.
  - In caso di errore di connessione al database, restituisce un messaggio di
    errore specifico.
  - Se l'ID utente non è fornito o è invalido, restituisce un errore di
    richiesta non valida.
- **Codici di stato restituiti**:
  - `200 OK`: La richiesta è stata completata con successo e i dettagli dei
    domini sono restituiti.
  - `400 Bad Request`: La richiesta non è valida, tipicamente a causa di un ID
    utente mancante o non valido.
  - `500 Internal Server Error`: Errore interno del server, come un problema di
    connessione al database.

### POST (buyDomain)

```
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
```

- **Descrizione**: Questo endpoint permette a un utente di acquistare un
  dominio. L'utente invia una richiesta di acquisto contenente l'ID del dominio
  desiderato, la durata dell'acquisto in anni, e i dettagli della carta di
  credito per il pagamento. Il servizio calcola la data di registrazione (data
  corrente) e la data di scadenza del dominio basandosi sulla durata
  specificata, determina il prezzo in base alla durata dell'acquisto, e poi
  procede con l'elaborazione del pagamento. Se il pagamento è andato a buon fine
  e l'operazione ha successo, il dominio viene registrato per l'utente per il
  periodo specificato.
- **Parametri**:
  - `DomainPurchaseRequest` (Body): Un oggetto JSON che contiene i dettagli
    della richiesta di acquisto, inclusi l'ID del dominio desiderato, la durata
    dell'acquisto in anni, e i dettagli della carta di credito dell'utente.
- **Header**: `Content-Type: application/json`
- **Body richiesta**: Un oggetto JSON che rappresenta la richiesta di acquisto
  del dominio. Deve includere:
  - `userId`: L'ID dell'utente che effettua l'acquisto.
  - `domainId`: L'ID del dominio che si desidera acquistare.
  - `cardOwnerName`: Il nome del titolare della carta.
  - `cardOwnerSurname`: Il cognome del titolare della carta.
  - `cardNumber`: Il numero della carta di credito.
  - `cardExpirationDate`: La data di scadenza della carta.
  - `CVV`: Il codice di sicurezza della carta.
  - `duration`: La durata dell'acquisto in anni.
  - `currentDate`: La data corrente, che rappresenta la data di inizio validità
    del dominio.
  - `expirationDate`: La data di scadenza del dominio, calcolata sulla base
    della durata dell'acquisto.
  - `price`: Il prezzo dell'acquisto, calcolato in base alla durata
    dell'acquisto e ad altri eventuali fattori.
- **Risposta**:
  - La conferma dell'acquisto del dominio o un messaggio di errore se il dominio
    non è disponibile per l'acquisto, o se si verifica un problema con la
    connessione al database.
- **Codici di stato restituiti**:
  - `200 OK`: L'acquisto del dominio è stato completato con successo.
  - `500 Internal Server Error`: Errore interno del server, come un problema di
    connessione al database o un errore durante il processo di acquisto.

### PUT (updateDomainExpiration)

```
@PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateDomainExpiration(UserBuyRequest userBuyRequest) {
    try {
      LocalDate expirationDate = LocalDate.parse(userBuyRequest.getExpirationDate());
      LocalDate newExpirationDate = expirationDate.plusYears(Long.parseLong(userBuyRequest.getDuration()));

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
```

- **Descrizione**: Questo endpoint viene utilizzato per aggiornare la data di
  scadenza di un dominio precedentemente acquistato. L'ID del dominio e la nuova
  durata dell'acquisto vengono passati nel corpo della richiesta. Questa
  operazione permette di estendere la validità di un dominio per un periodo
  specificato.
- **Parametri**:
  - `UserBuyRequest` (Body): Un oggetto che contiene i seguenti campi:
    - `userId`: ID dell'utente che effettua l'acquisto.
    - `domainId`: ID del dominio da aggiornare.
    - `cardOwnerName`: Nome del proprietario della carta.
    - `cardOwnerSurname`: Cognome del proprietario della carta.
    - `cardNumber`: Numero della carta di credito.
    - `cardExpirationDate`: Data di scadenza della carta.
    - `CVV`: Codice di sicurezza della carta.
    - `duration`: Durata dell'estensione in anni.
    - `currentDate`: Data corrente dell'acquisto.
    - `expirationDate`: Data di scadenza attuale del dominio.
    - `price`: Prezzo dell'estensione.
- **Header**: `Content-Type: application/json` - Indica che il corpo della
  richiesta e della risposta sarà in formato JSON.
- **Body richiesta**: JSON che rappresenta l'oggetto `UserBuyRequest` con i
  campi sopra elencati.
- **Risposta**:
  - In caso di successo, restituisce un JSON con la nuova data di scadenza del
    dominio.
  - In caso di errore, restituisce un messaggio di errore appropriato.
- **Codici di stato restituiti**:
  - `200 OK`: L'aggiornamento della data di scadenza del dominio è stato
    completato con successo.
  - `400 Bad Request`: Si verifica un errore nella richiesta, ad esempio se il
    corpo della richiesta è malformato.
  - `500 Internal Server Error`: Errore interno del server, impossibile
    completare l'operazione di aggiornamento.

### DELETE (releaseDomain)

```
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
```

- **Descrizione**: Questo endpoint viene utilizzato per rilasciare un dominio
  che è stato precedentemente segnato per l'acquisto ma non è stato
  effettivamente acquistato. Il dominio da rilasciare è identificato dall'ID
  `domainId` passato come parametro nell'URL. Questa operazione è tipicamente
  necessaria in tre scenari:
  - L'utente completa il processo di acquisto, e il dominio deve essere
  rilasciato perché è stato acquistato con successo;
  - L'utente è sulla pagina di acquisto ma decide di non procedere con
  l'acquisto, e il timer per completare l'acquisto scade;
  - L'utente ritorna alla homepage senza completare l'acquisto, ma prima che il
  timer sulla pagina di acquisto scada. In tutti e tre i casi, il dominio
  `domainToRelease` salvato nella `sessionStorage` dell'utente viene rilasciato
  per renderlo nuovamente disponibile per l'acquisto da parte di altri utenti.
- **Parametri**:
  - `domainId` (PathParam): L'ID del dominio che si sta rilasciando.
- **Header**: `Content-Type: application/json` - Indica che la risposta sarà in
  formato JSON.
- **Body richiesta**: Nessuno. L'ID del dominio da rilasciare viene passato
  direttamente nell'URL.
- **Risposta**:
  - In caso di successo, restituisce un messaggio che conferma il rilascio del
    dominio.
  - In caso di errore, ad esempio se il dominio non è stato trovato o non può
    essere rilasciato, restituisce un messaggio di errore appropriato.
- **Codici di stato restituiti**:
  - `200 OK`: Il dominio è stato rilasciato con successo.
  - `400 Bad Request`: Si verifica un errore nella richiesta, ad esempio se la
    richiesta è malformata.
  - `500 Internal Server Error`: Errore interno del server, impossibile
    completare l'operazione di rilascio del dominio.

### GET (GetUserOrders)
```
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
```
- **Descrizione**:
- Questo endpoint viene utilizzato per ottenere gli ordini di
  un utente specifico. L'utente è identificato dall'ID `userId` passato come
  parametro di query. Questa operazione è tipicamente necessaria quando si vuole
  visualizzare l'elenco degli ordini effettuati da un utente. Se l'ID
  dell'utente non è specificato, l'endpoint restituirà tutti gli ordini.

- **Parametri**:
  - `userId` (QueryParam): L'ID dell'utente i cui ordini si desidera ottenere.
- **Header**: 
  - `Content-Type: application/json` - Indica che la risposta sarà in
  formato JSON.
- **Body richiesta**: 
  - Nessuno. L'ID del dominio da rilasciare viene passato
  direttamente nell'URL.
- **Risposta**:
  - In caso di successo, gli ordini, Ogni ordine e' un JSON objext che contiene
    `domainId`, `orderDate`, `type` and `price`.
  - In caso di errore, ad esempio se si verifica un errore durante il processo
    di recupero degli ordini dal database, restituisce un oggetto JSON con un
    messaggio di errore.
  - **Codici di stato restituiti**:
    - `200 OK`: Il dominio è stato rilasciato con successo.
    - `500 Internal Server Error`: Errore interno del server, impossibile
    completare l'operazione di rilascio del dominio.

### POST (UserOrder)
```
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
  ```
  - **Descrizione**: 
    - Questo endpoint viene utilizzato per creare un nuovo
    ordine. L'ordine viene creato sulla base dei dati forniti nel corpo della
    richiesta. Questa operazione è tipicamente necessaria quando un utente
    effettua un nuovo ordine, o il rinnovo di un dominio preesitente.
- **Parametri**:
  - Nessuno. Tutti i dati necessari per creare un nuovo ordine vengono forniti
    nel corpo della richiesta.
  - **Header**: `Content-Type: application/json` - Indica che la richiesta sarà in
  formato JSON.
- **Body richiesta**:
  - Un oggetto JSON che rappresenta l'ordine da creare.
  - Questo dovrebbe includere tutti i dettagli necessari per l'ordine.
- **Risposta**:
  - In caso di successo, restituisce un messaggio di conferma che l'ordine è stato creato con successo.
  - In caso di errore, ad esempio se i dati dell'ordine non sono validi o se si
    verifica un errore durante la creazione dell'ordine, restituisce un
    messaggio di errore appropriato.
  - Viene restituito un errore anche nel caso in cui si cerchi di creare un
    ordine di rinnovo per un dominio non esistente.
- **Codici di stato restituiti**:
  - `200 OK`: Il dominio è stato rilasciato con successo.
  - `500 Internal Server Error`: Errore interno del server, impossibile
    completare l'operazione di rilascio del dominio.