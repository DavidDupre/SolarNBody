

public class Timer extends Thread{
	private Thread thread;
	public double elapsedTime;
	private double lastTime;
	private double time = 0;
	private int samples = 0;
	private double average = 0;
	private double deltaTime;
	
	public Timer() {
		this.elapsedTime = 0;
	}
	
	public void run() {
		while (true){
			lastTime = time;
			time = elapsedTime/60/60/24/365;
			deltaTime = time-lastTime;
			double sum = average*samples;
			sum += deltaTime;
			samples += 1;
			average = sum/samples;
			System.out.printf("Delta time: %f, Average time: %f\n", deltaTime, average);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setName("Timer");
			thread.start();
		}
	}
}
