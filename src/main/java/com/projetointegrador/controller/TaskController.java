package com.projetointegrador.controller;

import com.projetointegrador.model.Task;
import com.projetointegrador.model.TaskStatus;
import com.projetointegrador.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller (MVC) for Task CRUD operations.
 * Maps HTTP requests to service calls and returns Thymeleaf view names.
 */
@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** List all tasks, with optional status filter or title search. */
    @GetMapping
    public String list(@RequestParam(required = false) TaskStatus status,
                       @RequestParam(required = false) String search,
                       Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("tasks", taskService.search(search));
            model.addAttribute("search", search);
        } else if (status != null) {
            model.addAttribute("tasks", taskService.findByStatus(status));
            model.addAttribute("selectedStatus", status);
        } else {
            model.addAttribute("tasks", taskService.findAll());
        }
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("pageTitle", "Tarefas");
        return "tasks/list";
    }

    /** Show detail view for a single task. */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("pageTitle", "Detalhes da Tarefa");
        return "tasks/detail";
    }

    /** Show form to create a new task. */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("pageTitle", "Nova Tarefa");
        return "tasks/form";
    }

    /** Handle create form submission. */
    @PostMapping
    public String create(@Valid @ModelAttribute Task task,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("pageTitle", "Nova Tarefa");
            return "tasks/form";
        }
        taskService.save(task);
        redirectAttributes.addFlashAttribute("successMessage", "Tarefa criada com sucesso!");
        return "redirect:/tasks";
    }

    /** Show form to edit an existing task. */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("pageTitle", "Editar Tarefa");
        return "tasks/form";
    }

    /** Handle update form submission. */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Task task,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", TaskStatus.values());
            model.addAttribute("pageTitle", "Editar Tarefa");
            return "tasks/form";
        }
        taskService.update(id, task);
        redirectAttributes.addFlashAttribute("successMessage", "Tarefa atualizada com sucesso!");
        return "redirect:/tasks";
    }

    /** Delete a task. */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        taskService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Tarefa removida com sucesso!");
        return "redirect:/tasks";
    }
}
