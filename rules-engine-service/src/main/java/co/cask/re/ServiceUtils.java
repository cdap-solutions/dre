package co.cask.re;

import co.cask.cdap.api.service.http.HttpServiceResponder;
import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.util.encoders.Hex;

import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * This class provides utility services to the service in this package.
 */
public final class ServiceUtils {

  /**
   * Generates a MD5 hash for a given string.
   *
   * This implementation is based on Bouncycastle. So, you would need to initialize the security
   * provider to use {@link org.bouncycastle.jce.provider.BouncyCastleProvider}.
   *
   * <code>
   *   Security.addProvider(new BouncyCastleProvider());
   * </code>
   * @param value to be converted to MD5.
   * @return String representation of MD5.
   */
  public static String generateMD5(String value) {
    byte[] input = value.getBytes(Charsets.UTF_8);
    MD5Digest md5Digest = new MD5Digest();
    md5Digest.update(input, 0, input.length);
    byte[] output = new byte[md5Digest.getDigestSize()];
    md5Digest.doFinal(output, 0);
    return new String(Hex.encode(output));
  }

  /**
   * Sends the error response back to client.
   *
   * @param responder to respond to the service request.
   * @param message to be included as part of the error
   */
  public static final void error(HttpServiceResponder responder, int status, String message) {
    JsonObject error = new JsonObject();
    error.addProperty("status", status);
    error.addProperty("message", message);
    sendJson(responder, HttpURLConnection.HTTP_INTERNAL_ERROR, error.toString());
  }

  /**
   * Sends the error response back to client for not Found.
   *
   * @param responder to respond to the service request.
   * @param message to be included as part of the error
   */
  public static final void notFound(HttpServiceResponder responder, String message) {
    JsonObject error = new JsonObject();
    error.addProperty("status", HttpURLConnection.HTTP_NOT_FOUND);
    error.addProperty("message", message);
    sendJson(responder, HttpURLConnection.HTTP_NOT_FOUND, error.toString());
  }

  /**
   * Returns a Json response back to client.
   *
   * @param responder to respond to the service request.
   * @param status code to be returned to client.
   * @param body to be sent back to client.
   */
  public static final void sendJson(HttpServiceResponder responder, int status, String body) {
    responder.send(status, ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)),
                   "application/json", new HashMap<String, String>());
  }

  /**
   * Sends the success response back to client.
   *
   * @param responder to respond to the service request.
   * @param message to be included as part of the error
   */
  public static final void success(HttpServiceResponder responder, String message) {
    JsonObject error = new JsonObject();
    error.addProperty("status", HttpURLConnection.HTTP_OK);
    error.addProperty("message", message);
    sendJson(responder, HttpURLConnection.HTTP_OK, error.toString());
  }
}
