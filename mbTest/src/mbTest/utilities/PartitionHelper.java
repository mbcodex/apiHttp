package mbTest.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class Partition {
	private static final Logger LOG = LogManager.getLogger();
	// Will linked lists offer better performance?
	// Can peformance metrics be an object instead of a list
	List<Integer> performanceMetrics = new ArrayList<>();
	List<List<String>> partitionInfo = new ArrayList<>();
	List<Integer> valueList = new ArrayList<>();
	List<String> partitionItems;
	String partitionScheme = "";
	int partitionId = 0;
	int cumulativeSum = 0;

	Partition() {
		createPartition();
	}

	@Override
	public String toString() {
		return "Partition [ partitionInfo=" + partitionInfo + "]";
	}

	void createPartition() {
		partitionItems = new ArrayList<>();
		partitionId++;
	}

	void addValue(int value) {
		valueList.add(value);
		logPartitionSum(value);
	}

	void addPartitionItem(String item) {
		if (partitionItems.indexOf(item) <= 0)
			partitionItems.add(item);
	}

	void removeLastPartitionItem() {
		if (!partitionItems.isEmpty()) {
			partitionItems.remove(partitionItems.size() - 1);
		}
	}

	public int getCumulativeSum() {
		return CommonHelper.getSumListSection(partitionItems, 1);
	}

	public int getCumulativeSumGlobal() {
		return getCumulativeSum() + valueList.stream().collect(Collectors.summingInt(Integer::intValue));
	}

	public int getStandardDeviation() {
		return performanceMetrics.get(0);
	}

	public int getMaximumDuration() {
		return performanceMetrics.get(1);
	}

	public int getMinimumDuration() {
		return performanceMetrics.get(2);
	}

	public int getAverageDuration() {
		return performanceMetrics.get(3);
	}

	public int getDeltaMaxAndAverage() {
		return performanceMetrics.get(4);
	}

	public String getPartitionScheme() {
		return partitionScheme;
	}

	public void savePartition() {
		partitionInfo.add(partitionItems);
	}

	public void addMetrics() {
		double stdDeviation = CommonHelper.stdDeviation(valueList);
		stdDeviation = CommonHelper.roundDouble(stdDeviation, 1);
		LOG.trace("Duration list:" + CommonHelper.convertIntegerListToString(valueList));
		LOG.trace("Standard deviation of durations:" + stdDeviation);
		IntSummaryStatistics stats = valueList.stream().mapToInt((x) -> x).summaryStatistics();
		performanceMetrics.add((int) stdDeviation);
		performanceMetrics.add(stats.getMax());
		performanceMetrics.add(stats.getMin());
		performanceMetrics.add((int) stats.getAverage());
		performanceMetrics.add(stats.getMax() - (int) stats.getAverage());// delta between max and average
	}

	void logPartitionSum(int value) {
		LOG.trace("Partition#" + partitionId + " sum: " + value);
	}

	void logKeyValue() {
		LOG.trace(CommonHelper.convertArrayListToString(partitionItems, "\n"));// keys and values
	}

	public boolean isEvenlyDistributed() {
		boolean ret = true;
		for (List<String> pi : partitionInfo) {
			ret &= !pi.isEmpty();
		}
		if (!ret) {
			LOG.info("The partition {} did not fill in all the available partitions", this.partitionScheme);
		}
		return true; // ret;
	}

	public List<Integer> getPartitionRuntimes() {
		List<Integer> ret = new ArrayList<>();
		partitionInfo.stream().forEach(p -> {
			ret.add(p.stream().map(l -> Integer.valueOf(l.split(",")[1])).mapToInt(Integer::intValue).sum());

		});
		return ret;
	}
}

public class PartitionHelper {
	private final static Logger LOG = LogManager.getLogger();

