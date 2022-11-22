package main;

import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class api {
	public static String baseUrl="https://1ryu4whyek.execute-api.us-west-2.amazonaws.com/dev/skus";

	public static HttpHelper getAllSkus() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		return new HttpHelper(baseUrl, "GET");
	}
	
	
	public static String getAllResponseBody() throws KeyManagementException,
	  NoSuchAlgorithmException, MalformedURLException { 
		  return getAllSkus().getResponse(); 
	}
	  

	public static HttpHelper getSingleSku(String sku) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		return new HttpHelper(baseUrl+"/"+sku, "GET");
	}
	
	/*
	 * public static String getSingleSkuResponseBody(String sku) throws
	 * KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
	 * HttpHelper mHttpHelper = new HttpHelper(baseUrl+"/"+sku, "GET"); return
	 * mHttpHelper.getResponse(); }
	 */

	public static HttpHelper postSingleSku(String sku,String description,String price) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		return new HttpHelper(baseUrl, "POST",sku,description,price);
	}

	public static HttpHelper deleteSku(String sku) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		return new HttpHelper(baseUrl+"/"+sku, "DELETE");
	}
	//Helpers
	public static String getRandomSku() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		JsonObject randomItem= JsonParserHelper2.getItemAtRandom();
		String randomSku=JsonParserHelper2.getFieldFromItem(randomItem, "sku");
		randomSku=randomSku.replaceAll("\"", "");
		return randomSku;
	}
	
	public static int getSkuCount(String response) {
		return JsonParserHelper2.getNumItems(response);
	}
	
	public static boolean doesItemExist(String sku) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		boolean isTestPass;
		String response= api.getSingleSku(sku).getResponse();
		JsonObject responseJson = JsonParserHelper2.jsonParse(response);
		JsonElement item = responseJson.get("Item");
		isTestPass=null!=item;
		return isTestPass;
	}
}
