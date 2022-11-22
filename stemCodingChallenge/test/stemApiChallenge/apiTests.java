package stemApiChallenge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import helpers.CommonHelper;
import helpers.HttpHelper;
import helpers.JsonParserHelper;
import main.api;

class apiTests {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		api.LOG = LogManager.getLogger();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp(TestInfo testInfo) throws Exception {
		api.LOG.info("Running test "+testInfo.getDisplayName());
	}

	@AfterEach
	void tearDown(TestInfo testInfo) throws Exception {
		api.LOG.info("Completed test "+testInfo.getDisplayName());
	}

	//SEARCH BY STRING  TESTS
	
	/*
	 * Using search method, search for all items that match the search string stem
	 * Assert that the result should contain at least 30 items Assert that the
	 * result contains items titled The STEM Journals and Activision: STEM - in the
	 * Videogame Industry
	 * Note: The STEM Journals will not be found in the database so the test will validate that it does not exist
	 */

	private static Stream<Arguments> searchParameters(){ return
			Stream.of( 
					Arguments.of("stem","Title","Activision: STEM - in the Videogame Industry",30,false),
					Arguments.of("stem","Title","The STEM Journals",30,true),
					Arguments.of("no such title","Title","Activision: STEM - in the Videogame Industry",0,true)
					)
			;}

