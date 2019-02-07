package io.corbs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Todo {
    private Integer id;
    private String title = "";
    private Boolean completed = false;
    private Set<String> hashtags = Collections.emptySet();
}

@SpringBootApplication
@EnableBinding(TodosProcessorApp.Channels.class)
public class TodosProcessorApp {

    private static final Logger LOG = LoggerFactory.getLogger(TodosProcessorApp.class);

    interface Channels {
        @Output
        MessageChannel output();
        @Input
        SubscribableChannel input();
    }

    private Channels channels;

    @Autowired
    public TodosProcessorApp(Channels channels) {
        this.channels = channels;
    }

    @StreamListener("input")
    @Output("output")
    public Todo transform(@Payload Todo todo) {
        StringTokenizer tokenizer = new StringTokenizer(todo.getTitle());
        Set<String> hashTags = new HashSet<>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.startsWith("#")) {
                LOG.info("hashtag found" + token);
                hashTags.add(token);
            }
        }
        return Todo.builder()
            .id(todo.getId())
            .completed(todo.getCompleted())
            .title(todo.getTitle())
            .hashtags(hashTags)
            .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(TodosProcessorApp.class, args);
    }
}




