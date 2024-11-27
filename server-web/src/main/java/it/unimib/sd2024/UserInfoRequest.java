package it.unimib.sd2024;

/**
 * Represents a request for retrieving user information.
 * This class encapsulates the necessary data for identifying the user whose
 * information is requested.
 */
public class UserInfoRequest {
  /**
   * The unique identifier for the user.
   */
  private String userId;

  /**
   * Default constructor for creating an empty UserInfoRequest.
   */
  public UserInfoRequest() {
  }

  /**
   * Constructs a new UserInfoRequest with the specified user ID.
   *
   * @param userId The unique identifier for the user.
   */
  public UserInfoRequest(String userId) {
    this.userId = userId;
  }

  /**
   * Retrieves the user ID associated with this request.
   *
   * @return The user ID.
   */
  public String getUserId() {
    return userId;
  }
}