package com.lyj.community.service;

import com.lyj.community.dto.NotificationDTO;
import com.lyj.community.dto.PageDTO;
import com.lyj.community.enums.NotificationEnum;
import com.lyj.community.enums.NotificationStatusEnum;
import com.lyj.community.exception.CustomizeErrorCode;
import com.lyj.community.exception.CustomizeException;
import com.lyj.community.mapper.NotificationMapper;
import com.lyj.community.model.Notification;
import com.lyj.community.model.NotificationExample;
import com.lyj.community.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    public PageDTO list(Integer id, Integer page, Integer size) {
        PageDTO<NotificationDTO> pageDTO = new PageDTO();
        Integer totalPage;
        NotificationExample countExample = new NotificationExample();
        countExample.createCriteria().andReceiverEqualTo(Long.parseLong(String.valueOf(id)));
        Integer totalCount = (int) notificationMapper.countByExample(countExample);

        if (totalCount % size == 0) {
            totalPage = totalCount / size;
        } else {
            totalPage = totalCount / size + 1;
        }

        if (page < 1) page = 1;
        if (page > totalPage) page = totalPage;

        pageDTO.setPaginaton(totalPage, page);
        Integer offset = size * (page - 1);
        NotificationExample example = new NotificationExample();
        example.setOffset(offset);
        example.setSize(size);
        example.setOrderByClause("gmt_create desc");
        example.createCriteria().andReceiverEqualTo(Long.parseLong(String.valueOf(id)));
        List<Notification> notifications = notificationMapper.selectByExample(example);
        if (notifications.size() == 0) {
            return pageDTO;
        }
        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        for (Notification notification : notifications) {
            NotificationDTO notificationDTO = new NotificationDTO();
            BeanUtils.copyProperties(notification, notificationDTO);
            notificationDTO.setTypeName(NotificationEnum.nameOfType(notification.getType()));
            notificationDTO.setNotifierName(notification.getNotifiername());
            notificationDTO.setOuterTitle(notification.getOutertitle());
            notificationDTOS.add(notificationDTO);
        }
        pageDTO.setData(notificationDTOS);
        return pageDTO;
    }


    public Long unreadCount(Long userId) {
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria()
                .andReceiverEqualTo(userId)
                .andStatusEqualTo(NotificationStatusEnum.UNREAD.getStatus());
        return notificationMapper.countByExample(notificationExample);
    }

    public NotificationDTO read(Long id, User user) {
        Notification notification = notificationMapper.selectByPrimaryKey(id);
        if (notification == null) {
            throw new CustomizeException(CustomizeErrorCode.NOTIFICATION_NOT_FOUND);
        }
//        if (!Objects.equals(notification.getReceiver(), user.getId())) {
//            throw new CustomizeException(CustomizeErrorCode.READ_NOTIFICATION_FAIL);
//        }

        notification.setStatus(NotificationStatusEnum.READ.getStatus());
        notificationMapper.updateByPrimaryKey(notification);

        NotificationDTO notificationDTO = new NotificationDTO();
        BeanUtils.copyProperties(notification, notificationDTO);
        notificationDTO.setTypeName(NotificationEnum.nameOfType(notification.getType()));
        return notificationDTO;
    }
}
