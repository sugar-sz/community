package com.lyj.community.service;

import com.lyj.community.dto.CommentDTO;
import com.lyj.community.enums.CommentTypeEnum;
import com.lyj.community.enums.NotificationEnum;
import com.lyj.community.enums.NotificationStatusEnum;
import com.lyj.community.exception.CustomizeErrorCode;
import com.lyj.community.exception.CustomizeException;
import com.lyj.community.mapper.CommentMapper;
import com.lyj.community.mapper.NotificationMapper;
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

    @Autowired
    private NotificationMapper notificationMapper;

    @Transactional
    public void insert(Comment comment, User commentator) {
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
            //新建评论
            commentMapper.insert(comment);
            //增加评论数
            QuestionExample example = new QuestionExample();
            example.createCriteria().andIdEqualTo(question.getId());
            question.setCommentCount(question.getCommentCount() + 1);
            questionMapper.updateByExampleSelective(question, example);
            //创建通知
            createNotify(comment, Long.parseLong(String.valueOf(question.getCreator())),
                    commentator.getName(), question.getTitle(),
                    NotificationEnum.REPLY_QUESTION, Long.parseLong(String.valueOf(question.getId())));
        } else { //回复评论
            Comment dbComment = commentMapper.selectByPrimaryKey(comment.getParentId());
            if (null == dbComment) {
                throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
            }
            //回复的问题
            Question question = questionMapper.selectByPrimaryKey(dbComment.getParentId().intValue());
            if (null == question) {
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            //新建评论
            commentMapper.insert(comment);
            //增加评论数
            CommentExample example = new CommentExample();
            example.createCriteria().andIdEqualTo(dbComment.getId());
            dbComment.setCommentcount(dbComment.getCommentcount() + 1);
            commentMapper.updateByExample(dbComment, example);
            //创建通知
            createNotify(comment, Long.parseLong(String.valueOf(dbComment.getCommentor())), commentator.getName(),
                    question.getTitle(), NotificationEnum.REPLY_COMMENT,
                    Long.parseLong(String.valueOf(question.getId())));
        }

    }

    private void createNotify(Comment comment, Long receiver, String notifierName, String outherTitle, NotificationEnum notificationType,Long outerId) {
        if(receiver.intValue() == comment.getCommentor()){
            return;
        }
        Notification notification = new Notification();
        notification.setGmtCreate(System.currentTimeMillis());
        notification.setType(notificationType.getType());
        notification.setOuterid(outerId);
        notification.setNotifier(Long.parseLong(String.valueOf(comment.getCommentor())));
        notification.setStatus(NotificationStatusEnum.UNREAD.getStatus());
        notification.setReceiver(receiver);
        notification.setNotifiername(notifierName);
        notification.setOutertitle(outherTitle);
        notificationMapper.insert(notification);
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
