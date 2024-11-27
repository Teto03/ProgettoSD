package it.unimib.sd2024;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * A filter that adds Cross-Origin Resource Sharing (CORS) headers to the
 * response.
 * This allows client applications from other domains to interact with the
 * server.
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

  /**
   * Adds CORS headers to the response context. This method is invoked for every
   * HTTP response.
   *
   * @param requestContext  The request context.
   * @param responseContext The response context where the CORS headers are added.
   */
  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
    responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
    responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
    responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
  }
}