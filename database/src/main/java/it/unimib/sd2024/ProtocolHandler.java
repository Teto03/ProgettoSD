package it.unimib.sd2024;

/**
 * Handles protocol requests by parsing input commands and executing database
 * operations. This class is responsible for interpreting the commands received
 * as input and performing the corresponding actions on the database, such as
 * retrieving, adding, updating, or deleting documents within collections.
 */
public class ProtocolHandler {
  private String inputLine;
  private Database database;

  /**
   * Constructs a new ProtocolHandler with the specified input line and database.
   *
   * @param inputLine the input line to be processed
   * @param database  the database to perform operations on
   */
  public ProtocolHandler(String inputLine, Database database) {
    this.inputLine = inputLine.trim();
    this.database = database;
  }

  /**
   * Returns the current input line.
   *
   * @return the current input line
   */
  public String getInput() {
    return inputLine;
  }

  /**
   * Sets the input line to the specified string.
   *
   * @param inputLine the new input line
   */
  public void setInput(String inputLine) {
    this.inputLine = inputLine.trim();
  }

  /**
   * Returns the current database.
   *
   * @return the current database
   */
  public Database getDatabase() {
    return database;
  }

  /**
   * Sets the database to the specified database.
   *
   * @param database the new database
   */
  public void setDatabase(Database database) {
    this.database = database;
  }

  /**
   * Handles the request based on the input line, performing the appropriate
   * database operation. It parses the input line into commands and parameters,
   * then executes the corresponding database operation based on the command.
   *
   * @return a string representing the result of the request
   */
  public String handleRequest() {
    String[] parts = inputLine.split(" ");
    if (parts.length < 2) {
      return "Invalid command format";
    }

    String command = parts[0]; // GET, PUT
    String collectionName = parts[1]; // domains, users
    String documentId = parts.length > 2 ? parts[2] : null; // example.com, 123
    String documentData = parts.length > 3 ? parts[3] : null; // { "key": "value" }

    switch (command) {
      case "CREATE":
        if (documentData != null) {
          return "Invalid command";
        }
        return handleCreate(collectionName);
      case "GET":
        if (documentData != null) {
          return "Invalid command";
        }
        return handleGet(collectionName, documentId);
      case "POST":
        if (documentData == null) {
          return "Invalid command";
        }
        return handlePost(collectionName, documentId, documentData);
      case "PUT":
        if (documentData == null) {
          return "Invalid command";
        }
        return handlePut(collectionName, documentId, documentData);
      case "DELETE":
        if (documentData != null) {
          return "Invalid command";
        }
        return handleDelete(collectionName, documentId);
      default:
        return "Unsupported command";
    }
  }

  /**
   * Handles a GET request for a collection or document. Retrieves either the
   * entire
   * collection or a specific document within the collection based on the provided
   * document ID.
   *
   * @param collectionName the name of the collection
   * @param documentId     the ID of the document (optional)
   * @return a string representing the collection or document
   */
  private String handleGet(String collectionName, String documentId) {
    Collection collection = database.getCollection(collectionName);
    if (collection == null) {
      return "Collection not found";
    }

    if (documentId == null) {
      return collection.toString();
    } else {
      Document document = collection.getDocument(documentId);
      if (document == null) {
        return "Document not found";
      }
      return document.toString();
    }
  }

  /**
   * Handles a POST request to add a new document to a collection. Adds a new
   * document
   * with the specified ID and data to the collection.
   *
   * @param collectionName the name of the collection
   * @param documentId     the ID of the new document
   * @param documentData   the data of the new document
   * @return a string indicating the result of the operation
   */
  private String handlePost(String collectionName, String documentId, String documentData) {
    Collection collection = database.getCollection(collectionName);
    if (collection == null) {
      return "Collection not found";
    }

    Document existingDocument = collection.getDocument(documentId);
    if (existingDocument != null) {
      return "Document with the same ID already exists. Use PUT to update it.";
    }

    Document document = new Document(documentId, documentData);
    collection.addDocument(documentId, document);

    return "Document added";
  }

  /**
   * Handles a PUT request to update an existing document in a collection. Updates
   * the
   * data of an existing document with the specified ID in the collection.
   *
   * @param collectionName the name of the collection
   * @param documentId     the ID of the document to update
   * @param documentData   the new data for the document
   * @return a string indicating the result of the operation
   */
  private String handlePut(String collectionName, String documentId, String documentData) {
    Collection collection = database.getCollection(collectionName);
    if (collection == null) {
      return "Collection not found";
    }

    Document document = collection.getDocument(documentId);
    if (document == null) {
      return "Document not found";
    }

    document.setData(documentData);
    return "Document updated";
  }

  /**
   * Handles a DELETE request to remove a document from a collection. Removes the
   * specified document from the collection.
   *
   * @param collectionName the name of the collection
   * @param documentId     the ID of the document to delete
   * @return a string indicating the result of the operation
   */
  private String handleDelete(String collectionName, String documentId) {
    Collection collection = database.getCollection(collectionName);
    if (collection == null) {
      return "Collection not found";
    }

    Document document = collection.getDocument(documentId);
    if (document == null) {
      return "Document not found";
    }

    collection.removeDocument(documentId);
    return "Document deleted";
  }

  /**
   * Handles a CREATE request to create a new collection. Creates a new collection
   * with the specified name and populates it with some initial data.
   *
   * @param collectionName the name of the new collection
   * @return a string indicating the result of the operation
   */
  private String handleCreate(String collectionName) {
    if (database.getCollection(collectionName) != null) {
      return "Collection already exists";
    }

    Collection newCollection = new Collection(collectionName);
    database.addCollection(collectionName, newCollection);

    return "Collection created";
  }
}
