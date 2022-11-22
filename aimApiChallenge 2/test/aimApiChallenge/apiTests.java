package aimApiChallenge;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import main.HttpHelper;
import main.api;

class apiTests {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	//GET TESTS
	//ALL
	@Test
	@DisplayName("Verify response code for service -- 'get all' skus")
	void getAllVerifyResponseCode() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		HttpHelper h =  api.getAllSkus();
		int statusCode=  h.getResponseCode();
		Assertions.assertEquals(200,statusCode,"The api service 'get all skus' did not return a status code of 200");
	}

	@Test
	@DisplayName("To demonstrate failure logging message. Spoofing failure of Verify response code for service -- 'get all' skus")
	void failureLogDemogetAllVerifyResponseCode() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		HttpHelper h =  api.getAllSkus();
		int statusCode=  h.getResponseCode();
		Assertions.assertEquals(201,statusCode,"Faking failure to demonstrate failure logging. The api service 'get all skus' did not return a status code of 201");
	}

	@Test
	@DisplayName("Verify response body for service -- 'get all' skus")
	void getAllVerifyResponseBody() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		HttpHelper h =  api.getAllSkus();
		String responseBody=  h.getResponse();
		int numItems= api.getSkuCount(responseBody);
		Assertions.assertTrue(numItems>100);//TODO: 100 is arbitrary. Run this test if you know exactly how many items you expect. Maybe by recreating the items after deleting them
	}
	//SINGLE ITEM

	@Test
	@DisplayName("Verify response code for getting a single sku")
	void getSingleSkuVerifyResponseCode() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		String randomSku  = api.getRandomSku();
		HttpHelper h =  api.getSingleSku(randomSku);
		int statusCode=  h.getResponseCode();
		Assertions.assertEquals(200,statusCode,"The api service 'get single sku' did not return a status code of 200");
	}

	@Test
	@DisplayName("Verify response body for getting a single sku")
	void getSingleSkuResponseBodyHappyPath() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		String randomSku  = api.getRandomSku();
		HttpHelper h =  api.getSingleSku(randomSku);
		boolean itemExists = api.doesItemExist(randomSku);
		Assertions.assertTrue(itemExists,"Item does not exist but service -get single sku- did not return a failure");
	}

	@Test
	@DisplayName("Verify response body for getting a single sku that does not exist")
	void getSingleSkuResponseBodyNonExistentItem() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		HttpHelper h =  api.getSingleSku("aaaaaa");
		boolean itemExists = api.doesItemExist("usdhfudshffdsho");
		Assertions.assertFalse(itemExists,"Item does not exist but service -get single sku- did not return a failure");
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	//POST
	/////////////////////////////////////////////////////////////////////////////////////////////
	private static Stream<Arguments> postParameters(){ 
		return Stream.of(
			Arguments.of("boohoohoo12345", "lawn mower","325"), 
			Arguments.of("yoohoohoo12345", "weed whacker","225") 
			);}
	
	@ParameterizedTest
	@MethodSource("postParameters")
	@DisplayName("Verify response for posting an item which does not exist yet")
	void postItemThatDoesNotExistYet(String sku,String description,String price) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		HttpHelper h =  api.postSingleSku(sku,description,price);
		int statusCode=  h.getResponseCode();
		boolean itemExists = api.doesItemExist(sku);
		Assertions.assertEquals(200,statusCode,"The api service 'post single sku' for a new item did not return a status code of 200");
		Assertions.assertTrue(itemExists,"The api service 'post single sku' for a new item did not return a status code of 200");
	}

	
	@ParameterizedTest
	@DisplayName("Verify response for posting an item which already exists")
	@MethodSource("postParameters")
	void postItemThatAlreadyExists(String sku,String description,String price) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		String randomSku  = api.getRandomSku();
		HttpHelper h =  api.postSingleSku(randomSku,description,price);
		int statusCode=  h.getResponseCode();
		boolean itemExists = api.doesItemExist(randomSku);
		Assertions.assertEquals(200,statusCode,"The api service 'post single sku' for an existing item did not return a status code of 200");
		Assertions.assertTrue(itemExists,"The api service 'post single sku' for an existing item did not return a status code of 200");
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//DELETE
	/////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	@DisplayName("Verify response code for deleting an item which already exists")
	void deleteItemThatAlreadyExists() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		String randomSku  = api.getRandomSku();
		HttpHelper h = api.deleteSku(randomSku);
		int statusCode=  h.getResponseCode();
		boolean itemDoesNotExist = ! api.doesItemExist(randomSku);
		Assertions.assertEquals(200,statusCode,"The api service 'delete single sku' for an existing item did not return a status code of 200");
		Assertions.assertTrue(itemDoesNotExist,"The api service 'delete single sku' for an existing item did not actually delete the item");
	}

}
