package com.lyj.community.controller;

import com.lyj.community.dto.AccessTokenDTO;
import com.lyj.community.dto.GithubUser;
import com.lyj.community.provider.GithubProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class AuthorizeController {

    @Autowired
    private GithubProvider provider;

    @Value("${github.client_id}")
    private String client_id;

    @Value("${github.client_secret}")
    private String client_secret;

    @Value("${github.redirect_uri}")
    private String redirect_uri;


    @GetMapping("/callback")
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state){
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(redirect_uri);
        accessTokenDTO.setClient_id(client_id);
        accessTokenDTO.setState(state);
        accessTokenDTO.setClient_secret(client_secret);
        String accessToken = provider.getAccessToken(accessTokenDTO);
        GithubUser user = provider.getUser(accessToken);
        //System.out.println(user.getId());
        System.out.println(user.getName());
        return "index";
    }
}
