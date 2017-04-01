package ticketingsystem;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.plaf.SliderUI;

public class Test {
	private static ExecutorService  executorService = Executors.newFixedThreadPool(16);
	
	private final static int routenum = 5 ;//车次总数
	private final static int coachnum = 8;//列车车厢数
	private final static int seatnum = 100;//座位数
	private final static int stationnum = 10;//车次经停站的数量
	
	private static TicketingDS ticketingDS = new TicketingDS(5,8,100,10);
	
	private static AtomicInteger counter = new AtomicInteger(0);
	
	private static BlockingQueue<Ticket> list = new LinkedBlockingQueue<>();
	
	public static class buyTicket implements Runnable {
		@Override
		public void run() {
			Random random = new Random(System.currentTimeMillis());
			
			String name = "" + (char)('a' + random.nextInt(26)) + (char)('a' + random.nextInt(26));
			int route = random.nextInt(routenum) + 1;
			int departure = random.nextInt(stationnum - 1) + 1;
			int arrival = random.nextInt(stationnum - departure) + 1 + departure;
			
			Ticket ticket = ticketingDS.buyTicket(name, route, departure, arrival);
			System.out.println("[" + (counter.incrementAndGet()) + ":" + ticket + "]");
			
			list.add(ticket);
		}
	}
	
	public static class refundTicket implements Runnable {
		@Override
		public void run() {
			while(true){
				Ticket ticket = list.poll();
				if(ticket != null){					
					System.out.println("[" + ticket + "  " + ticketingDS.refundTicket(ticket) + "]");
				}
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}
	
	public static class queryTicket implements Runnable {

		@Override
		public void run() {
			Random random = new Random(System.currentTimeMillis());
			int route = random.nextInt(routenum) + 1;
			int departure = random.nextInt(stationnum - 1) + 1;
			int arrival = random.nextInt(stationnum - departure) + 1 + departure;
			
			int res = ticketingDS.inquiry(route, departure, arrival);
			System.out.println("[" + route + ":" + departure + ":" + arrival + "  " + res + "]");
		}
		
	}

	public static void main(String[] args){
		//重定向 for debug
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream("./stdio.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.setOut(ps);
		
		
		for(int i = 0; i < 1000; i++){
			executorService.execute(new buyTicket());			
			executorService.execute(new queryTicket());			
		}		
		
		executorService.execute(new queryTicket());	
		
		executorService.shutdown();
		
		//关闭售票代理
//		try {
//			Thread.sleep(10000); //代理等待客户端请求完毕
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}		
//		ticketingDS.shutDown();
	}
}
