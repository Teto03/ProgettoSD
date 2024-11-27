package it.unimib.sd2024;

public class UserOrderRequest {
  private String userId;
  private String domainId;
  private String orderDate;
  private String type;
  private String price;

  public UserOrderRequest() {
  }

  public UserOrderRequest(String userId, String domainId, String orderDate, String type, String price) {
    this.userId = userId;
    this.domainId = domainId;
    this.orderDate = orderDate;
    this.type = type;
    this.price = price;
  }

  public String getUserId() {
    return userId;
  }

  public String getDomainId() {
    return domainId;
  }

  public String getOrderDate() {
    return orderDate;
  }

  public String getType() {
    return type;
  }

  public String getPrice() {
    return price;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  public void setOrderDate(String orderDate) {
    this.orderDate = orderDate;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setPrice(String price) {
    this.price = price;
  }
}
