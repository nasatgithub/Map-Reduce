package nasir.hadoop.hw4;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseScanner {
	public static Configuration conf;
	public static HTable table ;
	public HBaseScanner() throws IOException{
		conf = new Configuration();
		table = new HTable(conf, "flights");
	}
public static void main(String args[]) throws IOException{
	HBaseScanner hbs=new HBaseScanner();
	hbs.scan1();
}
public void scan1() throws IOException{
	    byte[] prefix = Bytes.toBytes("UA#3#9");
	    Scan scan = new Scan(prefix);
	    Filter prefixFilter = new PrefixFilter(prefix);
	    scan.setFilter(prefixFilter);
	    ResultScanner resultScanner = table.getScanner(scan);
	    
	    for(Iterator<Result> iterator = resultScanner.iterator(); iterator.hasNext();){
	    	Result r=iterator.next();
	    	System.out.println(Bytes.toString(r.getRow())+" "+Bytes.toInt(r.getValue(Bytes.toBytes("delay"),Bytes.toBytes("val"))));
	    	//System.out.println();
	    }
}
}
