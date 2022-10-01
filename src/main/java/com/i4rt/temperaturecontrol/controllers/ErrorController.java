package com.i4rt.temperaturecontrol.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("403")
    public String accessError(){
        return "403";
    }
}
