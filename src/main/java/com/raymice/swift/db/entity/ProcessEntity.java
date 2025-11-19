/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a process in the database.
 * Contains information about process name, payload, status, and timestamps.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "process")
public class ProcessEntity {

  /**
   * Unique identifier for the process.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Name of the process.
   */
  @Column(nullable = false)
  private String name;

  /**
   * Payload data associated with the process.
   */
  @Column(nullable = false)
  private String payload;

  /**
   * Current status of the process.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Status status;

  /**
   * Timestamp when the process was created.
   */
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  /**
   * Timestamp when the process was last updated.
   */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Enumeration representing the possible statuses of a process.
   */
  public enum Status {
    /**
     * Process has been created but not yet validated.
     */
    CREATED,

    /**
     * Process has been validated.
     */
    VALIDATED,

    /**
     * Process has been completed successfully.
     */
    COMPLETED,

    /**
     * Process contains unsupported features.
     */
    UNSUPPORTED,

    /**
     * Process has failed.
     */
    FAILED
  }
}
