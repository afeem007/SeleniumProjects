package SeleniumFramworkDesign.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReporterNG {

	public static ExtentReports getReportObject() {
		Path reportDirectory = Paths.get(System.getProperty("user.dir"), "report");
		try {
			Files.createDirectories(reportDirectory);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not create Extent report directory: " + reportDirectory, e);
		}

		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
		String reportName = "ExtentReport_" + timestamp + "_" + UUID.randomUUID() + ".html";
		Path reportPath = reportDirectory.resolve(reportName);
		ExtentSparkReporter reporter = new ExtentSparkReporter(reportPath.toString());
		reporter.config().setReportName("WebAutomation Results");
		reporter.config().setDocumentTitle("Test Results");
		ExtentReports extent = new ExtentReports();
		extent.attachReporter(reporter);
		extent.setSystemInfo("Tester", "shahil anand");
		return extent;
	}
}