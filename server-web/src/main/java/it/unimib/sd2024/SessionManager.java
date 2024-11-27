package it.unimib.sd2024;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Provides utility methods for managing user sessions in a web application.
 * This class includes methods for creating a new session, retrieving the user
 * ID from a session,
 * and invalidating an existing session.
 */
public class SessionManager {

  /**
   * Creates a new session for the user or retrieves the existing session if one
   * already exists.
   * It also sets the user ID attribute in the session.
   *
   * @param request The HttpServletRequest from which to obtain the session.
   * @param userId  The user ID to set in the session.
   * @return The HttpSession object for the user.
   */
  public static HttpSession createSession(HttpServletRequest request, String userId) {
    HttpSession session = request.getSession(true);
    session.setAttribute("userId", userId);
    return session;
  }

  /**
   * Retrieves the user ID attribute from the provided HttpSession.
   *
   * @param session The HttpSession from which to retrieve the user ID.
   * @return The user ID as a String. Returns null if the attribute is not found.
   */
  public static String getUserId(HttpSession session) {
    return (String) session.getAttribute("userId");
  }

  /**
   * Invalidates the existing session if it exists. This method does not create a
   * new session.
   *
   * @param request The HttpServletRequest from which to obtain the session to
   *                invalidate.
   */
  public static void invalidateSession(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
  }
}
