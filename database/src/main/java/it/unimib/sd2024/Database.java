package it.unimib.sd2024;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a database with collections.
 * This class is thread-safe.
 */
public final class Database {
  private String name;
  private ConcurrentHashMap<String, Collection> collections;

  /**
   * Constructs a new Database with the specified name.
   *
   * @param name the name of the database, must not be null or empty
   * @throws IllegalArgumentException if name is null or empty
   */
  public Database(String name) {
    validateName(name);
    this.name = name;
    this.collections = new ConcurrentHashMap<>();
  }

  /**
   * Returns the name of the database.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the database.
   *
   * @param name the new name, must not be null or empty.
   * @throws IllegalArgumentException if name is null or empty.
   */
  public void setName(String name) {
    validateName(name);
    this.name = name;
  }

  /**
   * Returns all the collections in the database.
   *
   * @return a map of collection names to collections
   */
  public ConcurrentHashMap<String, Collection> getAllCollections() {
    return collections;
  }

  /**
   * Sets all the collections in the database.
   *
   * @param collections a map of collection names to collections
   */
  public void setAllCollections(ConcurrentHashMap<String, Collection> collections) {
    validateCollections(collections);
    this.collections.clear();
    this.collections.putAll(collections);
  }

  /**
   * Adds a new collection to the database.
   *
   * @param name       the name of the collection, must not be null or empty.
   * @param collection the collection to add, must not be null.
   * @throws IllegalArgumentException if name is null, empty, or if collection is
   *                                  null.
   */
  public void addCollection(String name, Collection collection) {
    Objects.requireNonNull(name, "Collection name cannot be null");
    Objects.requireNonNull(collection, "Collection cannot be null");
    collections.put(name, collection);
  }

  /**
   * Updates the documents of a collection in the database. If the collection does
   * not exist, logs a warning.
   * 
   * @param name      the name of the collection to update.
   * @param documents the new set of documents for the collection, must not be
   *                  null.
   * @throws IllegalArgumentException if documents is null.
   */
  public void updateCollection(String name, ConcurrentHashMap<String, Document> documents) {
    Collection collection = collections.get(name);
    if (collection != null) {
      collection.setAllDocuments(documents);
    }
  }

  /**
   * Removes a collection from the database.
   *
   * @param name the name of the collection to remove, must not be null or empty
   * @throws IllegalArgumentException if name is null or empty
   */
  public void removeCollection(String name) {
    collections.remove(name);
  }

  /**
   * Returns a collection by name.
   *
   * @param name the name of the collection to return
   * @return the collection with the specified name, or null if not found
   */
  public Collection getCollection(String name) {
    return collections.get(name);
  }

  /**
   * Validates the name of the collection.
   *
   * @param name the name to validate
   * @throws IllegalArgumentException if name is null or empty
   */
  private void validateName(String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
  }

  /**
   * Validates the ConcurrentHashMap of collections.
   * 
   * @param collections the ConcurrentHashMap to validate
   * @throws IllegalArgumentException if collections is null
   */
  private void validateCollections(ConcurrentHashMap<String, Collection> collections) {
    if (collections == null) {
      throw new IllegalArgumentException("Collections cannot be null");
    }
  }
}