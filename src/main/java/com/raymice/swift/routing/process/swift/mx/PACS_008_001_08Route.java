/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.process.swift.mx;

import static com.raymice.swift.utils.CamelUtils.getProcessId;
import static com.raymice.swift.utils.CamelUtils.getQueueName;
import static com.raymice.swift.utils.CamelUtils.getUpdatedFileName;
import static com.raymice.swift.utils.CamelUtils.setFileName;

import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.processor.UpdateStatusProcessor;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PACS_008_001_08Route extends DefaultRoute {

  @Override
  public void configure() throws Exception {

    final var routeConfig = getApplicationConfig().getRouting();
    final String inputQueueUri = ActiveMqUtils.getQueueUri(routeConfig.getQueue().getPacs008());
    final String outputSuccessFilePath = getSuccessFileEndpoint();

    // Call the parent method to apply the shared error handling
    setupCommonExceptionHandling();

    // Take messages from ActiveMQ queue {{app.routing.queue.pacs008}}, validate and route
    // accordingly
    from(inputQueueUri)
        .routeId(getRouteId())
        .process(logProcessor)
        .process(setNameProcessor)
        .process(new UpdateStatusProcessor(getProcessService(), ProcessEntity.Status.COMPLETED))
        .to(outputSuccessFilePath)
        .end();
  }

  private final org.apache.camel.Processor logProcessor =
      exchange -> {
        final String processId = getProcessId(exchange);
        final String queueName = getQueueName(exchange);
        log.info(
            "📥 Receiving pacs.008.001.08 message from ActiveMQ (queue={} processId={})",
            queueName,
            processId);
        log.warn(
            "Need to implement PACS.008.001.08 specific processing here... (processId={}})",
            processId);
      };

  private final org.apache.camel.Processor setNameProcessor =
      exchange -> {
        // Set file name based on header
        String fileName = getUpdatedFileName(exchange);
        // Set file name for output
        setFileName(exchange, fileName);
      };
}
