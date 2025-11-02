/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.unit.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.raymice.swift.utils.XmlUtils;
import org.junit.jupiter.api.Test;

class XmlUtilsTest {

  @Test
  void isXMLWellFormed_ReturnsTrue_ForValidXML() {
    String validXML = "<root><child>Content</child></root>";
    boolean result = XmlUtils.isXMLWellFormed(validXML);

    assertTrue(result);
  }

  @Test
  void isXMLWellFormed_ReturnsFalse_ForMalformedXML() {
    String malformedXML = "<root><child>Content</root>";
    boolean result = XmlUtils.isXMLWellFormed(malformedXML);

    assertFalse(result);
  }

  @Test
  void isXMLWellFormed_ReturnsFalse_ForBlankInput() {
    String blankXML = " ";
    boolean result = XmlUtils.isXMLWellFormed(blankXML);

    assertFalse(result);
  }

  @Test
  void isXMLWellFormed_ReturnsFalse_ForNullInput() {
    String nullXML = null;
    boolean result = XmlUtils.isXMLWellFormed(nullXML);

    assertFalse(result);
  }

  @Test
  void isXMLWellFormed_ReturnsFalse_ForNonXMLContent() {
    String nonXML = "This is not XML.";
    boolean result = XmlUtils.isXMLWellFormed(nonXML);

    assertFalse(result);
  }

  @Test
  void isXMLWellFormed_ReturnsTrue_ForXMLWithNamespaces() {
    String xmlWithNamespaces =
        "<root xmlns:ns=\"http://example.com\"><ns:child>Content</ns:child></root>";
    boolean result = XmlUtils.isXMLWellFormed(xmlWithNamespaces);

    assertTrue(result);
  }

  @Test
  void isXMLWellFormed_ReturnsTrue_ForXMLWithSpecialCharacters() {
    String xmlWithSpecialChars = "<root><child>&lt;Content&gt;</child></root>";
    boolean result = XmlUtils.isXMLWellFormed(xmlWithSpecialChars);

    assertTrue(result);
  }
}
