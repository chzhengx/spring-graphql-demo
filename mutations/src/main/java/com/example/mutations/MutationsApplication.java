package com.example.mutations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class MutationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MutationsApplication.class, args);
    }

}

/**
 * 用于处理GraphQL的查询和变更请求
 */
@Controller
class MutationsController {

    // 使用线程安全的HashMap作为模拟数据库
    private final Map<Integer, User> db = new ConcurrentHashMap<>();
    // 使用原子整数生成唯一用户ID
    private final AtomicInteger id = new AtomicInteger();

    // GraphQL变更映射，用于添加新用户
    @MutationMapping
//    @SchemaMapping(typeName = "Mutation", field = "addUser")
    User addUser(@Argument String name) {
        var id = this.id.incrementAndGet(); // 生成下一个用户ID
        var value = new User(id, name); // 创建新用户
        this.db.put(id, value); // 将用户存储在数据库模拟中
        return value; // 返回新创建的用户
    }

    // GraphQL查询映射，根据ID查询用户
    @QueryMapping
    User userById(@Argument Integer id) {
        return this.db.get(id); // 从数据库模拟中获取用户
    }
}

// 定义一个记录类型，表示用户
record User(Integer id, String name) {
}