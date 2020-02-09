package com.lyj.community.service;

import com.lyj.community.dto.PageDTO;
import com.lyj.community.dto.QuestionDTO;
import com.lyj.community.dto.QuestionQueryDTO;
import com.lyj.community.exception.CustomizeErrorCode;
import com.lyj.community.exception.CustomizeException;
import com.lyj.community.mapper.CommentMapper;
import com.lyj.community.mapper.QuestionMapper;
import com.lyj.community.mapper.UserMapper;
import com.lyj.community.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    public PageDTO list(String search,Integer page, Integer size) {

        if (StringUtils.isNotBlank(search)) {
            String[] tags = StringUtils.split(search, " ");
            search = Arrays
                    .stream(tags)
                    .filter(StringUtils::isNotBlank)
                    .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining("|"));
        }

        PageDTO<QuestionDTO> pageDTO = new PageDTO();
        Integer totalPage;

        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);

        Integer totalCount = questionMapper.countBySearch(questionQueryDTO);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) page = 1;
        if (page > totalPage) page = totalPage;

        pageDTO.setPaginaton(totalPage, page);
        Integer offset = page < 1 ? 0 : size * (page - 1);
        questionQueryDTO.setPage(offset);
        questionQueryDTO.setSize(size);
        List<Question> questions = questionMapper.selectBySearch(questionQueryDTO);
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
        pageDTO.setData(questionDTOS);

        return pageDTO;
    }

    public PageDTO list(Integer id, Integer page, Integer size) {

        PageDTO<QuestionDTO> pageDTO = new PageDTO();
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
        example2.setOrderByClause("gmt_create desc");
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
        pageDTO.setData(questionDTOS);
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
        questionMapper.updateByExampleSelective(question, example);
    }

    public List<QuestionDTO> selectTags(QuestionDTO queryDTO) {
        if (StringUtils.isBlank(queryDTO.getTag())) {
            return new ArrayList<>();
        }

        String[] tags = StringUtils.split(queryDTO.getTag(), ',');
        String regexpTag = Arrays
                .stream(tags)
                .filter(StringUtils::isNotBlank)
                .map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("|"));
        Question question = new Question();
        question.setId(queryDTO.getId());
        question.setTag(regexpTag);
        List<Question> questions = questionMapper.selectTags(question);
        List<QuestionDTO> questionDTOS = questions.stream().map(q -> {
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(q, questionDTO);
            return questionDTO;
        }).collect(Collectors.toList());
        return questionDTOS;
    }

}
