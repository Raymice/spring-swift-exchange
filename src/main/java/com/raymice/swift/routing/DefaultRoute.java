/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing;

import com.raymice.swift.configuration.RoutingConfig;
import com.raymice.swift.db.sevice.ProcessService;
import com.raymice.swift.exception.UnsupportedException;
import com.raymice.swift.exception.WorkflowStatusException;
import com.raymice.swift.processor.ErrorProcessor;
import com.raymice.swift.processor.UnsupportedProcessor;
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

  @Autowired private ProcessService processService;
  @Autowired private RoutingConfig routingConfig;
  private final String routeId;
  private String errorEndpoint;
  private String unsupportedEndpoint;

  public DefaultRoute() {
    this.routeId = this.getClass().getSimpleName();
  }

  @PostConstruct
  void postConstruct() {
    this.errorEndpoint =
        URI.create(String.format("file:%s", routingConfig.getOutput().getError())).toString();
    this.unsupportedEndpoint =
        URI.create(String.format("file:%s", routingConfig.getOutput().getUnsupported())).toString();
  }

  public void setupCommonExceptionHandling() {
    onException(UnsupportedException.class)
        .handled(true)
        .process(new UnsupportedProcessor(processService))
        .to(unsupportedEndpoint);

    onException(WorkflowStatusException.class, Exception.class)
        .handled(true)
        .process(new ErrorProcessor(processService))
        .to(errorEndpoint);
  }
}
