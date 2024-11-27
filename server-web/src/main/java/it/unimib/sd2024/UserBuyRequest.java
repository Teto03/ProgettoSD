package it.unimib.sd2024;

/**
 * Represents a request to buy or renew a domain.
 * This class encapsulates all necessary information for processing a domain
 * purchase or renewal,
 * including user and payment details, as well as domain-specific information.
 */
public class UserBuyRequest {
  private String userId;
  private String domainId;

  private String cardOwnerName;
  private String cardOwnerSurname;
  private String cardNumber;
  private String cardExpirationDate;
  private String CVV;

  private String duration;
  private String currentDate;
  private String expirationDate;

  /**
   * Default constructor for creating an empty UserBuyRequest.
   */
  public UserBuyRequest() {
  }

  /**
   * Constructs a new UserBuyRequest with specified details.
   *
   * @param userId             The ID of the user making the request.
   * @param domainId           The ID of the domain to be purchased or renewed.
   * @param cardOwnerName      The name of the card owner.
   * @param cardOwnerSurname   The surname of the card owner.
   * @param cardNumber         The card number.
   * @param cardExpirationDate The expiration date of the card.
   * @param CVV                The CVV of the card.
   * @param duration           The duration for which the domain is being
   *                           purchased or renewed.
   * @param price              The price of the domain purchase or renewal.
   */
  public UserBuyRequest(String userId, String domainId, String cardOwnerName, String cardOwnerSurname,
      String cardNumber, String cardExpirationDate, String CVV, String duration, String price, String type) {
    this.userId = userId;
    this.domainId = domainId;
    this.cardOwnerName = cardOwnerName;
    this.cardOwnerSurname = cardOwnerSurname;
    this.cardNumber = cardNumber;
    this.cardExpirationDate = cardExpirationDate;
    this.CVV = CVV;
    this.duration = duration;
  }

  // Getters and setters with appropriate Javadoc comments

  /**
   * Gets the user ID.
   *
   * @return The user ID.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Gets the domain ID.
   *
   * @return The domain ID.
   */
  public String getDomainId() {
    return domainId;
  }

  /**
   * Gets the card owner's name.
   *
   * @return The card owner's name.
   */
  public String getCardOwnerName() {
    return cardOwnerName;
  }

  /**
   * Gets the card owner's surname.
   *
   * @return The card owner's surname.
   */
  public String getCardOwnerSurname() {
    return cardOwnerSurname;
  }

  /**
   * Gets the card number.
   *
   * @return The card number.
   */
  public String getCardNumber() {
    return cardNumber;
  }

  /**
   * Gets the card expiration date.
   *
   * @return The card expiration date.
   */
  public String getCardExpirationDate() {
    return cardExpirationDate;
  }

  /**
   * Gets the CVV of the card.
   *
   * @return The CVV.
   */
  public String getCVV() {
    return CVV;
  }

  /**
   * Gets the duration of the domain purchase or renewal.
   *
   * @return The duration.
   */
  public String getDuration() {
    return duration;
  }

  /**
   * Gets the current date.
   *
   * @return The current date.
   */
  public String getCurrentDate() {
    return currentDate;
  }

  /**
   * Gets the expiration date of the domain.
   *
   * @return The expiration date.
   */
  public String getExpirationDate() {
    return expirationDate;
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
   * Sets the domain ID.
   *
   * @param domainId The domain ID to set.
   */
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  /**
   * Sets the card owner's name.
   *
   * @param cardOwnerName The card owner's name to set.
   */
  public void setCardOwnerName(String cardOwnerName) {
    this.cardOwnerName = cardOwnerName;
  }

  /**
   * Sets the card owner's surname.
   *
   * @param cardOwnerSurname The card owner's surname to set.
   */
  public void setCardOwnerSurname(String cardOwnerSurname) {
    this.cardOwnerSurname = cardOwnerSurname;
  }

  /**
   * Sets the card number.
   *
   * @param cardNumber The card number to set.
   */
  public void setCardNumber(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  /**
   * Sets the card expiration date.
   *
   * @param cardExpirationDate The card expiration date to set.
   */
  public void setCardExpirationDate(String cardExpirationDate) {
    this.cardExpirationDate = cardExpirationDate;
  }

  /**
   * Sets the CVV of the card.
   *
   * @param CVV The CVV to set.
   */
  public void setCVV(String CVV) {
    this.CVV = CVV;
  }

  /**
   * Sets the duration of the domain purchase or renewal.
   *
   * @param duration The duration to set.
   */
  public void setDuration(String duration) {
    this.duration = duration;
  }

  /**
   * Sets the current date.
   *
   * @param currentDate The current date to set.
   */
  public void setCurrentDate(String currentDate) {
    this.currentDate = currentDate;
  }

  /**
   * Sets the expiration date of the domain.
   *
   * @param expirationDate The expiration date to set.
   */
  public void setExpirationDate(String expirationDate) {
    this.expirationDate = expirationDate;
  }
}