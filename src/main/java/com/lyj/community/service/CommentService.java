package com.lyj.community.service;

import com.lyj.community.dto.CommentDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public void insert(Comment comment) {
        if (null == comment.getParentId() || 0 == comment.getParentId()) {
            throw new CustomizeException(CustomizeErrorCode.TARGET_PARAM_NOT_FOUND);
        }

        if (null == comment.getType() || !CommentTypeEnum.isExist(comment.getType())) {
            throw new CustomizeException(CustomizeErrorCode.TYPE_PARAM_WRONG);
        }

        //回复问题
        if (comment.getType() == CommentTypeEnum.QUESTION.getType()) {
            Question question = questionMapper.selectByPrimaryKey(comment.getParentId().intValue());
            if (null == question) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            commentMapper.insert(comment);
            QuestionExample example = new QuestionExample();
            example.createCriteria().andIdEqualTo(question.getId());
            question.setCommentCount(question.getCommentCount() + 1);
            questionMapper.updateByExampleSelective(question, example);
        } else { //回复评论
            Comment dbComment = commentMapper.selectByPrimaryKey(comment.getParentId());
            if (null == dbComment) {
                throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
            }
            commentMapper.insert(comment);
            CommentExample example = new CommentExample();
            example.createCriteria().andIdEqualTo(dbComment.getId());
            dbComment.setCommentcount(dbComment.getCommentcount() + 1);
            commentMapper.updateByExample(dbComment, example);
        }

    }

    public List<CommentDTO> listByTargetId(Long id, CommentTypeEnum type) {
        CommentExample example = new CommentExample();
        example.createCriteria().andParentIdEqualTo(id).andTypeEqualTo(type.getType());
        example.setOrderByClause("gmt_create desc");
        //查询当前问题所有评论
        List<Comment> comments = commentMapper.selectByExample(example);
        if (comments.size() == 0) {
            return new ArrayList<>();
        }
        //获取去重后的评论人
        Set<Integer> commentators = comments.stream()
                .map(comment -> comment.getCommentor())
                .collect(Collectors.toSet());
        List<Integer> userIDs = new ArrayList<>(commentators);
        //查询所有用户信息,并且转换为Map
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIDs);
        List<User> users = userMapper.selectByExample(userExample);
        Map<Integer, User> userMap = users.stream().collect(Collectors.toMap(user -> user.getId(), user -> user));

        //构建CommentDTO集合 返回
        List<CommentDTO> commentDTOS = comments.stream().map(comment -> {
            CommentDTO commentDTO = new CommentDTO();
            BeanUtils.copyProperties(comment, commentDTO);
            Long commenter = Long.parseLong(String.valueOf(comment.getCommentor()));
            commentDTO.setCommentor(commenter);
            commentDTO.setCommentCount(comment.getCommentcount());
            commentDTO.setUser(userMap.get(comment.getCommentor()));
            return commentDTO;
        }).collect(Collectors.toList());

        return commentDTOS;
    }
}
