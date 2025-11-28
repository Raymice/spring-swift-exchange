/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.utils;

import com.raymice.swift.constant.Common;
import jakarta.annotation.Nullable;

/**
 * Utility class for String operations.
 */
public class StringUtils {

  /**
   * Returns "unknown" if the input string is blank, otherwise returns the
   * original string.
   *
   * @param value the input string
   * @return "unknown" if blank, else the original string
   */
  public static String unknownIfBlank(@Nullable String value) {
    return org.apache.commons.lang3.StringUtils.defaultIfBlank(value, Common.UNKNOWN);
  }
}