	@ParameterizedTest
	@MethodSource("searchParameters")
	@DisplayName("Verify -- search by string -- will return results and verify search results count and that it contain certain items")
	void searchByString(String searchTerm, String fieldName, String fieldValue,int expectedCount,boolean failureExpected) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		boolean testPassed = true;
		int searchResultsCount=0;
		JsonArray searchResultsArray =  api.searchByString(searchTerm);
		String failureMessage="";
		if (null == searchResultsArray) {
			failureMessage="No items returned in search results";
			testPassed&=false;
		}else {
			searchResultsCount=searchResultsArray.size();
			if(searchResultsCount>expectedCount) {
				api.LOG.info("PASS: Search result count: "+searchResultsCount+" greater than the expected: "+expectedCount);
				testPassed&=true;
			}else {
				api.LOG.error("FAIL: Search result count: "+searchResultsCount+" less than the expected: "+expectedCount);
				testPassed&=false;
			}
			failureMessage="Items returned in search results";
			JsonObject itemToSearch = api.getItemFromSearchResults(searchResultsArray, fieldName, fieldValue);
			if( null == itemToSearch) {
				failureMessage+="... but none matched the search criteria";
				api.LOG.error("FAIL: Item with field:"+fieldName+"= "+fieldValue+" was not found");
				testPassed&=false;
			}else {
				testPassed&=true;
				api.LOG.info("PASS: Item with field:"+fieldName+"= "+fieldValue+" was found");
			}
		}
		Assertions.assertTrue((testPassed & !failureExpected)||(!testPassed && failureExpected),failureMessage);
	}

	//GET BY ID  TESTS
	
	/*
	 * From the list returned by search above, get imdbID for item
	 * titled Activision: STEM - in the Videogame Industry and use it to get details
	 * on this movie using get_by_id method. Assert that the movie was released
	 * on 23 Nov 2010 and was directed by Mike Feurstein
	 */

	
	private static Stream<Arguments> getByIdParameters(){ return
			Stream.of( 
					Arguments.of("stem","Title","Activision: STEM - in the Videogame Industry","Released","23 Nov 2010",false),
					Arguments.of("stem","Title","Activision: STEM - in the Videogame Industry","Director","Mike Feurstein",false),
					Arguments.of("stem","Title","Activision: STEM - in the Videogame Industry","Released","23 Nov 2011",true)
					)
			;}

	@ParameterizedTest
	@MethodSource("getByIdParameters")
	@DisplayName("Verify -- get by id  -- item id is obtained from search results and then verify the item's information")
	void getByIdUsingSerachResults(String searchTerm, String filterByFieldName, String filterValue,String fieldName, String expectedFieldValue,boolean failureExpected) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		boolean testPassed = false;
		JsonObject jo=api.getItemUsingSearch(searchTerm,filterByFieldName,filterValue);
		String failureMessage="";
		if (null == jo) {
			failureMessage="No item found";
		}else {
			String imdbId = JsonParserHelper.getFieldFromJsonObject(jo, "imdbID");
			imdbId=CommonHelper.removeQuotes(imdbId);
			getById(imdbId,  fieldName,  expectedFieldValue, failureExpected);
		}
	}
	
	//This test was not asked for but it tests the basic get by id call
	private static Stream<Arguments> getByIdParameters2(){ return
			Stream.of( 
					Arguments.of("tt3896198","Title","Guardians of the Galaxy Vol. 2",false),
					Arguments.of("tt3896198","Title","Guardians of the Galaxy",true)
					)
			;}

	@ParameterizedTest
	@MethodSource("getByIdParameters2")
	@DisplayName("Verify -- get by id  -- will return an item based on its id and verify the item's information")
	void getById(String id, String fieldName, String expectedFieldValue,boolean failureExpected) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		boolean testPassed = true;
		String response =   api.getById(id);
		String failureMessage="";
		if (null == response) {
			failureMessage="No item found";
			testPassed&=false;
			api.LOG.info("FAIL: Item with id:"+id+ " was not found");
		}else {
			String fieldValue = JsonParserHelper.getFieldFromJsonString(response, fieldName);
			fieldValue=CommonHelper.removeQuotes(fieldValue);
			if( fieldValue.contentEquals(expectedFieldValue)) {
				testPassed&=true;
				api.LOG.info("PASS: Item found and field:"+fieldName+ " was "+expectedFieldValue+" as expected");
			}else {
				failureMessage+="... but field value not correct";
				testPassed&=false;
				api.LOG.info("FAIL: Item found but field:"+fieldName+ " was not "+expectedFieldValue+" as expected");
			}
		}
		Assertions.assertTrue((testPassed & !failureExpected)||(!testPassed && failureExpected),failureMessage);
	}
	
	
	//GET BY TITLE  TESTS
	//Using get_by_title method, get item by title The STEM Journals and assert that the plot contains the string Science, Technology, Engineering and Math and has a runtime of 22 minutes.
	//Use lower case for field values

	private static Stream<Arguments> getByTitleParameters(){ return
			Stream.of( 
					Arguments.of("The STEM Journals","Plot","science, technology, engineering and math",false),
					Arguments.of("The STEM Journals","Runtime","22 min",false),
					Arguments.of("Guardians of the Galaxy Vol. 2","Year","2017",false),
					Arguments.of("Guardians of the Galaxy Vol. 2","Year","2018",true),
					Arguments.of("Guardians of the Galaxy not found","Year","2017",true)
					)
			;}

	@ParameterizedTest
	@MethodSource("getByTitleParameters")
	@DisplayName("Verify -- search by string -- will return results and verify search results contain certain items")
	void getByTitle(String title, String fieldName, String expectedFieldValue,boolean failureExpected) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		boolean testPassed = true;
		String response =   api.getByTitle(title);
		String failureMessage="";
		if (null == response) {
			failureMessage="No item found";
			testPassed&=false;
			api.LOG.error("FAIL: Item with title:"+title+ " was not found");
		}else {
			String fieldValue = JsonParserHelper.getFieldFromJsonString(response, fieldName);
			fieldValue=CommonHelper.removeQuotes(fieldValue);
			if( fieldValue.toLowerCase().contains(expectedFieldValue)) {
				testPassed&=true;
				api.LOG.info("PASS: Item found and field:"+fieldName+ " was "+expectedFieldValue+" as expected");
			}else {
				failureMessage+="... but field value not correct";
				testPassed&=false;
				api.LOG.info("FAIL: Item found but field:"+fieldName+ " was not "+expectedFieldValue+" as expected");
			}
		}
		Assertions.assertTrue((testPassed & !failureExpected)||(!testPassed && failureExpected),failureMessage);
	}

}
