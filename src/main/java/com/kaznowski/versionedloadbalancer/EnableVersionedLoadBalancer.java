package com.kaznowski.versionedloadbalancer;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(VersionedLoadBalancerConfiguration.class)
@interface EnableVersionedLoadBalancer {
}
