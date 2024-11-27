package it.unimib.sd2024;

/**
 * Represents a user's request to search for a domain.
 */
public class UserSearchRequest {
  private String domainId;
  private String userId;

  /**
   * Default constructor for creating an empty user search request.
   */
  public UserSearchRequest() {
    // Intentionally left blank
  }

  /**
   * Constructs a user search request with specified domain ID and user ID.
   *
   * @param domainId The ID of the domain being searched.
   * @param userId   The ID of the user making the request.
   */
  public UserSearchRequest(String domainId, String userId) {
    this.domainId = domainId;
    this.userId = userId;
  }

  /**
   * Gets the domain ID of the search request.
   *
   * @return The domain ID.
   */
  public String getDomainId() {
    return domainId;
  }

  /**
   * Sets the domain ID of the search request.
   *
   * @param domainId The domain ID to set.
   */
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  /**
   * Gets the user ID of the search request.
   *
   * @return The user ID.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user ID of the search request.
   *
   * @param userId The user ID to set.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }
}