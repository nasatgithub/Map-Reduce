package nasir.hadoop.hw4;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
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
import org.apache.tools.ant.DirectoryScanner;

import com.opencsv.CSVParser;



public class HbaseMapPopulate_Distb {
	public static int numReduceTasks=5;
	  public static enum counter{fCounter};
	  public static class MapperHBase 
	       extends Mapper<LongWritable, Text, ImmutableBytesWritable, KeyValue>{
		    
		    private final static IntWritable one = new IntWritable(1);
		    private Text rowValues = new Text();
		    private KeyValue keyV;
		    ImmutableBytesWritable hKey = new ImmutableBytesWritable();
		      
		    public void map(LongWritable key, Text value, Context context
		                    ) throws IOException, InterruptedException {
		    	CSVParser parser=new CSVParser(',');
		         String[] c=parser.parseLine(value.toString());
		         System.out.println("Entering Map");
		         if(key.get()!=0){
			       	if(!(c[0].isEmpty()||c[2].isEmpty()||c[7].isEmpty()||c[37].isEmpty())){
			   	      int year=Integer.parseInt(c[0]);
			   	      int month=Integer.parseInt(c[2]);
			   	      String airId=c[7];
			   	      double delay=Double.parseDouble(c[37]);
			   	      hKey.set(Bytes.toBytes(airId+"#"+month+"#"+key.hashCode()));
			   	      //System.out.println("Record : "+year+" "+month+" "+airId+" "+delay);
			   	      keyV=new KeyValue(hKey.get(), 
			   	    		   Bytes.toBytes("delay"),Bytes.toBytes("val"), Bytes.toBytes(String.valueOf(delay)));
			   	      System.out.println("Writing"+hKey.toString()+" "+keyV.toString());
				      context.write(hKey,keyV);
			       	}
		         }
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

		    conf.set("hbase.table.name", "flights");
		    // Load hbase-site.xml 
		    HBaseConfiguration.addHbaseResources(conf);
		    @SuppressWarnings("deprecation")
			Job job = new Job(conf, "word count");
		    job.setJarByClass(HbaseMapPopulate_Distb.class);
		    job.setMapperClass(MapperHBase.class);
		    job.setOutputKeyClass(ImmutableBytesWritable.class);
		    //job.setMapOutputValueClass(IntWritable.class);
		    job.setOutputValueClass(KeyValue.class);
//		    /job.setPartitionerClass(MyCustomPartitioner.class);
		    job.setInputFormatClass(TextInputFormat.class);
		    job.setOutputFormatClass(HFileOutputFormat.class);
	 
		    //TotalOrderPartitioner.setPartitionFile(conf, new Path("/home/nasir/partition/partition.lst"));
		    // Auto configure partitioner and reducer
		    HTable table=new HTable(conf, "flights");
		    HFileOutputFormat.configureIncrementalLoad(job, table);
		    
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
