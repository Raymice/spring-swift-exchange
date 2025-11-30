/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.sse.constant;

public class Header {

  /**
   * <p>
   * All values need to start with 'X_' to follow a convention
   * </p>
   * <p>
   * Convention will be use in different places
   * </p>
   */
  public static final String CUSTOM_PATTERN = "X_";

  public static final String CUSTOM_HEADER_MX_ID = CUSTOM_PATTERN + "MX_ID";
  public static final String CUSTOM_HEADER_ORIGINAL_FILE_NAME =
      CUSTOM_PATTERN + "ORIGINAL_FILE_NAME";
  public static final String CUSTOM_HEADER_UPDATED_FILE_NAME = CUSTOM_PATTERN + "UPDATED_FILE_NAME";
  public static final String CUSTOM_HEADER_PROCESS_ID = CUSTOM_PATTERN + "PROCESS_ID";
  public static final String CUSTOM_HEADER_STATUS = CUSTOM_PATTERN + "STATUS";
}
