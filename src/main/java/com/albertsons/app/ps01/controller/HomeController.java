package com.albertsons.app.ps01.controller;

import com.albertsons.app.ps01.controller.resource.RoleType;
import com.albertsons.app.ps01.controller.resource.User;
import com.albertsons.app.ps01.service.CycleChangeRequestService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    private CycleChangeRequestService cycleChangeRequestService;

    public HomeController(CycleChangeRequestService cycleChangeRequestService) {
        this.cycleChangeRequestService = cycleChangeRequestService;
    }

    @GetMapping("/home")
    public String login(Model model) {

        User user = new User();
        user.setUsername("rsapl00");
        user.setEmail("rsapl00@safeway.com");
        user.setDivision("10");
        user.setRoleType(RoleType.USER_ADMIN);

        model.addAttribute("user", user);

        //cycleChangeRequestService.generateCycleChangeRequest("19", java.sql.Date.valueOf("2019-9-15") , java.sql.Date.valueOf("2019-9-29"));

        return "home";
    }

    @GetMapping("/home/{division}/{startDate}/{endDate}")
    public String login (@PathVariable String division, @PathVariable String startDate, @PathVariable String endDate) {
        cycleChangeRequestService.generateCycleChangeRequest(division, java.sql.Date.valueOf(startDate) , java.sql.Date.valueOf(endDate));

        return "redirect:/home";
    }

    
}