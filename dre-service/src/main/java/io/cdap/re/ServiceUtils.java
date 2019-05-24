/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.re;

import com.google.gson.JsonObject;
import io.cdap.cdap.api.service.http.HttpServiceResponder;

import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * This class provides utility services to the service in this package.
 */
public final class ServiceUtils {
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
    sendJson(responder, status, error.toString());
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
