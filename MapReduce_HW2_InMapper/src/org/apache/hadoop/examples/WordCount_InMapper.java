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
package org.apache.hadoop.examples;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class WordCount_InMapper {
  public static int numReduceTasks=5;
  public static char[] validStarts={'m','n','o','p','q'};
  public static class TokenizerMapper 
       extends Mapper<Object, Text, Text, IntWritable>{
	    
	    private final static IntWritable one = new IntWritable(1);
	    private Text word;
	    public static HashMap<Text,IntWritable> hashWordC;
	    protected void setup(Context context){
	    	hashWordC=new HashMap<Text,IntWritable>();
	    }
	    
	    public void map(Object key, Text value, Context context
	                    ) throws IOException, InterruptedException {
	      StringTokenizer itr = new StringTokenizer(value.toString());
	      while (itr.hasMoreTokens()) {	
	    	    word=new Text();
		        word.set(itr.nextToken());
		        char beginLetter=(char)word.charAt(0);
		      if(isValidWord(beginLetter)){
		    	  if(hashWordC.containsKey(word)){
		    		//  int newCount=hashWordC.get(word).get()+1;
		             hashWordC.put(word,new IntWritable(hashWordC.get(word).get()+1));
		    	  }
		    	  else
		    		  hashWordC.put(word,one);  
		      }
	      }
	    }
	    protected void cleanup(Context context) throws IOException, InterruptedException{
	    	for(Text word:hashWordC.keySet())
	    		context.write(word, hashWordC.get(word));
	    }
	    
  }
  
  public static class IntSumReducer 
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values, 
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {  
	long startTime=System.currentTimeMillis();
	long endTime;
	File outFolder=new File("out");  
	//System.out.println(outFolder.exists());
	if(outFolder.exists()){
		String[] files=outFolder.list();
		for(String s:files){
			File f=new File(outFolder.getPath(),s);
			f.delete();
		}
		outFolder.delete();
	}
	
	  
	  
	Configuration conf = new Configuration();
	String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	if (otherArgs.length < 2) {
	  System.err.println("Usage: wordcount <in> [<in>...] <out>");
	  System.exit(2);
	}
   
	    Job job = new Job(conf, "word count");
	    job.setJarByClass(WordCount_InMapper.class);
	    job.setMapperClass(TokenizerMapper.class);
	    //job.setCombinerClass(IntSumReducer.class);
	    job.setReducerClass(IntSumReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    job.setPartitionerClass(MyCustomPartitioner.class);
	    job.setNumReduceTasks(numReduceTasks);


    for (int i = 0; i < otherArgs.length - 1; ++i) {
        FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
      }
      FileOutputFormat.setOutputPath(job,
        new Path(otherArgs[otherArgs.length - 1]));

      job.waitForCompletion(true);
      endTime=System.currentTimeMillis();
      System.out.println("Time taken(mins) at WordCount_InMapper : "+(endTime-startTime)/(1000));

  }
  /* This method takes in the beginLetter of a word, and returns true if that letter is 
   * present in the validStarts[] char array.
   */	
  public static boolean isValidWord(char beginLetter){
	  beginLetter=Character.toLowerCase(beginLetter);
	  for(char c:validStarts){
		  if(beginLetter==c)
			  return true;
	  }
	  return false;
  }
  
  
  /* MyCustomPartitioner receives the intermediate (key,value) pair given my the Mapper function
   * and sends each real word to it's corresponding reducer based on the start letter of that
   * of that real word.  
   * 
   * If number of Reduce Tasks set is 0, then nothing is done, and all the words are send to
   * just one reducer.
   * Otherwise the following steps are done
   *   1)The character array validStarts[] has m,n,o,p,q in it. The presence of begin letter of each 
   *      word received as key from map function is checked in validStarts. 
   *   2) If it is present, then that word is sent to the corresponding reducer, which is matched 
   *      with the index at which the character was found at validStarts
   */ 
  public static class MyCustomPartitioner extends Partitioner<Text, IntWritable>{
	 @Override
	public int getPartition(Text key, IntWritable value, int numReduceTasks) {
		// TODO Auto-generated method stub
		  char beginLetter=Character.toLowerCase((char)key.charAt(0));
		  if(numReduceTasks>0){
			  for(int i=0;i<validStarts.length;i++){
			  if(beginLetter==validStarts[i])
				  return i % numReduceTasks;
			  }
			  return 0;
		  }
		  else{
		   return 0;
		  }
	}
  }
}
