package com.bw.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by 钰杰 on 2019/8/21.
 */
public class WebFilter extends ZuulFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        System.out.println("这是网关过滤器");
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        if (request.getMethod().equals("OPTIONS")){
            return null;
        }
        int login = request.getRequestURI().indexOf("login");
        if(login>0){
            return null;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")){
            String authHeader = authorization.substring(7);
            Claims claims = jwtUtil.parseJWT(authHeader);
            if (claims != null) {
                if ("admin".equals(claims.get("roles"))) {
                    currentContext.addZuulRequestHeader("Authorization",
                            authHeader);
                    System.out.println("token 验证通过，添加了头信息" + authHeader);
                    return null;
                }
            }
        }
        currentContext.setSendZuulResponse(false);// 终止运行
        currentContext.setResponseStatusCode(401);// http状态码
        currentContext.setResponseBody("无权访问");
        currentContext.getResponse().setContentType(
                "text/html;charset=UTF-8");
        return null;
    }
}
