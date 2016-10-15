package nasir.hadoop.hw5;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.opencsv.CSVParser;

/* This is the Map-Only program which reads input data(data.csv) and partitions it into 
 * 2 files, one contains all the flights that has Origin as New York(JFK) and another 
 * has all the flights that has Destination as Chicago(ORD).
 */

public class MapOnlySplitData {
	public static class BinningMapper extends
	Mapper<Object, Text, Text, NullWritable> {
public String cma=", ";
private MultipleOutputs<Text, NullWritable> mos = null;
@SuppressWarnings({ "unchecked", "rawtypes" })
@Override
protected void setup(Context context) {
	// Create a new MultipleOutputs using the context object
	mos = new MultipleOutputs(context);
}

/*
 * (non-Javadoc)
 * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN, org.apache.hadoop.mapreduce.Mapper.Context)
 * 
 * The map function reads input lines from the input split, filters out flight records
 * that are cancelled or diverted. 
 * Further, all flights whose flight date range is between June 2007 and May 2008 alone 
 * are written into corresponding partition file with columns that are required for further
 * processing in the next job.    
 */
@Override
protected void map(Object key, Text value, Context context)
		throws IOException, InterruptedException {
    LongWritable keyVal=(LongWritable)key;   
	CSVParser parser=new CSVParser(',');
    String[] c=parser.parseLine(value.toString());
    String flightId,f1Dest,fDate,f1ArrivalTime,delay;
    String f2Origin,f2DeptTime;
    if(keyVal.get()!=0){  // ignoring csv header line
  //      System.out.println("Cancelled "+c[41]);
	    int cancelled=(int)Double.parseDouble(c[41]);
	    int diverted=(int)Double.parseDouble(c[43]);
	    if(cancelled==0 && diverted==0){
	    	if(betweenValidDate(c)){
	    		String origin=c[11].trim();
	    		String destination=c[17].trim();
	    		
	    		fDate=c[5];
	    		flightId=c[7]+c[10];
	    		f1Dest=c[17];
	    		f2Origin=c[11];
	    		f1ArrivalTime=c[35];
	    		f2DeptTime=c[24];
	    		delay=c[37];
	    		
			    if(origin.equals("ORD")){
			    	String s=flightId+cma+fDate+cma+f1Dest+cma+f1ArrivalTime+cma+delay;
			    	mos.write("flights", new Text(s), NullWritable.get(), "inF1/f1");
			    }
			    if(destination.equals("JFK")){
			    	String s=flightId+cma+fDate+cma+f2Origin+cma+f2DeptTime+cma+delay;
			    	mos.write("flights", new Text(s), NullWritable.get(), "inF2/f2");  
			    }
	    	}
	    }
    }
}

@Override
protected void cleanup(Context context) throws IOException,
		InterruptedException {
	// Close multiple outputs!
	mos.close();
}

/*
 * This method checks for valid flights with flight date range between June 2007 and May 2008.
 */
protected boolean betweenValidDate(String[] c){
	int year=Integer.parseInt(c[0]);
	int month=Integer.parseInt(c[2]);
	return (year == 2007 && month >= 6) || (year == 2008 && month <= 5);
}
}

public static void main(String[] args) throws Exception {
delfold(new File("out"));
Configuration conf = new Configuration();
String[] otherArgs = new GenericOptionsParser(conf, args)
		.getRemainingArgs();
if (otherArgs.length != 3) {
	System.err.println("Usage: MapOnlySplitData <indir> <outdir> <outFinaldir>");
	System.exit(1);
}

Job job = new Job(conf,"MapOnlySplitData");
job.setJarByClass(MapOnlySplitData.class);
job.setMapperClass(BinningMapper.class);
LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
job.setNumReduceTasks(0);


FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

FileOutputFormat.setOutputPath(job,
        new Path(otherArgs[1]));

// Configure the MultipleOutputs by adding an output called "flights"
// With the proper output format and mapper key/value pairs
MultipleOutputs.addNamedOutput(job, "flights", TextOutputFormat.class,
		Text.class, NullWritable.class);

// Enable the counters for the job
// If there is a significant number of different named outputs, this
// should be disabled
MultipleOutputs.setCountersEnabled(job, false);

job.waitForCompletion(true);

//Starting Next Job ReduceSideJoiner
ReduceSideJoiner.main(args);
}

/* The defold(File f) function deletes the folder f specified along with the files and 
 * folders inside it.
 */
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