	static List<String> createSampleDurationList(int numTests, int min, int max,double[] fractionalDistributionDecile) {
		List<Integer> durationsList = new ArrayList<>();
		int duration = 0;
		int rangePerDecile = (max - min + 10) / 10;
		int runningTotal = 0;
		for (int decile = 1; decile <= 10; decile++) {
			int nominalItemsInDecile = (int) ((fractionalDistributionDecile[decile - 1]) * numTests);
			int numItemsInDecile = decile == 10 ? numTests - runningTotal : nominalItemsInDecile;
			runningTotal += nominalItemsInDecile;
			for (int counter = 1; counter <= numItemsInDecile; counter++) {
				int decileMinimum = min + rangePerDecile * (decile - 1);
				duration = (int) Math.floor(Math.random() * rangePerDecile + decileMinimum);
				durationsList.add(duration);
			}
		}
		Collections.sort(durationsList);
		return createTestitemListFromDurationList(durationsList);
	}

	public static List<String> createTestitemListFromDurationList(List<Integer> durationsList) {
		List<String> durationItemList = new ArrayList<>();
		for (int itemCounter = 1; itemCounter <= durationsList.size(); itemCounter++) {
			String testName = "Test" + String.valueOf(itemCounter);
			String testDuration = String.valueOf(durationsList.get(itemCounter - 1));
			String testItem = String.join(",", testName, testDuration);
			durationItemList.add(testItem);
		}
		return durationItemList;
	}

	public static List<List<String>> getBestPartitionInfo(List<String> keyValueList, int numPartitions) {
		// Return the partitions based on the best scheme
		return getBestPartition(keyValueList, numPartitions).partitionInfo;
	}

	public static Partition getBestPartition(List<String> keyValueList, int numPartitions) {
		// Get the best scheme
		List<Partition> partitionList = new ArrayList<>();
		List<String> sortedKeyValueList = CommonHelper.sortListBySubstring(keyValueList, Constants.ASCENDING_ORDER, 1,"INT");
		LOG.trace("List sorted in ascending order of values:"+ CommonHelper.convertArrayListToString(sortedKeyValueList, "\n"));
		// TODO: Make the schemes an enum
		Partition partitionsSequential = PartitionHelper.getPartitionsSequential(sortedKeyValueList, numPartitions);
		if (partitionsSequential.isEvenlyDistributed()) {
			partitionList.add(partitionsSequential);
		}
		Partition partitionsJumpLowHigh = PartitionHelper.getPartitionsJumpLowHigh(sortedKeyValueList, numPartitions);
		if (partitionsJumpLowHigh.isEvenlyDistributed()) {
			partitionList.add(partitionsJumpLowHigh);
		}
		Partition partitionsSymmetrical = PartitionHelper.getPartitionsSymmetrical(sortedKeyValueList, numPartitions);
		if (partitionsSymmetrical.isEvenlyDistributed()) {
			partitionList.add(partitionsSymmetrical);
		}
		Partition partitionsSymmetricalHybrid = PartitionHelper.getPartitionsSymmetricalHybrid(sortedKeyValueList,
				numPartitions);
		if (partitionsSymmetricalHybrid.isEvenlyDistributed()) {
			partitionList.add(partitionsSymmetricalHybrid);
		}

		Partition partitionsBigToSmall = PartitionHelper.getPartitionsBigToSmall(sortedKeyValueList,
				numPartitions);
		if (partitionsBigToSmall.isEvenlyDistributed()) {
			partitionList.add(partitionsBigToSmall);
		}
		
//		Partition partitionsBuildToLongest = PartitionHelper.getBuildToLongest(sortedKeyValueList, numPartitions);
//		if (partitionsBuildToLongest.isEvenlyDistributed()) {
//		  partitionList.add(partitionsBuildToLongest);
//		}
		// Gets the partition with the least standard deviation
		Partition bestPartition = Collections.min(partitionList,Comparator.comparing(s -> s.getStandardDeviation()));
		return bestPartition;
	}

	/**
	 * Sort the timings list in ascending order
	 * Scheme Start with the first item in the sorted list and keep adding items to the first partition
	 * as long as the sum of the added items is greater than the target sum. 
	 * Back track one element to see if you get a better fit 
	 * If you get a better fit, stop adding to the partition
	 * Keep going down the line filling partitions
	 *
	 */

