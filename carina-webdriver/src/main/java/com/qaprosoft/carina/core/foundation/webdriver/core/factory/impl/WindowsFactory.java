/*******************************************************************************
 * Copyright 2020-2022 Zebrunner Inc (https://www.zebrunner.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.webdriver.core.factory.impl;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.webdriver.core.capability.impl.windows.WindowsCapabilities;
import com.qaprosoft.carina.core.foundation.webdriver.core.factory.AbstractFactory;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.options.WindowsOptions;

/**
 * WindowsFactory creates instance {@link WebDriver} for Windows native application testing.
 * 
 * @author Sergei Zagriychuk (sergeizagriychuk@gmail.com)
 */
public class WindowsFactory extends AbstractFactory<WindowsOptions> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public WebDriver create(String name, WindowsOptions capabilities, String seleniumHost) {

        if (seleniumHost == null) {
            seleniumHost = Configuration.getSeleniumUrl();
        }
        LOGGER.debug("selenium: " + seleniumHost);

        String driverType = Configuration.getDriverType(capabilities);
        if (!SpecialKeywords.WINDOWS.equals(driverType)) {
            throw new RuntimeException(String.format("Driver type %s is not applicable for Windows driver", driverType));
        }

        WindowsDriver driver = null;
        if (isCapabilitiesEmpty(capabilities)) {
            capabilities = getCapabilities(name);
        }

        URL url;
        try {
            url = new URL(seleniumHost);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed appium URL!", e);
        }
        driver = new WindowsDriver(url, capabilities);

        return driver;
    }

    private WindowsOptions getCapabilities(String name) {
        return new WindowsCapabilities().getCapability(name);
    }

    @Override
    public WebDriver registerListeners(WebDriver driver, WebDriverEventListener... listeners) {
        return super.registerListeners(driver, listeners);
    }

}
