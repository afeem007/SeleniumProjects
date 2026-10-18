package SeleniumFramworkDesign.base;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import SeleniumFramworkDesign.pages.LandingPage;
import io.github.bonigarcia.wdm.WebDriverManager;

public class BaseTest {

	public WebDriver driver;
	public LandingPage landingpage;
	private String baseUrl;

	public WebDriver initializeDriver() throws IOException {
	    Properties settings = loadSettings();
	    String environment = getSetting(settings, "APP_ENV", "app.env", "qa").trim().toUpperCase(Locale.ROOT);
	    if (!environment.equals("DEV") && !environment.equals("QA")) {
	        throw new IllegalArgumentException("APP_ENV must be either 'dev' or 'qa', but was: " + environment);
	    }
	    String urlKey = environment + "_BASE_URL";
	    String baseUrlProperty = environment.toLowerCase(Locale.ROOT) + ".base.url";
	    baseUrl = getSetting(settings, urlKey, baseUrlProperty, null);
	    if (baseUrl == null || baseUrl.isBlank()) {
	        throw new IOException("Missing " + environment + "_BASE_URL in .env");
	    }

	    Properties legacyProperties = new Properties();
	    try (java.io.InputStream input = BaseTest.class.getResourceAsStream("/config/GlobalData.properties")) {
	        if (input == null) {
	            throw new IOException("Missing classpath resource /config/GlobalData.properties");
	        }
	        legacyProperties.load(input);
	    }
	    String browserName = getSetting(settings, "BROWSER", "browser",
	            legacyProperties.getProperty("browser", "chrome"));
	    boolean headless = Boolean.parseBoolean(getSetting(settings, "HEADLESS", "headless", "false"));

	    if (browserName.equalsIgnoreCase("chrome")) {
	        WebDriverManager.chromedriver().setup();
	        ChromeOptions options = new ChromeOptions();
	        if (headless) {
	            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
	        }
	        driver = new ChromeDriver(options);
	    } else if (browserName.equalsIgnoreCase("edge")) {
	        EdgeOptions options = new EdgeOptions();
	        if (headless) {
	            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
	        }
	        String edgeDriverPath = getSetting(settings, "EDGE_DRIVER_PATH", "edge.driver.path", null);
	        if (edgeDriverPath == null || edgeDriverPath.isBlank()) {
	            driver = new EdgeDriver(options);
	        } else {
	            Path executable = Paths.get(edgeDriverPath).toAbsolutePath().normalize();
	            if (!Files.isRegularFile(executable)) {
	                throw new IOException("EDGE_DRIVER_PATH does not point to a driver file: " + executable);
	            }
	            EdgeDriverService service = new EdgeDriverService.Builder()
	                    .usingDriverExecutable(executable.toFile())
	                    .build();
	            driver = new EdgeDriver(service, options);
	        }
	    } else if (browserName.equalsIgnoreCase("firefox")) {
	        WebDriverManager.firefoxdriver().setup();
	        FirefoxOptions options = new FirefoxOptions();
	        if (headless) {
	            options.addArguments("-headless");
	        }
	        driver = new FirefoxDriver(options);
	    } else {
	        throw new IllegalArgumentException("Unsupported browser: " + browserName
	                + ". Set BROWSER to chrome, edge, or firefox.");
	    }

	    if (headless) {
	        driver.manage().window().setSize(new Dimension(1440, 900));
	    } else {
	        driver.manage().window().maximize();
	    }
	    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
	    return driver;
	}

	private Properties loadSettings() throws IOException {
		Properties settings = new Properties();
		Path envFile = Paths.get(System.getProperty("user.dir"), ".env");
		if (Files.exists(envFile)) {
			try (java.io.Reader reader = Files.newBufferedReader(envFile, StandardCharsets.UTF_8)) {
				settings.load(reader);
			}
		}
		return settings;
	}

