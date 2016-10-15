package org.apache.hadoop.examples;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class test {
public static void main(String args[]){
	Map<String,Integer> map=new HashMap<String,Integer>();
	ValueComparator vc=new ValueComparator(map);
	TreeMap<String,Integer> sorted_map=new TreeMap<String,Integer>(vc);
	map.put("A",15);
	map.put("B",10);
	sorted_map.putAll(map);
	System.out.println(sorted_map);
	
	
}

}
 class ValueComparator implements Comparator<String>{
	Map<String,Integer> base;
	public ValueComparator(Map<String,Integer> m){
		base=m;
	}
	public int compare(String w1,String w2){
		if(base.get(w1)>base.get(w2))
			return 1;
		else 
			return -1;
	}
}