package com.example.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// 启用反应式方法安全性
@EnableReactiveMethodSecurity
@SpringBootApplication
public class SecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }

    // 配置基于内存的用户详情服务，用于认证
    @Bean
    MapReactiveUserDetailsService authentication() {
        // 创建用户凭证，使用默认的密码编码器，指定用户名、密码和角色
        var users = Map.of(
                        "corwin", new String[]{"USER"}, // 用户corwin具有USER角色
                        "chzhengx", "ADMIN,USER".split(",") // 用户chzhengx具有ADMIN和USER角色
                )
                .entrySet()
                .stream()
                .map(e -> org.springframework.security.core.userdetails.User.withDefaultPasswordEncoder()
                        .username(e.getKey())
                        .password("pw")
                        .roles(e.getValue())
                        .build()
                )
                .toList();
        return new MapReactiveUserDetailsService(users);
    }

    // 配置安全过滤链，定义安全行为
    @Bean
    SecurityWebFilterChain authorization(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // 禁用CSRF保护
                .authorizeExchange(ae -> ae.anyExchange().permitAll()) // 允许所有请求
                .httpBasic(Customizer.withDefaults()) // 启用HTTP Basic认证
                .build();
    }
}

/**
 * 处理GraphQL数据请求
 */
@Controller
class SecureGraphqlController {

    private final UsrService usr;

    SecureGraphqlController(UsrService usr) {
        this.usr = usr;
    }

    @MutationMapping
    Mono<User> insert(@Argument String name) {
        return this.usr.insert(name); // 调用服务层插入新用户
    }

    @QueryMapping
    Mono<User> userById(@Argument Integer id) {
        return this.usr.getUserById(id); // 根据ID查询用户
    }
}

/**
 * 包含业务逻辑和安全注解
 */
@Service
class UsrService {

    private final Map<Integer, User> db =
            new ConcurrentHashMap<>(); // 使用线程安全的HashMap模拟数据库

    private final AtomicInteger id = new AtomicInteger(); // 原子整数，用于生成用户ID

    @Secured("ROLE_USER") // 限制访问此方法的用户必须具有USER角色
    public Mono<User> getUserById(Integer id) {
        var user = this.db.get(id);
        return Mono.just(user);
    }

    @PreAuthorize("hasRole('ADMIN')") // 限制访问此方法的用户必须具有ADMIN角色
    public Mono<User> insert(String name) {
        var newUser = new User(id.incrementAndGet(), name); // 创建新用户
        this.db.put(newUser.id(), newUser); // 存储用户
        return Mono.just(newUser);
    }
}

// 记录类型，定义用户数据结构
record User(Integer id, String name) {
}