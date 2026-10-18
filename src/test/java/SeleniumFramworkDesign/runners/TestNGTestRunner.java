package SeleniumFramworkDesign.runners;

import org.testng.annotations.Test;

import SeleniumFramworkDesign.base.CucumberRetry;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;

@CucumberOptions(features="src/test/resources/features/ui",
				 glue={"SeleniumFramworkDesign.stepDefinitions.ui", "SeleniumFramworkDesign.base"},
				 tags="@Regression",
				 monochrome=true,
				 plugin= {"html:target/cucumber.html"})
@org.testng.annotations.Listeners(SeleniumFramworkDesign.base.Listeners.class)
public class TestNGTestRunner extends AbstractTestNGCucumberTests{
	@Override
	@Test(groups = "cucumber", description = "Runs Cucumber scenarios", dataProvider = "scenarios",
			retryAnalyzer = CucumberRetry.class)
	public void runScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
		super.runScenario(pickleWrapper, featureWrapper);
	}
}
