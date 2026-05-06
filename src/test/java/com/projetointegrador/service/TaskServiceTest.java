package com.projetointegrador.service;

import com.projetointegrador.model.Usuario;
import com.projetointegrador.model.TipoUsuario;
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

    private Usuario sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Usuario();
        sampleTask.setId(1L);
        sampleTask.setTitle("Tarefa de Teste");
        sampleTask.setDescription("Descrição de teste");
        sampleTask.setStatus(TipoUsuario.PENDING);
    }

    @Test
    void findAll_shouldReturnAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(sampleTask));

        List<Usuario> result = taskService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Tarefa de Teste");
        verify(taskRepository).findAll();
    }

    @Test
    void findById_shouldReturnTask_whenFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Usuario result = taskService.findById(1L);

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

        Usuario result = taskService.save(sampleTask);

        assertThat(result.getTitle()).isEqualTo("Tarefa de Teste");
        verify(taskRepository).save(sampleTask);
    }

    @Test
    void update_shouldModifyExistingTask() {
        Usuario updated = new Usuario();
        updated.setTitle("Título Atualizado");
        updated.setDescription("Desc Atualizada");
        updated.setStatus(TipoUsuario.DONE);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(sampleTask)).thenReturn(sampleTask);

        Usuario result = taskService.update(1L, updated);

        assertThat(result.getTitle()).isEqualTo("Título Atualizado");
        assertThat(result.getStatus()).isEqualTo(TipoUsuario.DONE);
    }

    @Test
    void delete_shouldRemoveTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        taskService.delete(1L);

        verify(taskRepository).delete(sampleTask);
    }

    @Test
    void findByStatus_shouldFilterCorrectly() {
        when(taskRepository.findByStatus(TipoUsuario.PENDING)).thenReturn(List.of(sampleTask));

        List<Usuario> result = taskService.findByStatus(TipoUsuario.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TipoUsuario.PENDING);
    }

    @Test
    void search_shouldReturnMatchingTasks() {
        when(taskRepository.findByTitleContainingIgnoreCase("teste")).thenReturn(List.of(sampleTask));

        List<Usuario> result = taskService.search("teste");

        assertThat(result).hasSize(1);
    }
}
