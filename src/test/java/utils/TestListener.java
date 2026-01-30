package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;

import java.lang.reflect.Field;

public class TestListener implements ITestListener, ISuiteListener {
    private static final Logger log = LogManager.getLogger(TestListener.class);

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> tlTest = new ThreadLocal<>();

    @Override
    public void onStart(ISuite suite) {
        extent = ExtentManager.getExtent();
        log.info("=== SUITE START: {} ===", suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("=== SUITE END: {} ===", suite.getName());
        if (extent != null) extent.flush();
        tlTest.remove();
    }

    @Override
    public void onTestStart(ITestResult result) {
        if (extent == null) extent = ExtentManager.getExtent();

        String name = result.getMethod().getMethodName();
        ExtentTest t = extent.createTest(name);
        tlTest.set(t);

        log.info("TEST START: {}", name);
        t.info("Test started");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest t = safeGetTest(result);
        t.pass("PASSED");
        attachScreenshot(result, "PASS", t);
        log.info("TEST PASS: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest t = safeGetTest(result);
        Throwable thr = result.getThrowable();
        if (thr != null) t.fail(thr);
        else t.fail("FAILED (no exception object)");

        attachScreenshot(result, "FAIL", t);
        log.error("TEST FAIL: {}", result.getMethod().getMethodName(), thr);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest t = safeGetTest(result);
        Throwable thr = result.getThrowable();
        t.skip(thr != null ? ("SKIPPED: " + thr.getMessage()) : "SKIPPED");
        attachScreenshot(result, "SKIP", t);
        log.warn("TEST SKIP: {}", result.getMethod().getMethodName());
    }

    // If onTestStart didn't run for some reason, create a test entry now.
    private ExtentTest safeGetTest(ITestResult result) {
        ExtentTest t = tlTest.get();
        if (t == null) {
            if (extent == null) extent = ExtentManager.getExtent();
            String name = result.getMethod() != null ? result.getMethod().getMethodName() : "UnknownTest";
            t = extent.createTest(name);
            tlTest.set(t);
            t.warning("ExtentTest was null; created entry in fallback.");
        }
        return t;
    }

    private void attachScreenshot(ITestResult result, String status, ExtentTest t) {
        try {
            Object instance = result.getInstance();
            if (instance == null) return;

            // BaseTest has 'driver' field
            Field driverField = instance.getClass().getSuperclass().getDeclaredField("driver");
            driverField.setAccessible(true);

            Object driverObj = driverField.get(instance);
            if (!(driverObj instanceof org.openqa.selenium.WebDriver)) return;

            String fileName = result.getMethod().getMethodName() + "_" + status + "_" + System.currentTimeMillis();
            String relativePath = ScreenshotUtil.takeScreenshot((org.openqa.selenium.WebDriver) driverObj, fileName);

            if (relativePath != null) t.addScreenCaptureFromPath(relativePath);
        } catch (Exception e) {
            log.error("Screenshot capture failed", e);
        }
    }
}
