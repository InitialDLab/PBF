package edu.ecnu.pbf.sketch.fm;

public class mytest {

	public static void main(String[] args) {

	    HyperLogLog hll = HyperLogLog.builder().build();
	    HyperLogLog hll2 = HyperLogLog.builder().build();
	    if (hll != hll2)
	    {
	    	System.out.println("good");
	    }
	    
	    for (int i = 0; i < 200000; i++) {
	    	for (int j = 0; j < 3; j++) {
	    		hll.addString(i+"i");
	    	}
	    }
	    System.out.println("estimate count: " + hll.count());
	}

}

