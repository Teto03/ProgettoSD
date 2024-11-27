package it.unimib.sd2024;

/**
 * Represents a request for user login, encapsulating the necessary information
 * for a login attempt.
 */
public class UserLoginRequest {
  /**
   * The unique identifier for the user attempting to log in.
   */
  private String userId;

  /**
   * Default constructor for creating an empty user login request.
   */
  public UserLoginRequest() {
  }

  /**
   * Constructs a new user login request with the specified user ID.
   *
   * @param userId The unique identifier for the user attempting to log in.
   */
  public UserLoginRequest(String userId) {
    this.userId = userId;
  }

  /**
   * Retrieves the user ID associated with this login request.
   *
   * @return The user ID.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user ID for this login request.
   *
   * @param userId The unique identifier for the user.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }
}