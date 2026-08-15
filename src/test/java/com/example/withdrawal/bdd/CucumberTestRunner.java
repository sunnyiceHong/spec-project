package com.example.withdrawal.bdd;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * Cucumber JUnit runner for the withdrawal BDD scenarios.
 *
 * <p>Pointed at the {@code features} classpath folder with the glue code in
 * {@code com.example.withdrawal.bdd.step}. Scenarios can be filtered by tag at
 * runtime, e.g. {@code mvn test -Dcucumber.filter.tags=@Withdrawal}.</p>
 */
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "classpath:features",
        glue = "com.example.withdrawal.bdd.step",
        plugin = {
                "pretty",
                "html:target/cucumber-report.html",
                "json:target/cucumber.json"
        },
        monochrome = true
)
public class CucumberTestRunner {
}
