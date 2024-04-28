package com.example.graphqldata;

import com.example.graphqldata.QUser;
import graphql.schema.idl.RuntimeWiring;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.ReactiveAggregationOperation;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.data.query.QuerydslDataFetcher;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.lang.annotation.Documented;

@SpringBootApplication
public class GraphqlDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphqlDataApplication.class, args);
    }

//    @Bean
//    RuntimeWiringConfigurer runtimeWiringConfigurer(UserRepository repository) {
//        return builder -> {
//            var data = QuerydslDataFetcher.builder(repository).many();
//            var datum = QuerydslDataFetcher.builder(repository).single();
//            builder.type("Query", wiring -> wiring
//                    .dataFetcher("user", datum)
//                    .dataFetcher("users", data));
//        };
//    }

    // 定义一个应用启动后执行的任务，操作用户数据
    @Bean
    ApplicationRunner applicationRunner(UserRepository repository) {
        return args -> {
            // 找出所有姓名不以"J"开头的用户
            Flux<User> all = repository
                    .findAll(QUser.user.name.startsWith("J").not());

            // 清空数据库，然后添加新的用户数据，并打印所有符合条件的用户
            repository
                    .deleteAll() // 删除所有用户
                    .thenMany(Flux.just("Jvrgen", "Josh", "Joe", "Dave", "Mark", "Yuxin", "Madhura", "Olga")) // 添加新用户姓名
                    .map(name -> new User(null, name)) // 将姓名转换为User对象
                    .flatMap(repository::save) // 保存到数据库
                    .thenMany(all) // 查询符合条件的用户
                    .subscribe(System.out::println); // 打印用户数据
        };
    }
}

/**
 * 处理GraphQL数据映射
 */
@Controller
class ProfileController {
    @SchemaMapping(typeName = "User")
    Profile profile(User user) {
        return new Profile(user.id()); // 根据用户数据生成Profile对象
    }
}

/**
 * 定义GraphQL仓库接口，扩展ReactiveCrudRepository和ReactiveQuerydslPredicateExecutor
 */
@GraphQlRepository
interface UserRepository extends ReactiveCrudRepository<User, String>,
        ReactiveQuerydslPredicateExecutor<User> {
}

// MongoDB文档对象，表示用户
@Document
record User(@Id String id, String name) {
}

// 简单记录类型，表示用户的Profile
record Profile(String id) {
}