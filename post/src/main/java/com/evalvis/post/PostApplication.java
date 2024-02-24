package com.evalvis.post;

import org.jsoup.safety.Safelist;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.evalvis.post", "com.evalvis.security"})
public class PostApplication {
    public static void main(String[] args) {
        SpringApplication.run(PostApplication.class, args);
    }

    @Bean
    public Safelist safelist() {
        return Safelist
                .basic()
                .addTags("img", "s", "h1", "h2")
                .addAttributes("img", "height", "src", "width")
                .addProtocols("img", "src", "http", "https", "data");
    }
}
