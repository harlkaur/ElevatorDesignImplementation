import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Stack;

public class ElevatorDesign {

	public static void main(String[] args) {
		System.out.println(" Welcome to Omega Elevator, Have a nice day!!! ");
		System.out.println(" Please enter the floor you want to go and your weight ");
		// Threads for request and processing
		Thread request = new Thread(new RequestThread());
		Thread process = new Thread(new ProcessThread());
		request.start();
		process.start();
	}
}

class ElevatorPair {
	int floor;
	int weight;

	ElevatorPair(int floor, int weight) {
		this.floor = floor;
		this.weight = weight;
	}

}

class RequestThread implements Runnable {

	@Override
	public void run() {
		while (true) {
			int floorNum = 0;
			int weight = 0;
			// Read input from user
			Scanner sc = new Scanner(System.in);
			floorNum = sc.nextInt();

			weight = sc.nextInt();

			System.out.println("User Pressed : " + floorNum);
			Elevator elevator = Elevator.getInstance();
			elevator.addFloor((floorNum), weight);
		}
	}
}

class ProcessThread implements Runnable {

	@Override
	public void run() {
		while (true) {
			Elevator elevator = Elevator.getInstance();
			int floor = elevator.getFloor();
			int currFloor = elevator.getCurrentFloor();
			if (currFloor > floor) {
				while (currFloor > floor) {
					elevator.setCurrentFloor(--currFloor);
					int flr = elevator.getFloor();
					if (flr == floor)
						continue;
					else {
						floor = flr;
					}
				}

				if (currFloor == floor) {
					System.out.println("Processing Request : " + floor);
					elevator.remove(floor);
				}

			} else {
				while (currFloor < floor) {
					elevator.setCurrentFloor(++currFloor);
					int flr = elevator.getFloor();
					if (flr == floor)
						continue;
					else {
						floor = flr;
					}
				}

				if (currFloor == floor) {
					System.out.println("Processing Request : " + floor);
					elevator.remove(floor);
				}
			}
		}
	}
}

class Elevator {

	public static PriorityQueue<ElevatorPair> queue = new PriorityQueue<ElevatorPair>(new Comparator<ElevatorPair>() {

		@Override
		public int compare(ElevatorPair o1, ElevatorPair o2) {
			// TODO Auto-generated method stub
			if (o1.floor < o2.floor)
				return -1;
			else if (o1.floor > o2.floor)
				return 1;

			return 0;
		}
	});
	private DIRECTION direction = DIRECTION.UP;
	private int currentFloor = 0;
	private static Elevator elevator = null;

	public enum DIRECTION {
		UP, DOWN;
	}

	private Elevator() {

	}

	public void remove(int floor) {

		PriorityQueue<ElevatorPair> temp = new PriorityQueue<ElevatorPair>(new Comparator<ElevatorPair>() {

			@Override
			public int compare(ElevatorPair o1, ElevatorPair o2) {
				if (o1.floor < o2.floor)
					return -1;
				else if (o1.floor > o2.floor)
					return 1;

				return 0;

			}
		});

		while (!queue.isEmpty()) {
			ElevatorPair floorQueue = queue.poll();
			if (floorQueue.floor == floor)
				continue;

			temp.offer(floorQueue);
		}
		queue = temp;
	}

	public DIRECTION getDirection() {
		return direction;
	}

	public void setDirection(DIRECTION direction) {
		this.direction = direction;
	}

	// Implementing singleton design pattern
	static Elevator getInstance() {
		if (elevator == null) {
			elevator = new Elevator();
		}
		return elevator;
	}

	public synchronized void addFloor(int floor, int weight) {
		// add the request from user to reach to specific floor

		PriorityQueue<ElevatorPair> temp = new PriorityQueue<ElevatorPair>(new Comparator<ElevatorPair>() {

			@Override
			public int compare(ElevatorPair o1, ElevatorPair o2) {
				// TODO Auto-generated method stub
				if (o1.floor < o2.floor)
					return -1;
				else if (o1.floor > o2.floor)
					return 1;

				return 0;

			}
		});
		Integer ceil = null, wght = 0;
		while (!queue.isEmpty()) {
			ElevatorPair floorQueue = queue.poll();
			temp.offer(floorQueue);
			wght = wght + floorQueue.weight;
		}

		if (wght + weight > 100)
			System.out.println("Sorry weight exceeds the capacity..");
		else {
			temp.add(new ElevatorPair(floor, weight));
			// sending notification to the thread to process it.

		}

		queue = temp;
		notify();
	}

	public int getCurrentFloor() {
		return currentFloor;
	}

	public void setCurrentFloor(int currentFloor) {
		if (this.currentFloor > currentFloor) {
			setDirection(DIRECTION.DOWN);
		} else {
			setDirection(DIRECTION.UP);
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			System.out.println(" Exception occurred: " + e.getMessage());
		}
		this.currentFloor = currentFloor;
		System.out.println(" Floor is : " + currentFloor);
	}

	private Integer findCeiling(int currentFloor) {
		PriorityQueue<ElevatorPair> temp = new PriorityQueue<ElevatorPair>(new Comparator<ElevatorPair>() {

			@Override
			public int compare(ElevatorPair o1, ElevatorPair o2) {
				// TODO Auto-generated method stub
				if (o1.floor < o2.floor)
					return -1;
				else if (o1.floor > o2.floor)
					return 1;

				return 0;

			}
		});
		Integer ceil = null;
		while (!queue.isEmpty()) {

			ElevatorPair elevator = queue.poll();
			Integer floor = elevator.floor;
			temp.offer(elevator);
			if (floor >= currentFloor) {
				ceil = floor;
				break;
			}
		}
		while (!queue.isEmpty()) {
			temp.offer(queue.poll());
		}
		queue = temp;
		return ceil;
	}

	private Integer findFloor(int currentFloor) {
		Stack<ElevatorPair> stack = new Stack<ElevatorPair>();
		while (!queue.isEmpty()) {
			stack.push(queue.poll());
		}

		Integer flr = null;
		while (!stack.isEmpty()) {
			ElevatorPair elevator = stack.pop();
			Integer floor = elevator.floor;
			queue.offer(elevator);
			if (floor <= currentFloor) {
				flr = floor;
				break;
			}
		}

		while (!stack.isEmpty()) {
			queue.offer(stack.pop());
		}
		return flr;

	}

	public synchronized int getFloor() {
		Integer floor = null;

		if (direction == DIRECTION.UP) {
			Integer value = findCeiling(currentFloor);
			if (value != null) {
				floor = value;
			} else {
				floor = findFloor(currentFloor);
			}
		} else {
			Integer value = findFloor(currentFloor);
			if (value != null) {
				floor = value;
			} else {
				floor = findCeiling(currentFloor);
			}
		}
		if (floor == null) {
			try {
				System.out.println("No Request to process. Waiting");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return (floor == null) ? -1 : floor;
	}

}
