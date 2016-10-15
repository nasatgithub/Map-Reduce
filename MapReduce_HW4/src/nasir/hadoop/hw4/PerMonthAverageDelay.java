package nasir.hadoop.hw4;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.SecondarySort.IntPair;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;


import com.opencsv.CSVParser;


// Program to Compute Per AirlineId Average Delays for each month
public class PerMonthAverageDelay {
  
  static class FlightMapper
    extends Mapper<LongWritable, Text, FlightKeyPair, DoubleWritable> {
  
    /*
     * The input record to the map is parsed and is emitted using key pair as (airlineId,Month)
     * and value as the delay for that particular month
     * 
     */
    @Override
    protected void map(LongWritable key, Text value,
        Context context) throws IOException, InterruptedException {
      CSVParser parser=new CSVParser(',');
      String[] c=parser.parseLine(value.toString());
      if(key.get()!=0){
    	if(!(c[0].isEmpty()||c[2].isEmpty()||c[7].isEmpty()||c[37].isEmpty())){
	      int year=Integer.parseInt(c[0]);
	      int month=Integer.parseInt(c[2]);
	      String airId=c[7];
	      double delay=Double.parseDouble(c[37]);
	      //System.out.println("Record : "+year+" "+month+" "+airId+" "+delay);
	      if(year==2008)
	    	  context.write(new FlightKeyPair(airId, month),new DoubleWritable(delay));
    	}
      }
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
  
  /* The custom partitioner partitions the intermediate output record from Map to 
   * the Reducers using only AirlineId as key
   */
  public static class FirstPartitioner
    extends Partitioner<FlightKeyPair, DoubleWritable> {
    @Override
    public int getPartition(FlightKeyPair key, DoubleWritable value, int numPartitions) {
      // multiply by 127 to perform some mixing
    
      return Math.abs(key.getAirId().hashCode() * 127 ) % numPartitions;
    }
  }
  
  /*
   * The Key Comparator on KeyPair first sorts the airlineId and then sorts the month  
   * for every same airlineId.
   */
  public static class KeyComparator extends WritableComparator {
    protected KeyComparator() {
      super(FlightKeyPair.class, true);
    }
    @Override
    public int compare(WritableComparable w1, WritableComparable w2) {
      FlightKeyPair fO1 = (FlightKeyPair) w1;
      FlightKeyPair fO2 = (FlightKeyPair) w2;
      return fO1.compareTo(fO2);
    }
  }
  
  /*
   * The Grouping Comparator is applied only to the airlineId of the Key Pair so that 
   * every airlineId along with all its month is received by the same reduce function. 
   */
  public static class GroupComparator extends WritableComparator {
    protected GroupComparator() {
      super(FlightKeyPair.class, true);
    }
    @Override
    public int compare(WritableComparable w1, WritableComparable w2) {
      FlightKeyPair fO1 = (FlightKeyPair) w1;
      FlightKeyPair fO2 = (FlightKeyPair) w2;
      return FlightKeyPair.compare(fO1.getAirId(), fO2.getAirId());
    }
  }
  
  public static void main(String[] args) throws Exception {
	  delfold(new File("out"));
	  Configuration conf = new Configuration();
	  String[] otherArgs = new GenericOptionsParser(conf, args)
	  		.getRemainingArgs();
	  if (otherArgs.length != 2) {
	  	System.err.println("Usage: MapOnlySplitData <indir> <outdir>");
	  	System.exit(1);
	  }

	  Job job = new Job(conf,"PerMonthAverageDelay");
	  	job.setJarByClass(PerMonthAverageDelay.class);
	    job.setMapperClass(FlightMapper.class);
	    job.setPartitionerClass(FirstPartitioner.class);
	    job.setSortComparatorClass(KeyComparator.class);
	    job.setGroupingComparatorClass(GroupComparator.class);
	    job.setReducerClass(FlightReducer.class);
	    job.setOutputKeyClass(FlightKeyPair.class);
	    job.setMapOutputValueClass(DoubleWritable.class);
	    job.setOutputValueClass(NullWritable.class);
	    job.setNumReduceTasks(10);
	    LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
	    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
	    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
	    
	    job.waitForCompletion(true);
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
// ^^ MaxTemperatureUsingSecondarySort