package com.xik.aibookkeeping;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
@MapperScan("com.xik.aibookkeeping.server.mapper")
@Slf4j
@EnableScheduling
public class AiBookkeepingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiBookkeepingApplication.class, args);
        log.info("server started");
    }

}
