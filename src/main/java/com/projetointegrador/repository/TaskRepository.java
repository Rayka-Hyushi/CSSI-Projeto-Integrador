package com.projetointegrador.repository;

import com.projetointegrador.model.Task;
import com.projetointegrador.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Task persistence.
 * Extends JpaRepository to inherit standard CRUD operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByTitleContainingIgnoreCase(String title);
}
