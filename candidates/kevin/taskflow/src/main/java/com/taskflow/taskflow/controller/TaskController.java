package com.taskflow.taskflow.controller;

import com.taskflow.taskflow.domain.Task;
import com.taskflow.taskflow.domain.User;
import com.taskflow.taskflow.dto.CreateTaskRequest;
import com.taskflow.taskflow.repositories.TaskRepository;
import com.taskflow.taskflow.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;


    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Task createTask(@RequestBody CreateTaskRequest request){
        log.info("Creating task: {}", request);
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        Optional<User> optionalUser = userRepository.findById(request.assignedUserId());
        optionalUser.ifPresentOrElse(
                user -> {task.setAssignedTo(user);},
                () -> {
                    throw new RuntimeException("User not found");
                }
        );
        taskRepository.save(task);
        return task;
    }


    public Task getTaskById(Long id) {
        return null;
    }

    @PutMapping(value = "/{taskID}/status")
    public Task updateTaskStatus(@PathVariable Long taskID, @RequestParam Task.Status status) {
        Task task = taskRepository.findById(taskID).orElseThrow( () -> new RuntimeException("Task not found"));
        task.setStatus(status);
        return task;
    }



    public List<Task> getTasksByUserID(Long userID) {
        return null;
    }

}