	public static Partition getPartitionsSequential(List<String> sortedKeyValueList, int numPartitions) {

		Partition p = new Partition();
		p.partitionScheme = "Sequential";
		Queue<String> q = new LinkedList<String>(sortedKeyValueList);
		// Get the sum of values for each partition
		@SuppressWarnings("unchecked")
		int sumValues = CommonHelper.getSumListSection((List<String>) q, 1);
		int sumValuesPerPartition = sumValues / numPartitions;
		LOG.trace("sumValuesPerPartition: " + sumValuesPerPartition);
		// Iterate across the list
		// Init stuff
		int cumulativeSumValues = 0;
		while (p.partitionId < numPartitions && !q.isEmpty()) {
			String currentItem = q.remove();
			int currentValue = Integer.parseInt(currentItem.split(",")[1]);
			if (currentValue > sumValuesPerPartition && p.partitionItems.isEmpty()) {
				p.addPartitionItem(currentItem);
				p.savePartition();
				p.createPartition();
				cumulativeSumValues = 0;
				continue;
			}
			cumulativeSumValues += currentValue;
			p.addPartitionItem(currentItem);
			// When the sum of values reaches the sum calculated earlier, assign to a
			// partition
			if (cumulativeSumValues >= sumValuesPerPartition) {
				// Checks if we get a better fit by back-tracking
				int sumMinusOne = cumulativeSumValues - currentValue;
				if ((sumValuesPerPartition - sumMinusOne) < (cumulativeSumValues - sumValuesPerPartition)) {
					p.addValue(sumMinusOne);
					p.removeLastPartitionItem();
					((LinkedList<String>) q).addFirst(currentItem); // we'll put it back into the queue
				} else {
					p.addValue(cumulativeSumValues);
				}
				p.logKeyValue();
				p.savePartition();
				p.createPartition();
				cumulativeSumValues = 0;
			}
		}
		// Dump remaining items into the last partition
		while (!q.isEmpty()) {
			String currentItem = q.remove();
			int currentValue = Integer.parseInt(currentItem.split(",")[1]);
			cumulativeSumValues += currentValue;
			p.addPartitionItem(currentItem);
		}
		p.addValue(cumulativeSumValues);
		p.savePartition();
		p.logKeyValue();
		LOG.trace("getPartitionsSequential:");
		p.addMetrics();

		return p;
	}

	/**
	 ** Scheme Start with the first item in the sorted list, then get the item one
	 * partition size down the list Keep going till the partition is filled Then go
	 * to the next partition
	 *
	 * @param keyValueList
	 * @param numPartitions
	 * @return
	 */
	public static Partition getPartitionsJumpLowHigh(List<String> sortedKeyValueList, int numPartitions) {
		Partition p = new Partition();
		p.partitionScheme = "JumpLowHigh";
		// Get the sum of values for each partition
		int sumValues = CommonHelper.getSumListSection(sortedKeyValueList, 1);
		int sumValuesPerPartition = sumValues / numPartitions;
		LOG.trace("sumValuesPerPartition: " + sumValuesPerPartition);
		// Iterate across the list
		int cumulativeSumValues = 0;
		while (p.partitionId <= numPartitions) {
			for (int elementId = 1; elementId <= sortedKeyValueList.size(); elementId++) {
				if (elementId % numPartitions == p.partitionId % numPartitions) {
					cumulativeSumValues += getIndexValueAndAddItem(elementId - 1, sortedKeyValueList, p);
				}
			}
			p.logKeyValue();
			p.savePartition();
			p.addValue(cumulativeSumValues);
			// reset for the next partition
			cumulativeSumValues = 0;
			p.createPartition();
		}
		LOG.trace("getPartitionsJumpLowHigh:");
		p.addMetrics();
		return p;
	}

