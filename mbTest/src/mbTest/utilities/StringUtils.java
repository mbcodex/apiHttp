package mbTest.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.text.WordUtils;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

	public enum ReturnGroup {
		NONE, FIRST, SECOND, THIRD, FOURTH
	}

	/**
	 * * TODO CodeReview: Move to StringUtils
	 *
	 * @param regex
	 * @param matchString
	 * @param mode
	 * @return
	 */

	public static String cleanString(String rawString) {
		String cleanedString = "";
		cleanedString = rawString.replaceAll("\\\\r|\\r", "");
		cleanedString = cleanedString.replaceAll("\\\\n|\\n", "");
		cleanedString = cleanedString.replaceAll("\\n", "");
		cleanedString = cleanedString.replaceAll("<br/>", "");
		cleanedString = cleanedString.replaceAll("\"", "");
		return cleanedString.trim();
	}

	/**
	 * Applies the regular expression to source and returns the matching group.
	 *
	 * @param source       - String to process
	 * @param regex        - Regular expression
	 * @param patternType  - Pattern modifier i.e. Pattern.MULTILINEl
	 * @param returnGroup  - Which group in the regular expression to return
	 * @param defaultValue - If no match is found return this value.
	 * @return - matching string
	 */
	public static String getMatchString(String source, String regex, int patternType,
			StringUtils.ReturnGroup returnGroup, String defaultValue) {
		Matcher matcher = getMatcher(regex, source, patternType);
		return matcher.find() ? cleanString(matcher.group(returnGroup.ordinal())) : defaultValue;
	}

	// Does not "clean" the matched string
	public static String getMatchStringRaw(String source, String regex, int patternType,StringUtils.ReturnGroup returnGroup, String defaultValue) {
		Matcher matcher = getMatcher(regex, source, patternType);
		return matcher.find() ? matcher.group(returnGroup.ordinal()) : defaultValue;
	}
	

	 
		public static List<String> getAllMatches(String regex, String matchString, int mode,StringUtils.ReturnGroup returnGroup, String defaultValue) {
			List<String> allMatches = new ArrayList<String>();
			 Matcher m = getMatcher( regex,  matchString,  mode);
			 String match= "";
			 while (m.find()) {
				 match= m.group(returnGroup.ordinal());
				 allMatches.add(match);
			 }
			 return allMatches;
		}

	public static Matcher getMatcher(String regex, String matchString, int mode) {
		Pattern pattern = Pattern.compile(regex, mode);
		return pattern.matcher(matchString);
	}

	public static String getMatchStringRaw(String fileContent, String searchPattern, int multiline, ReturnGroup first,
			Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param regex
	 * @param matchString
	 * @param mode
	 * @return
	 */

	/**
	 * Reverse rotate moves the last string in a csv string to the front s1,s2,s3 ->
	 * s3,s1,s2
	 *
	 * @param string is a csv string
	 * @return csv string
	 */
	public static String rrotCSV(String csvString) {
		String[] ret = csvString.split(",");
		String t = ret[ret.length - 1];
		for (int i = ret.length - 1; i > 0; --i) {
			ret[i] = ret[i - 1];
		}
		ret[0] = t;

		return String.join(",", ret);
	}

	/**
	 * Split an array of Strings into a map.
	 *
	 * @param array of strings with the even elements as the 'key' and the odd
	 *              elements 'values'
	 * @return Map mapping "key" -> "value"
	 */
	public static Map<String, String> splitToMap(final String[] pairs) {
		if (pairs.length % 2 != 0) {
			throw new IllegalArgumentException(
					"The string to split must have an even number of items. Each key must have a value");
		}
		final Map<String, String> ret = new LinkedHashMap<>(pairs.length);
		for (int i = 0; i < pairs.length; i += 2) {
			ret.put(pairs[i], pairs[i + 1]);
		}
		return Collections.unmodifiableMap(ret);

	}

	public static Map<String, String> splitToMap(Stream<String> s, String delimiter) {
		return s.map(e -> e.split(delimiter)).collect(Collectors.toMap(e -> e[0], e -> e[1]));
	}

	public static Map<String, String> splitToMap(final String strToParse, final String delimiter) {
		final Map<String, String> ret = new HashMap<String, String>(Constants.INITIAL_CONTAINER_CAPACITY);
		final String[] splits = strToParse.split(delimiter);
		if (splits.length % 2 != 0) {
			throw new RuntimeException(
					"splitToMap, The string to split must have an even number of items. Each key must have a value");
		}
		for (int i = 0; i < splits.length && i + 1 < splits.length; i += 2) {
			ret.put(splits[i].trim(), splits[i + 1].trim());
		}
		return ret;
	}

	public static List<String> splitToList(final String strToParse, final String delimiter) {
		String s = strToParse.replaceAll(delimiter, ", ");
		return Arrays.asList(s);
	}
	
	/**
	 * Retrieve the selected key from the configuration file. Converts simple json
	 * file to pipe delimited pairs.
	 *
	 * @param fullPath
	 * @param key
	 * @return
	 */
	public static String getAppConfigValue(String fullPath, String key) {
		String contents = "";
		if (CommonHelper.isSimulator()) {
			contents = FileHelper.readFileAsString(fullPath);
		} else {
			contents = FileHelper.readRemoteFileAsString(fullPath, PowinProperty.TURTLEHOST.toString(),
					PowinProperty.TURTLEUSER.toString(), PowinProperty.TURTLEPASSWORD.toString());
		}
		Map<String, String> map = splitToMap(
				contents.replaceAll("[\"{}\\n]", "").replaceAll("[:,]", Constants.PIPE_DELIMITER),
				Constants.PIPE_DELIMITER);
		return map.get(key);
	}

	/**
	 * Builds a map from the string representation {key=value, key=value}
	 *
	 * @param strToParse
	 * @return
	 */
	public static Map<String, String> mapFromString(final String strToParse) {
		final String worker = strToParse.replaceAll("[{}]", "").replaceAll("[,=]", "|");
		return splitToMap(worker, "\\|");
	}
	

	/**
	 * Change a string to title case
	 *
	 * @return modified string
	 */
	public static String stringToTitleCase(String inputString) {
		final char[] delimiters = { ' ', '_' };
		return WordUtils.capitalizeFully(inputString, delimiters);
	}

	public static String appendIfMissing(String sourceStr, String suffix) {
		return sourceStr.endsWith(suffix) ? sourceStr : sourceStr.concat(suffix);
	}

	public static String addDotBeforeAsterisk(String inputStr) {
		// Adds dot
		String outStr = "";
		String regExp = "[^\\.]\\*";// Matches asterisk preceded by a non-dot
		String replacementString = "\\.\\*";// Replace with asterisk preceded with a dot
		outStr = Pattern.compile(regExp).matcher(inputStr).replaceAll(replacementString);
		System.out.println(outStr);
		return outStr;
	}

	public static String stripDotBeforeAsterisk(String inputStr) {
		// Strips dot
		String outStr = "";
		String regExp = "\\.\\*";// Matches asterisk preceded by a non-dot
		String replacementString = "\\*";// Replace with asterisk
		outStr = inputStr.replaceAll(regExp, replacementString);
		System.out.println(outStr);
		return outStr;
	}

	public static String passFail(Result isTestPass) {
		String passFail = isTestPass.isAllTestsPass() ? "PASS" : "FAIL";
		return passFail;
	}

	public static String getFieldFromDelimitedString(String testData, int fieldIndex, String delimiter) {
		String field = "";
		if (!(null == testData || null == delimiter)) {
			String [] testDataArray=testData.split(Pattern.quote(delimiter));
			if (fieldIndex <= testDataArray.length) {
				field = testDataArray[fieldIndex].trim(); 
			}
		}
		return field;
	}

	public static String removeTrailingCharacter(String sourceStr, String trailingCharacter) {
		return StringUtils.removeEnd(sourceStr, trailingCharacter);
	}

}
