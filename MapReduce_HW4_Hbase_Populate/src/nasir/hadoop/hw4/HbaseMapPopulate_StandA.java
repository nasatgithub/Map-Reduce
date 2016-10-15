package nasir.hadoop.hw4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.google.common.collect.Sets;
import com.opencsv.CSVParser;



public class HbaseMapPopulate_StandA {
	public static int numReduceTasks=5;
	  public static enum counter{fCounter};
	  public static List<String> columnList=Arrays.asList("Year","Quarter","Month","DayofMonth","DayOfWeek","FlightDate","UniqueCarrier",
			                                        "AirlineID","Carrier","TailNum","FlightNum","Origin","OriginCityName","OriginState",
			                                        "OriginStateFips","OriginStateName","OriginWac","Dest","DestCityName","DestState","DestStateFips",
			                                        "DestStateName","DestWac","CRSDepTime","DepTime","DepDelay","DepDelayMinutes","DepDel15","DepartureDelayGroups",
			                                        "DepTimeBlk","TaxiOut","WheelsOff","WheelsOn","TaxiIn","CRSArrTime","ArrTime","ArrDelay","ArrDelayMinutes","ArrDel15",
			                                        "ArrivalDelayGroups","ArrTimeBlk","Cancelled","CancellationCode","Diverted","CRSElapsedTime","ActualElapsedTime",
			                                        "AirTime","Flights","Distance","DistanceGroup","CarrierDelay","WeatherDelay","NASDelay","SecurityDelay","LateAircraftDelay");
	  public static List<String> smallColumnList=Arrays.asList("Year","ArrDelayMinutes");
	  public static class MapperHBase 
	       extends Mapper<LongWritable, Text, ImmutableBytesWritable, Put>{
		    private HTable hTable;
		    private Configuration conf;
		    private List<Put> putList;
		    private final static IntWritable one = new IntWritable(1);
		    private Text rowValues = new Text();
		    
		    /*
		     * Creating Hbase table client instance once for every Map task in setup()
		     */
            @Override
            protected void setup(Context context)
            	throws IOException, InterruptedException {
            // TODO Auto-generated method stub
            Configuration conf=new Configuration();
            hTable=new HTable(conf, "flights");
            hTable.setAutoFlush(false);
            hTable.setWriteBufferSize(153600);
            putList=new ArrayList<Put>();
            }
		      
            /*  The key input received by the map function is used as a prime factor 
             *  to uniquely ditinguish records that are stored into the hbase table. 
             *  Along with this key, airlineId and month are also used as keys 
             *  so that Hbase sorting functionality can be applied which can be 
             *  later used during table scan and computations.
             */
		    public void map(LongWritable key, Text value, Context context
		                    ) throws IOException, InterruptedException {
		    	CSVParser parser=new CSVParser(',');
		         String[] c=parser.parseLine(value.toString());
		         if(key.get()!=0){
					  String month=c[2];
			   	      String airId=c[7];
			   	      String rowKey=airId+"#"+month+"#"+key;
			   	      Put p=new Put(Bytes.toBytes(rowKey));
			   	      for(int i=0;i<columnList.size();i++){
			   	    	 // if(smallColumnList.contains(columnList.get(i))){ // writing only 2 columns for testing purposes
					   	      String columnName = columnList.get(i);
					   	      String columnValue=c[i];
					   	      p.add(Bytes.toBytes(columnName), Bytes.toBytes("val"), Bytes.toBytes(columnValue));
			   	    	 // }
			   	      }
			   	      //System.out.println("Record : "+year+" "+month+" "+airId+" "+delay);
			   	      putList.add(p);
		         }
		  }
		    
		  @Override
		protected void cleanup(Context context)
			throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		hTable.put(putList);
		hTable.close();
		}  
	  }

	  public static void main(String[] args) throws Exception {  
		long startTime=System.currentTimeMillis();
		long endTime;
		File outFolder=new File(args[1]);  
	    delfold(outFolder);
		CreateHbaseTable.createHTable("flights");
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length < 2) {
		  System.err.println("Usage:  <in> <out>");
		  System.exit(2);
		}

		    conf.set(TableOutputFormat.OUTPUT_TABLE, "flights");
		    @SuppressWarnings("deprecation")
			Job job = new Job(conf, "HBasePopulate Standalone");
		    job.setJarByClass(HbaseMapPopulate_StandA.class);
		    job.setMapperClass(MapperHBase.class);
		    job.setOutputKeyClass(ImmutableBytesWritable.class);
		    job.setOutputValueClass(Put.class);
		    job.setInputFormatClass(TextInputFormat.class);
		    job.setOutputFormatClass(TableOutputFormat.class);
	 
		    
		    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		    FileOutputFormat.setOutputPath(job,new Path(otherArgs[1]));

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
