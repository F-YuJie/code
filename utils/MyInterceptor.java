package com.bw.fyj.config;

import com.bw.fyj.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by 钰杰 on 2019/8/21.
 */
@Component
public class MyInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //前后端约定：前端请求微服务时需要添加头信息Authorization ,内容为Bearer+空格+token
        String authorization = request.getHeader("Authorization");
        if(authorization != null && authorization.startsWith("Bearer ")){
           final String substring = authorization.substring(7);
            Claims claims = jwtUtil.parseJWT(substring);
            if(claims != null){
                if("admin".equals(claims.get("roles"))){//判断是否为管理员
                    request.setAttribute("admin_claims", claims);
                }
                if("user".equals(claims.get("roles"))){//判断如果是用户
                    request.setAttribute("user_claims", claims);
                }
            }
        }
        return true;
    }
}