package com.lucas.feeder;

import com.lucas.feeder.service.ExcelRepo;
import com.lucas.feeder.service.FeedRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FeederApplication implements CommandLineRunner {

    @Autowired
    FeedRepo feedRepo;

    @Autowired
    ExcelRepo excelRepo;

    @Value("${load.data.excel}")
    Boolean shouldLoad;

    @Value("${feed.data}")
    Boolean shouldFeed;

    public static void main(String[] args) {
        SpringApplication.run(FeederApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (shouldLoad) {
            log.info("Loading data from excel file");
            boolean isLoaded = excelRepo.loadData();
            if (isLoaded) {
                log.info("Sucessfully loaded data from excel file");
            }
        }
        if (shouldFeed) {
            log.info("Attempting to feed points on portal");
            feedRepo.feedPoints();
        }

    }

}
