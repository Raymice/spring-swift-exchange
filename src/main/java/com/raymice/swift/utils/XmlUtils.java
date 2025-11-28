/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Nullable;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.InputSource;

/**
 * Utility class for XML operations.
 */
@Slf4j
public class XmlUtils {

  /**
   * Checks if the provided XML string is well-formed.
   *
   * @param xmlString the XML string to check
   * @return true if well-formed, false otherwise
   */
  @Timed
  public static boolean isXMLWellFormed(@Nullable String xmlString) {

    if (StringUtils.isBlank(xmlString)) {
      return false;
    }

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      // Parsing will fail if not well-formed
      builder.parse(new InputSource(new StringReader(xmlString)));
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
