package com.lyj.community.service;

import com.lyj.community.dto.QuestionDTO;
import com.lyj.community.mapper.QuestionMapper;
import com.lyj.community.mapper.UserMapper;
import com.lyj.community.model.Question;
import com.lyj.community.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    public List<QuestionDTO> findAll() {
        List<Question> questions = questionMapper.findAll();
        List<QuestionDTO> questionDTOS = new ArrayList<>();
        for(Question question:questions){
            User user = userMapper.findById(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            questionDTO.setUser(user);
            questionDTOS.add(questionDTO);
        }
        return questionDTOS;
    }
}
