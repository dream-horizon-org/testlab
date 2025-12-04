package com.ascend.testlab.provider;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * CORS filter to add Access-Control headers to responses.
 *
 * <p>This filter handles Cross-Origin Resource Sharing (CORS) by adding the necessary headers to
 * allow cross-origin requests from web browsers.
 *
 * @author Anudeep Reddy
 * @version 1.0
 * @since 1.0
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

  /**
   * Adds CORS headers to the response.
   *
   * @param requestContext the request context
   * @param responseContext the response context
   * @throws IOException if an I/O exception occurs
   */
  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
    responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
    responseContext
        .getHeaders()
        .add(
            "Access-Control-Allow-Headers",
            "origin, content-type, accept, authorization, x-project-key, x-tenant-id");
    responseContext
        .getHeaders()
        .add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
    responseContext.getHeaders().add("Access-Control-Max-Age", "1209600");
  }
}
