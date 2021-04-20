package hs.algorithmplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AlgorithmplatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgorithmplatformApplication.class, args);
    }

}
