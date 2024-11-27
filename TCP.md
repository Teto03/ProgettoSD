# Progetto Sistemi Distribuiti 2023-2024 - TCP

Il protocollo è testuale e ogni richiesta consiste di una singola linea di testo
che contiene il comando seguito da eventuali parametri. Ogni comando è
interpretato dal ProtocolHandler, che gestisce le operazioni sul database. Le
risposte sono anch'esse testuali, con messaggi che indicano il risultato
dell'operazione.

### Formato delle richieste
```
COMANDO COLLECTION_NAME [DOCUMENT_ID] [DOCUMENT_DATA]
```

- `COMANDO` è l'operazione da eseguire (`GET`, `POST`, `PUT`, `DELETE`).
- `COLLECTION_NAME` è il nome della collezione su cui operare.
- `DOCUMENT_ID` è l'identificatore del documento (opzionale per alcuni comandi).
- `DOCUMENT_DATA` è il contenuto del documento in formato JSON (necessario solo
  per `POST` e `PUT`).

### Formato delle Risposte
Le risposte sono stringhe testuali che descrivono il risultato dell'operazione:

- Se l'operazione ha successo, viene restituito il documento o un messaggio di
  conferma.
- Se l'operazione fallisce, viene restituito un messaggio di errore.

### GET:
- **Descrizione:** Recupera una collezione o un documento specifico.
- **Formato:** `GET COLLECTION_NAME [DOCUMENT_ID]`

#### Risposte: 
- Collezione trovata: ritorna una rappresentazione testuale della collezione.
- Documento trovato: ritorna una rappresentazione testuale del documento.
- Collezione non trovata: `Collection not found`.
- Documento non trovato: `Document not found`.

```
Richiesta: GET domains
Risposta: {"name": "domains", "allDocuments": {...}}
```

### POST:
- **Descrizione:** Aggiunge un nuovo documento a una collezione.
- **Formato:** `POST COLLECTION_NAME [DOCUMENT_ID] [DOCUMENT_DATA]`

#### Risposte: 
- Documento aggiunto: `Document added`.
- Collezione non trovata: `Collection not found`.
- Documento già esistente: `Document with the same ID already exists. Use PUT to
  update it`.

```
Richiesta: POST users 123 {"name":"John Doe"}
Risposta: Document added
```

### PUT:
- **Descrizione:** Aggiorna un documento esistente in una collezione.
- **Formato:** `PUT COLLECTION_NAME [DOCUMENT_ID] [DOCUMENT_DATA]`

#### Risposte: 
- Documento aggiornato: `Document updated`.
- Collezione non trovata: `Collection not found`.
- Documento non trovato: `Document not found`.

```
Richiesta: PUT users 123 {"name":"Jane Doe"}
Risposta: Document updated
```


### DELETE:
- **Descrizione:** Rimuove un documento da una collezione.
- **Formato:** `DELETE COLLECTION_NAME [DOCUMENT_ID]`

#### Risposte:
- Documento rimosso: `Document deleted`.
- Collezione non trovata: `Collection not found`.
- Documento non trovato: `Document not found`.

```
 Richiesta: DELETE users 123
 Risposta: Document deleted
```

### Architettura del Sistema
- `Main`: Classe principale che avvia il server e gestisce le connessioni dei
  client.
- `ProtocolHandler`: Classe responsabile della gestione e dell'interpretazione
  delle richieste, e dell'esecuzione delle operazioni sul database.
- `Database`: Classe che rappresenta il database, contenente una mappa delle
  collezioni.
- `Collection`: Classe che rappresenta una collezione di documenti.
- `Document`: Classe che rappresenta un documento con un ID e dati in formato
  JSON.

### TCP nel Nostro Progetto
Nel contesto di questo progetto, TCP viene utilizzato per garantire una
comunicazione affidabile tra il client e il server. Il server ascolta su una
specifica porta (`3030`) per le connessioni in arrivo, e per ogni connessione
stabilita, viene creato un thread dedicato (Handler) per gestire le richieste
del client.


### La Classe Main

**Descrizione della Classe**

La classe Main è il punto di ingresso principale per l'applicazione del server
database. Questa classe inizializza il server, ascolta le connessioni in arrivo
e gestisce le richieste dei client tramite un handler dedicato.

Funzionamento della Classe Main Variabili e Costanti

```
public static final int PORT = 3030;: La porta su cui il server ascolta le connessioni in arrivo.
private static Database database;: Istanza del database utilizzata dal server.
```

**Metodo startServer()**

Questo metodo è responsabile dell'avvio del server e della gestione delle
connessioni dei client.

```
public static void startServer() throws IOException {
    var server = new ServerSocket(PORT);

    System.out.println("Database listening at localhost:" + PORT);
    database = new Database("Database1");

    database.addCollection("registrations", new Collection("registrations"));
    database.addCollection("domains", new Collection("domains"));

    try {
        while (true)
            new Handler(server.accept()).start();
    } catch (IOException e) {
        System.err.println(e);
    } finally {
        server.close();
    }
}
```

1. Creazione del ServerSocket: Viene creato un ServerSocket che ascolta sulla
   porta definita.

2. Inizializzazione del Database: Viene inizializzato il database e vengono
   create alcune collezioni di esempio.

3. Gestione delle Connessioni: Il server entra in un ciclo infinito in cui
accetta nuove connessioni client e per ciascuna connessione crea un nuovo thread
Handler per gestire le richieste.


**Classe Interna Handler**

La classe Handler gestisce le singole connessioni dei client. Ogni istanza di
Handler è associata a un singolo socket client.


Costruttore: Inizializza il Handler con il socket client.

```
private static class Handler extends Thread {
    private Socket client;

    public Handler(Socket client) {
        this.client = client;
    }
}
```

Metodo run: Legge le richieste dal client, le processa usando ProtocolHandler, e
invia le risposte al client.

```
    public void run() {
        try (var out = new PrintWriter(client.getOutputStream(), true);
             var in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                ProtocolHandler protocolHandler = new ProtocolHandler(inputLine, database);
                String response = protocolHandler.handleRequest();
                System.out.println(response);
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
```

**Metodo main**

Il metodo main avvia il server chiamando il metodo startServer.

```
public static void main(String[] args) throws IOException {
    startServer();
}
```
