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
  private String errorFileEndpoint;
  private String unsupportedFileEndpoint;
  private String successFileEndpoint;
  private String deadLetterQueueEndpoint;

  public DefaultRoute() {
    this.routeId = this.getClass().getSimpleName();
  }

  @PostConstruct
  void postConstruct() {
    var fileOutput = routingConfig.getFile().getOutput();

    this.errorFileEndpoint = URI.create(String.format("file:%s", fileOutput.getError())).toString();
    this.unsupportedFileEndpoint =
        URI.create(String.format("file:%s", fileOutput.getUnsupported())).toString();
    this.successFileEndpoint =
        URI.create(String.format("file:%s", fileOutput.getSuccess().getPath())).toString();
    this.deadLetterQueueEndpoint =
        URI.create(String.format("activemq:queue:%s", routingConfig.getQueue().getDeadLetter()))
            .toString();
  }

  public void setupCommonExceptionHandling() {
    onException(UnsupportedException.class)
        .handled(true)
        .process(new UnsupportedProcessor(processService))
        .to(unsupportedFileEndpoint);

    // Non retryable exceptions
    onException(WorkflowStatusException.class, Exception.class)
        .handled(true)
        .process(new ErrorProcessor(processService))
        .multicast()
        .to(deadLetterQueueEndpoint)
        .to(errorFileEndpoint);
  }
}
