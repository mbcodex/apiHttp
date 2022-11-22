package mbTest;

import java.io.*;
import java.math.*;
import java.security.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class Result {

	/*
	 * Complete the 'arrayManipulation' function below.
	 *
	 * The function is expected to return a LONG_INTEGER. The function accepts
	 * following parameters: 1. INTEGER n 2. 2D_INTEGER_ARRAY queries
	 */
	public static List<Long> maxList = new ArrayList();

//	static class MyRunnable implements Runnable {
//		private int startIndex;
//		private int numElements;
//		private List<List<Integer>> queries=new ArrayList();;
//
//		MyRunnable(int startIndex, int numElements, List<List<Integer>> queries) {
//			this.startIndex = startIndex;
//			this.numElements = numElements;
//			this.queries.addAll(queries);
//		}
//
//		@Override
//		public void run() {
//			long max = arrayManipulationThread(startIndex, numElements, queries);
//			maxList.add(max);
//			System.out.println("max:" + max);
//		}
//	}
	static class MyRunnable implements Runnable {
		private int startIndex;
		private List<List<Integer>> queries=new ArrayList();;

		MyRunnable(int startIndex, List<List<Integer>> queries) {
			this.startIndex = startIndex;
			this.queries.addAll(queries);
		}

		@Override
		public void run() {
			long max = arrayManipulationThread(startIndex, queries);
//			if(maxList.size()<=startIndex) {
//				maxList.add(max);
//			}else {
//			long temp=maxList.get(startIndex);
//			maxList.set(startIndex,max+temp);
//			}
//			System.out.println("max:" + max);
		}
	}

//	public static long arrayManipulation(int n, List<List<Integer>> queries) {
//		// Write your code here
//		int arrayPartitionSize=100000000;
//		int fullCycles;
//		int partialCycleCount;
//		fullCycles = n / arrayPartitionSize;
//		partialCycleCount = n % arrayPartitionSize;
//		List<Thread> threads = new ArrayList<Thread>();
//		for (int fullCycleCount = 1; fullCycleCount <= fullCycles + 1; fullCycleCount++) {
//
//			// We will create threads for each full cycle and one for the remaining partial
//			// cycle
//			int numElements;
//			int startIndex;
//			if (fullCycleCount != fullCycles + 1) {
//				numElements = arrayPartitionSize;
//				startIndex = arrayPartitionSize * (fullCycleCount - 1)+1;
//			} else {
//				numElements = partialCycleCount;
//				startIndex = arrayPartitionSize * (fullCycleCount - 1)+1;
//			}
//
//			Runnable task = new MyRunnable(startIndex, numElements, queries);
//			Thread worker = new Thread(task);
//			// We can set the name of the thread
//			worker.setName(String.valueOf(fullCycleCount));
//			// Start the thread, never call method run() direct
//			worker.start();
//			// Remember the thread for later usage
//			threads.add(worker);
//
//		}
//		int running = 0;
//		do {
//			running = 0;
//			for (Thread thread : threads) {
//				if (thread.isAlive()) {
//					running++;
//				}
//			}
//			System.out.println("We have " + running + " running threads. ");
//		} while (running > 0);
//		return Collections.max(maxList);
//	}
	public static long arrayManipulation(int n, List<List<Integer>> queries) {
		// Write your code here
		int queriesPartitionSize=1000;
		int fullCycles;
		int partialCycleCount;
		fullCycles = queries.size() / queriesPartitionSize;
		partialCycleCount = queries.size() % queriesPartitionSize;
		List<Thread> threads = new ArrayList<Thread>();
		for ( int arrayIdx=0;arrayIdx<n;arrayIdx++) {
			for (int fullCycleCount = 0; fullCycleCount < fullCycles ; fullCycleCount++) {
				int numElements;
				int startIndex;
				List<List<Integer>>sublist=new ArrayList();
				numElements = queriesPartitionSize;
				startIndex = queriesPartitionSize * (fullCycleCount );
				sublist=queries.subList(startIndex, startIndex+numElements);
				Runnable task = new MyRunnable(fullCycleCount,sublist);
				Thread worker = new Thread(task);
				// We can set the name of the thread
				worker.setName(String.valueOf(fullCycleCount));
				// Start the thread, never call method run() direct
				worker.start();
				// Remember the thread for later usage
				threads.add(worker);
	
			}
			if(partialCycleCount>0) {
				
				// We will create threads for each full cycle and one for the remaining partial
				// cycle
				int numElements;
				int startIndex;
				List<List<Integer>>sublist=new ArrayList();
				numElements = partialCycleCount;
				startIndex = queriesPartitionSize * (fullCycles );
				sublist=queries.subList(startIndex, startIndex+partialCycleCount);
	
				Runnable task = new MyRunnable(fullCycles,sublist);
				Thread worker = new Thread(task);
				// We can set the name of the thread
				worker.setName(String.valueOf(fullCycles));
				// Start the thread, never call method run() direct
				worker.start();
				// Remember the thread for later usage
				threads.add(worker);
	
			}
			int running = 0;
			do {
				running = 0;
				for (Thread thread : threads) {
					if (thread.isAlive()) {
						running++;
					}
				}
				System.out.println("We have " + running + " running threads. ");
			} while (running > 0);
		}
		return Collections.max(maxList);
	}

	public static long arrayManipulationThread(int index, List<List<Integer>> queries) {
		// Write your code here
		int start = 0;
		int end = 0;
		int add = 0;
		long MAX = 0L;
		//for (int idx1 = startIndex; idx1 < startIndex + numElements; idx1++) {
		long newValue = 0L;
		for (int idx = 0; idx < queries.size(); idx++) {
			start = queries.get(idx).get(0)-1;
			end = queries.get(idx).get(1)-1;
			add = queries.get(idx).get(2);
			if (index >= start && index <= end) {
				newValue += Long.valueOf(add);
			}
		}
		if(maxList.size()<=index) {
			maxList.add(index,newValue);
		}else {
			long temp=maxList.get(index);
			maxList.set(index,newValue+temp);
		}
		return newValue;
	}
}

public class arrayManipulation {
	public static void main(String[] args) throws IOException {

//max:2493657359
//max:2495472558
//max:2494773569
//max:249 586 6814

		//249 716 9732 answer for problem 7
		BufferedReader bufferedReader = new BufferedReader(new FileReader("/Users/mb/test.txt"));
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(System.out));

		String[] firstMultipleInput = bufferedReader.readLine().replaceAll("\\s+$", "").split(" ");

		int n = Integer.parseInt(firstMultipleInput[0]);

		int m = Integer.parseInt(firstMultipleInput[1]);

		List<List<Integer>> queries = new ArrayList<>();

		IntStream.range(0, m).forEach(i -> {
			try {
				queries.add(Stream.of(bufferedReader.readLine().replaceAll("\\s+$", "").split(" "))
						.map(Integer::parseInt).collect(toList()));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		});

		long result = Result.arrayManipulation(n, queries);

		bufferedWriter.write(String.valueOf(result));
		bufferedWriter.newLine();

		bufferedReader.close();
		bufferedWriter.close();
	}
}
