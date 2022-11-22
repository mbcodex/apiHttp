package helpers;

import main.api;

public class CommonHelper {

	public static String removeQuotes(String numItems) {
		numItems=numItems.replaceAll("\"", "");
		return numItems;
	}

	public static int getNumPages(int totalItems) {
		//Based on the number of items in a page, calculates the number of pages 
		return (int)(Math.ceil((double)totalItems/api.NUM_ITEMS_PER_PAGE));
	}

}
