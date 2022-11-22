package mbTest.utilities;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Result object that provides more context than a simple boolean.
 * 
 * @author powin
 *
 */
public class Result {
	private final static Logger LOG = LogManager.getLogger();

	private boolean isPass;
	private boolean isWithinTolerance;
	private String comment;
	private String timestamp;
	private List<String> callStack;
	private double tolerancePercentage;
	private String cCallingMethodName;
	private static String pattern="yyyy-MM-dd hh:mm:ss:SSS";
//	private static SimpleDateFormat formatter = new SimpleDateFormat(pattern);
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS").withZone(ZoneId.systemDefault());
	private static Map<String, List<Result>> results = new HashMap<>();

	private Result(boolean status, String comment) {
		isPass = status;
		isWithinTolerance = false;
		this.comment = comment;
		callStack = getCallStack();
		timestamp = ZonedDateTime.now().format(formatter);
		int lastItem = callStack.size() - 1;
		cCallingMethodName = callStack.get(lastItem).split(":")[2];
		List<Result> list = results.get(cCallingMethodName);
		if (null == list) {
			list = new ArrayList<>();
			list.add(this);
			results.put(cCallingMethodName, list);
		} else {
			list.add(this);
		}
	}
	
	public  static Map<String, List<Result>> getResults(){
		return results;
	}

	public List<String> getCallStack() {
		StackTraceElement[] stackTrace = (new Exception()).getStackTrace();
		int methodNameIndex = 0;
		for (; this.getClass().toString().replace("class ", "")
				.equals(stackTrace[methodNameIndex].getClassName()); ++methodNameIndex) {
			// LOG.info("{}, {}",this.getClass().toString(),
			// stackTrace[methodNameIndex].getClassName());
		}
		// methodNameIndex--;
		List<String> ret = new ArrayList<>();
		for (; !stackTrace[methodNameIndex].getFileName().equals("Result.java"); methodNameIndex--) {
			ret.add(String.join(":", stackTrace[methodNameIndex].getFileName(),
					String.valueOf(stackTrace[methodNameIndex].getLineNumber()),
					stackTrace[methodNameIndex].getMethodName()));
		}
		return ret;

	}

	public static Result getInstance(boolean status, double tolerancePercentage) {
		Result instance = getInstance(status, "");
		instance.setTolerancePercentage(tolerancePercentage);
		return instance;
	}

	public static Result getInstance(boolean status) {
		return getInstance(status, "");
	}

	public static void reset() {
		LOG.info("clearing {} entries {}", results.size(), results);
		printResults();

		results.clear();
		LOG.info("cleared {} entries {}", results.size(), results);

	}

	public static void printResults() {
		results.entrySet().stream().forEach(entry -> {
			entry.getValue().stream().forEach(System.out::println);
		});
	}

	public static Result getInstance(boolean status, String comment) {
		return new Result(status, comment);
	}
	
	public String getTimestamp() {
		return timestamp;
	}

	public double getTolerancePercentage() {
		return tolerancePercentage;
	}

	public void setTolerancePercentage(double tolerancePercentage) {
		this.tolerancePercentage = tolerancePercentage;
		isWithinTolerance();
	}

	public Result add(boolean status, String comment) {
		return getInstance(status, comment);
	}

	public Result add(Result instance) {
		return instance;
	}

	public boolean isWithinTolerance() {
		List<Result> resultsList = new ArrayList<>();
		results.entrySet().stream().forEach(e -> {
			resultsList.addAll(e.getValue());
		});
		long numPass = resultsList.stream().filter(e -> e.isPass).count();
		long numFail = resultsList.stream().filter(e -> !e.isPass).count();
		 LOG.info("There are {} passing tests.", numPass );
		 LOG.info("There are {} failing tests.", numFail);
		double percentageFails = ((double) numFail / (numFail + numPass)) * 100;
		 LOG.info("percent fail {}",percentageFails);
		isWithinTolerance = percentageFails <= getTolerancePercentage();
		return isWithinTolerance;
	}

	public boolean getWithinTolerance() {
		return isWithinTolerance;
	}

	/**
	 * Test the status of all of the result objects specified in this test.
	 * 
	 * @return
	 */
	public boolean isAllTestsPass() {
		AtomicBoolean ab = new AtomicBoolean(true);
		results.entrySet().stream().forEach(entry -> {
			boolean v = ab.get();
			v &= entry.getValue().stream().allMatch(result -> result.isPass());
			ab.set(v);
		});
		return ab.get();
	}

	public boolean isPass() {
		return isPass;
	}

	public void setPass(boolean isPass) {
		this.isPass = isPass;
	}

	public String getComment() {
		return comment;
	}

//	public String getComments() {
//		List<Result> resultsList = new ArrayList<>();
//		results.entrySet().stream().forEach(e -> {
//			resultsList.addAll(e.getValue());
//		});
//		return resultsList.toString().replaceAll("([a-zA-Z]*\\.java.+?\\, )", "\n$1").replaceAll("(Result \\[)",
//				"\n\n$1");
//	}
	
	public String getComments() {
		List<Result> resultsList = new ArrayList<>();
		results.entrySet().stream().forEach(e -> {
			resultsList.addAll(e.getValue());
		});
		List<Result> resultsListSorted=resultsList.stream()
		.sorted((d1, d2) -> ZonedDateTime.parse(d1.getTimestamp(),formatter)
						.compareTo(ZonedDateTime.parse(d2.getTimestamp(),formatter))
				)
		.collect(Collectors.toList());
		return resultsListSorted.toString().replaceAll("([a-zA-Z]*\\.java.+?\\, )", "\n$1").replaceAll("(Result \\[)",
				"\n\n$1");
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return String.format("Result [timestamp=%s, isPass=%s, comment=%s, tolerancePercentage=%s, callStack=%s]",
				timestamp, isPass, comment, tolerancePercentage, callStack);
	}

	/**
	 * The Result object was added to the list when it was created This method is
	 * just syntactic sugar.
	 * 
	 * @param instance
	 */

}
