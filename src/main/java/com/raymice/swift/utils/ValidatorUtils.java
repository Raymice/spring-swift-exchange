/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ValidatorUtils {

  public static boolean isXMLWellFormed(String xmlstring) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.parse(
          new InputSource(new StringReader(xmlstring))); // Parsing will fail if not well-formed
      return true;
    } catch (SAXException | IOException e) {
      System.err.println("XML is not well-formed: " + e.getMessage());
      return false;
    } catch (Exception e) {
      System.err.println("An unexpected error occurred: " + e.getMessage());
      return false;
    }
  }
}