	/**
	 * Scheme Start with the first item in the sorted list, then get the list item
	 * mirror image Keep going till the partition is filled Then go to the next
	 * partition
	 *
	 * @param keyValueList
	 * @param numPartitions
	 * @return
	 */
	public static Partition getPartitionsSymmetrical(List<String> sortedKeyValueList, int numPartitions) {
		Partition p = new Partition();
		p.partitionScheme = "Symmetrical";
		// Get the sum of values for each partition
		int sumValues = CommonHelper.getSumListSection(sortedKeyValueList, 1);
		;
		int sumValuesPerPartition = sumValues / numPartitions;
		LOG.trace("sumValuesPerPartition: " + sumValuesPerPartition);
		// Iterate across the list
		int cumulativeSumValues = 0;
		for (int partitionId = 1; partitionId <= numPartitions; partitionId++) {
			for (int elementId = 1; elementId <= (sortedKeyValueList.size() + 1) / 2; elementId++) {
				if (elementId % numPartitions == partitionId % numPartitions) {
					cumulativeSumValues += getIndexValueAndAddItem(elementId - 1, sortedKeyValueList, p);
					cumulativeSumValues += getIndexValueAndAddItem(sortedKeyValueList.size() - elementId,
							sortedKeyValueList, p);
				}
			}
			// Save partition info
			p.logKeyValue();
			p.savePartition();
			p.addValue(cumulativeSumValues);
			// reset for the next partition
			cumulativeSumValues = 0;
			p.createPartition();
		}
		LOG.trace("getPartitionsSymmetrical:");
		p.addMetrics();
		return p;
	}

	public static Partition getPartitionsSymmetricalHybrid(List<String> sortedKeyValueList, int numPartitions) {
		// Scheme:
		// Start from the right end of the list ( largest items) and move left till the
		// cumulative sum is just less than the
		// sum expected for the partition
		// Then start from the left-most position and move right, adding items to the
		// partition till the cumulative sum and the
		// sum obtained in the leftward step are as close to the sum expected for the
		// partition
		Partition p = new Partition();
		p.partitionScheme = "SymmetricalHybrid";
		// Get the sum of values for each partition
		int sumValues = CommonHelper.getSumListSection(sortedKeyValueList, 1);
		int sumValuesPerPartition = sumValues / numPartitions;
		int maxTestTime=Integer.parseInt(sortedKeyValueList.get(sortedKeyValueList.size()-1).split(",")[1]);
		sumValuesPerPartition=Math.max(maxTestTime, sumValuesPerPartition);
		LOG.trace("sumValuesPerPartition: " + sumValuesPerPartition);
		// Iterate across the list
		int leftStart = 0;
		int rightStart = sortedKeyValueList.size() - 1;
		for (int partitionId = 1; partitionId <= numPartitions && rightStart >= leftStart; partitionId++) {
			// keep adding items from the left side of the list till the sum is just less than the partition average
			rightStart = moveHighEndToLowEnd(sortedKeyValueList, p, sumValuesPerPartition, rightStart, partitionId);
			// Add items from the right till the sum is just less than the partition average
			leftStart = moveLowEndToHighEnd(sortedKeyValueList, numPartitions, p, sumValuesPerPartition, leftStart,partitionId);
			// Save partition info
			p.logKeyValue();
			p.savePartition();
			p.addValue(p.getCumulativeSum());
			// reset for the next partition
			p.createPartition();
		}
		LOG.trace("getPartitionsSymmetricalHybrid:");
		p.addMetrics();
		return p;
	}
	
