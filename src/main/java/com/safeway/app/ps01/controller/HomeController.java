package com.safeway.app.ps01.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public ResponseEntity<String> login() {

        return new ResponseEntity<>( "Welcome in Host POS Temporary Cycle Change Automation Website.", HttpStatus.OK);
    }

}