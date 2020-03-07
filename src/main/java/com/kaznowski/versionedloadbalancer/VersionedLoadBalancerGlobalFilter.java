package com.kaznowski.versionedloadbalancer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

/**
 * Based on {@link org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter}
 * <p>
 * Also includes the implementation of {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer}
 */
public class VersionedLoadBalancerGlobalFilter implements GlobalFilter, Ordered {

  private static final Log log = LogFactory.getLog(ReactiveLoadBalancerClientFilter.class);

  private static final int LOAD_BALANCER_CLIENT_FILTER_ORDER = 10150;

  private final LoadBalancerClientFactory clientFactory;

  private LoadBalancerProperties properties;

  private final AtomicInteger position;

  private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

  private final String serviceId;

  public VersionedLoadBalancerGlobalFilter(LoadBalancerClientFactory clientFactory,
                                           LoadBalancerProperties properties,
                                           // RoundRobinLoadBalancer dependencies
                                           ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                           int seed,
                                           String serviceId
  ) {
    this.clientFactory = clientFactory;
    this.properties = properties;
    this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    this.serviceId = serviceId;
    this.position = new AtomicInteger(seed);
  }

  @Override
  public int getOrder() {
    return LOAD_BALANCER_CLIENT_FILTER_ORDER;
  }

  @Override
  @SuppressWarnings("Duplicates")
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
    String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);
    if (url == null
            || (!"lb".equals(url.getScheme()) && !"lb".equals(schemePrefix))) {
      return chain.filter(exchange);
    }
    // preserve the original url
    addOriginalRequestUrl(exchange, url);

    if (log.isTraceEnabled()) {
      log.trace(ReactiveLoadBalancerClientFilter.class.getSimpleName()
              + " url before: " + url);
    }

    return choose(url).doOnNext(response -> {

      if (!response.hasServer()) {
        throw NotFoundException.create(properties.isUse404(),
                "Unable to find instance for " + url.getHost());
      }

      URI uri = exchange.getRequest().getURI();

      // if the `lb:<scheme>` mechanism was used, use `<scheme>` as the default,
      // if the loadbalancer doesn't provide one.
      String overrideScheme = null;
      if (schemePrefix != null) {
        overrideScheme = url.getScheme();
      }

      DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(
              response.getServer(), overrideScheme);

      URI requestUrl = LoadBalancerUriTools.reconstructURI(serviceInstance, uri);

      if (log.isTraceEnabled()) {
        log.trace("LoadBalancerClientFilter url chosen: " + requestUrl);
      }
      exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
    }).then(chain.filter(exchange));
  }

  /**
   * This method is the implementation of {@link RoundRobinLoadBalancer}
   *
   * @return the instance that will be used to connect
   * @param url the url requested used to qualify versioned instances
   */
  private Mono<Response<ServiceInstance>> choose(URI url) {
    // TODO: move supplier to Request?
    // Temporary conditional logic till deprecated members are removed.
    ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
            .getIfAvailable(NoopServiceInstanceListSupplier::new);
    return supplier.get().next().map(this::getInstanceResponse);
  }

  private Response<ServiceInstance> getInstanceResponse(
          List<ServiceInstance> instances) {
    if (instances.isEmpty()) {
      log.warn("No servers available for service: " + this.serviceId);
      return new EmptyResponse();
    }
    // TODO: enforce order?
    int pos = Math.abs(this.position.incrementAndGet());

    ServiceInstance instance = instances.get(pos % instances.size());

    return new DefaultResponse(instance);
  }
}
