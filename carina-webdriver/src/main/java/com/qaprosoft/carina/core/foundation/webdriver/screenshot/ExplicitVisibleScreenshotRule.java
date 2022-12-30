package com.qaprosoft.carina.core.foundation.webdriver.screenshot;

import com.qaprosoft.carina.core.foundation.webdriver.ScreenshotType;

public class ExplicitVisibleScreenshotRule implements IScreenshotRule {

    @Override
    public ScreenshotType getEventType() {
        return ScreenshotType.EXPLICIT_VISIBLE;
    }

    @Override
    public boolean isTakeScreenshot() {
        return true;
    }

    @Override
    public boolean isAllowFullSize() {
        return false;
    }
}