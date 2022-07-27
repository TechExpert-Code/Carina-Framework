package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.webdriver.core.capability.CapabilitiesBuilder;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.OptionsType;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.IAbstractFactory;
import com.qaprosoft.carina.core.foundation.webdriver.listener.EventFiringSeleniumCommandExecutor;

import io.appium.java_client.remote.options.SupportsAutomationNameOption;

/**
 * Desktop firefox browser
 */
public class FirefoxFactory extends IAbstractFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String testName, String seleniumHost) {
        return this.create(testName, seleniumHost, null);
    }

    @Override
    public WebDriver create(String testName, String seleniumHost, Capabilities capabilities) {
        CapabilitiesBuilder capabilitiesBuilder = CapabilitiesBuilder.builder();
        if (capabilities != null) {
            capabilitiesBuilder.fromCustomCapabilities(capabilities);
        }
        capabilitiesBuilder.chooseOptionsType(OptionsType.FIREFOX);
        Capabilities firefoxOptions = capabilitiesBuilder.build();

        LOGGER.debug("capabilities: {}", firefoxOptions);

        URL hostURL;
        try {
            hostURL = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed selenium URL!", e);
        }

        EventFiringSeleniumCommandExecutor ce = new EventFiringSeleniumCommandExecutor(hostURL);
        WebDriver driver = new RemoteWebDriver(ce, firefoxOptions);
        resizeBrowserWindow(driver, firefoxOptions);
        return driver;
    }

    /**
     * Determines if the driver is suitable for the current capabilities
     */
    public static boolean isSuitable(Capabilities capabilities) {
        if (capabilities.getPlatformName() != null) {
            return false;
        }

        if (capabilities.getCapability(SupportsAutomationNameOption.AUTOMATION_NAME_OPTION) != null) {
            return false;
        }

        if (capabilities.getBrowserName().equalsIgnoreCase(Browser.FIREFOX.browserName())) {
            return true;
        }

        return false;
    }
}
