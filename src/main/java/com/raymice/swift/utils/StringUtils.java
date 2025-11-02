/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import com.raymice.swift.constant.Common;

/**
 * Utility class for String operations.
 */
public class StringUtils {

  public static String unknownIfBlank(String value) {
    return org.apache.commons.lang3.StringUtils.defaultIfBlank(value, Common.UNKNOWN);
  }
}
