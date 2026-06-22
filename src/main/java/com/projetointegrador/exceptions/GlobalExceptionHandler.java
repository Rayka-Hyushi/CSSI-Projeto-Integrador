package com.projetointegrador.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Tratador global de exceções. Captura erros lançados antes dos Controllers,
 * como o erro de tamanho de arquivo excedido (HTTP 413).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("erro",
                "O arquivo enviado é muito grande. O tamanho máximo permitido é de 2MB.");

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/perfil";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalStateException(
            IllegalStateException ex,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("multipart")) {
            logger.warn("Rejeição de upload no nível do servlet container: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("erro",
                    "O arquivo enviado é muito grande. O tamanho máximo permitido é de 2MB.");

            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isBlank()) {
                return "redirect:" + referer;
            }
            return "redirect:/perfil";
        }

        throw ex;
    }
}
