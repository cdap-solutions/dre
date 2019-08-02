/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.re;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.cdap.cdap.api.NamespaceSummary;
import io.cdap.cdap.api.annotation.TransactionControl;
import io.cdap.cdap.api.annotation.TransactionPolicy;
import io.cdap.cdap.api.service.http.AbstractSystemHttpServiceHandler;
import io.cdap.cdap.api.service.http.HttpServiceRequest;
import io.cdap.cdap.api.service.http.HttpServiceResponder;
import io.cdap.cdap.spi.data.transaction.TransactionRunners;
import io.cdap.re.exception.StatusCodeException;
import io.cdap.re.exception.conflict.ConflictException;
import io.cdap.re.exception.notfound.NamespaceNotFoundException;
import io.cdap.re.exception.notfound.NotFoundException;
import org.apache.commons.jexl3.JexlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * This class {@link YARERulebookHandler} provides rules and rulebooks management service.
 */
public class YARERulebookHandler extends AbstractSystemHttpServiceHandler {

  private static final Logger LOG = LoggerFactory.getLogger(YARERulebookHandler.class);
  private static final Gson GSON = new Gson();

  @GET
  @Path("health")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void healthCheck(HttpServiceRequest request, HttpServiceResponder responder) {
    responder.sendStatus(HttpURLConnection.HTTP_OK);
  }

