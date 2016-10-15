package org.apache.hadoop.examples;

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
import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class Longest {
  
  public static class TokenizerMapper 
       extends Mapper<Object, Text, IntWritable, TreeMap<IntWritable,Text>>{
    
    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private TreeMap<Text,IntWritable> localTop=new TreeMap<Text,IntWritable>();
    private TreeMap<Text,IntWritable> final_sorted_localTop=new TreeMap<Text,IntWritable>();
    private Context ctx;
    public void setup(){
    	System.out.println("Setup method called");
    	
    }
    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      ctx=context;
      StringTokenizer itr = new StringTokenizer(value.toString());
      while (itr.hasMoreTokens()) {		
        word.set(itr.nextToken());
    
        localTop.put(word,new IntWritable(word.getLength()));
        if(localTop.size()>10)
        	localTop.remove(localTop);
      }
    }
    public void cleanup() throws IOException, InterruptedException{
       ctx.write(new IntWritable(1), localTop);
    }
  }
  
  public static class IntSumReducer 
       extends Reducer<IntWritable,TreeMap<IntWritable,Text>,NullWritable,Text> {
    
    private TreeMap<IntWritable,Text> globalMap;
    public void setup(){
    	globalMap=new TreeMap<IntWritable,Text>();
    }
    public void reduce(IntWritable key, Iterable<TreeMap<IntWritable,Text>> mapperTree, 
                       Context context
                       ) throws IOException, InterruptedException {
       for(IntWritable)
    }
  }

  public static void main(String[] args) throws Exception {  
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
    job.setJarByClass(WordCount.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    for (int i = 0; i < otherArgs.length - 1; ++i) {
      FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
    }
    FileOutputFormat.setOutputPath(job,
      new Path(otherArgs[otherArgs.length - 1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
class ValueComparatorMR implements Comparator<String>{
	Map<String,Integer> base;
	public ValueComparatorMR(Map<String,Integer> m){
		base=m;
	}
	public int compare(String w1,String w2){
		if(base.get(w1)>base.get(w2))
			return 1;
		else 
			return -1;
	}
}
