package stepdefinitions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cucumber.api.java.en.When;

public class BaseStepDefs {
	private Logger logger = LogManager.getLogger();

	@When("^I add two numbers$")
	public void i_add_two_numbers() throws Exception {
		// Write code here that turns the phrase above into concrete actions
		System.out.println("adding");
	}

}
