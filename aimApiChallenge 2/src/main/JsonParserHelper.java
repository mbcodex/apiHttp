package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import com.powin.modbusfiles.reports.Reports;

public class JsonParserHelper {
	private final static Logger LOG = LogManager.getLogger();
	private JSONObject cJsonSource;

	public JsonParserHelper() {
		
	}
	
	public JsonParserHelper(JSONObject jsonSource) {
		cJsonSource = jsonSource;
	}

	public JsonParserHelper(String jsonSource) {
		try {
			cJsonSource = (JSONObject) new JSONParser().parse(jsonSource);
		} catch (ParseException e) {
			LOG.error("JSON parser Exception", e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public JSONObject getJsonSource() {
		return cJsonSource;
	}
	//TODO: Add method to get all the fields from a flat json file
	public static List<String> getFieldsFromJSONObject(JSONObject jsonObj){
		Set<String > fieldsSet=jsonObj.keySet();
		return new ArrayList<>(fieldsSet);
	}

	public static List<String> getFieldJSONObject(JSONObject searchObject, String searchTerm, String path) {
		List<String> resultContent = new ArrayList<>();
		return JsonParserHelper.getFieldJSONObject(searchObject, searchTerm, path, resultContent);
	}

	public static List<String> getFieldJSONObject(JSONObject searchObject, String searchTerm, String path,
			List<String> resultContent) {
		return getFieldJSONObjectWithTimestamp(searchObject, searchTerm, path, resultContent, "");
	}

	public static List<String> getFieldJSONObjectRaw(JSONObject searchObject, String searchTerm, String path,
			List<String> resultContent) {
		return getFieldJSONObjectWithTimestampRaw(searchObject, searchTerm, path, resultContent, "");
	}

	static Object getJSONObject1(JSONObject searchObject, String searchTerm) {
		String[] searchTermArray = searchTerm.split("\\|");
		Object testElement = searchObject.get(searchTermArray[0]);
		String newSearchTerm = String.join("|", Arrays.copyOfRange(searchTermArray, 1, searchTermArray.length));
		if (newSearchTerm.equals(""))
			return testElement;
		JSONArray ja = null;
		JSONObject jo = null;

		if (testElement.getClass().toString().contains("Array")) {
			ja = (JSONArray) testElement;
			for (Object o : ja) {
				testElement = getJSONObject1((JSONObject) o, newSearchTerm);
			}
		} else if (testElement.getClass().toString().contains("Object")) {
			jo = (JSONObject) testElement;
			testElement = getJSONObject1(jo, newSearchTerm);
		}
		return testElement;
	}

	/*
	 * public void updateJson(String content, String outputFile) { // Get to the
	 * nested json object JsonObject jsonObj = new Gson().fromJson(content,
	 * JsonObject.class); JsonObject nestedJsonObj =
	 * jsonObj.getAsJsonObject("blockConfiguration").getAsJsonArray(
	 * "arrayConfigurations") .get(0).getAsJsonObject()
	 * 
	 * ; // nestedJsonObj=nestedJsonObj.getAsJsonArray("stringConfigurations")
	 * 
	 * // Update values nestedJsonObj.addProperty("level-4b-1", "new-value-4b-1");
	 * nestedJsonObj.getAsJsonObject("level-4b-3").addProperty("StartDate",
	 * "newdate"); }
	 */

	public static List<String> getFieldsFromJsonFile(String filePath) {
		File toolsPermissionsFile = new File(filePath);
		JSONObject jsonFromFile =getJSONFromFile(toolsPermissionsFile.getAbsolutePath());
		List<String> fields=getFieldsFromJSONObject(jsonFromFile);
		return fields;
	}

	/**
	 * Return the integer value of a json field.
	 * @param json
	 * @param fieldname
	 * @return
	 */
	public static int toInt(JSONObject json, String fieldname) {
		int ret = Integer.MIN_VALUE;
		Object object = json.get(fieldname);
		if (null == object) {
			ret = 0;
		}
		else {
			try {
				ret =  ((Double)object).intValue();
			} catch (Exception e) {
				ret =  ((Long)object).intValue();
			}
		}
		return ret;
	}

	public static JSONObject parseJsonFromString(String jsonString) {
		JSONObject jo = null;
		try {
			jo = (JSONObject) new JSONParser().parse(jsonString);
		} catch (ParseException e) {
			throw new RuntimeException("Unable to parse json", e);
		}
		return jo;
	}
	
	public static JSONObject parseJsonFromResource(String fullyQualifiedName) {
		InputStream st = (new JsonParserHelper()).getClass().getResourceAsStream(fullyQualifiedName);
		JSONObject ret = new JSONObject();
		if (null != st) {
			String readInputStreamAsString = readInputStreamAsString(st);
			ret = JsonParserHelper.parseJsonFromString(readInputStreamAsString);
			try {
				st.close();
			} catch (IOException e) {
				;
			} 
		}
		return ret;
	}
	public static String readInputStreamAsString(InputStream is) {
		 return new BufferedReader(
			      new InputStreamReader(is, StandardCharsets.UTF_8))
			        .lines()
			        .collect(Collectors.joining("\n"));
	}

	static Object editJSONObject1(JSONObject searchObject, String searchTerm) {
		String[] searchTermArray = searchTerm.split("\\|");
		Object testElement = searchObject.get(searchTermArray[0]);
		String newSearchTerm = String.join("|", Arrays.copyOfRange(searchTermArray, 1, searchTermArray.length));

		JSONArray ja = null;
		JSONObject jo = null;

		if (testElement.getClass().toString().contains("Array")) {
			ja = (JSONArray) testElement;
			for (Object o : ja) {
				testElement = getJSONObject1((JSONObject) o, newSearchTerm);
			}
		} else if (testElement.getClass().toString().contains("Object")) {
			jo = (JSONObject) testElement;
			if (newSearchTerm.equals("")) {
				searchObject.put("stringIndexes", "[44]");
				return jo;
			}
			testElement = getJSONObject1(jo, newSearchTerm);
		}
		return testElement;
	}

	public static List<String> getFieldJSONObjectWithTimestamp(JSONObject searchObject, String searchTerm, String path,
			List<String> resultContent, String ts) {
		String[] searchTermArray = searchTerm.split("\\|");
		Object testElement = searchObject.get(searchTermArray[0]);
		String newSearchTerm = String.join("|", Arrays.copyOfRange(searchTermArray, 1, searchTermArray.length));
		JSONArray ja = null;
		JSONObject jo = null;

		if (testElement.getClass().toString().contains("Array")) {
			int arrayIndex = 0;
			ja = (JSONArray) testElement;
			String tmp = path;
			if (!ja.get(0).getClass().toString().contains("Object")
					&& !ja.get(0).getClass().toString().contains("Array")) {
				for (Object o : ja) {
					String bit = "";
					if (o.getClass().toString().contains("Double")) {
						bit = "" + o.toString();
					} else if (o.getClass().toString().contains("Long")) {
						bit = Long.toString((Long) o);
					} else {
						bit = o.toString();
					}
					bit = ts + path + bit;
					resultContent.add(bit);
					path = "";
				}

				return resultContent;

			} else {
				for (Object o : ja) {
					arrayIndex++;
					path += arrayIndex + ",";
					getFieldJSONObjectWithTimestamp((JSONObject) o, newSearchTerm, path, resultContent, ts);
					path = tmp;
				}
				path = "";
			}
		} else if (testElement.getClass().toString().contains("Object")) {
			jo = (JSONObject) testElement;
			getFieldJSONObjectWithTimestamp(jo, newSearchTerm, path, resultContent, ts);
		} else {
			String bit = "";
			if (testElement.getClass().toString().contains("Double")) {
				bit = "" + testElement.toString();
			} else if (testElement.getClass().toString().contains("Long")) {
				bit = Long.toString((Long) testElement);
			} else {
				bit = testElement.toString();
			}
			bit = ts + path + bit;
			resultContent.add(bit);
			path = "";
			return resultContent;
		}
		return resultContent;

	}

//
	static JSONObject getJSONObject(JSONObject searchObject, String searchTerm) {
		String[] searchTermArray = searchTerm.split("\\|");
		if (searchTermArray.length == 0)
			return searchObject;
		Object testElement = searchObject.get(searchTermArray[0]);
		String newSearchTerm = String.join("|", Arrays.copyOfRange(searchTermArray, 1, searchTermArray.length));

		JSONArray ja = null;
		JSONObject jo = null;

		if (testElement.getClass().toString().contains("Array")) {
			ja = (JSONArray) testElement;
			for (Object o : ja) {
				getJSONObject((JSONObject) o, newSearchTerm);
			}
		} else if (testElement.getClass().toString().contains("Object")) {
			jo = (JSONObject) testElement;
			getJSONObject(jo, newSearchTerm);
		} else
			return jo;
		return jo;
	}
	
	static JSONObject getItemFromSku(JSONArray testElement, String sku) {


		JSONArray ja = null;
		JSONObject jo = null;

		if (testElement.getClass().toString().contains("Array")) {
			ja = testElement;
			for (Object o : ja) {
				getJSONObject((JSONObject) o, sku);
			}
		} 
		return jo;
	}

	public static List<String> getFieldJSONObjectWithTimestampRaw(JSONObject searchObject, String searchTerm,
			String path, List<String> resultContent, String ts) {
		String[] searchTermArray = searchTerm.split("\\|");
		Object testElement = searchObject.get(searchTermArray[0]);
		String newSearchTerm = String.join("|", Arrays.copyOfRange(searchTermArray, 1, searchTermArray.length));
		JSONArray ja = null;
		JSONObject jo = null;

		if (testElement.getClass().toString().contains("Array")) {
			ja = (JSONArray) testElement;
			for (Object o : ja) {
				// path += arrayIndex + ",";
				getFieldJSONObjectWithTimestampRaw((JSONObject) o, newSearchTerm, path, resultContent, ts);
				// path = tmp;
			}
			path = "";
		} else if (testElement.getClass().toString().contains("Object")) {
			jo = (JSONObject) testElement;
			getFieldJSONObjectWithTimestampRaw(jo, newSearchTerm, path, resultContent, ts);
		} else {
			String bit = "";
			if (testElement.getClass().toString().contains("Double")) {
				bit = "" + testElement.toString();
			} else if (testElement.getClass().toString().contains("Long")) {
				bit = Long.toString((Long) testElement);
			} else {
				bit = testElement.toString();
			}
			bit = ts + path + bit;
			resultContent.add(bit);
			path = "";
			return resultContent;
		}
		return resultContent;

	}

	static List<String> getFieldJSONObjectWithTimestampRaw1(JSONObject searchObject, String searchTerm, String path,
			List<String> resultContent, String ts) {
		String[] searchTermArray = searchTerm.split("\\|");
		Object testElement = searchObject.get(searchTermArray[0]);
		String newSearchTerm = String.join("|", Arrays.copyOfRange(searchTermArray, 1, searchTermArray.length));
		JSONArray ja = null;
		JSONObject jo = null;

		if (testElement.getClass().toString().contains("Array")) {
			ja = (JSONArray) testElement;
			for (Object o : ja) {
				// path += arrayIndex + ",";
				getFieldJSONObjectWithTimestampRaw((JSONObject) o, newSearchTerm, path, resultContent, ts);
				// path = tmp;
			}
			path = "";
		} else if (testElement.getClass().toString().contains("Object")) {
			jo = (JSONObject) testElement;
			getFieldJSONObjectWithTimestampRaw(jo, newSearchTerm, path, resultContent, ts);
		} else {
			String bit = "";
			if (testElement.getClass().toString().contains("Double")) {
				bit = "" + testElement.toString();
			} else if (testElement.getClass().toString().contains("Long")) {
				bit = Long.toString((Long) testElement);
			} else {
				bit = testElement.toString();
			}
			bit = ts + path + bit;
			resultContent.add(bit);
			path = "";
			return resultContent;
		}
		return resultContent;

	}

	public static JSONObject jsonParse(String jsonString) {
		JSONObject jo = null;
		try {
			jo = (JSONObject) (new JSONParser()).parse(jsonString);
		} catch (ParseException e) {
			LOG.error("Unable to parse {}", e.getMessage());
		}
		;
		return jo;
	}

	public static JSONObject getJSONFromFile(String pathToFile) {
		String jsonText = readFileAsString(pathToFile);
		return jsonParse(jsonText);
	}
	
	public static String readFileAsString(String pathToFile) {
	
		String ret;
		try {
			ret = new String(Files.readAllBytes(Paths.get(pathToFile)));
		} catch (IOException e) {
			LOG.error("Unable to read file {}.", pathToFile);
			throw new RuntimeException();
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject snakeCaseKeys(JSONObject jsonFromFile) {
		JSONObject ret = new JSONObject();
		for (Object key : jsonFromFile.keySet()) {
			String scKey = toSnakeCase((String) key);
			try {
				ret.put(scKey, jsonFromFile.get(key));
			} catch (Exception e) {
				LOG.info(e.getMessage());
			}
		}
		return ret;
	}

	public static String toSnakeCase(String key) {
		StringBuilder sb = new StringBuilder();
		for (Character c : key.toCharArray()) {
			if (Character.isUpperCase(c)) {
				sb.append("_").append(c);
			} else {
				sb.append(Character.toUpperCase(c));
			}
		}
		return sb.toString();
	}



}