	public static Partition getPartitionsBigToSmall(List<String> sortedKeyValueList, int numPartitions) {
		// Scheme:
		// Start from the right end of the list ( largest items) and move left. Select the next item that keeps the
		// cumulative sum  less than the sum expected for the partition. Keep going down the line till the cumulative sum is
		// reached to the required tolerance.
		// Delete all the selected items from the list
		// Repeat the process with the truncated list

		Partition p = new Partition();
		p.partitionScheme = "BigToSmall";
		// Get the sum of values for each partition
		int sumValues = CommonHelper.getSumListSection(sortedKeyValueList, 1);
		int sumValuesPerPartition = sumValues / numPartitions;
		int maxTestTime=Integer.parseInt(sortedKeyValueList.get(sortedKeyValueList.size()-1).split(",")[1]);
		sumValuesPerPartition=Math.max(maxTestTime, sumValuesPerPartition);
		int modifiedNumPartitions= sumValues/sumValuesPerPartition;
		modifiedNumPartitions=Math.min(numPartitions, modifiedNumPartitions);
		LOG.trace("sumValuesPerPartition: " + sumValuesPerPartition);
		// Iterate across the list
		for (int partitionId = 1; partitionId <= modifiedNumPartitions; partitionId++) {
			// keep adding items from the left side of the list till the sum is just higher than the partition average
			sortedKeyValueList = addBigToSmall(sortedKeyValueList, p, sumValuesPerPartition,  partitionId);
			// Save partition info
			p.logKeyValue();
			p.savePartition();
			p.addValue(p.getCumulativeSum());
			// reset for the next partition
			p.createPartition();
		}
		LOG.trace("getPartitionsBigToSmall:");
		p.addMetrics();
		return p;
	}
	private static List<String> addBigToSmall(List<String> sortedKeyValueList, Partition p, int sumValuesPerPartition, int partitionId) {
		String currentItem;
		int idx=sortedKeyValueList.size();
		List<String> sortedKeyValueList1=new ArrayList<String>();
		sortedKeyValueList1.addAll(sortedKeyValueList);
		int sizeLimit= Math.abs( p.getCumulativeSumGlobal() - sumValuesPerPartition * partitionId ); 
		int smallesTestTime=Integer.parseInt(sortedKeyValueList.get(0).split(",")[1]);;
		while (sizeLimit > smallesTestTime-1 ){
			idx = getNextSmallEnoughItem(sortedKeyValueList1, idx-1,sizeLimit );
			if(idx>-1) {
				currentItem= sortedKeyValueList1.get(idx);
				sortedKeyValueList1.remove(idx);
				p.addPartitionItem(currentItem);
			}
			sizeLimit= Math.abs( p.getCumulativeSumGlobal() - sumValuesPerPartition * partitionId ); 
		}
		return sortedKeyValueList1;
	}
	 private static int getNextSmallEnoughItem(List<String> sortedKeyValueList, int startIndex, int sizeLimit) {
		 String item="";
		 int testTime=0;
		 boolean found=false;
		 int startIdx= startIndex;
		 while(!found&&startIdx>-1) {
			 item=sortedKeyValueList.get(startIdx);
			 testTime=Integer.parseInt(item.split(",")[1]);
			 if(testTime<=sizeLimit) {
				 found = true;
			 }else {
				 startIdx--;
			 }
		 }
		 return startIdx;
	 }
	 
