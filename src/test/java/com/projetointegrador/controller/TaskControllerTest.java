package com.projetointegrador.controller;

import com.projetointegrador.model.Task;
import com.projetointegrador.model.TaskStatus;
import com.projetointegrador.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private Task buildTask(Long id) {
        Task t = new Task();
        t.setId(id);
        t.setTitle("Tarefa " + id);
        t.setDescription("Desc " + id);
        t.setStatus(TaskStatus.PENDING);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        return t;
    }

    @Test
    void list_shouldReturnListView() throws Exception {
        when(taskService.findAll()).thenReturn(List.of(buildTask(1L)));

        mockMvc.perform(get("/tasks"))
               .andExpect(status().isOk())
               .andExpect(view().name("tasks/list"))
               .andExpect(model().attributeExists("tasks", "statuses"));
    }

    @Test
    void list_withStatusFilter_shouldFilterTasks() throws Exception {
        when(taskService.findByStatus(TaskStatus.PENDING)).thenReturn(List.of(buildTask(1L)));

        mockMvc.perform(get("/tasks").param("status", "PENDING"))
               .andExpect(status().isOk())
               .andExpect(view().name("tasks/list"))
               .andExpect(model().attribute("selectedStatus", TaskStatus.PENDING));
    }

    @Test
    void list_withSearch_shouldSearchTasks() throws Exception {
        when(taskService.search("tarefa")).thenReturn(List.of(buildTask(1L)));

        mockMvc.perform(get("/tasks").param("search", "tarefa"))
               .andExpect(status().isOk())
               .andExpect(view().name("tasks/list"))
               .andExpect(model().attribute("search", "tarefa"));
    }

    @Test
    void detail_shouldReturnDetailView() throws Exception {
        when(taskService.findById(1L)).thenReturn(buildTask(1L));

        mockMvc.perform(get("/tasks/1"))
               .andExpect(status().isOk())
               .andExpect(view().name("tasks/detail"))
               .andExpect(model().attributeExists("task"));
    }

    @Test
    void newForm_shouldReturnFormView() throws Exception {
        mockMvc.perform(get("/tasks/new"))
               .andExpect(status().isOk())
               .andExpect(view().name("tasks/form"))
               .andExpect(model().attributeExists("task", "statuses"));
    }

    @Test
    void create_withValidData_shouldRedirect() throws Exception {
        Task saved = buildTask(1L);
        when(taskService.save(any(Task.class))).thenReturn(saved);

        mockMvc.perform(post("/tasks")
                       .param("title", "Nova Tarefa")
                       .param("description", "Desc")
                       .param("status", "PENDING"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/tasks"));
    }

    @Test
    void create_withMissingTitle_shouldReturnForm() throws Exception {
        mockMvc.perform(post("/tasks")
                       .param("title", "")
                       .param("status", "PENDING"))
               .andExpect(status().isOk())
               .andExpect(view().name("tasks/form"));
    }

    @Test
    void editForm_shouldReturnFormWithTask() throws Exception {
        when(taskService.findById(1L)).thenReturn(buildTask(1L));

        mockMvc.perform(get("/tasks/1/edit"))
               .andExpect(status().isOk())
               .andExpect(view().name("tasks/form"))
               .andExpect(model().attributeExists("task"));
    }

    @Test
    void update_withValidData_shouldRedirect() throws Exception {
        Task updated = buildTask(1L);
        when(taskService.update(eq(1L), any(Task.class))).thenReturn(updated);

        mockMvc.perform(post("/tasks/1")
                       .param("title", "Atualizada")
                       .param("status", "DONE"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/tasks"));
    }

    @Test
    void delete_shouldRedirectAfterDeletion() throws Exception {
        doNothing().when(taskService).delete(1L);

        mockMvc.perform(post("/tasks/1/delete"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/tasks"));

        verify(taskService).delete(1L);
    }
}
