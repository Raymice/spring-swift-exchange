/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing;

import com.raymice.swift.configuration.RoutingConfig;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component
public abstract class DefaultRoute extends RouteBuilder {

  @Autowired private RoutingConfig routingConfig;
  private final String routeId;
  private String errorEndpoint;

  public DefaultRoute() {
    this.routeId = this.getClass().getSimpleName();
  }

  @PostConstruct
  void postConstruct() {
    this.errorEndpoint =
        URI.create(String.format("file:%s", routingConfig.getOutput().getError())).toString();
  }
}
