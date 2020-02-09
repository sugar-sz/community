package com.lyj.community.controller;


import com.lyj.community.dto.PageDTO;
import com.lyj.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(name = "page", defaultValue = "1") Integer page,
                        @RequestParam(name = "size", defaultValue = "6") Integer size,
                        @RequestParam(name = "search", required = false) String search
    ) {

        PageDTO pages = questionService.list(search,page, size);
        model.addAttribute("search", search);
        model.addAttribute("pages", pages);
        return "index";
    }
}
