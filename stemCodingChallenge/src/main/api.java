package main;

import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import helpers.CommonHelper;
import helpers.HttpHelper;
import helpers.JsonParserHelper;


public class api {
	public static Logger LOG = LogManager.getLogger();
	private static String baseUrl="http://www.omdbapi.com/";
	private static String apiKey="478a9996";
	public static final int  NUM_ITEMS_PER_PAGE=10;

	//Returns the response to a get_by_id call. Takes in imdbId as paraeter
	//The response will contain detailed information of the item with the specific imdbId
	public static String getById(String id)
			throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		String url=getUrlForGetById(id);
		HttpHelper h= new HttpHelper(url);
		String responseContents=h.getResponse();
		if(JsonParserHelper.getFieldFromJsonString(responseContents,"Response").contentEquals("\"True\"")) {
			return responseContents;
		}else {
			return null;
		}
	}
	//Returns the response to a get_by_title call. Takes in the item title as parameter.
	//The title should be exact.
	//The response will contain detailed information of the item with the specific title
	public static String getByTitle(String title)
			throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		String url=getUrlForGetByTitle(title);
		HttpHelper h= new HttpHelper(url);
		String responseContents=h.getResponse();
		if(JsonParserHelper.getFieldFromJsonString(responseContents,"Response").contentEquals("\"True\"")) {
			return responseContents;
		}else {
			return null;
		}
	}
	//Returns an array of items that match the search term.
	//Search term should be a fragment of the title
	//The response will contain some information of the item with the specific imdbId
	//The response can be further parsed, filtered for information on a specific item
	public static JsonArray searchByString(String searchString)
			throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		String url=getUrlForSearchByString(searchString,1);
		HttpHelper h= new HttpHelper(url);
		String responseContents=h.getResponse();
		if(JsonParserHelper.getFieldFromJsonString(responseContents,"Response").contentEquals("\"True\"")) {
			String numItems=JsonParserHelper.getFieldFromJsonString(responseContents,"totalResults");
			numItems = CommonHelper.removeQuotes(numItems);
			int pageCount=CommonHelper.getNumPages(Integer.parseInt(numItems));
			JsonArray ja = new JsonArray();
			for( int pageNumber=1;pageNumber<pageCount;pageNumber++) {
				url=getUrlForSearchByString(searchString,pageNumber);
				h= new HttpHelper(url);
				responseContents=h.getResponse();
				JsonArray pageSearchResults=JsonParserHelper.getJsonArray(responseContents,"Search");
				ja.addAll(pageSearchResults);
				
			}
			return ja;
		}else {
			return null;
		}
	}
	//Gets a specific item from the search results.
	//The field values can be Title, Year, imdbID,Type,Poster
	//Please see https://www.omdbapi.com/#usage for the latest info
	public static JsonObject getItemFromSearchResults(JsonArray searchResults, String fieldName, String fieldValue) {
		JsonObject jo = null;
		jo=JsonParserHelper.getItemFromJsonArray(searchResults, fieldName, fieldValue);
		return jo;
	}
	//Given a search term, this function will return the search results and them get the specific item that matches a specific field
	public static JsonObject getItemUsingSearch(String searchTerm,String fieldName, String fieldValue) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		JsonArray searchResultsArray =  searchByString(searchTerm);
		JsonObject itemToSearch=null;
		if (null != searchResultsArray) {
			itemToSearch = getItemFromSearchResults(searchResultsArray, fieldName, fieldValue);
		}
		return itemToSearch;
	}
	//Composes the url for search by string
	private static String getUrlForSearchByString(String searchString, int pageNumber) {
		return new StringBuilder()
				.append(baseUrl)
				.append("?s=")
				.append(searchString)
				.append("&")
				.append("page=")
				.append(String.valueOf(pageNumber))
				.append("&")
				.append("apikey=")
				.append(apiKey)
				.toString();
	}
	//Composes the url for get by id
	private static String getUrlForGetById(String id) {
		return new StringBuilder()
				.append(baseUrl)
				.append("?i=")
				.append(id)
				.append("&")
				.append("apikey=")
				.append(apiKey)
				.toString();
	}
	//Composes the url for get by title
	private static String getUrlForGetByTitle(String title) {
		return new StringBuilder()
				.append(baseUrl)
				.append("?t=")
				.append(title)
				.append("&")
				.append("apikey=")
				.append(apiKey)
				.toString();

	}
}
