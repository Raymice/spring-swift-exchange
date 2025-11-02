/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.event;

import static com.raymice.swift.utils.CamelUtils.getJMSMessageId;
import static com.raymice.swift.utils.CamelUtils.getQueueName;
import static com.raymice.swift.utils.CamelUtils.getUuid;

import com.raymice.swift.utils.StringUtils;
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

@Slf4j
public class AuditEventNotifier extends EventNotifierSupport {

  @Override
  protected void doStart() throws Exception {
    // Configure which event types to ignore if desired
    setIgnoreCamelContextEvents(true);
    setIgnoreRouteEvents(true);
    setIgnoreServiceEvents(true);
  }

  @Override
  public void notify(CamelEvent event) {
    if (event instanceof ExchangeCreatedEvent createdEvent) {
      final Exchange exchange = createdEvent.getExchange();
      final String uuid = StringUtils.unknownIfBlank(getUuid(exchange));

      if (isInFileExchange(exchange)) {
        final String path =
            ((GenericFileMessage<?>) exchange.getIn()).getGenericFile().getAbsoluteFilePath();
        log.info("📥 Received file (uuid={} path={})", uuid, path);

      } else if (isInActiveMQExchange(exchange)) {
        final String queueName = getQueueName(exchange);
        final String messageId = getJMSMessageId(exchange);
        log.info(
            "📥 Received ActiveMQ message (uuid={} queue={} messageId={})",
            uuid,
            queueName,
            messageId);
      }

    } else if (event instanceof ExchangeSendingEvent sendingEvent) {
      final Exchange exchange = sendingEvent.getExchange();
      final String uuid = getUuid(exchange);

      if (isOutFileExchange(sendingEvent)) {
        final String path = ((FileEndpoint) sendingEvent.getEndpoint()).getFile().getPath();
        log.info("📤 Sending file (uuid={} path={})", uuid, path);

      } else if (isOutActiveMQExchange(sendingEvent)) {
        final String queueName =
            ((ActiveMQQueueEndpoint) sendingEvent.getEndpoint()).getDestinationName();
        log.info("📤 Sending ActiveMQ message (uuid={} queue={})", uuid, queueName);
      }

    } else if (event instanceof ExchangeSentEvent sentEvent) {
      final Exchange exchange = sentEvent.getExchange();
      final String timeTaken = TimeUtils.printDuration(sentEvent.getTimeTaken(), true);
      final String uuid = getUuid(exchange);

      if (isOutFileExchange(sentEvent)) {
        final String path = ((FileEndpoint) sentEvent.getEndpoint()).getFile().getPath();
        log.info("✔️File sent in {} (uuid={} path={})", timeTaken, uuid, path);

      } else if (isOutActiveMQExchange(sentEvent)) {
        final String queueName =
            ((ActiveMQQueueEndpoint) sentEvent.getEndpoint()).getDestinationName();
        log.info("✔️ActiveMQ message sent in {} (uuid={} queue={})", timeTaken, uuid, queueName);
      }

    } else if (event instanceof ExchangeCompletedEvent completedEvent) {
      Exchange exchange = completedEvent.getExchange();
      String routeId = exchange.getFromRouteId();
      final String uuid = getUuid(exchange);

      log.info("✅Exchange completed for route: {} (uuid={})", routeId, uuid);
    }
  }

  private boolean isInFileExchange(Exchange exchange) {
    return exchange.getIn() instanceof GenericFileMessage<?>;
  }

  private boolean isInActiveMQExchange(Exchange exchange) {
    return exchange.getIn() instanceof JmsMessage
        && ((JmsMessage) exchange.getIn()).getJmsMessage() instanceof ActiveMQMessage;
  }

  private boolean isOutFileExchange(ExchangeSendingEvent sendingEvent) {
    return sendingEvent.getEndpoint() instanceof FileEndpoint;
  }

  private boolean isOutActiveMQExchange(ExchangeSendingEvent sendingEvent) {
    return sendingEvent.getEndpoint() instanceof ActiveMQQueueEndpoint;
  }

  private boolean isOutFileExchange(ExchangeSentEvent sentEvent) {
    return sentEvent.getEndpoint() instanceof FileEndpoint;
  }

  private boolean isOutActiveMQExchange(ExchangeSentEvent sentEvent) {
    return sentEvent.getEndpoint() instanceof ActiveMQQueueEndpoint;
  }
}
