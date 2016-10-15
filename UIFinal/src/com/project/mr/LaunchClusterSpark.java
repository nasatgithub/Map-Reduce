package com.project.mr;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.Application;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.elasticmapreduce.util.StepFactory;

public class LaunchClusterSpark {

	public static void main(String[] args) {
		AWSCredentials credentials = new BasicAWSCredentials("AKIAIS6HZ576JP2DQVRA", 
										"KxOkDMA1V+CFfnqCnVisQuHekyEQ2EM7SvqWlQiM");
		AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(credentials);
		
		
		Application sparkApp = new Application()
				.withName("Spark");
				

		
		String[] params = {"spark-submit","--deploy-mode","cluster","--class","simple.ReadSparkSql",
							"s3://mr-project/SparkSql.jar","s3://mr-project/input1/",
							"s3://mr-project/input2/","s3://mr-project/SparkSql.jar",
							"s3://mr-project/output-spark1"};


		StepFactory stepFactory = new StepFactory();
		
		StepConfig enabledebugging = new StepConfig()
				.withName("Enable debugging")
				.withActionOnFailure("TERMINATE_JOB_FLOW")
				.withHadoopJarStep(stepFactory.newEnableDebuggingStep());
		
		HadoopJarStepConfig sparkStepConf = new HadoopJarStepConfig()
				.withJar("command-runner.jar")
				.withArgs(params);	
		
		StepConfig sparkStep = new StepConfig()
				.withName("Spark Step")
				.withActionOnFailure("CONTINUE")
				.withHadoopJarStep(sparkStepConf);


		RunJobFlowRequest request = new RunJobFlowRequest()
				.withName("Spark Cluster")
				.withApplications(sparkApp)
				.withReleaseLabel("emr-4.2.0")
				.withServiceRole("EMR_DefaultRole")
				.withJobFlowRole("EMR_EC2_DefaultRole")
				.withLogUri("s3://log-project/")
				.withSteps(enabledebugging,sparkStep)
				.withInstances(new JobFlowInstancesConfig()
						.withInstanceCount(6)
						.withKeepJobFlowAliveWhenNoSteps(true)
						.withMasterInstanceType("m1.large")
						.withSlaveInstanceType("m1.large")
						);

		RunJobFlowResult result = emr.runJobFlow(request);
		System.out.println(result.toString());


	}

}
