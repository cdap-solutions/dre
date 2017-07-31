package co.cask.yare;

import co.cask.cdap.etl.api.Lookup;
import co.cask.cdap.etl.api.StageMetrics;
import co.cask.cdap.etl.api.TransformContext;
import co.cask.wrangler.api.ExecutorContext;
import co.cask.wrangler.api.TransientStore;

import java.net.URL;
import java.util.Map;

/**
 * Class description here.
 */
class RulesEngineContext implements ExecutorContext {
  private final Environment environment;
  private final TransformContext context;

  private final TransientStore store;
  private StageMetrics metrics;
  private String name;
  private Map<String, String> properties;

  RulesEngineContext(Environment environment, TransformContext context, TransientStore store) {
    this.environment = environment;
    this.metrics = context.getMetrics();
    this.name = context.getStageName();
    this.properties = context.getPluginProperties().getProperties();
    this.context = context;
    this.store = store;
  }

  /**
   * @return Environment this context is prepared for.
   */
  @Override
  public Environment getEnvironment() {
    return environment;
  }

  /**
   * @return Measurements context.
   */
  @Override
  public StageMetrics getMetrics() {
    return metrics;
  }

  /**
   * @return Context name.
   */
  @Override
  public String getContextName() {
    return name;
  }

  /**
   * @return Properties associated with run and pipeline.
   */
  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Returns a valid service url.
   *
   * @param applicationId id of the application to which a service url.
   * @param serviceId     id of the service within application.
   * @return URL if service exists, else null.
   */
  @Override
  public URL getService(String applicationId, String serviceId) {
    return context.getServiceURL(applicationId, serviceId);
  }

  @Override
  public TransientStore getTransientStore() {
    return store;
  }

  /**
   * Provides a handle to dataset for lookup.
   *
   * @param s name of the dataset.
   * @param map properties associated with dataset.
   * @return handle to dataset for lookup.
   */
  @Override
  public <T> Lookup<T> provide(String s, Map<String, String> map) {
    return context.provide(s, map);
  }
}

