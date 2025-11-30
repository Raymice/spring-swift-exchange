/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.event;

import static com.raymice.sse.utils.CamelUtils.getJMSMessageId;
import static com.raymice.sse.utils.CamelUtils.getQueueName;

import com.raymice.sse.configuration.mdc.annotation.ExchangeMDC;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.camel.Exchange;
import org.apache.camel.component.activemq.ActiveMQQueueEndpoint;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.component.file.GenericFileMessage;
import org.apache.camel.component.jms.JmsMessage;
import org.apache.camel.impl.event.ExchangeCompletedEvent;
import org.apache.camel.impl.event.ExchangeCreatedEvent;
import org.apache.camel.impl.event.ExchangeSendingEvent;
import org.apache.camel.impl.event.ExchangeSentEvent;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.apache.camel.util.TimeUtils;
import org.springframework.stereotype.Service;

/**
 * Event notifier implementation for auditing Camel exchanges.
 * <p>
 * This notifier tracks and logs various exchange events including:
 * <ul>
 * <li>Exchange creation</li>
 * <li>Exchange sending</li>
 * <li>Exchange sent completion</li>
 * <li>Exchange completion</li>
 * </ul>
 * <p>
 * It provides detailed logging for both file and ActiveMQ message exchanges,
 * including process IDs, paths, queue names, and timing information.
 *
 * @see EventNotifierSupport
 * @see ExchangeCreatedEvent
 * @see ExchangeSendingEvent
 * @see ExchangeSentEvent
 * @see ExchangeCompletedEvent
 */
@Slf4j
@Service
public class AuditEventNotifier extends EventNotifierSupport {

  @Override
  protected void doStart() throws Exception {
    // Configure which event types to ignore if desired
    setIgnoreCamelContextEvents(true);
    setIgnoreRouteEvents(true);
    setIgnoreServiceEvents(true);
  }

  @ExchangeMDC
  @Override
  public void notify(CamelEvent event) {
    switch (event) {
      case ExchangeCreatedEvent createdEvent -> {
        final Exchange exchange = createdEvent.getExchange();

        if (isInFileExchange(exchange)) {

          final String path =
              ((GenericFileMessage<?>) exchange.getIn()).getGenericFile().getAbsoluteFilePath();
          log.debug("📥 Received file (path={})", path);

        } else if (isInActiveMQExchange(exchange)) {

          final String queueName = getQueueName(exchange);
          final String messageId = getJMSMessageId(exchange);
          log.debug("📥 Received ActiveMQ message (queue={} messageId={})", queueName, messageId);
        }
      }

      case ExchangeSendingEvent sendingEvent -> {
        if (isOutFileExchange(sendingEvent)) {

          final String path = ((FileEndpoint) sendingEvent.getEndpoint()).getFile().getPath();
          log.debug("📤 Sending file (path={})", path);

        } else if (isOutActiveMQExchange(sendingEvent)) {

          final String queueName =
              ((ActiveMQQueueEndpoint) sendingEvent.getEndpoint()).getDestinationName();
          log.debug("📤 Sending ActiveMQ message (queue={})", queueName);
        }
      }

      case ExchangeSentEvent sentEvent -> {
        final String timeTaken = TimeUtils.printDuration(sentEvent.getTimeTaken(), true);

        if (isOutFileExchange(sentEvent)) {

          final String path = ((FileEndpoint) sentEvent.getEndpoint()).getFile().getPath();
          log.debug("✔️File sent in {} (path={})", timeTaken, path);

        } else if (isOutActiveMQExchange(sentEvent)) {

          final String queueName =
              ((ActiveMQQueueEndpoint) sentEvent.getEndpoint()).getDestinationName();
          log.debug("✔️ActiveMQ message sent in {} (queue={})", timeTaken, queueName);
        }
      }

      // case ExchangeCompletedEvent completedEvent -> {
      // Exchange exchange = completedEvent.getExchange();
      // String routeId = exchange.getFromRouteId();
      //
      // log.debug("✅Exchange completed for route: {}", routeId);
      // }

      default -> {
        // Handle unknown event types if needed
      }
    }
  }

  /**
   * Checks if the given exchange contains an incoming file message.
   *
   * @param exchange the Camel exchange to check
   * @return true if the exchange contains a GenericFileMessage, false otherwise
   */
  private boolean isInFileExchange(Exchange exchange) {
    return exchange.getIn() instanceof GenericFileMessage<?>;
  }

  /**
   * Checks if the given exchange contains an incoming ActiveMQ message.
   *
   * @param exchange the Camel exchange to check
   * @return true if the exchange contains a JmsMessage with an ActiveMQMessage as
   *         its JMS message,
   *         false otherwise
   */
  private boolean isInActiveMQExchange(Exchange exchange) {
    return exchange.getIn() instanceof JmsMessage
        && ((JmsMessage) exchange.getIn()).getJmsMessage() instanceof ActiveMQMessage;
  }

  /**
   * Checks if the given sending event is for a file exchange.
   *
   * @param sendingEvent the ExchangeSendingEvent to check
   * @return true if the event's endpoint is a FileEndpoint, false otherwise
   */
  private boolean isOutFileExchange(ExchangeSendingEvent sendingEvent) {
    return sendingEvent.getEndpoint() instanceof FileEndpoint;
  }

  /**
   * Checks if the given sending event is for an ActiveMQ exchange.
   *
   * @param sendingEvent the ExchangeSendingEvent to check
   * @return true if the event's endpoint is an ActiveMQQueueEndpoint, false
   *         otherwise
   */
  private boolean isOutActiveMQExchange(ExchangeSendingEvent sendingEvent) {
    return sendingEvent.getEndpoint() instanceof ActiveMQQueueEndpoint;
  }

  /**
   * Checks if the given sent event is for a file exchange.
   *
   * @param sentEvent the ExchangeSentEvent to check
   * @return true if the event's endpoint is a FileEndpoint, false otherwise
   */
  private boolean isOutFileExchange(ExchangeSentEvent sentEvent) {
    return sentEvent.getEndpoint() instanceof FileEndpoint;
  }

  /**
   * Checks if the given sent event is for an ActiveMQ exchange.
   *
   * @param sentEvent the ExchangeSentEvent to check
   * @return true if the event's endpoint is an ActiveMQQueueEndpoint, false
   *         otherwise
   */
  private boolean isOutActiveMQExchange(ExchangeSentEvent sentEvent) {
    return sentEvent.getEndpoint() instanceof ActiveMQQueueEndpoint;
  }
}
