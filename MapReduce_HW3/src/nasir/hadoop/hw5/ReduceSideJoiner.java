package nasir.hadoop.hw5;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.opencsv.CSVParser;

/*
 * This programs uses 2 Mappers and One Reducer. The purpose of 2 Mappers is to read 2 different 
 * input flight files(referred as F1 and F2 respectively) that was created in MapOnlySplitData job. 
 * The join is performed on the Reducer Side between these 2 files on (FlightDate,Destination/Origin) 
 * where Destination is considered for files that has F1 flight records and Origin is considered for 
 * files that has F2 flight records. In other words, for a two-legged flight the destination of the first flight is same as the origin of the second flight.
 * 
 * The Reducer further applies filtering(F1.arrivalTime<F2.departureTime) on Flights F1 and F2 record pair
 * that were joined. The reducer also computes average delay time for all the remaining F1,F2 flight record pairs. 
 */ 
public class ReduceSideJoiner {
	public static enum counter{fCounter,TotalDelay}; // used for global counters in the Reducer
	public static class F1JoinMapper extends Mapper<Object, Text, Text, Text> {

		private Text outkey = new Text();
		private Text outvalue = new Text();
        private String fDate,dest_origin,arr_dept,delay;
        
        /*
         * (non-Javadoc)
         * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN, org.apache.hadoop.mapreduce.Mapper.Context)
         * 
         * This map function receives records from Flight F1 files, and passes (FlightDate,Destination) as the key
         * and the remaining required fields as value, as intermediate results to the Reduce Functions. 
         */
		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			// Parse the input string into a nice map
			CSVParser parser=new CSVParser(',');
			String[] c=parser.parseLine(value.toString());
			
			fDate=c[1].trim();
			dest_origin=c[2].trim();
			arr_dept=c[3].trim();
			delay=c[4].trim();
			outkey=new Text(fDate+" "+dest_origin);
			outvalue=new Text("1 "+arr_dept+" "+delay);
			context.write(outkey, outvalue);
		}
	}

	public static class F2JoinMapper extends
			Mapper<Object, Text, Text, Text> {

		private Text outkey = new Text();
		private Text outvalue = new Text();
		 private String fDate,dest_origin,arr_dept,delay;
		/*
		 * (non-Javadoc)
		 * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN, org.apache.hadoop.mapreduce.Mapper.Context)
		 * 
		 * This map function receives records from Flight F2 files, and passes (FlightDate,Origin) as the key
         * and the remaining required fields as value, as intermediate results to the Reduce Functions. 
		 */
		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			CSVParser parser=new CSVParser(',');
			String[] c=parser.parseLine(value.toString());
			fDate=c[1].trim();
			dest_origin=c[2].trim();
			arr_dept=c[3].trim();
			delay=c[4].trim();
			outkey=new Text(fDate+" "+dest_origin);
			outvalue=new Text("2 "+arr_dept+" "+delay);
			context.write(outkey, outvalue);
		}
	}

	public static class JoinReducer extends Reducer<Text, Text, Text, Text> {

		private ArrayList<Text> list1 = new ArrayList<Text>();
		private ArrayList<Text> list2 = new ArrayList<Text>();
        //int perReducerCounts,perReducerDelays;
		@Override
		public void setup(Context context) {
			// Get the type of join from our configuration
			//perReducerCounts=perReducerDelays=0;
		}
        /*
         * (non-Javadoc)
         * @see org.apache.hadoop.mapreduce.Reducer#reduce(KEYIN, java.lang.Iterable, org.apache.hadoop.mapreduce.Reducer.Context)
         * 
         * This reduce function receives the joined attributes as the key, and the remaining attributes as values where each 
         * value can iterated over and appropriate attribute can be obtained from it. 
         */
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			// Clear the lists
			list1.clear();
			list2.clear();

			// iterate through all our values, binning each record based on what
			// it was tagged with.
			for (Text t : values) {
				if (t.charAt(0) == '1') {
					list1.add(new Text(t.toString().substring(2)));
				} else if (t.charAt(0) == '2') {
					list2.add(new Text(t.toString().substring(2)));
				}
			}

			// The join logic is called to read and join each lists created above.
			executeJoinLogic(context);
		}

		/*
		 * This method joins both the lists that has same FlightDate and Destination/Origin 
		 * for all flights F1 and F2 that satisfies the condtion (F1.arrivalTime<F2.departureTime).
		 * Then, the delays of both flights are added and is incremented to the global counter  
		 * 'counter.TotalDelay', so that the aggregated value from all Reducer Tasks can be used 
		 * to compute Average Delay time at the end of the job.
		 */
		private void executeJoinLogic(Context context) throws IOException,
				InterruptedException {
				// If both lists are not empty, join A with B
				if (!list1.isEmpty() && !list2.isEmpty()) {
					for (Text F1 : list1) {
						String[] c1=F1.toString().split(" ");
						int f1Arrival=Integer.parseInt(c1[0]);
						Integer f1delay=(int)Double.parseDouble(c1[1]);
						for (Text F2 : list2) {
							String[] c2=F2.toString().split(" ");
							int f2Dept=Integer.parseInt(c2[0]);
							Integer f2delay=(int)Double.parseDouble(c2[1]);
							if(f1Arrival<f2Dept){
						    context.getCounter(counter.fCounter).increment(1);
						    context.getCounter(counter.TotalDelay).increment(f1delay+f2delay);
							context.write(F1, F2);
							}
						}
					}
				}
		}
	}

	public static void main(String[] args) throws Exception {
		delfold(new File("outFinal"));  
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 3) {
			System.err
					.println("Usage: ReduceSideJoin <in> <out> <outFinal>");
			System.exit(1);
		}


		Job job = new Job(conf, "Reduce Side Join");
        
		// Configure the join type
		job.setJarByClass(ReduceSideJoiner.class);

		// Multiple inputs to set which input uses what mapper	
		MultipleInputs.addInputPath(job, new Path(otherArgs[1]+"/inF1"),
				TextInputFormat.class, F1JoinMapper.class);

		MultipleInputs.addInputPath(job, new Path(otherArgs[1]+"/inF2"),
				TextInputFormat.class, F2JoinMapper.class);
        
		job.setReducerClass(JoinReducer.class);
		LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
        job.setNumReduceTasks(10);
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[2]));

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
        
		job.waitForCompletion(true);
		
		Counter totalFlights=job.getCounters().findCounter(counter.fCounter);
        Counter totalFLightDelays=job.getCounters().findCounter(counter.TotalDelay);
        System.out.println(totalFLightDelays.getValue());
        double averageDelay=totalFLightDelays.getValue()/(double)totalFlights.getValue();
        System.out.println("Total Flights = "+totalFlights.getValue());
        System.out.println("Average Delay = "+averageDelay);
        
        System.exit(0);
	}
	/* The defold(File f) function deletes the folder f specified along with the files and 
	 * folders inside it.
	 */
	public static void delfold(File folder){
		File outFolder=folder;  
		//System.out.println(outFolder.exists());
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