	 /**
		 * Scheme Start with the first item in the sorted list, sorted by largest test first. The list of tests is traversed looking
		 * for tests that fit to match the longest running test.
		 *
		 * @param keyValueList
		 * @param numPartitions
		 * @return
		 */
		public static Partition getBuildToLongest(List<String> sortedKeyValueList, int numPartitions) {
			Partition p = new Partition();
			List<String> sortedList = new ArrayList<>(sortedKeyValueList);
			Collections.reverse (sortedList);     // copy the list
			p.partitionScheme = "ResortFlip";
			
			int sumValues = CommonHelper.getSumListSection(sortedKeyValueList, 1);
			int sumValuesPerPartition = sumValues / numPartitions;
			int maxTestTime=Integer.parseInt(sortedKeyValueList.get(sortedKeyValueList.size()-1).split(",")[1]);
			sumValuesPerPartition=Math.max(maxTestTime, sumValuesPerPartition);
			int modifiedNumPartitions= sumValues/sumValuesPerPartition;
			modifiedNumPartitions=Math.min(numPartitions, modifiedNumPartitions);
			
			
		   while (!sortedList.isEmpty()) {
			   int i = 0;
			   String largest = sortedList.remove(i);
			   p.addPartitionItem(largest);
			   p.savePartition();
				int maxRuntime = p.getCumulativeSum();
				p.addValue(maxRuntime);
				p.createPartition();
			   
			   i++;
			   while (i < modifiedNumPartitions && !sortedList.isEmpty()) {
				   int length = 0;
				   String next = sortedList.remove(i);
				   p.addPartitionItem(next);
				   length += getLength(next);
				   while (!sortedList.isEmpty() && length <  maxRuntime) {
					    boolean found = false;
					    Iterator<String> iterator = sortedList.iterator();
					    int j = 0;
					    for (; iterator.hasNext(); ++j) {                            
							int lengthNext = getLength(iterator.next());
							if (length  + lengthNext <= maxRuntime) {
					    		next = sortedList.remove(j);
					    		length += getLength(next);
					    		p.addPartitionItem(next);
					    		found = true;
					    		break;
					    	}
					    }
					    if (!found) {
					    	break;
					    }
				   }
				   p.savePartition();
				   int runtime = p.getCumulativeSum();
					p.addValue(runtime);
					p.createPartition();

				   i++;
				   
			   }
				
			}
		   p.addMetrics();
	       return p;
		}

		public static int getLength(String next) {
			return Integer.parseInt(next.split(",")[1]);
		}

	
	
	private static int moveLowEndToHighEnd(List<String> sortedKeyValueList, int numPartitions, Partition p,int sumValuesPerPartition, int leftStart, int partitionId) {
		String currentItem;
		while (p.getCumulativeSumGlobal() <= sumValuesPerPartition * partitionId) {
			currentItem = sortedKeyValueList.get(leftStart);
			p.addPartitionItem(currentItem);
			leftStart++;
		}
		// Remove the last added item to bring the sum less than the average
		if (p.getCumulativeSumGlobal() > sumValuesPerPartition * partitionId && p.partitionId < numPartitions && p.partitionItems.size() >1) {
			p.removeLastPartitionItem();
			leftStart--;
		}
		return leftStart;
	}

	private static int moveHighEndToLowEnd(List<String> sortedKeyValueList, Partition p, int sumValuesPerPartition,int rightStart, int partitionId) {
		String currentItem;
		while (rightStart >= 0 && p.getCumulativeSumGlobal() <= sumValuesPerPartition * partitionId) {
			currentItem = sortedKeyValueList.get(rightStart);
			p.addPartitionItem(currentItem);
			rightStart--;
		}
		// Remove the last added item to bring the sum less than the average
		if (p.getCumulativeSumGlobal() > sumValuesPerPartition * partitionId ) {
			p.removeLastPartitionItem();
			rightStart++;
		}
		return rightStart;
	}

//TO DO: This function does 2 things: returns index value and updates the partition. Side effects must be explicitly called out
	private static int getIndexValueAndAddItem(int index, List<String> sortedKeyValueList, Partition p) {
		String currentItem = sortedKeyValueList.get(index);
		int currentValue = Integer.parseInt(currentItem.split(",")[1]);
		p.addPartitionItem(currentItem);
		return currentValue;
	}

