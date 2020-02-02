package com.lyj.community.service;

import com.lyj.community.dto.PageDTO;
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

    public PageDTO findAll(Integer page, Integer size) {
        PageDTO pageDTO = new PageDTO();
        Integer totalPage;
        Integer totalCount = questionMapper.count();

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) page = 1;
        if (page > totalPage) page = totalPage;

        pageDTO.setPaginaton(totalPage, page);
        Integer offset = size * (page - 1);
        List<Question> questions = questionMapper.findAll(offset, size);
        List<QuestionDTO> questionDTOS = new ArrayList<>();

        for (Question question : questions) {
            User user = userMapper.findById(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(user);
            questionDTOS.add(questionDTO);
        }
        pageDTO.setQuestions(questionDTOS);

        return pageDTO;
    }

    public PageDTO findAll(Integer id, Integer page, Integer size) {

        PageDTO pageDTO = new PageDTO();
        Integer totalPage;
        Integer totalCount = questionMapper.countByUserId(id);
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) page = 1;
        if (page > totalPage) page = totalPage;

        pageDTO.setPaginaton(totalPage, page);

        Integer offset = size * (page - 1);
        List<Question> questions = questionMapper.findOwn(id, offset, size);
        List<QuestionDTO> questionDTOS = new ArrayList<>();

        for (Question question : questions) {
            User user = userMapper.findById(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(user);
            questionDTOS.add(questionDTO);
        }
        pageDTO.setQuestions(questionDTOS);
        return pageDTO;
    }

    public QuestionDTO findById(Integer id){
        QuestionDTO questionDTO = new QuestionDTO();
        Question question = questionMapper.findById(id);
        BeanUtils.copyProperties(question,questionDTO);
        User user = userMapper.findById(question.getCreator());
        questionDTO.setUser(user);
        return questionDTO;
    }

    public Question findQuestionById(Integer id){
        return questionMapper.findById(id);
    }

    public void createOrUpdate(Question question) {
        if(question.getId() == null){
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            questionMapper.createQuestion(question);
        }else{
            question.setGmtModified(System.currentTimeMillis());
            questionMapper.updateQuestion(question);
        }


    }
}
