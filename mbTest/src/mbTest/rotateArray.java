package mbTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class rotateArray {
	
	public static List<List<Integer>> rotate(List<List<Integer>> inputSquare) {
		HashMap<List<Integer>,Integer> initialValues= new HashMap<List<Integer>, Integer>();
		HashMap<List<Integer>,List<Integer>> rotationMap= new HashMap<List<Integer>,List<Integer>>();
		for ( int rows=0;rows<inputSquare.size();rows++) {
			for( int cols=0;cols<inputSquare.size();cols++) {
				List<Integer> indices= Arrays.asList(rows,cols);
				int value= inputSquare.get(rows).get(cols);
				initialValues.put(indices, value);
				List<Integer>rotatedIndices=getRotatedIndices(indices,inputSquare.size());
				rotationMap.put(indices, rotatedIndices);
			}
		}
		System.out.println("Rotation map:");
		rotationMap.forEach((key,value)->System.out.println(Arrays.toString(key.toArray())+":"+Arrays.toString(value.toArray())));
		System.out.println("Initial values:");
		initialValues.forEach((key,value)->System.out.println(Arrays.toString(key.toArray())+":"+value));
		for (List<Integer> initialIndices:initialValues.keySet()) {		
				List<Integer >rotatedIndices=rotationMap.get(initialIndices);
				int originalValue=initialValues.get(initialIndices);
				inputSquare.get(rotatedIndices.get(0)).set(rotatedIndices.get(1),originalValue);
		}
		return inputSquare;
	}

	private static List<Integer> getRotatedIndices(List<Integer> indices, int arraySize) {
		int newRow=indices.get(1);//new row = old column
		int newCol =arraySize -1 -indices.get(0);//new col = size-1-old_row
		return Arrays.asList(newRow,newCol);
	}
	
	public static void main (String[ ]args) {
		int arraySize=4;
		List<List<Integer>> inputSquare= new ArrayList<List<Integer>>();
		for ( int rows=0;rows<arraySize;rows++) {
			List<Integer>inputRow= new ArrayList<Integer>();
			for( int cols=0;cols<arraySize;cols++) {
				inputRow.add((int)(100*Math.random()));
			}
			inputSquare.add(inputRow);
		}	
		System.out.println("Before rotation:"+Arrays.deepToString(inputSquare.toArray()));	
		rotate(inputSquare);
		System.out.println("After rotation:"+Arrays.deepToString(inputSquare.toArray()));
	}
}
