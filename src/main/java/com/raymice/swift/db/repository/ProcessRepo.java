/* Raymice - https://github.com/Raymice - 2025 */
package com.raymice.swift.db.repository;

import com.raymice.swift.db.entity.ProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProcessRepo extends JpaRepository<ProcessEntity, Long> {

  @Modifying
  @Transactional
  @Query(
      value =
          "UPDATE ProcessEntity p SET p.status = :newStatus, p.updatedAt = instant WHERE p.id ="
              + " :id")
  void updateStatusById(
      @Param("newStatus") ProcessEntity.Status status, @Param("id") long processId);
}
