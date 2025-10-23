/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Slf4j
public class ValidatorUtils {

  public static boolean isXMLWellFormed(String xmlstring) {

    if (StringUtils.isBlank(xmlstring)) {
      return false;
    }

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      // Parsing will fail if not well-formed
      builder.parse(new InputSource(new StringReader(xmlstring)));
      return true;
    } catch (SAXException | IOException e) {
      log.error("XML is not well-formed: {}", e.getMessage());
      return false;
    } catch (Exception e) {
      log.error("An unexpected error occurred: {}", e.getMessage());
      return false;
    }
  }
}
