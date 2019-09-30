package com.safeway.app.ps01.controller;

import com.safeway.app.ps01.model.RoleType;
import com.safeway.app.ps01.model.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String login(Model model) {

        User user = new User();
        user.setUsername("rsapl00");
        user.setEmail("rsapl00@safeway.com");
        user.setDivision("10");
        user.setRoleType(RoleType.USER_ADMIN);

        model.addAttribute("user", user);

        return "home";
    }

}