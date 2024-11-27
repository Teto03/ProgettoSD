package it.unimib.sd2024;

import java.util.Objects;

/**
 * Represents a document with an ID and data.
 * This class is not thread-safe.
 */
public class Document {
  private String id;
  private String data;

  /**
   * Constructs a new Document with the specified ID and data.
   *
   * @param id   the ID of the document, must not be null or empty
   * @param data the data of the document, must not be null or empty
   * @throws IllegalArgumentException if id or data is null or empty
   */
  public Document(String id, String data) {
    validateId(id);
    validateData(data);
    this.id = id;
    this.data = data;
  }

  /**
   * Returns the ID of the document.
   *
   * @return the ID of the document
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the data of the document.
   *
   * @return the data of the document
   */
  public String getData() {
    return data;
  }

  /**
   * Sets the ID of the document.
   *
   * @param id the new ID, must not be null or empty
   * @throws IllegalArgumentException if id is null or empty
   */
  public void setId(String id) {
    validateId(id);
    this.id = id;
  }

  /**
   * Sets the data of the document.
   *
   * @param data the new data, must not be null or empty
   * @throws IllegalArgumentException if data is null or empty
   */
  public void setData(String data) {
    validateData(data);
    this.data = data;
  }

  @Override
  public String toString() {
    return ("{\"id\": \"" + id + "\",\"data\":" + data + "}");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Document document = (Document) o;
    return id.equals(document.id) && data.equals(document.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, data);
  }

  private void validateId(String id) {
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("ID cannot be null or empty");
    }
  }

  private void validateData(String data) {
    if (data == null || data.isEmpty()) {
      throw new IllegalArgumentException("Data cannot be null or empty");
    }
  }
}