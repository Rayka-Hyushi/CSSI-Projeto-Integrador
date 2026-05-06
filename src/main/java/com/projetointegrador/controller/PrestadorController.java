package com.projetointegrador.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prestador")
public class PrestadorController {

    @GetMapping("/inicio")
    public String homePrestador() {
        return "prestador/inicio";
    }
}
