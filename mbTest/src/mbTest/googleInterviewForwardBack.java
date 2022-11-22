package mbTest;

public class googleInterviewForwardBack {
	
	public static long drive(String instructions) {
		long totalDistance=0L;
		char[] instructionsArray=instructions.toCharArray();
		int cursor=0;
		boolean movingForward=true;
		while(cursor < instructionsArray.length) {
			while(cursor < instructionsArray.length && instructionsArray[cursor]=='R') {
				cursor++;
				movingForward=!movingForward;
			}
			int movementIncrement=0;
			while(cursor < instructionsArray.length && instructionsArray[cursor]=='A') {
				cursor++;
				movementIncrement++;
			}
			long netMovement= getSumOfMovementIncrements(movementIncrement);
			if(movingForward) {
				totalDistance += netMovement;
			}else {
				totalDistance -= netMovement;
			}
		}
		return totalDistance;
	}

	private static long getSumOfMovementIncrements(int movementIncrement) {
		return (long) Math.pow(2, movementIncrement) -1;
	}

	public static void main(String[] args) {
		String instructions="RAAARAAARAAARAAA";
		System.out.println("Distance:"+drive(instructions));
	}

}
