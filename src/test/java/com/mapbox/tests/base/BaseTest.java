package com.mapbox.tests.base;

import com.mapbox.core.channel.GrpcChannelFactory;
import com.mapbox.core.config.ConfigManager;
import com.mapbox.services.campaign.CampaignClient;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.lang.reflect.Method;

/**
 * Base test class providing common setup and teardown
 * All test classes should extend this
 */
@Slf4j
public abstract class BaseTest {

    protected static GrpcChannelFactory channelFactory;
    protected CampaignClient campaignClient;

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        log.info("=== Starting Test Suite ===");
        log.info("Environment: {}", ConfigManager.getConfig().environment());
        log.info("gRPC Host: {}", ConfigManager.getConfig().grpcHost());
        log.info("gRPC Port: {}", ConfigManager.getConfig().grpcPort());

        channelFactory = new GrpcChannelFactory();

        // Add environment info to Allure report
        Allure.addAttachment("Environment", "text/plain",
                ConfigManager.getConfig().environment());
        Allure.addAttachment("gRPC Endpoint", "text/plain",
                ConfigManager.getConfig().grpcHost() + ":" + ConfigManager.getConfig().grpcPort());
    }

    @BeforeMethod(alwaysRun = true)
    public void testSetup(Method method) {
        log.info("=== Starting Test: {} ===", method.getName());

        // Initialize clients
        campaignClient = new CampaignClient(channelFactory);

        // Add test info to Allure
        Allure.addAttachment("Test Method", "text/plain", method.getName());
    }

    @AfterSuite(alwaysRun = true)
    public void globalTeardown() {
        log.info("=== Test Suite Completed ===");

        if (channelFactory != null) {
            channelFactory.shutdownAll();
        }
    }

    /**
     * Utility method to add attachment to Allure report
     */
    protected void addAllureAttachment(String name, String content) {
        Allure.addAttachment(name, "text/plain", content);
    }

    /**
     * Utility method to add step to Allure report
     */
    protected void allureStep(String stepName) {
        Allure.step(stepName);
    }
}