  /**
   * This API request is for validating the 'when' clause specified in the expression.
   *
   * @param request to gather information of the request.
   * @param responder to respond to the service request.
   */
  @POST
  @Path("validate-when")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void validateWhen(HttpServiceRequest request, HttpServiceResponder responder) {
    try {
      ServiceUtils.success(responder, "Valid when clause");
    } catch (JexlException e) {
      ServiceUtils.error(responder, HttpURLConnection.HTTP_BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      ServiceUtils.error(responder, HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
    }
  }

  @POST
  @Path("contexts/{context}/rules")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void create(HttpServiceRequest request, HttpServiceResponder responder,
                     @PathParam("context") String namespace) {
    respond(request, responder, () -> {
      RequestExtractor handler = new RequestExtractor(request);
      String content = handler.getContent(StandardCharsets.UTF_8);
      RuleRequest rule = GSON.fromJson(content, RuleRequest.class);
      NamespacedId namespacedId = getNamespacedId(namespace, rule.getId());
      TransactionRunners.run(getContext(), context -> {
        RulesDB rulesDB = RulesDB.get(context);
        rulesDB.createRule(namespacedId, rule);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message", String.format("Successfully created rule '%s'.", namespacedId.getId()));
      response.addProperty("count", 1);

      JsonArray values = new JsonArray();
      values.add(new JsonPrimitive(namespacedId.getId()));
      response.add("values", values);

      return response;
    });
  }

  @GET
  @Path("contexts/{context}/rules")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void rules(HttpServiceRequest request, HttpServiceResponder responder,
                    @PathParam("context") String namespace) {
    respond(request, responder, () -> {
      Namespace ns = getNamespace(namespace);
      List<Map<String, Object>> rules = TransactionRunners.run(getContext(), context -> {

        RulesDB rulesDB = RulesDB.get(context);
        return rulesDB.rules(ns);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message", "Successfully listed rules, testing .");
      response.addProperty("count", rules.size());
      response.add("values", GSON.toJsonTree(rules));

      return response;
    });
  }

  @PUT
  @Path("contexts/{context}/rules/{rule-id}")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void update(HttpServiceRequest request, HttpServiceResponder responder, @PathParam("context") String namespace,
                     @PathParam("rule-id") String id) {
    respond(request, responder, () -> {
      RequestExtractor handler = new RequestExtractor(request);
      String content = handler.getContent(StandardCharsets.UTF_8);
      RuleRequest rule = GSON.fromJson(content, RuleRequest.class);
      NamespacedId namespacedId = getNamespacedId(namespace, rule.getId());

      TransactionRunners.run(getContext(), context -> {
        RulesDB rulesDB = RulesDB.get(context);
        rulesDB.updateRule(namespacedId, rule);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message", String.format("Successfully updated rule '%s'.", namespacedId.getId()));
      response.addProperty("count", 1);

      JsonArray values = new JsonArray();
      values.add(new JsonPrimitive(namespacedId.getId()));
      response.add("values", values);

      return response;
    });
  }

  @GET
  @Path("contexts/{context}/rules/{rule-id}")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void retrieve(HttpServiceRequest request, HttpServiceResponder responder,
                       @PathParam("context") String namespace,
                       @PathParam("rule-id") String id, @QueryParam("format") String format) {
    respond(request, responder, () -> TransactionRunners.run(getContext(), context -> {
      NamespacedId namespacedId = getNamespacedId(namespace, id);
      RulesDB rulesDB = RulesDB.get(context);
      Map<String, Object> result = rulesDB.retrieveRule(namespacedId);

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message", String.format("Successfully retrieved rule '%s'.", namespacedId.getId()));
      response.addProperty("count", 1);

      if (format == null || format.equalsIgnoreCase("json")) {
        response.add("values", GSON.toJsonTree(result));
      } else {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(rulesDB.retrieveUsingRuleTemplate(id)));
        response.add("values", array);
      }

      return response;
    }));
  }

  @DELETE
  @Path("contexts/{context}/rules/{rule-id}")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void delete(HttpServiceRequest request, HttpServiceResponder responder, @PathParam("context") String namespace,
                     @PathParam("rule-id") String id) {
    respond(request, responder, () -> {
      NamespacedId namespacedId = getNamespacedId(namespace, id);
      TransactionRunners.run(getContext(), context -> {
        RulesDB rulesDB = RulesDB.get(context);
        rulesDB.deleteRule(namespacedId);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message", String.format("Successfully deleted rule '%s'", namespacedId.getId()));

      return response;
    });
  }

  @POST
  @Path("contexts/{context}/rulebooks")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void createRb(HttpServiceRequest request, HttpServiceResponder responder,
                       @PathParam("context") String namespace) {
    respond(request, responder, () -> TransactionRunners.run(getContext(), context -> {
      RequestExtractor handler = new RequestExtractor(request);
      String content = handler.getContent(StandardCharsets.UTF_8);
      RulesDB rulesDB = RulesDB.get(context);
      NamespacedId namespacedId;
      String id;

      if (handler.isContentType("application/json")) {
        RulebookRequest rb = GSON.fromJson(content, RulebookRequest.class);
        namespacedId = getNamespacedId(namespace, rb.getId());
        rulesDB.createRulebook(namespacedId, rb);
        id = namespacedId.getId();
      } else if (handler.isContentType("application/rules-engine")) {
        Reader reader = new StringReader(content);
        Compiler compiler = new RulebookCompiler();
        Rulebook rulebook = compiler.compile(reader);
        namespacedId = getNamespacedId(namespace, rulebook.getName());
        rulesDB.createRulebook(namespacedId, rulebook);
        id = namespacedId.getId();
      } else {
        String header = handler.getHeader(RequestExtractor.CONTENT_TYPE, "");
        throw new StatusCodeException("Unsupported content type " + header + ".",
                                      HttpURLConnection.HTTP_BAD_REQUEST);
      }

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty(
        "message", String.format("Successfully created rulebook '%s'.", namespacedId.getId()));
      response.addProperty("count", 1);

      JsonArray values = new JsonArray();
      values.add(new JsonPrimitive(id));
      response.add("values", values);

      return response;
    }));
  }

  @GET
  @Path("contexts/{context}/rulebooks")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void rulebooks(HttpServiceRequest request, HttpServiceResponder responder,
                        @PathParam("context") String namespace) {
    respond(request, responder, () -> {
      List<Map<String, Object>> rulebooks = TransactionRunners.run(getContext(), context -> {
        Namespace ns = getNamespace(namespace);
        RulesDB rulesDB = RulesDB.get(context);
        return rulesDB.rulebooks(ns);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message", "Successfully listed rulebooks.");
      response.addProperty("count", rulebooks.size());
      response.add("values", GSON.toJsonTree(rulebooks));

      return response;
    });
  }

  @PUT
  @Path("contexts/{context}/rulebooks/{rulebook-id}")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void updateRb(HttpServiceRequest request, HttpServiceResponder responder,
                       @PathParam("context") String namespace,
                       @PathParam("rulebook-id") String id) {
    respond(request, responder, () -> {
      RequestExtractor handler = new RequestExtractor(request);
      String content = handler.getContent(StandardCharsets.UTF_8);
      RulebookRequest rulebook = GSON.fromJson(content, RulebookRequest.class);
      NamespacedId namespacedId = getNamespacedId(namespace, id);
      TransactionRunners.run(getContext(), context -> {
        RulesDB rulesDB = RulesDB.get(context);
        rulesDB.updateRulebook(namespacedId, rulebook);

      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message", String.format("Successfully updated rule '%s'.", namespacedId.getId()));
      response.addProperty("count", 1);

      JsonArray values = new JsonArray();
      values.add(new JsonPrimitive(id));
      response.add("values", values);

      return response;
    });
  }

  @GET
  @Path("contexts/{context}/rulebooks/{rulebook-id}")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void retrieveRb(HttpServiceRequest request, HttpServiceResponder responder,
                         @PathParam("context") String namespace,
                         @PathParam("rulebook-id") String id) {
    respond(request, responder, () -> {
      NamespacedId namespacedId = getNamespacedId(namespace, id);
      String rulebookString = TransactionRunners.run(getContext(), context -> {
        RulesDB rulesDB = RulesDB.get(context);
        return rulesDB.generateRulebook(namespacedId);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message", String.format("Successfully generated rulebook '%s'.", namespacedId.getId()));
      response.addProperty("count", 1);

      JsonArray values = new JsonArray();
      values.add(new JsonPrimitive(rulebookString));
      response.add("values", values);

      return response;
    });
  }

  @GET
  @Path("contexts/{context}/rulebooks/{rulebook-id}/rules")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void retrieveRbRules(HttpServiceRequest request, HttpServiceResponder responder,
                              @PathParam("context") String namespace,
                              @PathParam("rulebook-id") String id) {
    respond(request, responder, () -> {
      NamespacedId namespacedId = getNamespacedId(namespace, id);
      JsonArray rules = TransactionRunners.run(getContext(), context -> {
        RulesDB rulesDB = RulesDB.get(context);
        return rulesDB.getRulebookRules(namespacedId);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message",
                           String.format("Successfully listed rules for the rulebook '%s'.", namespacedId.getId()));
      response.addProperty("count", rules.size());
      response.add("values", rules);

      return response;
    });
  }

  @DELETE
  @Path("contexts/{context}/rulebooks/{rulebook-id}")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void deleteRb(HttpServiceRequest request, HttpServiceResponder responder,
                       @PathParam("context") String namespace,
                       @PathParam("rulebook-id") String id) {
    respond(request, responder, () -> {
      NamespacedId namespacedId = getNamespacedId(namespace, id);
      TransactionRunners.run(getContext(), context -> {
        RulesDB rulesDB = RulesDB.get(context);
        rulesDB.deleteRulebook(namespacedId);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message",
                           String.format("Successfully deleted rulebook '%s'", namespacedId.getId()));

      return response;
    });
  }

  @PUT
  @Path("contexts/{context}/rulebooks/{rulebook-id}/rules/{rule-id}")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void addRuleToRb(HttpServiceRequest request, HttpServiceResponder responder,
                          @PathParam("context") String namespace,
                          @PathParam("rulebook-id") String rbId, @PathParam("rule-id") String id) {
    respond(request, responder, () -> {
      NamespacedId rulebookNamespacedId = getNamespacedId(namespace, rbId);
      NamespacedId ruleNamespacedId = getNamespacedId(namespace, id);
      TransactionRunners.run(getContext(), context -> {
        RulesDB rulesDB = RulesDB.get(context);
        rulesDB.addRuleToRulebook(rulebookNamespacedId, ruleNamespacedId);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message",
                           String.format("Successfully added rule '%s' to rulebook '%s'", ruleNamespacedId.getId(),
                                         rulebookNamespacedId.getId()));

      return response;
    });
  }

  @DELETE
  @Path("contexts/{context}/rulebooks/{rulebook-id}/rules/{rule-id}")
  @TransactionPolicy(value = TransactionControl.EXPLICIT)
  public void deleteRuleFromRb(HttpServiceRequest request, HttpServiceResponder responder,
                               @PathParam("context") String namespace,
                               @PathParam("rulebook-id") String rbId, @PathParam("rule-id") String id) {
    respond(request, responder, () -> {
      NamespacedId rulebookNamespacedId = getNamespacedId(namespace, rbId);
      NamespacedId ruleNamespacedId = getNamespacedId(namespace, id);
      TransactionRunners.run(getContext(), context -> {
        RulesDB rulesDB = RulesDB.get(context);
        rulesDB.removeRuleFromRulebook(rulebookNamespacedId, ruleNamespacedId);
      });

      JsonObject response = new JsonObject();
      response.addProperty("status", HttpURLConnection.HTTP_OK);
      response.addProperty("message",
                           String.format("Successfully removed rule '%s' to rulebook '%s'", ruleNamespacedId.getId(),
                                         rulebookNamespacedId.getId()));

      return response;

    });
  }

  private NamespacedId getNamespacedId(String namespace, String id) throws IOException, NamespaceNotFoundException {
    Namespace ns = getNamespace(namespace);

    return new NamespacedId(ns, id);
  }

  private Namespace getNamespace(String namespace) throws IOException, NamespaceNotFoundException {
    NamespaceSummary namespaceSummary;

    if (Contexts.SYSTEM.equals(namespace)) {
      return new Namespace(namespace, 0L);
    }

    namespaceSummary = getContext().getAdmin().getNamespaceSummary(namespace);

    if (namespaceSummary == null) {
      throw new NamespaceNotFoundException(namespace);
    }

    return new Namespace(namespaceSummary.getName(), namespaceSummary.getGeneration());
  }

  /**
   * Utility method for executing an endpoint with common error handling built in. A response will always be sent after
   * this method is called so the http responder should not be used after this. The endpoint logic should also not use
   * the responder in any way.
   *
   * If the callable throws a {@link StatusCodeException}, the exception's status code and message will be used to
   * create the response. If a {@link JsonSyntaxException} is thrown, a 400 response will be sent. If anything else if
   * thrown, a 500 response will be sent. If nothing is thrown, the result of the callable will be sent as json.
   *
   * @param request the http request
   * @param responder the http responder
   * @param callable the endpoint logic to run
   */
  private void respond(HttpServiceRequest request, HttpServiceResponder responder, Callable<JsonObject> callable) {
    try {
      JsonObject results = callable.call();
      ServiceUtils.sendJson(responder, HttpURLConnection.HTTP_OK, results.toString());
    } catch (NotFoundException nfe) {
      ServiceUtils.error(responder, HttpURLConnection.HTTP_NOT_FOUND, nfe.getMessage());
    } catch (ConflictException ce) {
      ServiceUtils.error(responder, HttpURLConnection.HTTP_CONFLICT, ce.getMessage());
    } catch (JsonSyntaxException jse) {
      ServiceUtils.error(responder, HttpURLConnection.HTTP_BAD_REQUEST, jse.getMessage());
    } catch (Exception e) {
      ServiceUtils.error(responder, HttpURLConnection.HTTP_INTERNAL_ERROR,
                         String.format("Error processing %s %s: %s", request.getMethod(), request.getRequestURI(),
                                       e.getMessage()));
    }
  }

}
