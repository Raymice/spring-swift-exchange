/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ValidatorUtilsTest {

  @Test
  void isXMLWellFormed_ReturnsTrue_ForValidXML() {
    String validXML = "<root><child>Content</child></root>";
    boolean result = ValidatorUtils.isXMLWellFormed(validXML);

    assertTrue(result);
  }

  @Test
  void isXMLWellFormed_ReturnsFalse_ForMalformedXML() {
    String malformedXML = "<root><child>Content</root>";
    boolean result = ValidatorUtils.isXMLWellFormed(malformedXML);

    assertFalse(result);
  }

  @Test
  void isXMLWellFormed_ReturnsFalse_ForBlankInput() {
    String blankXML = " ";
    boolean result = ValidatorUtils.isXMLWellFormed(blankXML);

    assertFalse(result);
  }

  @Test
  void isXMLWellFormed_ReturnsFalse_ForNullInput() {
    String nullXML = null;
    boolean result = ValidatorUtils.isXMLWellFormed(nullXML);

    assertFalse(result);
  }

  @Test
  void isXMLWellFormed_ReturnsFalse_ForNonXMLContent() {
    String nonXML = "This is not XML.";
    boolean result = ValidatorUtils.isXMLWellFormed(nonXML);

    assertFalse(result);
  }

  @Test
  void isXMLWellFormed_ReturnsTrue_ForXMLWithNamespaces() {
    String xmlWithNamespaces =
        "<root xmlns:ns=\"http://example.com\"><ns:child>Content</ns:child></root>";
    boolean result = ValidatorUtils.isXMLWellFormed(xmlWithNamespaces);

    assertTrue(result);
  }

  @Test
  void isXMLWellFormed_ReturnsTrue_ForXMLWithSpecialCharacters() {
    String xmlWithSpecialChars = "<root><child>&lt;Content&gt;</child></root>";
    boolean result = ValidatorUtils.isXMLWellFormed(xmlWithSpecialChars);

    assertTrue(result);
  }
}
