package com.qaprosoft.carina.core.foundation.webdriver.core.capability;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qaprosoft.carina.browsermobproxy.ProxyPool;
import com.qaprosoft.carina.core.foundation.commons.SpecialKeywords;
import com.qaprosoft.carina.core.foundation.report.ReportContext;
import com.qaprosoft.carina.core.foundation.utils.Configuration;
import com.qaprosoft.carina.core.foundation.utils.R;
import com.qaprosoft.carina.core.foundation.webdriver.IDriverPool;
import com.qaprosoft.carina.proxy.SystemProxy;

import io.appium.java_client.remote.options.SupportsBrowserNameOption;

public abstract class IAbstactCapabilities<T extends Capabilities> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String CAPABILITIES_PREFIX = SpecialKeywords.CAPABILITIES + ".";

    public abstract T getCapabilities();

    public abstract T getCapabilitiesWithCustom(Capabilities capabilities);

    // todo add description (delete)
    protected Capabilities getBrowserCapabilities(String browser, String testName) {
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability(SupportsBrowserNameOption.BROWSER_NAME_OPTION, browser);

        if (!IDriverPool.DEFAULT.equalsIgnoreCase(testName)) {
            // #1573: remove "default" driver name capability registration
            capabilities.setCapability("name", testName);
        }

        if (isProxyConfigurationAvailable()) {
            capabilities.setCapability(CapabilityType.PROXY, setupProxy());
        }
        return capabilities;
    }

    /**
     * Read all properties which starts from "capabilities.*" prefix and add return them as Capabilities object
     */
    protected Capabilities getConfigurationCapabilities() {
        MutableCapabilities capabilities = new MutableCapabilities();
        Properties properties = R.CONFIG.getProperties();

        Set<String> capabilitiesNames = R.CONFIG.getProperties()
                .stringPropertyNames()
                .stream()
                .filter((propertyName) -> propertyName.startsWith(CAPABILITIES_PREFIX))
                .map(capability -> capability.replace(CAPABILITIES_PREFIX, ""))
                .collect(Collectors.toSet());

        for (String capabilityName : capabilitiesNames) {
            String value = properties.getProperty(CAPABILITIES_PREFIX + capabilityName);

            if (value.isEmpty()) {
                continue;
            }

            if (isNumber(value)) {
                LOGGER.debug("Adding {} to capabilities as integer", capabilityName);
                capabilities.setCapability(capabilityName, Integer.parseInt(value));
                continue;
            }

            if ("false".equalsIgnoreCase(value)) {
                capabilities.setCapability(capabilityName, false);
                continue;
            }

            if ("true".equalsIgnoreCase(value)) {
                capabilities.setCapability(capabilityName, true);
                continue;
            }

            capabilities.setCapability(capabilityName, value);
        }

        return capabilities;
    }

    protected boolean isNumber(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return false;
        }

        return true;
    }

    // todo move to proxy utils
    protected static boolean isProxyConfigurationAvailable() {
        String proxyHost = Configuration.get(Configuration.Parameter.PROXY_HOST);
        String proxyPort = Configuration.get(Configuration.Parameter.PROXY_PORT);
        if (Configuration.get(Configuration.Parameter.BROWSERMOB_PROXY).equals("true")) {
            proxyPort = Integer.toString(ProxyPool.getProxyPortFromThread());
        }
        return proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty();
    }

    // todo move to proxy utils
    protected static Proxy setupProxy() {
        ProxyPool.setupBrowserMobProxy();
        SystemProxy.setupProxy();

        String proxyHost = Configuration.get(Configuration.Parameter.PROXY_HOST);
        String proxyPort = Configuration.get(Configuration.Parameter.PROXY_PORT);
        String noProxy = Configuration.get(Configuration.Parameter.NO_PROXY);

        if (Configuration.get(Configuration.Parameter.BROWSERMOB_PROXY).equals("true")) {
            proxyPort = Integer.toString(ProxyPool.getProxyPortFromThread());
        }
        List<String> protocols = Arrays.asList(Configuration.get(Configuration.Parameter.PROXY_PROTOCOLS).split("[\\s,]+"));

        // TODO: test removal comparing with null
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {

            org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
            String proxyAddress = String.format("%s:%s", proxyHost, proxyPort);

            if (protocols.contains("http")) {
                LOGGER.info(String.format("Http proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setHttpProxy(proxyAddress);
            }

            if (protocols.contains("https")) {
                LOGGER.info(String.format("Https proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setSslProxy(proxyAddress);
            }

            if (protocols.contains("ftp")) {
                LOGGER.info(String.format("FTP proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setFtpProxy(proxyAddress);
            }

            if (protocols.contains("socks")) {
                LOGGER.info(String.format("Socks proxy will be set: %s:%s", proxyHost, proxyPort));
                proxy.setSocksProxy(proxyAddress);
            }

            if (!noProxy.isEmpty()) {
                proxy.setNoProxy(noProxy);
            }

            return proxy;
        }

        return null;
    }

    protected String getAutoDownloadFolderPath() {
        // use custom folder for auto download
        String autoDownloadFolder = Configuration.get(Configuration.Parameter.AUTO_DOWNLOAD_FOLDER);
        File autoDownloadPath;

        if (!autoDownloadFolder.isEmpty()) {
            autoDownloadPath = new File(autoDownloadFolder);
            boolean isCreated = autoDownloadPath.exists() && autoDownloadPath.isDirectory();
            if (!isCreated) {
                isCreated = autoDownloadPath.mkdir();
            } else {
                LOGGER.info("Folder for auto download already exists: " + autoDownloadPath.getAbsolutePath());
            }
        } else {
            // if no AUTO_DOWNLOAD_FOLDER defined use artifacts folder
            autoDownloadPath = ReportContext.getArtifactsFolder();
        }

        return autoDownloadPath.getAbsolutePath();
    }

}
