package com.bw.fyj.util;

import com.bw.fyj.pojo.User;
import com.bw.fyj.service.IMailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MyUserDetails implements UserDetailsService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Reference
    private IMailService iMailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("登陆的用户名：" + username);

        User byUsername = iMailService.findByUsername(username);

        /**
         * 拥有访问用户列表的权限
         */

        List arrayList = new ArrayList<>();
        if (StringUtils.isNotBlank(byUsername.getRoles())) {
            String[] roles = byUsername.getRoles().split(",");
            for (String role : roles) {
                if (StringUtils.isNotBlank(role)) {
                    //权限数组
                    SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role.trim());
                    arrayList.add(simpleGrantedAuthority);
                }
            }
        }
        log.info("数据库中的信息：" + byUsername.getUsername(), byUsername.getPassword(), arrayList);
        org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(byUsername.getUsername(), byUsername.getPassword(), arrayList);
        return user;
    }

}
