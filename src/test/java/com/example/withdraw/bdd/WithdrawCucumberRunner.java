package com.example.withdraw.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * JUnit4 entry point that runs the Gherkin {@code withdraw.feature} scenarios via
 * Cucumber. Surefire is configured to pick up {@code *CucumberRunner} classes.
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features",
        glue = "com.example.withdraw.bdd",
        plugin = {"pretty", "html:target/cucumber-reports.html"})
public class WithdrawCucumberRunner {
}
