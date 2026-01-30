package tests;

import base.BaseTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.LoginPage;
import utils.ExcelReader;
import utils.TestListener;

@Listeners(TestListener.class)
public class LoginDDTTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(LoginDDTTest.class);

    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return ExcelReader.readSheet("test-data/login-data.xlsx", "Sheet1");
    }

    @Test(dataProvider = "loginData")
    public void DDT_Login_Test(String caseId,
                              String username,
                              String password,
                              String expectedOutcome,
                              String expectedMessageContains) {

        log.info("=== DATASET START: {} ===", caseId);
        log.info("Input: username='{}', password='{}', expectedOutcome={}, expectedMsgContains='{}'",
                username, password.isEmpty() ? "(empty)" : "(hidden)", expectedOutcome, expectedMessageContains);

        LoginPage page = new LoginPage(driver);
        page.open();
        page.login(username, password);

        String actualMessage = page.getFlashMessage();
        log.info("Actual message: {}", actualMessage.replace("\n", " ").trim());

        if ("SUCCESS".equalsIgnoreCase(expectedOutcome)) {
            Assert.assertTrue(actualMessage.contains(expectedMessageContains),
                    "Expected success message to contain: " + expectedMessageContains + " but got: " + actualMessage);
        } else {
            Assert.assertTrue(actualMessage.contains(expectedMessageContains),
                    "Expected error message to contain: " + expectedMessageContains + " but got: " + actualMessage);
        }

        log.info("=== DATASET RESULT: {} PASSED ===", caseId);
    }
}
