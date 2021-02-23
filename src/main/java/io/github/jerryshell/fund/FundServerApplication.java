package io.github.jerryshell.fund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class FundServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FundServerApplication.class, args);
    }

}
