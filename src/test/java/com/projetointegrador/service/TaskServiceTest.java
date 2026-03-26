package com.projetointegrador.service;

import com.projetointegrador.model.Task;
import com.projetointegrador.model.TaskStatus;
import com.projetointegrador.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setTitle("Tarefa de Teste");
        sampleTask.setDescription("Descrição de teste");
        sampleTask.setStatus(TaskStatus.PENDING);
    }

    @Test
    void findAll_shouldReturnAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(sampleTask));

        List<Task> result = taskService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Tarefa de Teste");
        verify(taskRepository).findAll();
    }

    @Test
    void findById_shouldReturnTask_whenFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Task result = taskService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void save_shouldPersistTask() {
        when(taskRepository.save(sampleTask)).thenReturn(sampleTask);

        Task result = taskService.save(sampleTask);

        assertThat(result.getTitle()).isEqualTo("Tarefa de Teste");
        verify(taskRepository).save(sampleTask);
    }

    @Test
    void update_shouldModifyExistingTask() {
        Task updated = new Task();
        updated.setTitle("Título Atualizado");
        updated.setDescription("Desc Atualizada");
        updated.setStatus(TaskStatus.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(sampleTask)).thenReturn(sampleTask);

        Task result = taskService.update(1L, updated);

        assertThat(result.getTitle()).isEqualTo("Título Atualizado");
        assertThat(result.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void delete_shouldRemoveTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        taskService.delete(1L);

        verify(taskRepository).delete(sampleTask);
    }

    @Test
    void findByStatus_shouldFilterCorrectly() {
        when(taskRepository.findByStatus(TaskStatus.PENDING)).thenReturn(List.of(sampleTask));

        List<Task> result = taskService.findByStatus(TaskStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void search_shouldReturnMatchingTasks() {
        when(taskRepository.findByTitleContainingIgnoreCase("teste")).thenReturn(List.of(sampleTask));

        List<Task> result = taskService.search("teste");

        assertThat(result).hasSize(1);
    }
}
