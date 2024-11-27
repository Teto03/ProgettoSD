package it.unimib.sd2024;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a collection of documents in a database.
 */
public final class Collection {

  private String name;
  private final ConcurrentHashMap<String, Document> documents;
  private final ReadWriteLock nameLock = new ReentrantReadWriteLock();

  /**
   * Constructs a new Collection with the specified name.
   *
   * @param name the name of the collection, must not be null or empty
   * @throws IllegalArgumentException if the name is null or empty
   */
  public Collection(String name) {
    validateName(name);
    this.name = name;
    this.documents = new ConcurrentHashMap<>();
  }

  /**
   * Returns the name of the collection.
   *
   * @return the name of the collection
   */
  public String getName() {
    nameLock.readLock().lock();
    try {
      return name;
    } finally {
      nameLock.readLock().unlock();
    }
  }

  /**
   * Sets the name of the collection to the specified value.
   *
   * @param name the new name for the collection, must not be null or empty
   * @throws IllegalArgumentException if the name is null or empty
   */
  public void setName(String name) {
    validateName(name);
    nameLock.writeLock().lock();
    try {
      this.name = name;
    } finally {
      nameLock.writeLock().unlock();
    }
  }

  /**
   * Returns all the documents in the collection as a {@link Map}.
   *
   * @return a map of document IDs to documents
   */
  public Map<String, Document> getAllDocuments() {
    return new ConcurrentHashMap<>(documents);
  }

  /**
   * Replaces all documents in the collection with the provided map of documents.
   *
   * @param documents the new documents to set, must not be null
   * @throws IllegalArgumentException if the documents map is null
   */
  public void setAllDocuments(ConcurrentHashMap<String, Document> documents) {
    validateDocuments(documents);
    this.documents.clear();
    this.documents.putAll(documents);
  }

  /**
   * Adds a document to the collection.
   *
   * @param id       the ID of the document, must not be null
   * @param document the document to add, must not be null
   * @throws NullPointerException if either the id or document is null
   */
  public void addDocument(String id, Document document) {
    Objects.requireNonNull(id, "Document ID cannot be null");
    Objects.requireNonNull(document, "Document cannot be null");
    documents.put(id, document);
  }

  /**
   * Updates the data of a document in the collection.
   *
   * @param id   the ID of the document to update, must exist in the collection
   * @param data the new data for the document
   */
  public void updateDocument(String id, String data) {
    Document document = documents.get(id);
    if (document != null) {
      document.setData(data);
    }
  }

  /**
   * Returns the document with the specified ID.
   *
   * @param id the ID of the document to retrieve
   * @return the document, or null if not found
   */
  public Document getDocument(String id) {
    return documents.get(id);
  }

  /**
   * Removes the document with the specified ID from the collection.
   *
   * @param id the ID of the document to remove
   */
  public void removeDocument(String id) {
    documents.remove(id);
  }

  /**
   * Converts a {@link Map} of documents to a string representation.
   *
   * @param documents the Map to convert to a string
   * @return a string representation of the documents
   */
  private static String mapToString(Map<String, Document> documents) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    documents.forEach((id, doc) -> {
      sb.append("\"").append(id).append("\": ").append(doc).append(",");
    });
    if (sb.lastIndexOf(",") > -1) {
      sb.delete(sb.lastIndexOf(","), sb.length());
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public String toString() {
    return "{"
        + "\"name\": \"" + name + "\","
        + "\"allDocuments\": " + mapToString(this.documents)
        + "}";
  }

  /**
   * Validates the name of the collection.
   *
   * @param name the name to validate
   * @throws IllegalArgumentException if the name is null or empty
   */
  private void validateName(String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
  }

  /**
   * Validates the provided {@link Map} of documents.
   * 
   * @param documents the Map to validate
   * @throws IllegalArgumentException if the documents map is null
   */
  private void validateDocuments(Map<String, Document> documents) {
    if (documents == null) {
      throw new IllegalArgumentException("Documents cannot be null");
    }
  }
}
