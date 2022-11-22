package helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonParserHelper {
	//Utility to convert the response strings to json for further parsing
	public static JsonObject jsonParse(String jsonString) {
		JsonObject jo = null;
		jo = (JsonObject) (new JsonParser()).parse(jsonString);
		return jo;
	}

	public static  JsonObject getItemFromJsonArray(JsonArray jsonArray, String fieldName, String fieldValue) {
		JsonObject retJsonObject=null;
		for (JsonElement jo : jsonArray) {
			String actualFieldValue=(((JsonObject) jo).get(fieldName)).toString();
			String strippedFieldValue=CommonHelper.removeQuotes(actualFieldValue);
			if (strippedFieldValue.contentEquals(fieldValue)) {
				retJsonObject=(JsonObject) jo;
				break;
			}
		}
		return retJsonObject;
	}

	public static JsonArray getJsonArray(String json) {
		JsonParser jp = new JsonParser();
		JsonElement root = jp.parse(json);
		JsonArray rootArr = root.getAsJsonArray();
		System.out.println("Num items:"+rootArr.size());
		return rootArr;
	}
	
	public static JsonArray getJsonArray(String json,String arrayName) {
		JsonParser jp = new JsonParser();
		JsonElement root = ((JsonObject) jp.parse(json)).get(arrayName);
		JsonArray rootArr = root.getAsJsonArray();
		return rootArr;
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

	public static  String getFieldFromJsonObject(JsonObject itemJson, String fieldName) {
		return  itemJson.get(fieldName).toString();
	}
	
	public static  String getFieldFromJsonString(String jsonString, String fieldName) {
		return  getFieldFromJsonObject(jsonParse(jsonString),fieldName);
	}	 

}
