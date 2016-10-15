package nasir.hadoop.hw4;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class HbaseCompute {
	public static class MyMapper 
	  extends TableMapper<FlightKeyPair, DoubleWritable>  {

		private final IntWritable ONE = new IntWritable(1);
	   	private Text text = new Text();

	   	/*
	   	 *  The map function receives record from the Hbase table with row key and row values as 
	   	 *  input parameters. It emits record key pair as (airlineId,Month)
         * and value as the delay for that particular month
	   	 */
	   	public void map(ImmutableBytesWritable row, Result value, Context context) throws IOException, InterruptedException {
	        	String delay = new String(value.getValue(Bytes.toBytes("ArrDelayMinutes"), Bytes.toBytes("val")));
	          	String[] rowValueSplits = new String(row.get()).split("#");
	          	String airId=rowValueSplits[0];
	          	String month=rowValueSplits[1];
	          	if(!(airId.isEmpty() || month.isEmpty() || delay.isEmpty()))
                 context.write(new FlightKeyPair(airId, Integer.parseInt(month)),
                		                       new DoubleWritable(Double.parseDouble(delay)));
	   	}
	}
	
	static class FlightReducer
    extends Reducer<FlightKeyPair, DoubleWritable, Text, NullWritable> {
  
		/*
		 * Each reduce function receives airlineId as partitioned with all its month. 
		 * The Iterable values received are the delays of particular month. 
		 * Thus using the above details, the average delay of each month is calculated
		 * and emitted out to the file.
		 */	
	    @Override
	    protected void reduce(FlightKeyPair key, Iterable<DoubleWritable> values,
	        Context context) throws IOException, InterruptedException {
	      
	      int currentMonth=0;
	      StringBuffer airLineRec;
	      double sumDelays=0;
	      int perMonthCount=0;
	      airLineRec=new StringBuffer(key.getAirId()+"\t");
	      for(DoubleWritable d:values){
	    	  int month=key.getMonth();
	    	  if(currentMonth!=month){
	    		  if(currentMonth!=0){
	    			  double avgDelay=Math.ceil(sumDelays/perMonthCount);
	    			  String perMonthTuple="("+currentMonth+","+avgDelay+")";
	    			  airLineRec.append(perMonthTuple+", ");
	    		  }
	    		  currentMonth=month;
	    		  sumDelays=0;
	    		  perMonthCount=0;
	    	  }
	    	  perMonthCount++;
	    	  sumDelays=sumDelays+d.get();
	      }
	      if(currentMonth!=0){
			  double avgDelay=Math.ceil(sumDelays/perMonthCount);
			  String perMonthTuple="("+currentMonth+","+avgDelay+")";
			  airLineRec.append(perMonthTuple);
		  }
	      context.write(new Text(airLineRec.toString()), NullWritable.get());
	    }
  }
  
  public static class FirstPartitioner
    extends Partitioner<FlightKeyPair, DoubleWritable> {
	/* 
	 * The custom partitioner partitions the intermediate output record from Map to 
	 * the Reducers using only AirlineId as key
	 */
    @Override
    public int getPartition(FlightKeyPair key, DoubleWritable value, int numPartitions) {
      // multiply by 127 to perform some mixing
    
      return Math.abs(key.getAirId().hashCode() * 127 ) % numPartitions;
    }
  }
  
  public static class KeyComparator extends WritableComparator {
    protected KeyComparator() {
      super(FlightKeyPair.class, true);
    }
    
    /*
     * The Key Comparator on KeyPair first sorts the airlineId and then sorts the month  
     * for every same airlineId.
     */
    @Override
    public int compare(WritableComparable w1, WritableComparable w2) {
      FlightKeyPair fO1 = (FlightKeyPair) w1;
      FlightKeyPair fO2 = (FlightKeyPair) w2;
      return fO1.compareTo(fO2);
    }
  }
  
  public static class GroupComparator extends WritableComparator {
    protected GroupComparator() {
      super(FlightKeyPair.class, true);
    }
    /*
     * The Grouping Comparator is applied only to the airlineId of the Key Pair so that 
     * every airlineId along with all its month is received by the same reduce function. 
     */
    @Override
    public int compare(WritableComparable w1, WritableComparable w2) {
      FlightKeyPair fO1 = (FlightKeyPair) w1;
      FlightKeyPair fO2 = (FlightKeyPair) w2;
      return FlightKeyPair.compare(fO1.getAirId(), fO2.getAirId());
    }
  }
	
	
	public static void main(String[] args) throws Exception {  
			long startTime=System.currentTimeMillis();
			long endTime;
			File outFolder=new File(args[0]);  
		    delfold(outFolder);
			Configuration conf = new Configuration();
			String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
			if (otherArgs.length < 1) {
			  System.err.println("Usage: <out>");
			  System.exit(2);
			}
			Job job = new Job(conf, "HBaseCompute Standalone");
		    job.setJarByClass(HbaseCompute.class);
		    job.setPartitionerClass(FirstPartitioner.class);
		    job.setSortComparatorClass(KeyComparator.class);
		    job.setGroupingComparatorClass(GroupComparator.class);
		    job.setReducerClass(FlightReducer.class);
		    job.setOutputKeyClass(FlightKeyPair.class);
		    job.setMapOutputValueClass(DoubleWritable.class);
		    job.setOutputValueClass(NullWritable.class);
		    job.setNumReduceTasks(10);
		    LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
		    FileOutputFormat.setOutputPath(job, new Path(otherArgs[0]));

		    
		    //splitting
		    HBaseAdmin admin = new HBaseAdmin(HBaseConfiguration.create());
		 
		    
			Scan scan = new Scan();
			scan.setCaching(500);        // 1 is the default in Scan, which will be bad for MapReduce jobs
			scan.setCacheBlocks(false);  // don't set to true for MR jobs
			// set other scan attrs
            SingleColumnValueFilter filter=new 
            		 SingleColumnValueFilter(Bytes.toBytes("Year"), 
            				                 Bytes.toBytes("val"), CompareFilter.CompareOp.EQUAL, Bytes.toBytes("2008"));
            scan.setFilter(filter);
            HTable ht=new HTable(conf, "flights");
            System.out.println( ht.getStartKeys().length);
            Pair<byte[][],byte[][]> pair=ht.getStartEndKeys();
            byte[][] start=pair.getFirst();
            byte[][] end=pair.getSecond();
            for(int c=0;c<start.length;c++)
            {
                    String st_end=new String(start[c]);    
                    System.out.println("StartKey :"+st_end);
            }
            for(int c=0;c<end.length;c++)
            {
                  
                    String en2=new String(end[c]);      
                    System.out.println("End Key :"+en2);
            }
			TableMapReduceUtil.initTableMapperJob(
				"flights",        // input table
				scan,               // Scan instance to control CF and attribute selection
				MyMapper.class,     // mapper class
				FlightKeyPair.class,         // mapper output key
				DoubleWritable.class,  // mapper output value
				job);

	        job.waitForCompletion(true);

	      endTime=System.currentTimeMillis();
	      System.out.println("Time taken(mins) at HbaseMapPopulate : "+(endTime-startTime)/(1000));

	  } 
	public static void delfold(File folder){
		File outFolder=folder;  
		if(outFolder.exists()){
			File[] files=outFolder.listFiles();

			for(File f:files){
				if(f.isDirectory())
					delfold(f);
				else
				f.delete();
			}
			outFolder.delete();
		}
	}    

}
