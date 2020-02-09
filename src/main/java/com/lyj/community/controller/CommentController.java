package com.lyj.community.controller;

import com.lyj.community.dto.CommentCreateDTO;
import com.lyj.community.dto.CommentDTO;
import com.lyj.community.dto.ResultDTO;
import com.lyj.community.enums.CommentTypeEnum;
import com.lyj.community.exception.CustomizeErrorCode;
import com.lyj.community.model.Comment;
import com.lyj.community.model.User;
import com.lyj.community.service.CommentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/comment")
    public ResultDTO post(@RequestBody CommentCreateDTO commentCreateDTO,
                          HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");

        if (null == user) {
            return ResultDTO.errorOf(CustomizeErrorCode.NO_LOGIN);
        }

        if (null == commentCreateDTO || StringUtils.isBlank(commentCreateDTO.getContent())) {
            return ResultDTO.errorOf(CustomizeErrorCode.CONTENT_IS_EMPTY);
        }
        Comment comment = new Comment();
        comment.setParentId(commentCreateDTO.getParentId());
        comment.setContent(commentCreateDTO.getContent());
        comment.setType(commentCreateDTO.getType());
        comment.setGmtCreate(System.currentTimeMillis());
        comment.setGmtModified(System.currentTimeMillis());
        comment.setLikeCount(0L);
        comment.setCommentcount(0);
        comment.setCommentor(user.getId());
        commentService.insert(comment,user);
        return ResultDTO.okOf();
    }

    @GetMapping("/comment/{id}")
    public ResultDTO comments(@PathVariable(name = "id") Long id){
        List<CommentDTO> commentDTOS = commentService.listByTargetId(id, CommentTypeEnum.COMMENT);
        return ResultDTO.okOf(commentDTOS);
    }
}
