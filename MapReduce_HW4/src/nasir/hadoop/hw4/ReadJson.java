package nasir.hadoop.hw4;
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.opencsv.CSVParser;

public class ReadJson {
  public static int numReduceTasks=5;
  public static enum counter{fCounter};
  public static class TokenizerMapper 
       extends Mapper<LongWritable, Text, Text, IntWritable>{
	    
	    private final static IntWritable one = new IntWritable(1);
	    private Text word = new Text();
	      
	    public void map(LongWritable key, Text value, Context context
	                    ) throws IOException, InterruptedException {
	    	 CSVParser parser=new CSVParser(',');
	         String[] c=parser.parseLine(value.toString());
	         if(key.get()!=0){
		       	if(!(c[0].isEmpty()||c[2].isEmpty()||c[7].isEmpty()||c[37].isEmpty())){
		   	      int year=Integer.parseInt(c[0]);
		   	      int month=Integer.parseInt(c[2]);
		   	      String airId=c[7];
		   	      double delay=Double.parseDouble(c[37]);
		   	      //System.out.println("Record : "+year+" "+month+" "+airId+" "+delay);
		   	      context.write(new Text(key+"#"+month+"#"+delay),new IntWritable(1));
		       	}
	         }
	  }
  }
  
  public static class IntSumReducer 
       extends Reducer<Text,IntWritable,Text,NullWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      int count = 0;
      for (IntWritable val : values) {
        count++;
        if(count>1){
        	context.write(key, NullWritable.get());
        	break;
        }
      }
      
    }
  }

  public static void main(String[] args) throws Exception {  
	long startTime=System.currentTimeMillis();
	long endTime;
	File outFolder=new File("outDupKey");  
    PerMonthAverageDelay.delfold(outFolder);
	 
	Configuration conf = new Configuration();
	String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	if (otherArgs.length < 2) {
	  System.err.println("Usage: wordcount <in> [<in>...] <out>");
	  System.exit(2);
	}

	 Job job = new Job(conf, "word count");
	    job.setJarByClass(ReadJson.class);
	    job.setMapperClass(TokenizerMapper.class);
	    //job.setCombinerClass(IntSumReducer.class);
	    job.setReducerClass(IntSumReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(IntWritable.class);
	    job.setOutputValueClass(NullWritable.class);
//	    /job.setPartitionerClass(MyCustomPartitioner.class);
	    job.setNumReduceTasks(numReduceTasks);


    for (int i = 0; i < otherArgs.length - 1; ++i) {
        FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
      }
      FileOutputFormat.setOutputPath(job,
        new Path(otherArgs[otherArgs.length - 1]));

      job.waitForCompletion(true);
      endTime=System.currentTimeMillis();
      System.out.println("Time taken(mins) at WordCount_localAgg : "+(endTime-startTime)/(1000));

  } 
}
