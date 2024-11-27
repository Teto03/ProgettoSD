package it.unimib.sd2024;

/**
 * Represents a request for user registration.
 */
public class UserRegistrationRequest {
  private String userId;
  private String name;
  private String surname;
  private String email;

  /**
   * Default constructor for creating an empty user registration request.
   */
  public UserRegistrationRequest() {
    // Intentionally left empty
  }

  /**
   * Constructs a user registration request with specified user ID, name, surname,
   * and email.
   *
   * @param userId  The unique identifier for the user.
   * @param name    The name of the user.
   * @param surname The surname of the user.
   * @param email   The email address of the user.
   */
  public UserRegistrationRequest(String userId, String name, String surname, String email) {
    this.userId = userId;
    this.name = name;
    this.surname = surname;
    this.email = email;
  }

  /**
   * Gets the user ID.
   *
   * @return The user ID.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user ID.
   *
   * @param userId The user ID to set.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Gets the name of the user.
   *
   * @return The name of the user.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the user.
   *
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the surname of the user.
   *
   * @return The surname of the user.
   */
  public String getSurname() {
    return surname;
  }

  /**
   * Sets the surname of the user.
   *
   * @param surname The surname to set.
   */
  public void setSurname(String surname) {
    this.surname = surname;
  }

  /**
   * Gets the email of the user.
   *
   * @return The email of the user.
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the email of the user.
   *
   * @param email The email to set.
   */
  public void setEmail(String email) {
    this.email = email;
  }
}