package com.taskflow.taskflow.repositories;

import com.taskflow.taskflow.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
