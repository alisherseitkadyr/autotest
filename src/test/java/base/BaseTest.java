package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.*;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class BaseTest {
    protected WebDriver driver;
    protected final Logger log = LogManager.getLogger(this.getClass());

    @Parameters({"runMode", "browser"})
    @BeforeMethod
    public void setUp(@Optional("local") String runMode,
                      @Optional("chrome") String browser) throws Exception {

        log.info("=== SETUP START (runMode={}, browser={}) ===", runMode, browser);

        if ("cloud".equalsIgnoreCase(runMode)) {
            driver = createSauceDriver(browser);
        } else {
            driver = createLocalDriver(browser);
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));

        log.info("Driver started successfully");
    }

    private WebDriver createLocalDriver(String browser) {
        if ("firefox".equalsIgnoreCase(browser)) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
            return new FirefoxDriver(options);
        }
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,800");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        return new ChromeDriver(options);
    }

    private WebDriver createSauceDriver(String browser) throws Exception {
        String sauceUser = System.getenv().getOrDefault("SAUCE_USERNAME", "");
        String sauceKey = System.getenv().getOrDefault("SAUCE_ACCESS_KEY", "");
        String region = System.getenv().getOrDefault("SAUCE_REGION", "us-west-1");

        if (sauceUser.isBlank() || sauceKey.isBlank()) {
            throw new RuntimeException("SAUCE_USERNAME / SAUCE_ACCESS_KEY env vars are not set.");
        }

        String hub = "https://ondemand." + region + ".saucelabs.com/wd/hub";
        URL url = new URL("https://" + sauceUser + ":" + sauceKey + "@ondemand." + region + ".saucelabs.com/wd/hub");

        MutableCapabilities caps;
        if ("firefox".equalsIgnoreCase(browser)) {
            caps = new FirefoxOptions();
            caps.setCapability("browserName", "firefox");
        } else {
            caps = new ChromeOptions();
            caps.setCapability("browserName", "chrome");
        }

        caps.setCapability("platformName", "Windows 11");
        caps.setCapability("browserVersion", "latest");

        Map<String, Object> sauceOptions = new HashMap<>();
        sauceOptions.put("name", "Assignment6-DDT-Login-" + browser);
        sauceOptions.put("build", "Assignment6-" + System.currentTimeMillis());
        sauceOptions.put("screenResolution", "1280x800");

        caps.setCapability("sauce:options", sauceOptions);

        return new RemoteWebDriver(url, caps);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        log.info("=== TEARDOWN START ===");
        if (driver != null) {
            driver.quit();
            log.info("Driver quit successfully");
        }
        log.info("=== TEARDOWN END ===");
    }
}
