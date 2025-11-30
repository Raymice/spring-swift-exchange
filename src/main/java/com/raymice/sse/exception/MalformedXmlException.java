/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.exception;

/**
 * Exception thrown when XML is not well-formed.
 */
public class MalformedXmlException extends Exception {

  public MalformedXmlException() {
    super("XML is not well-formed");
  }
}
