Readme.md
General:
 The solution was implemented in Java using Eclipse IDE. I chose Maven to gather the dependencies. The test runner was jUnit5.
 Logging was using the native jUnit5 plus a simple log4j2 framework.
Instructions to run the test
	1. Unzip the folder 
	2. Set up your running environment
		a. Install jdk 11 or later
		b. Install maven 3.8.6 or later
	3. Use an IDE ( I used Eclipse)
		a. Open the extracted project
		b. Update maven project
		c. Navigate to the apiTests class in the test/stemApiChallenge folder
		d. Right click --> Run As --> jUnit test
	OR
	
	In Terminal, navigate to folder and run: mvn test -Dtest=apiTests
	4. I have set up a basic log4j which will send test results to the console. Additional logging will be found in the jUnit pane.
Additonal run information
If you need to extend the tests, please add the tests in the parameterized portion. I have added a few additional tests as an example.

Test details
	Service description ( See https://www.omdbapi.com/#usage )

		The following services in  OMDb API will be automated:
		1. search(string): returns a list of all results that matched that search string. Takes pagination in account when generating the list. Query uses s parameter as specified here.
		2. get_by_id(string): returns the result based on the input id e.g. tt999999. Query uses i parameter as specified here.
		3. get_by_title(string): returns the result based on input string as title name. Query uses t parameter as specified here.
	
	Test description
	
	1.Using search method, search for all items that match the search string stem
		a. Assert that the result should contain at least 30 items
		b. Assert that the result contains items titled The STEM Journals and Activision: STEM - in the Videogame Industry
	
	2. From the list returned by search above, get imdbID for item titled Activision: STEM - in the Videogame Industry and use it to get details on this movie using get_by_id method.
		a. Assert that the movie was released on 23 Nov 2010 and was directed by Mike Feurstein
		b. I added an additional test to validate the basic get by id call
	3. Using get_by_title method, get item by title The STEM Journals and assert that the plot contains the string Science, Technology, Engineering and Math and has a runtime of 22 minutes.