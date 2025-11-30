/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.routing.process.swift.mx;

import com.raymice.sse.db.entity.ProcessEntity;
import com.raymice.sse.processor.UpdateStatusProcessor;
import com.raymice.sse.routing.DefaultRoute;
import com.raymice.sse.utils.ActiveMqUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class PACS_008_001_08Route extends DefaultRoute {

  private final PACS_008_001_08RouteService pacs00800108RouteService;

  @Override
  public void configure() throws Exception {

    final var routeConfig = getApplicationConfig().getRouting();
    final String inputQueueUri = ActiveMqUtils.getQueueUri(routeConfig.getQueue().getPacs008());
    final String outputSuccessFilePath = getSuccessFileEndpoint();

    // Call the parent method to apply the shared error handling
    setupCommonExceptionHandling();

    // Take messages from ActiveMQ queue {{app.routing.queue.pacs008}}, validate and
    // route accordingly
    from(inputQueueUri)
        .routeId(getRouteId())
        .process(pacs00800108RouteService::logProcessor)
        .process(pacs00800108RouteService::setNameProcessor)
        .process(new UpdateStatusProcessor(getProcessService(), ProcessEntity.Status.COMPLETED))
        .to(outputSuccessFilePath)
        .end();
  }
}
