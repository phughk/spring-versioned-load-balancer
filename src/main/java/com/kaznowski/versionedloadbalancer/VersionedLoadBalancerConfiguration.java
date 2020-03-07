package com.kaznowski.versionedloadbalancer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VersionedLoadBalancerConfiguration {
  @Bean
  VersionedLoadBalancerGlobalFilter versionedLoadBalancerGlobalFilter(LoadBalancerClientFactory loadBalancerClientFactory,
                                                                      LoadBalancerProperties loadBalancerProperties,
                                                                      ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                                                      int seed, String serviceId) {
    return new VersionedLoadBalancerGlobalFilter(loadBalancerClientFactory, loadBalancerProperties, serviceInstanceListSupplierProvider,
        seed, serviceId);
  }
}
