package com.albertsons.app.ps01.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.albertsons.app.ps01.security.userdetails.User;
import com.albertsons.app.ps01.service.CycleChangeRequestService;
import com.albertsons.app.ps01.service.MailNotificationService;
import com.albertsons.app.ps01.service.resource.Ps01MailMessage;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    private CycleChangeRequestService cycleChangeRequestService;
    private MailNotificationService mailService;

    public HomeController(CycleChangeRequestService cycleChangeRequestService, MailNotificationService mailService) {
        this.cycleChangeRequestService = cycleChangeRequestService;
        this.mailService = mailService;
    }

    @GetMapping("/home")
    public String login(HttpServletRequest request, HttpServletResponse response, Model model) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        model.addAttribute("user", user);

        return "home";
    }

    @GetMapping("/home/{division}/{startDate}/{endDate}")
    public String login (@PathVariable String division, @PathVariable String startDate, @PathVariable String endDate) {
        cycleChangeRequestService.generateCycleChangeRequest(division, java.sql.Date.valueOf(startDate) , java.sql.Date.valueOf(endDate));

        return "redirect:/home";
    }

    @GetMapping("/rest/sendEmail")
    public String sendEmail() {
        Ps01MailMessage message = new Ps01MailMessage();
        message.setToRecipient(Arrays.asList("ryan.saplan@safeway.com"));
        message.setMessageTitle("Test email");
        message.setMessageBody("Do not reply. This is system generated email.");
        mailService.sendMailAttachmentNotification(message);

        return "redirect:/home";
    }
}