	private String getSetting(Properties settings, String environmentKey, String systemProperty, String defaultValue) {
		String value = System.getProperty(systemProperty);
		if (value == null || value.isBlank()) {
			value = settings.getProperty(environmentKey);
		}
		if (value == null || value.isBlank()) {
			value = System.getenv(environmentKey);
		}
		return value == null || value.isBlank() ? defaultValue : value;
	}

	public String getRequiredSetting(String key) throws IOException {
		Properties settings = loadSettings();
		String environment = getEnvironment(settings);
		String environmentKey = environment + "_" + key;
		String systemProperty = environment.toLowerCase(Locale.ROOT) + "."
				+ key.toLowerCase(Locale.ROOT).replace('_', '.');
		String value = getSetting(settings, environmentKey, systemProperty, null);
		if (value == null || value.isBlank()) {
			throw new IOException("Missing required test setting " + environmentKey
					+ "; set it in .env or as an environment variable.");
		}
		return value;
	}

	private String getEnvironment(Properties settings) {
		String environment = getSetting(settings, "APP_ENV", "app.env", "qa")
				.trim().toUpperCase(Locale.ROOT);
		if (!environment.equals("DEV") && !environment.equals("QA")) {
			throw new IllegalArgumentException("APP_ENV must be either 'dev' or 'qa', but was: " + environment);
		}
		return environment;
	}

	public Path getTestDataPath(String fileName) throws IOException {
		Path dataPath = Paths.get(System.getProperty("user.dir"), "test-data",
				getEnvironment(loadSettings()).toLowerCase(Locale.ROOT), fileName);
		if (!Files.isRegularFile(dataPath)) {
			throw new IOException("Missing test data file: " + dataPath);
		}
		return dataPath;
	}

	@BeforeMethod(alwaysRun = true)
	public LandingPage launchApplication() throws IOException {
		driver = initializeDriver();
		landingpage = new LandingPage(driver);
		landingpage.goTo(baseUrl);
		return landingpage;
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	public List<HashMap<String, String>> getJsonDataToMap(String filepath) throws IOException {
//read json to string
		String jsonContent = FileUtils.readFileToString(new File(filepath), StandardCharsets.UTF_8);
//String to HashMap Jackson Databind
		ObjectMapper mapper = new ObjectMapper();
		List<HashMap<String, String>> data = mapper.readValue(jsonContent,
				new TypeReference<List<HashMap<String, String>>>() {
				});
		for (HashMap<String, String> row : data) {
			for (HashMap.Entry<String, String> entry : row.entrySet()) {
				String value = entry.getValue();
				if (value != null && value.matches("\\$\\{[A-Z][A-Z0-9_]*\\}")) {
					String key = value.substring(2, value.length() - 1);
					entry.setValue(getRequiredSetting(key));
				}
			}
		}
		return data;
	}

	public String getScreenshot(String TestCaseName, WebDriver driver) throws IOException {
		TakesScreenshot ts = (TakesScreenshot) driver;
		File source = ts.getScreenshotAs(OutputType.FILE);
		return saveScreenshot(TestCaseName, source);
	}

	public String getElementScreenshot(String testCaseName, WebElement element) throws IOException {
		if (element == null) {
			throw new IllegalArgumentException("Cannot capture a screenshot of a null WebElement.");
		}
		File source = element.getScreenshotAs(OutputType.FILE);
		return saveScreenshot(testCaseName, source);
	}

	private String saveScreenshot(String testCaseName, File source) throws IOException {
		Path reportDirectory = Paths.get(System.getProperty("user.dir"), "reportss");
		Files.createDirectories(reportDirectory);
		String safeTestCaseName = testCaseName.replaceAll("[^A-Za-z0-9._-]", "_");
		Path screenshotPath = reportDirectory.resolve(
				safeTestCaseName + "_" + UUID.randomUUID() + ".png");
		FileUtils.copyFile(source, screenshotPath.toFile());
		return screenshotPath.toAbsolutePath().toString();
	}

}