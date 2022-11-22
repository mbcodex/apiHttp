package mbTest.utilities;

import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MathHelper {
	// function to calculate m and c that best fit points
	// represented by x[] and y[]
	public static double[] bestApproximate(int x[], int y[]) {
		int n = x.length;
		double m, c, sum_x = 0, sum_y = 0, sum_xy = 0, sum_x2 = 0;
		for (int i = 0; i < n; i++) {
			sum_x += x[i];
			sum_y += y[i];
			sum_xy += x[i] * y[i];
			sum_x2 += pow(x[i], 2);
		}

		m = (n * sum_xy - sum_x * sum_y) / (n * sum_x2 - pow(sum_x, 2));
		c = (sum_y - m * sum_x) / n;

		System.out.println("m = " + m);
		System.out.println("c = " + c);
		double arr[]={m,c};
		return arr;
	}
	
	public static double[] bestApproximate1(List<List<Double>> pointList) {
		int n = pointList.size();
		double m, c, x,y,sum_x = 0, sum_y = 0, sum_xy = 0, sum_x2 = 0;
		for (int i = 0; i < n; i++) {
			x=pointList.get(i).get(0);
			y=pointList.get(i).get(1);
			sum_x += x;
			sum_y += y;
			sum_xy += x * y;
			sum_x2 += pow(x, 2);
		}

		m = (n * sum_xy - sum_x * sum_y) / (n * sum_x2 - pow(sum_x, 2));
		c = (sum_y - m * sum_x) / n;

		System.out.println("m = " + m);
		System.out.println("c = " + c);
		double arr[]={m,c};
		return arr;
	}
	
	public static double[] bestApproximate(List<List<Long>> pointList) {
		int n = pointList.size();
		double m, c, x,y,sum_x = 0, sum_y = 0, sum_xy = 0, sum_x2 = 0;
		for (int i = 0; i < n; i++) {
			x=pointList.get(i).get(0);
			y=pointList.get(i).get(1);
			sum_x += x;
			sum_y += y;
			sum_xy += x * y;
			sum_x2 += pow(x, 2);
		}

		m = (n * sum_xy - sum_x * sum_y) / (n * sum_x2 - pow(sum_x, 2));
		c = (sum_y - m * sum_x) / n;

		System.out.println("m = " + m);
		System.out.println("c = " + c);
		double arr[]={m,c};
		return arr;
	}

	// Driver main function
	public static void main(String args[]) {
		int x[] = { 1, 2, 3, 4, 5 };
		int y[] = { 14, 27, 40, 55, 68 };
		bestApproximate(x, y);
		
		List<Long> pt1= Arrays.asList(1L,14L);
		List<Long> pt2= Arrays.asList(2L,27L);
		List<Long> pt3= Arrays.asList(3L,40L);
		List<Long> pt4= Arrays.asList(4L,55L);
		List<Long> pt5= Arrays.asList(5L,68L);
		List<List<Long>> pointList=new ArrayList<>();
		pointList.add(pt1);
		pointList.add(pt2);
		pointList.add(pt3);
		pointList.add(pt4);
		pointList.add(pt5);
		bestApproximate(pointList);
		
	}

}
