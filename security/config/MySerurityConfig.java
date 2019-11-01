package com.bw.fyj.config;

import com.bw.fyj.service.IMailService;
import com.bw.fyj.util.AjaxAuthFailureHandler;
import com.bw.fyj.util.ErrorResponse;
import com.bw.fyj.util.MyUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class MySerurityConfig extends WebSecurityConfigurerAdapter {


    /**
     * 用户仓库调用
     */
    @Reference
    private IMailService iMailService;

    /**
     * json 格式装换类
     */
    private ObjectMapper objectMapper;


    /**
     * ajax请求失败处理器。
     */
    private AjaxAuthFailureHandler ajaxAuthFailureHandler;

//    @Autowired
//    public MySerurityConfig(ObjectMapper objectMapper,
//                              AjaxAuthFailureHandler ajaxAuthFailureHandler) {
//        this.objectMapper = objectMapper;
//        this.ajaxAuthFailureHandler = ajaxAuthFailureHandler;
//
//    }

    /**
     * 验证用户名、密码和授权。
     *
     * @return
     * @throws UsernameNotFoundException
     */
//    @Override
//    public UserDetailsService userDetailsService() throws UsernameNotFoundException {
//        return (username) -> {
//            com.bw.fyj.pojo.User byUsername = iMailService.findByUsername(username);
//            if (byUsername == null) {
//                throw new UsernameNotFoundException("用户不存在！");
//            }
//            /**
//             * 拥有访问用户列表的权限
//             */
//            List simpleGrantedAuthorities = new ArrayList<>();
//            if (StringUtils.isNotBlank(byUsername.getRoles())) {
//                String[] roles = byUsername.getRoles().split(",");
//                for (String role : roles){
//                    if (StringUtils.isNotBlank(role)){
//                        simpleGrantedAuthorities.add(new SimpleGrantedAuthority(role.trim()));
//                    }
//                }
//            }
//            return new User(byUsername.getUsername(), byUsername.getPassword(), simpleGrantedAuthorities);
//        };
//    }

//    /**
//     * 配置自定义验证用户名、密码和授权的服务。
//     *
//     * @param auth
//     * @throws Exception
//     */
//    @Override
//    public void configure(AuthenticationManagerBuilder auth)
//            throws Exception {
//        auth.userDetailsService(this.userDetailsService());
//    }

//    /**
//     * 配置核心过滤器，对静态资源放行
//     * @param web
//     * @throws Exception
//     */
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().mvcMatchers("/resources/**");
//    }


    //注册UserDetailsService 的bean
    @Bean
    UserDetailsService myUserDetailService(){
        return new MyUserDetails();
    }

    //user Details Service验证
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailService());
    }


    /**
     * http请求配置：
     * 1.开启权限拦截路径。
     * 2.释放资源配置。
     * 3.登录请求配置。
     * 4.退出登录配置。
     * 5.默认开启csrf防护。
     *
     * @param http
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
//                .exceptionHandling()
//                .authenticationEntryPoint(unauthorizedEntryPoint())
//                .accessDeniedHandler(handleAccessDeniedForUser())
//                .and()
                .formLogin()
                .loginPage("/test/login")
                .permitAll()
                .and()
                .authorizeRequests()
//                .antMatchers( "/test/login","/test/logout").permitAll()
                .antMatchers("/static/**","/webjars/**").permitAll()
                .antMatchers("/test/public/**").permitAll()
//                .hasRole("admin")
//                .anyRequest()
//                .authenticated()
                .and()

//                .logout()
//                .logoutUrl("/test/logout")
//                .logoutSuccessUrl("/test/login")
//                .invalidateHttpSession(true)
//                .deleteCookies("JSESSIONID")
//                .and()
//                .userDetailsService(this.myUserDetailService())//当前登陆验证
                .csrf()
                .disable();
    }

//    /**
//     * 自定义 “未登入系统，直接请求资源” 处理器。
//     * 判断是否ajax请求，是ajax请求则返回json，否则跳转至登录页面。
//     * @return
//     */
//    private AuthenticationEntryPoint unauthorizedEntryPoint() {
//        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
//            String requestedWithHeader = request.getHeader("X-Requested-With");
//            if ("XMLHttpRequest".equals(requestedWithHeader)) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.setContentType("application/json");
//                response.getOutputStream().write(authException.getMessage().getBytes());
//            } else {
//                response.sendRedirect("/test/login");
//            }
//        };
//    }

//    /**
//     * 自定义登录成功处理器。
//     * @return
//     */
//    private AuthenticationSuccessHandler ajaxAuthSuccessHandler() {
//        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
//            response.setStatus(HttpServletResponse.SC_OK);
//            response.setContentType("application/json");
//            ObjectNode root = objectMapper.createObjectNode();
//            root.put("redirect",
//                    request.getRequestURI().equals("/test/public/findByPageAndContent") ? "/test/public/findByPageAndContent" : request.getRequestURI());
//            response.getOutputStream().write(root.toString().getBytes());
//        };
//    }

//    /**
//     * 自定义 “无权请求的资源” 处理器。
//     * @return
//     */
//    private AccessDeniedHandler handleAccessDeniedForUser() {
//        return (HttpServletRequest request,
//                HttpServletResponse response,
//                AccessDeniedException accessDeniedException) -> {
//            String requestedWithHeader = request.getHeader("X-Requested-With");
//            if ("XMLHttpRequest".equals(requestedWithHeader)) {
//                ErrorResponse errorResponse = new ErrorResponse(accessDeniedException.getMessage());
//                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                response.setContentType("application/json");
//                response.getOutputStream().write(objectMapper.writeValueAsBytes(errorResponse));
//            } else {
//                response.sendRedirect("/test/login");
//            }
//        };
//    }


//    /**
//     * 自定义退出成功处理器。
//     * @return
//     */
//    private LogoutSuccessHandler ajaxLogoutSuccessHandler() {
//        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
//            response.setStatus(HttpServletResponse.SC_OK);
//            response.setContentType("application/json");
//            ObjectNode root = objectMapper.createObjectNode();
//            root.put("redirect", "/test/login");
//
//            response.getOutputStream().write(root.toString().getBytes());
//        };
//    }

}