/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.exception;

/**
 * Exception thrown when a workflow is in an invalid status for the requested operation.
 */
public class WorkflowStatusException extends Exception {

  public WorkflowStatusException(String message) {
    super(message);
  }
}
