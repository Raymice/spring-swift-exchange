/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.routing;

import com.raymice.sse.configuration.ApplicationConfig;
import com.raymice.sse.configuration.mdc.MdcService;
import com.raymice.sse.db.sevice.ProcessService;
import com.raymice.sse.exception.MalformedXmlException;
import com.raymice.sse.exception.UnsupportedException;
import com.raymice.sse.exception.WorkflowStatusException;
import com.raymice.sse.processor.ErrorProcessor;
import com.raymice.sse.processor.RetryProcessor;
import com.raymice.sse.processor.UnsupportedProcessor;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.rmi.UnexpectedException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Component
public abstract class DefaultRoute extends RouteBuilder {

  @Autowired private MdcService mdcService;
  @Autowired private ProcessService processService;
  @Autowired private ApplicationConfig applicationConfig;

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
    var routing = applicationConfig.getRouting();
    var fileOutput = routing.getFile().getOutput();

    this.errorFileEndpoint = URI.create(String.format("file:%s", fileOutput.getError())).toString();
    this.unsupportedFileEndpoint =
        URI.create(String.format("file:%s", fileOutput.getUnsupported())).toString();
    this.successFileEndpoint =
        URI.create(String.format("file:%s", fileOutput.getSuccess().getPath())).toString();
    this.deadLetterQueueEndpoint =
        URI.create(String.format("activemq:queue:%s", routing.getQueue().getDeadLetter()))
            .toString();
  }

  public void setupCommonExceptionHandling() {
    final ApplicationConfig.Redelivery redelivery = getApplicationConfig().getRedelivery();

    // Non retryable exception
    onException(UnsupportedException.class)
        .handled(true)
        .process(new UnsupportedProcessor(processService))
        .to(unsupportedFileEndpoint);

    // Non retryable exceptions
    onException(
            WorkflowStatusException.class,
            MalformedXmlException.class,
            UnexpectedException.class,
            NullPointerException.class)
        .handled(true)
        .process(new ErrorProcessor(processService, mdcService))
        .multicast()
        .to(deadLetterQueueEndpoint)
        .to(errorFileEndpoint);

    // Manage disconnection of PostgresDB
    onException(
            org.springframework.dao.DataAccessResourceFailureException.class,
            org.springframework.orm.jpa.JpaSystemException.class,
            org.springframework.transaction.CannotCreateTransactionException.class)
        // Unlimited retry
        .maximumRedeliveries(-1);

    // Retryable exceptions
    onException(Exception.class)
        .onRedelivery(new RetryProcessor())
        .maximumRedeliveries(redelivery.getMaximumRedeliveries())
        .redeliveryDelay(redelivery.getDelay())
        .useExponentialBackOff()
        .backOffMultiplier(redelivery.getBackOffMultiplier()) // Multiply the delay each time
        .maximumRedeliveryDelay(redelivery.getMaximumRedeliveryDelay())
        .useCollisionAvoidance()
        .handled(true)
        .process(new ErrorProcessor(processService, mdcService))
        .multicast()
        .to(deadLetterQueueEndpoint)
        .to(errorFileEndpoint);
  }
}
