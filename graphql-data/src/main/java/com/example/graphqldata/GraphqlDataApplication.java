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

    @Bean
    ApplicationRunner applicationRunner(UserRepository repository) {
        return args -> {

            Flux<User> all = repository
                    .findAll(QUser.user.name.startsWith("J").not());

            repository
                    .deleteAll()
                    .thenMany(Flux.just("Jvrgen", "Josh", "Joe", "Dave", "Mark", "Yuxin", "Madhura", "Olga"))
                    .map(name -> new User(null, name))
                    .flatMap(repository::save)
                    .thenMany(all)
                    .subscribe(System.out::println);
        };
    }
}

@Controller
class ProfileController {
    @SchemaMapping(typeName = "User")
    Profile profile(User user) {
        return new Profile(user.id());
    }
}

@GraphQlRepository
interface UserRepository extends ReactiveCrudRepository<User, String>,
        ReactiveQuerydslPredicateExecutor<User> {
}

@Document
record User(@Id String id, String name) {
}

record Profile(String id) {
}