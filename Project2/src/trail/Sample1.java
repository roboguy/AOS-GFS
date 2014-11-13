package trail;

import java.util.HashMap;

public class Sample1 {
	public static void main(String[] args) {
		HashMap<Integer, Long> list =  new HashMap<>();
		list.put(1, System.currentTimeMillis());
		list.put(2, System.currentTimeMillis());
		list.put(3, System.currentTimeMillis());
		System.out.println(list);
		try{
			Thread.sleep(2000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		if(list.get(1) != null) {
			long diff = System.currentTimeMillis() - list.get(1);
			System.out.println("difference : "+ diff);
			list.put(1, System.currentTimeMillis());
		}
		System.out.println(list);
		
		String some = "Sunil:Munavalli:Shwetha";
		String[] chunk = some.split(":");
		if(chunk.length > 2) {
			System.out.println(chunk[2]);
		}
				
	}
}
