package com.lyj.community.service;

import com.lyj.community.dto.CommentDTO;
import com.lyj.community.dto.PageDTO;
import com.lyj.community.dto.QuestionDTO;
import com.lyj.community.enums.CommentTypeEnum;
import com.lyj.community.exception.CustomizeErrorCode;
import com.lyj.community.exception.CustomizeException;
import com.lyj.community.mapper.CommentMapper;
import com.lyj.community.mapper.QuestionMapper;
import com.lyj.community.mapper.UserMapper;
import com.lyj.community.model.*;
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

    @Autowired
    private CommentMapper commentMapper;

    public PageDTO findAll(Integer page, Integer size) {
        PageDTO pageDTO = new PageDTO();
        Integer totalPage;
        Integer totalCount = (int) questionMapper.countByExample(new QuestionExample());

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) page = 1;
        if (page > totalPage) page = totalPage;

        pageDTO.setPaginaton(totalPage, page);
        Integer offset = size * (page - 1);
        QuestionExample example = new QuestionExample();
        example.setOffset(offset);
        example.setSize(size);
        List<Question> questions = questionMapper.selectByExample(example);
        List<QuestionDTO> questionDTOS = new ArrayList<>();

        for (Question question : questions) {
            UserExample userExample = new UserExample();
            userExample.createCriteria()
                    .andIdEqualTo(question.getCreator());
            List<User> users = userMapper.selectByExample(userExample);
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(users.get(0));
            questionDTOS.add(questionDTO);
        }
        pageDTO.setQuestions(questionDTOS);

        return pageDTO;
    }

    public PageDTO findAll(Integer id, Integer page, Integer size) {

        PageDTO pageDTO = new PageDTO();
        Integer totalPage;

        QuestionExample example = new QuestionExample();
        example.createCriteria().andCreatorEqualTo(id);
        Integer totalCount = (int) questionMapper.countByExample(example);
        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) page = 1;
        if (page > totalPage) page = totalPage;

        pageDTO.setPaginaton(totalPage, page);

        Integer offset = size * (page - 1);
        QuestionExample example2 = new QuestionExample();
        example2.createCriteria().andCreatorEqualTo(id);
        example2.setOffset(offset);
        example2.setSize(size);
        List<Question> questions = questionMapper.selectByExample(example2);
        List<QuestionDTO> questionDTOS = new ArrayList<>();

        for (Question question : questions) {
            UserExample userExample = new UserExample();
            userExample.createCriteria()
                    .andIdEqualTo(question.getCreator());
            List<User> users = userMapper.selectByExample(userExample);
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            questionDTO.setUser(users.get(0));
            questionDTOS.add(questionDTO);
        }
        pageDTO.setQuestions(questionDTOS);
        return pageDTO;
    }

    public QuestionDTO findById(Long id) {
        Question question = questionMapper.selectByPrimaryKey(id.intValue());
        if (question == null) {
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDTO);
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andIdEqualTo(question.getCreator());
        List<User> users = userMapper.selectByExample(userExample);
        questionDTO.setUser(users.get(0));
        return questionDTO;
    }

    public Question findQuestionById(Integer id) {
        return questionMapper.selectByPrimaryKey(id);
    }

    public void createOrUpdate(Question question) {
        if (question.getId() == null) {
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(System.currentTimeMillis());
            question.setViewCount(0);
            question.setCommentCount(0);
            question.setLikeCount(0);
            questionMapper.insertSelective(question);

        } else {
            QuestionExample example = new QuestionExample();
            example.createCriteria().andIdEqualTo(question.getId());
            question.setGmtModified(System.currentTimeMillis());
            int update = questionMapper.updateByExampleSelective(question, example);
            if (update != 1) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
        }
    }

    public void incrementViews(Long id) {
        Question question = questionMapper.selectByPrimaryKey(id.intValue());
        question.setViewCount(question.getViewCount() + 1);
        QuestionExample example = new QuestionExample();
        example.createCriteria().andIdEqualTo(id.intValue());
        questionMapper.updateByExampleSelective(question,example);
    }

}
