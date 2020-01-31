package com.lyj.community.controller;

import com.lyj.community.dto.AccessTokenDTO;
import com.lyj.community.dto.GithubUser;
import com.lyj.community.mapper.UserMapper;
import com.lyj.community.model.User;
import com.lyj.community.provider.GithubProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


@Controller
public class AuthorizeController {

    @Autowired
    private GithubProvider provider;

    @Autowired
    private UserMapper userMapper;

    @Value("${github.client_id}")
    private String client_id;

    @Value("${github.client_secret}")
    private String client_secret;

    @Value("${github.redirect_uri}")
    private String redirect_uri;


    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state,
                           HttpServletResponse response) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirect_uri);
        accessTokenDTO.setClient_id(client_id);
        accessTokenDTO.setState(state);
        accessTokenDTO.setClient_secret(client_secret);
        String accessToken = provider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = provider.getUser(accessToken);
//        System.out.println(githubUser.getName());
        if (null != githubUser) {
            User user = new User();
            String token = UUID.randomUUID().toString();
            user.setToken(token);
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            user.setBio(githubUser.getBio());
            user.setAvatarUrl(githubUser.getAvatarUrl());
            userMapper.insertUser(user);
            response.addCookie(new Cookie("token", token));
            //request.getSession().setAttribute("githubUser", githubUser);
            return "redirect:/";
        } else {
            return "redirect:/";
        }
    }
}
