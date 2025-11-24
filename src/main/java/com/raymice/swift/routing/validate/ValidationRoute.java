/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.routing.validate;

import static com.raymice.swift.constant.Mx.PACS_008_001_08;

import com.raymice.swift.configuration.ApplicationConfig;
import com.raymice.swift.constant.Header;
import com.raymice.swift.db.entity.ProcessEntity;
import com.raymice.swift.processor.UpdateStatusProcessor;
import com.raymice.swift.routing.DefaultRoute;
import com.raymice.swift.utils.ActiveMqUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ValidationRoute extends DefaultRoute {

  private final ValidationRouteService validationRouteService;

  @Override
  public void configure() {
    final ApplicationConfig.Routing routing = getApplicationConfig().getRouting();

    final String inputQueueUri = ActiveMqUtils.getQueueUri(routing.getQueue().getValidator());
    final String outputQueueUri = ActiveMqUtils.getQueueUri(routing.getQueue().getPacs008());

    // Call the parent method to apply the shared error handling
    setupCommonExceptionHandling();

    // Take messages from ActiveMQ queue {{app.routing.queue.input}}, validate and route accordingly
    from(inputQueueUri)
        .routeId(getRouteId())
        .process(validationRouteService::parseAndValidate)
        .choice()
        .when(header(Header.CUSTOM_HEADER_MX_ID).isEqualTo(PACS_008_001_08))
        .process(validationRouteService::logProcessor)
        .process(new UpdateStatusProcessor(getProcessService(), ProcessEntity.Status.VALIDATED))
        .to(outputQueueUri) // Forward to next queue
        .otherwise()
        .process(validationRouteService::unsupportedProcessor)
        .endChoice()
        .end();
  }
}
