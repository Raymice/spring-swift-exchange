/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.utils;

import static com.raymice.sse.constant.Header.CUSTOM_PATTERN;
import static org.apache.camel.component.jms.JmsConstants.JMS_HEADER_DESTINATION;
import static org.apache.camel.component.jms.JmsConstants.JMS_HEADER_MESSAGE_ID;

import com.raymice.sse.constant.Header;
import com.raymice.sse.db.entity.ProcessEntity;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.zip.CRC32;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Utility class for Apache Camel related operations.
 */
public class CamelUtils {

  /**
   * Calculate CRC32 hash of a file from Camel Exchange.
   * Using CRC32 for file hashing due to its speed and low memory usage,
   * making it suitable for small to medium-sized files where performance is
   * critical.
   *
   * @param exchange Camel Exchange containing the file
   * @return CRC32 hash value
   * @throws Exception if an error occurs during reading the file
   */
  public static long calculateFileHash(Exchange exchange) throws Exception {
    Message message = exchange.getIn();
    CRC32 crc = new CRC32();
    try (InputStream is = new BufferedInputStream((message.getBody(InputStream.class)))) {
      byte[] buffer = new byte[65536]; // 64KB buffer
      int bytesRead;
      while ((bytesRead = is.read(buffer)) != -1) {
        crc.update(buffer, 0, bytesRead);
      }
    }
    return crc.getValue();
  }

  /**
   * Get process ID from Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @return process ID
   */
  public static String getProcessId(@NotNull Exchange exchange) {
    return getHeader(exchange, Header.CUSTOM_HEADER_PROCESS_ID);
  }

  /**
   * Set process ID in Camel Exchange header.
   *
   * @param exchange  Camel Exchange
   * @param processId process ID
   */
  public static void setProcessId(@NotNull Exchange exchange, @NotNull Long processId) {
    Validate.notNull(processId, "ProcessId must not be null");
    setHeader(exchange, Header.CUSTOM_HEADER_PROCESS_ID, String.valueOf(processId));
  }

  /**
   * Get original file name from Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @return original file name
   */
  public static String getOriginalFileName(@NotNull Exchange exchange)
      throws IllegalArgumentException {
    return getHeader(exchange, Header.CUSTOM_HEADER_ORIGINAL_FILE_NAME);
  }

  /**
   * Get updated file name from Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @return updated file name
   */
  public static String getUpdatedFileName(@NotNull Exchange exchange)
      throws IllegalArgumentException {
    return getHeader(exchange, Header.CUSTOM_HEADER_UPDATED_FILE_NAME);
  }

  /**
   * Set original file name in Camel Exchange header.
   *
   * @param exchange         Camel Exchange
   * @param originalFileName original file name
   */
  public static void setOriginalFileName(
      @NotNull Exchange exchange, @NotBlank String originalFileName) {
    setHeader(exchange, Header.CUSTOM_HEADER_ORIGINAL_FILE_NAME, originalFileName);
  }

  /**
   * Set updated file name in Camel Exchange header.
   *
   * @param exchange        Camel Exchange
   * @param updatedFileName updated file name
   */
  public static void setUpdatedFileName(
      @NotNull Exchange exchange, @NotBlank String updatedFileName) {
    setHeader(exchange, Header.CUSTOM_HEADER_UPDATED_FILE_NAME, updatedFileName);
  }

  /**
   * Get file name from Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @return file name
   */
  public static String getFileName(@NotNull Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Exchange.FILE_NAME);
  }

  /**
   * Set file name in Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @param fileName file name
   */
  public static void setFileName(@NotNull Exchange exchange, @NotBlank String fileName) {
    setHeader(exchange, Exchange.FILE_NAME, fileName);
  }

  /**
   * Get JMS Queue name from Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @return JMS Queue name
   */
  public static String getQueueName(@NotNull Exchange exchange) {
    return getHeader(exchange, JMS_HEADER_DESTINATION);
  }

  /**
   * Set MX ID in Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @param mxId     MX ID
   */
  public static void setMxId(@NotNull Exchange exchange, @NotBlank String mxId) {
    setHeader(exchange, Header.CUSTOM_HEADER_MX_ID, mxId);
  }

  /**
   * Get MX ID from Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @return MX ID
   */
  public static String getMxId(@NotNull Exchange exchange) {
    return getHeader(exchange, Header.CUSTOM_HEADER_MX_ID);
  }

  /**
   * Get JMS Message ID from Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @return JMS Message ID
   */
  public static String getJMSMessageId(@NotNull Exchange exchange) {
    return getHeader(exchange, JMS_HEADER_MESSAGE_ID);
  }

  /**
   * Get Status from Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @return Status
   */
  public static ProcessEntity.Status getStatus(@NotNull Exchange exchange) {
    return ProcessEntity.Status.valueOf(getHeader(exchange, Header.CUSTOM_HEADER_STATUS));
  }

  /**
   * Set Status in Camel Exchange header.
   *
   * @param exchange Camel Exchange
   * @param status   Status
   * @throws IllegalArgumentException error
   */
  public static void setStatus(@NotNull Exchange exchange, @NotNull ProcessEntity.Status status) {
    setHeader(exchange, Header.CUSTOM_HEADER_STATUS, status.name());
  }

  /**
   * Checks if the given header key and value represent a custom header.
   * A custom header is identified by:
   * <ul>
   * <li>A non-blank header key</li>
   * <li>A non-null header value</li>
   * <li>A non-blank header value (when converted to CharSequence)</li>
   * <li>The header key starting with the custom pattern prefix</li>
   * </ul>
   *
   * @param headerKey   the header key to check (can be null)
   * @param headerValue the header value to check (can be null)
   * @return true if the header meets all custom header criteria, false otherwise
   */
  public static boolean isCustomHeader(@Nullable String headerKey, @Nullable Object headerValue) {
    return StringUtils.isNotBlank(headerKey)
        && headerValue instanceof String
        && StringUtils.isNotBlank((String) headerValue)
        && headerKey.startsWith(CUSTOM_PATTERN);
  }

  /**
   * Set header in Camel Exchange.
   *
   * @param exchange    Camel Exchange
   * @param headerName  header name
   * @param headerValue header value
   */
  private static void setHeader(
      @NotNull Exchange exchange, @NotBlank String headerName, @NotBlank String headerValue) {
    Validate.notNull(exchange, "Exchange must not be null");
    Validate.notBlank(headerName, "HeaderName must not be blank");
    Validate.notBlank(
        headerValue, "HeaderValue name must not be blank (headerName=%s)".formatted(headerName));

    exchange.getIn().setHeader(headerName, headerValue);
  }

  /**
   * Get header from Camel Exchange.
   *
   * @param exchange   Camel Exchange
   * @param headerName header name
   * @return header value
   */
  private static String getHeader(@NotNull Exchange exchange, @NotBlank String headerName) {
    Validate.notNull(exchange, "Exchange must not be null");
    Validate.notBlank(headerName, "HeaderName name must not be blank");

    return exchange.getIn().getHeader(headerName, String.class);
  }
}
