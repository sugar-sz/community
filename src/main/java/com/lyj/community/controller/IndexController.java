package com.lyj.community.controller;


import com.lyj.community.dto.QuestionDTO;
import com.lyj.community.mapper.QuestionMapper;
import com.lyj.community.mapper.UserMapper;
import com.lyj.community.model.Question;
import com.lyj.community.model.User;
import com.lyj.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionService questionService;

    @GetMapping("/")
    public String index(HttpServletRequest request,
                        Model model) {
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                String token = cookie.getValue();
                User user = userMapper.findByToken(token);
                if (null != user) {
                    request.getSession().setAttribute("user", user);
                }
                break;
            }
        }

        List<QuestionDTO> questionList = questionService.findAll();
        model.addAttribute("questions",questionList);
        return "index";
    }
}
