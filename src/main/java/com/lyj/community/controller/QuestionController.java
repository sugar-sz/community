package com.lyj.community.controller;

import com.lyj.community.dto.CommentDTO;
import com.lyj.community.dto.QuestionDTO;
import com.lyj.community.enums.CommentTypeEnum;
import com.lyj.community.service.CommentService;
import com.lyj.community.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/question/{id}")
    public String question(@PathVariable(name = "id") Long id,
                           Model model) {

        QuestionDTO questionDTO = questionService.findById(id);
        List<QuestionDTO> tags = questionService.selectTags(questionDTO);
        List<CommentDTO> commentDTOS = commentService.listByTargetId(id, CommentTypeEnum.QUESTION);

        questionService.incrementViews(id);
        model.addAttribute("question", questionDTO);
        model.addAttribute("comments", commentDTOS);
        model.addAttribute("relatedQuestions", tags);
        return "question";
    }
}