	// 107 test list
//	durationsList = Arrays.asList("basicInverterChargeHighVoltageLimit,258",
//	"basicInverterChargeLowVoltageLimit,246", "basicInverterChargeMinimumStringCount,257",
//	"basicInverterChargeVoltageDeltaLimit,275", "basicInverterDischargeHighVoltageLimit,253",
//	"basicInverterDischargeLowVoltageLimit,259", "basicInverterDischargeMinimumStringCount,248",
//	"basicInverterDischargeVoltageDeltaLimit,249", "basicInverterOperationHighVoltageLimit,301",
//	"basicInverterOperationLowVoltageLimit,279", "basicInverterOperationMinimumStringCount,267",
//	"basicInverterOperationVoltageDeltaLimit,280", "basicOpenClose,237",
//	"basicOpsFirstPowerCommandSecond,875", "basicOpsFirstSunspecSecond,185",
//	"basicOpsMessageValidation,840", "basicOpsPowerValidationBasic,2448",
//	"basicOpsPowerValidationComplex,922", "c2988_GuiEnableTest,88", "c3190_DisableTest,164",
//	"c3190_EnableTest,164", "c3194_GetVoltageFromMeterAndShowInAppStatusTest,176",
//	"c3200_SetOutOfRangeVoltageTest,164", "c3201_SetInRangeVoltageTest,164", "C3288_ResetToDefault,136",
//	"C3288_SetFreqHzPower,114", "C3290_TestUpperBoundTest,178", "c3306Test,72", "regression_bpVoltage,2640",
//	"c3307Test,83", "c3308Test,73", "c3309_EnableTestTest,1094", "c3311_DisableTestTest,1152",
//	"c3312_DisconnectTestTest,79", "c3313_ConnectTestTest,74",
//	"c3314_IssueDisconnectCommandWithAppDisabledTest,66", "c3315_IssueConnectCommandWithAppDisabledTest,86",
//	"c3343Test,74", "c3344Test,74", "c3345Test,98", "c3346Test,74", "c3519_ChargingTestWithoutStopTest,202",
//	"c3520_SwitchFromCharging2DischargingTest,139", "c3521_DischargingTestWithoutStopTest,201",
//	"c3522_SwitchFromDischarging2ChargingTest,139", "c3710_SetFrequencyToTargethzWhileChargingTest,189",
//	"c3711_SetFrequencyToTargethzWhileDischargingTest,191", "chrgBlockedTest,206",
//	"demandManagementMessageValidation,634", "disable,79", "disableDemandManagementAppTest,95",
//	"disablePowerCommandAppTest,67", "disableSchedulerAppTest,1296",
//	"disableSunspecPowerCommandAppTest,201", "dischgBlockedTest,214", "enable,979",
//	"enableDemandManagementAppTest,95", "enablePowerCommandAppTest,67", "enableRechargerAppTest,159",
//	"enableSchedulerAppTest,403", "enableSunspecPowerCommandAppTest,3542", "frequencywatt,85",
//	"hysteresisTestInverterChargeHighVoltageLimit,159", "hysteresisTestInverterChargeLowVoltageLimit,155",
//	"inverterChargingHappyPath,123", "inverterDischargingHappyPath,113", "modbus103,489", "modbus120,2304",
//	"modbus123,1497", "modbus802,1584", "modbus803,2419",
//	"movePowerWhenSoCLowerThanTriggerAndStopsAtTarget,150", "noPowerMovesWhenSoCHigherThanTrigger,136",
//	"opsBlockedHvhTest,1699", "opsBlockedHvTest,217", "regression2,2505", "hysteresis,432",
//	"regressionKnownFail,2390", "opsBlockedLvhTest,218", "opsBlockedLvTest,217", "powercommand,158",
//	"PowerCommandFirstBasicOpsSecond,876", "PowerCommandFirstSchedulerSecond,806", "realpowercommand,249",
//	"RechargerStopsMovingPowerWhenBasicOpMovesPower,720",
//	"RechargerStopsMovingPowerWhenSunSpecMovesPower,432", "retry,322",
//	"SchedulerFirstPowerCommandSecond,1227", "SchedulerFirstSunspecSecond,1270", "scheduleScheduler,3254",
//	"setPowerTest,212", "slowcharge,79", "soccalibrator,110", "sunspecFirstBasicOpsSecond,1071",
//	"SunspecFirstSchedulerSecond,1273", "validateDemandManagementTest,927", "validateLoadEqDct,1576",
//	"validateLoadGtDct,1576", "validateLoadLtDct,1576", "verifyDisableTest,100", "verifyEnableTest,3254",
//	"verifyPowerTest,155", "verifyVarSetParameterizedTest,275", "recharger,383", "estop,237",
//	"ercotffr,258", "frequencydroop,177");

}
