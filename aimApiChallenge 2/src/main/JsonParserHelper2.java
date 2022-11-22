package main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//import com.powin.modbusfiles.reports.Reports;

public class JsonParserHelper2 {

	private static String readFileAsString(String pathToFile) {
		String ret;
		try {
			ret = new String(Files.readAllBytes(Paths.get(pathToFile)));
		} catch (IOException e) {
			throw new RuntimeException();
		}
		return ret;
	}
	
	public static JsonObject jsonParse(String jsonString) {
		JsonObject jo = null;
		jo = (JsonObject) (new JsonParser()).parse(jsonString);
		return jo;
	}

	public static  JsonObject getItemGivenSku(String jsonAllSkus, String sku) {
		JsonArray rootArr = getJsonArray(jsonAllSkus);
		System.out.println("Num items:"+rootArr.size());
		JsonObject retJsonObject = null;
		for (JsonElement jo : rootArr) {
			if ((((JsonObject) jo).get("sku")).toString().contentEquals(sku)) {
				printItem((JsonObject) jo);
				retJsonObject=(JsonObject) jo;
				break;
			}
		}
		return retJsonObject;
	}


	public static JsonObject getItemAtRandom(String jsonAllSkus) {
		JsonArray rootArr = getJsonArray(jsonAllSkus);
		int randomIndex=(int)(rootArr.size()*(Math.random()));
		System.out.println("Random Index:"+randomIndex);
		JsonObject retJsonObject=(JsonObject) rootArr.get(randomIndex);
		printItem(retJsonObject);
		return retJsonObject;
	}
	
	public static JsonObject getItemAtRandom() throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		String jsonAllSkus=api.getAllSkus().getResponse();
		return getItemAtRandom( jsonAllSkus);
	}

	public static JsonArray getJsonArray(String jsonAllSkus) {
		JsonParser jp = new JsonParser();
		JsonElement root = jp.parse(jsonAllSkus);
		JsonArray rootArr = root.getAsJsonArray();
		System.out.println("Num items:"+rootArr.size());
		return rootArr;
	}
	public static int getNumItems(String jsonAllSkus) {
		return getJsonArray( jsonAllSkus).size();
	}
	
	

	public static JsonObject getItemAtIndex(String jsonAllSkus, int index) {
		JsonObject retJsonObject;
		JsonArray rootArr = getJsonArray(jsonAllSkus);
		retJsonObject=(JsonObject) rootArr.get(index);
		printItem(retJsonObject);
		return retJsonObject;
	}


	private static void printItem(JsonObject jo) {
		jo.entrySet().forEach(entry ->
		System.out.println(entry.getKey()+": "+entry.getValue().getAsString())
				);
	}

	public static  String getFieldFromItem(JsonObject itemJson, String fieldName) {
		return  itemJson.get(fieldName).toString();
	}

	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, MalformedURLException {
		//String json = readFileAsString("/Users/mb/Downloads/aim_get_all.json");
		String allSkus = api.getAllResponseBody();
		getItemGivenSku(allSkus, "\"Tc5tUiQn29mJPc8X0lSM\"");
		JsonObject randomItem=getItemAtRandom(allSkus);
		String fieldValue=getFieldFromItem(randomItem,"sku");
		System.out.println(fieldValue);

	}

}
