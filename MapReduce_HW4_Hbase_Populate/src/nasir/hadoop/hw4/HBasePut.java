package nasir.hadoop.hw4;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class HBasePut {
	public static Configuration conf;
	public static HTable table ;
	public byte[] colFamily;
	public byte[] colQName;
	public HBasePut() throws IOException{
		conf = new Configuration();
		table = new HTable(conf, "flights");
		colFamily=Bytes.toBytes("delay");
		colQName=Bytes.toBytes("val");
	}
public static void main(String args[]) throws IOException{
	HBasePut hbs=new HBasePut();
	hbs.put1();
}
public void put1() throws IOException{
	    byte[] rowKey=Bytes.toBytes("UA#3#9");
	    byte[] val=Bytes.toBytes(26);
	    Put p=new Put(rowKey) ;
	    p.add(colFamily, colQName, val);
	    table.put(p);
	    System.out.println("Value Put");
}
}
