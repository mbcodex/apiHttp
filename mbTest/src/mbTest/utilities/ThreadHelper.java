package mbTest.utilities;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThreadHelper {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<String> testList=new ArrayList();
		for (int idx=0;idx<10;idx++){
			testList.add(String.valueOf(idx));
		}
		System.out.println(CommonHelper.convertListToString(testList));
	}

}
