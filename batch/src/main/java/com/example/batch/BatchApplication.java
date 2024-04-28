package com.example.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.beans.Customizer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

}

/**
 * 处理GraphQL相关的数据请求
 */
@Controller
class BatchController {

    // 定义GraphQL查询，返回用户集合
    @QueryMapping
    Collection<User> users() {
        return List.of(new User(1, "A"), new User(2, "B")); // 静态返回两个用户
    }

    // 定义批量处理方法，输入是一组用户，输出是用户到账户的映射
    @BatchMapping
    Map<User, Account> account(List<User> users) {
        System.out.println("calling account for " + users.size() + " users.");
        // 为每个用户创建账户，并将用户与账户关联
        return users.stream()
                .collect(Collectors.toMap(user -> user, user -> new Account(user.id())));
    }

    // 单用户账户获取方法
//    @SchemaMapping(typeName = "User")
//    Account account(User user) {
//        System.out.println("getting account for user # " + user.id());
//        return new Account(user.id());
//    }
}

// 账户记录，包含账户ID
record Account(Integer id) {
}

// 用户记录，包含用户ID和名称
record User(Integer id, String name) {
}
