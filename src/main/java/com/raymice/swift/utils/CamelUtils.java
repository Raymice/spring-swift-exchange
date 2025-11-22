/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import static com.raymice.swift.constant.Header.CUSTOM_PATTERN;
import static org.apache.camel.component.jms.JmsConstants.JMS_HEADER_DESTINATION;
import static org.apache.camel.component.jms.JmsConstants.JMS_HEADER_MESSAGE_ID;

import com.raymice.swift.constant.Header;
import com.raymice.swift.db.entity.ProcessEntity;
import jakarta.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.zip.CRC32;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for Apache Camel related operations.
 */
public class CamelUtils {

  /**
   * Calculate CRC32 hash of a file from Camel Exchange.
   * Using CRC32 for file hashing due to its speed and low memory usage,
   * making it suitable for small to medium-sized files where performance is critical.
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
   * @param exchange Camel Exchange
   * @return process ID
   * @throws IllegalArgumentException error
   */
  public static String getProcessId(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Header.CUSTOM_HEADER_PROCESS_ID);
  }

  /**
   * Set process ID in Camel Exchange header.
   * @param exchange Camel Exchange
   * @param processId process ID
   * @throws IllegalArgumentException error
   */
  public static void setProcessId(Exchange exchange, Long processId)
      throws IllegalArgumentException {
    setHeader(exchange, Header.CUSTOM_HEADER_PROCESS_ID, String.valueOf(processId));
  }

  /**
   * Get original file name from Camel Exchange header.
   * @param exchange Camel Exchange
   * @return original file name
   * @throws IllegalArgumentException error
   */
  public static String getOriginalFileName(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Header.CUSTOM_HEADER_ORIGINAL_FILE_NAME);
  }

  /**
   * Get updated file name from Camel Exchange header.
   * @param exchange Camel Exchange
   * @return updated file name
   * @throws IllegalArgumentException error
   */
  public static String getUpdatedFileName(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Header.CUSTOM_HEADER_UPDATED_FILE_NAME);
  }

  /**
   * Set original file name in Camel Exchange header.
   * @param exchange Camel Exchange
   * @param originalFileName original file name
   * @throws IllegalArgumentException error
   */
  public static void setOriginalFileName(Exchange exchange, String originalFileName)
      throws IllegalArgumentException {
    setHeader(exchange, Header.CUSTOM_HEADER_ORIGINAL_FILE_NAME, originalFileName);
  }

  /**
   * Set updated file name in Camel Exchange header.
   * @param exchange Camel Exchange
   * @param updatedFileName updated file name
   * @throws IllegalArgumentException error
   */
  public static void setUpdatedFileName(Exchange exchange, String updatedFileName)
      throws IllegalArgumentException {
    setHeader(exchange, Header.CUSTOM_HEADER_UPDATED_FILE_NAME, updatedFileName);
  }

  /**
   * Get file name from Camel Exchange header.
   * @param exchange Camel Exchange
   * @return file name
   * @throws IllegalArgumentException error
   */
  public static String getFileName(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Exchange.FILE_NAME);
  }

  /**
   * Set file name in Camel Exchange header.
   * @param exchange Camel Exchange
   * @param fileName file name
   * @throws IllegalArgumentException error
   */
  public static void setFileName(Exchange exchange, String fileName)
      throws IllegalArgumentException {
    setHeader(exchange, Exchange.FILE_NAME, fileName);
  }

  /**
   * Get JMS Queue name from Camel Exchange header.
   * @param exchange Camel Exchange
   * @return JMS Queue name
   * @throws IllegalArgumentException error
   */
  public static String getQueueName(Exchange exchange) {
    return getHeader(exchange, JMS_HEADER_DESTINATION);
  }

  /**
   * Set MX ID in Camel Exchange header.
   * @param exchange Camel Exchange
   * @param mxId MX ID
   * @throws IllegalArgumentException error
   */
  public static void setMxId(Exchange exchange, String mxId) throws IllegalArgumentException {
    setHeader(exchange, Header.CUSTOM_HEADER_MX_ID, mxId);
  }

  /**
   * Get MX ID from Camel Exchange header.
   * @param exchange Camel Exchange
   * @return MX ID
   * @throws IllegalArgumentException error
   */
  public static String getMxId(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, Header.CUSTOM_HEADER_MX_ID);
  }

  /**
   * Get JMS Message ID from Camel Exchange header.
   * @param exchange Camel Exchange
   * @return JMS Message ID
   * @throws IllegalArgumentException error
   */
  public static String getJMSMessageId(Exchange exchange) throws IllegalArgumentException {
    return getHeader(exchange, JMS_HEADER_MESSAGE_ID);
  }

  /**
   * Get Status from Camel Exchange header.
   * @param exchange Camel Exchange
   * @return Status
   * @throws IllegalArgumentException error
   */
  public static ProcessEntity.Status getStatus(Exchange exchange) throws IllegalArgumentException {
    return ProcessEntity.Status.valueOf(getHeader(exchange, Header.CUSTOM_HEADER_STATUS));
  }

  /**
   * Set Status in Camel Exchange header.
   * @param exchange Camel Exchange
   * @param status Status
   * @throws IllegalArgumentException error
   */
  public static void setStatus(Exchange exchange, ProcessEntity.Status status)
      throws IllegalArgumentException {
    setHeader(exchange, Header.CUSTOM_HEADER_STATUS, status.name());
  }

  /**
   * Checks if the given header key and value represent a custom header.
   * A custom header is identified by:
   * <ul>
   *   <li>A non-blank header key</li>
   *   <li>A non-null header value</li>
   *   <li>A non-blank header value (when converted to CharSequence)</li>
   *   <li>The header key starting with the custom pattern prefix</li>
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
   * @param exchange Camel Exchange
   * @param headerName  header name
   * @param headerValue header value
   * @throws IllegalArgumentException error
   */
  private static void setHeader(Exchange exchange, String headerName, String headerValue)
      throws IllegalArgumentException {
    if (exchange == null) {
      throw new IllegalArgumentException("Exchange must not be null", null);
    }
    if (StringUtils.isBlank(headerName)) {
      throw new IllegalArgumentException("HeaderName name must not be blank", null);
    }
    if (StringUtils.isBlank(headerValue)) {
      throw new IllegalArgumentException(
          "HeaderValue name must not be blank (headerName=%s)".formatted(headerName), null);
    }

    exchange.getIn().setHeader(headerName, headerValue);
  }

  /**
   * Get header from Camel Exchange.
   * @param exchange Camel Exchange
   * @param headerName header name
   * @return header value
   * @throws IllegalArgumentException error
   */
  private static String getHeader(Exchange exchange, String headerName)
      throws IllegalArgumentException {
    if (exchange == null) {
      throw new IllegalArgumentException("Exchange must not be null", null);
    }
    if (StringUtils.isBlank(headerName)) {
      throw new IllegalArgumentException("HeaderName name must not be blank", null);
    }

    return exchange.getIn().getHeader(headerName, String.class);
  }
}
