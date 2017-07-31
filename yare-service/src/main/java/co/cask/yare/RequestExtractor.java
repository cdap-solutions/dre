package co.cask.yare;

import co.cask.cdap.api.service.http.HttpServiceRequest;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * This class {@link RequestExtractor} provides utility functions for extracting different aspects of the request.
 * It provides functionality to extract headers and content.
 */
public final class RequestExtractor {
  private final HttpServiceRequest request;
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String CHARSET = "charset";


  public RequestExtractor(HttpServiceRequest request) {
    this.request = request;
  }

  /**
   * Checks if the request has a HTTP header.
   *
   * @param name of the header to be checked.
   * @return true if header is found, false otherwise.
   */
  public boolean hasHeader(String name) {
    String header = request.getHeader(name);
    return (header == null ? false : true);
  }

  /**
   * Extracts the HTTP header, if the header is not present, then default value is returned.
   *
   * @param name of the HTTP header to be extracted.
   * @param defaultValue value to returned if header doesn't exist.
   * @return value defined for the header.
   */
  public <T> T getHeader(String name, String defaultValue) {
    String header = request.getHeader(name);
    return (T) (header == null ? defaultValue : header);
  }

  /**
   * @return Content as received by the HTTP multipart/form body.
   */
  public byte[] getContent() {
    ByteBuffer content = request.getContent();
    if (content != null && content.hasRemaining()) {
      byte[] bytes = new byte[content.remaining()];
      content.get(bytes);
      return bytes;
    }
    return null;
  }

  /**
   * Returns the content by converting it to UNICODE from the provided charset.
   *
   * @param charset of the content being extracted and converted to UNICODE.
   * @return UNICODE representation of the content, else null.
   */
  public String getContent(Charset charset) {
    ByteBuffer content = request.getContent();
    if (content != null && content.hasRemaining()) {
      return charset.decode(content).toString();
    }
    return null;
  }

  public String getContent(String charset) {
    return getContent(Charset.forName(charset));
  }

  /**
   * Checks if the 'Content-Type' matches expected.
   *
   * @param expectedType to be checked for content type.
   * @return true if it matches, false if it's not present or doesn't match expected.
   */
  public boolean isContentType(String expectedType) {
    if (hasHeader(CONTENT_TYPE)) {
      String header = getHeader(CONTENT_TYPE, null);
      return (header != null && !header.equalsIgnoreCase(expectedType)) ? false : true;
    }
    return false;
  }


}
