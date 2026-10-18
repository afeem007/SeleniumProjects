package SeleniumFramworkDesign.base;

import java.io.IOException;

import org.openqa.selenium.WebDriverException;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import SeleniumFramworkDesign.utils.ExtentReporterNG;

public class Listeners implements ITestListener {
	ExtentReports extent = ExtentReporterNG.getReportObject(); // calling report method
	ThreadLocal<ExtentTest> extentTest = new ThreadLocal<ExtentTest>();

	@Override
	public void onTestStart(ITestResult result) {
		ExtentTest test = extent.createTest(result.getMethod().getMethodName());
		extentTest.set(test);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		getOrCreateTest(result).log(Status.PASS, "Test Passed");
	}

	@Override
	public void onTestFailure(ITestResult result) {
		ExtentTest currentTest = getOrCreateTest(result);
		currentTest.log(Status.FAIL, "Test Failed");
		currentTest.fail(result.getThrowable());

		if (!(result.getInstance() instanceof BaseTest)) {
			currentTest.log(Status.WARNING, "Failure screenshot unavailable: test instance is not a BaseTest.");
			return;
		}

		BaseTest baseTest = (BaseTest) result.getInstance();
		if (baseTest.driver == null) {
			currentTest.log(Status.WARNING, "Failure screenshot unavailable: WebDriver is not initialized.");
			return;
		}

		try {
			String filepath = baseTest.getScreenshot(result.getMethod().getMethodName(), baseTest.driver);
			currentTest.addScreenCaptureFromPath(filepath, result.getMethod().getMethodName());
		} catch (IOException | WebDriverException e) {
			currentTest.log(Status.WARNING, "Could not capture failure screenshot: " + e.getMessage());
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		ExtentTest currentTest = getOrCreateTest(result);
		currentTest.log(Status.SKIP, "Test skipped");
		if (result.getThrowable() != null) {
			currentTest.skip(result.getThrowable());
		}
	}

	@Override
	public void onFinish(ITestContext context) {
		extent.flush();
	}

	private ExtentTest getOrCreateTest(ITestResult result) {
		ExtentTest currentTest = extentTest.get();
		if (currentTest == null) {
			currentTest = extent.createTest(result.getMethod().getMethodName());
			extentTest.set(currentTest);
		}
		return currentTest;
	}

}
