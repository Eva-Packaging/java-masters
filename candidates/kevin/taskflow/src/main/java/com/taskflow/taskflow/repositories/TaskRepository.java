package com.taskflow.taskflow.repositories;

import com.taskflow.taskflow.domain.Task;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
