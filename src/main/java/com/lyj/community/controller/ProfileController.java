package com.lyj.community.controller;

import com.lyj.community.dto.PageDTO;
import com.lyj.community.model.User;
import com.lyj.community.service.NotificationService;
import com.lyj.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/profile/{action}")
    public String profile(@PathVariable("action") String action,
                          @RequestParam(name = "page", defaultValue = "1") Integer page,
                          @RequestParam(name = "size", defaultValue = "6") Integer size,
                          HttpServletRequest request,
                          Model model) {

        User user = (User) request.getSession().getAttribute("user");

        if (null == user) return "redirect:/";

        if ("questions".equals(action)) {
            model.addAttribute("section", "questions");
            model.addAttribute("sectionName", "我的提问");
            PageDTO pages = questionService.list(user.getId(), page, size);
            model.addAttribute("pages", pages);
        } else if ("replies".equals(action)) {
            PageDTO paginationDTO = notificationService.list(user.getId(), page, size);
            model.addAttribute("section", "replies");
            model.addAttribute("pages", paginationDTO);
            model.addAttribute("sectionName", "最新回复");
        }
        return "profile";
    }
}
