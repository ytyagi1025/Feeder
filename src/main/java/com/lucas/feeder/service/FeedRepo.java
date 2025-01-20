package com.lucas.feeder.service;

import com.lucas.feeder.dao.PointsRepository;
import com.lucas.feeder.model.*;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FeedRepo {

    @Value("${portal.jodidaar.url}")
    private String jodidaarUrl;

    @Value("${portal.retailer.url}")
    private String retailerUrl;

    @Value("${account.type}")
    private String accountType;

    @Value("${jodidaar.portal.username}")
    private String jodidaarUserName;

    @Value("${jodidaar.portal.password}")
    private String jodidaarPassword;

    @Value("${retailer.portal.username}")
    private String retailerUserName;

    @Value("${retailer.portal.password}")
    private String retailerPassword;

    @Value("${feed.batch.size}")
    private int batchSize;

    private Account account;

    private String inputXPath = "(//div[@class='input-group']//input)[1]";

    @Autowired
    private PointsRepository pointsRepository;

    public void feedPoints() {
        feedRetailerPoints();
        feedJodidaarPoints();
    }

    private Account getAccount() {
        if (accountType.contains("retailer")) {
            return Account.RETAILER;
        } else {

            return Account.JODIDAAR;
        }
    }

    private void feedRetailerPoints() {
        PointsData result = pointsRepository.findFirstByStatusAndRetailerStatusAndCodeType(Status.FAILED, RetailerStatus.NOT_ATTEMPTED, CodeType.RETAILER);

        if (result == null) {
            log.info("No entries found to feed");
            return;
        }
        log.info("Feeding retailer points started");
        WebDriver webDriver = new ChromeDriver();
        webDriver.get(retailerUrl);
        webDriver.manage().window().maximize();
        WebElement userNameElement = webDriver.findElement(By.xpath("//input[@id='txtUserName']"));
        userNameElement.sendKeys(retailerUserName);
        WebElement passwordElement = webDriver.findElement(By.xpath("//input[@id='txtPwd']"));
        passwordElement.sendKeys(retailerPassword);
        WebElement login = webDriver.findElement(By.xpath("//input[@name='btnLogin']"));
        login.click();
        log.info("Login completed {}, {}", retailerUserName, retailerPassword);
        int completedCount = 0;
        while (result != null && completedCount < batchSize) {
            WebElement codeInputElement = webDriver.findElement(By.xpath(inputXPath));
            codeInputElement.sendKeys(result.getGenuineCode());
            WebElement submitButton = webDriver.findElement(By.xpath("//a[@id='MainContent_ancValidate']"));
            submitButton.click();
            WebElement submissionResult = webDriver.findElement(By.xpath("//span[@id='MainContent_dgGenuinePonits_Label2_0']"));
            String resultValue = submissionResult.getText();
            if (resultValue.contains("Genuine Code validated for Part No")) {
                result.setRetailerStatus(RetailerStatus.SUCCESS);
                result.setStatus(Status.SUCCESS);
                completedCount++;
            } else if (resultValue.contains("Genuine Code validated earlier by another Retailer")) {
                result.setRetailerStatus(RetailerStatus.VALIDATED_ELSE);
                result.setStatus(Status.SUCCESS);
            } else if (resultValue.contains("Points have already been claimed")) {
                result.setRetailerStatus(RetailerStatus.VALIDATED_EARLIER);
                result.setStatus(Status.SUCCESS);
            } else {
                result.setRetailerStatus(RetailerStatus.FAILED);
            }
            pointsRepository.save(result);
            WebElement backButton = webDriver.findElement(By.xpath("//a[contains(text(),'Back')]"));
            backButton.click();
            result = pointsRepository.findFirstByStatusAndRetailerStatusAndCodeType(Status.FAILED, RetailerStatus.NOT_ATTEMPTED, CodeType.RETAILER);
        }
        webDriver.quit();
        log.info("Feeding retailer points completed {}", completedCount);

    }


    private void feedJodidaarPoints() {
        PointsData result = pointsRepository.findFirstByStatusAndJodidaarStatusAndCodeType(Status.FAILED, JodidaarStatus.NOT_ATTEMPTED, CodeType.JODIDAR);

        if (result == null) {
            log.info("No entries found to feed");
            return;
        }
        log.info("Feeding jodidaar points started");
        WebDriver webDriver = new ChromeDriver();
        webDriver.get(jodidaarUrl);
        webDriver.manage().window().maximize();
        WebElement userNameElement = webDriver.findElement(By.xpath("//input[@id='txtUserName']"));
        userNameElement.sendKeys(jodidaarUserName);
        WebElement passwordElement = webDriver.findElement(By.xpath("//input[@id='txtPwd']"));
        passwordElement.sendKeys(jodidaarPassword);
        WebElement login = webDriver.findElement(By.xpath("//input[@name='btnLogin']"));
        login.click();
        log.info("Login completed {}, {}", jodidaarUserName, jodidaarPassword);
        int completedCount = 0;
        while (result != null && completedCount < batchSize) {
            WebElement codeInputElement = webDriver.findElement(By.xpath(inputXPath));
            codeInputElement.sendKeys(result.getGenuineCode());
            WebElement submitButton = webDriver.findElement(By.xpath("//a[@id='MainContent_ancValidate']"));
            submitButton.click();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            submitButton = webDriver.findElement(By.xpath("//a[@id='MainContent_ancValidate']"));
            submitButton.click();
            WebElement submissionResult = webDriver.findElement(By.xpath("//span[@id='MainContent_dgGenuinePonits_Label2_0']"));
            String resultValue = submissionResult.getText();
            if (resultValue.contains("Genuine Code validated for Part No")) {
                result.setJodidaarStatus(JodidaarStatus.SUCCESS);
                result.setStatus(Status.SUCCESS);
                completedCount++;
            } else if (resultValue.contains("Points have already been claimed")) {
                result.setJodidaarStatus(JodidaarStatus.VALIDATED_EARLIER);
                result.setStatus(Status.SUCCESS);
            } else if (resultValue.contains("Genuine Code validated earlier by another Jodidar")) {
                result.setJodidaarStatus(JodidaarStatus.VALIDATED_ELSE);
                result.setStatus(Status.SUCCESS);
            } else if (resultValue.contains("Genuine Code validated. Points not credited")) {
                result.setJodidaarStatus(JodidaarStatus.VALIDATED_OLD);
                result.setStatus(Status.SUCCESS);
            }
            else {
                result.setJodidaarStatus(JodidaarStatus.FAILED);
            }
            pointsRepository.save(result);
            WebElement backButton = webDriver.findElement(By.xpath("//a[contains(text(),'Back')]"));
            backButton.click();
            result = pointsRepository.findFirstByStatusAndJodidaarStatusAndCodeType(Status.FAILED, JodidaarStatus.NOT_ATTEMPTED, CodeType.JODIDAR);
        }
        webDriver.quit();
        log.info("Feeding jodidaar points completed {}", completedCount);
    }
}
