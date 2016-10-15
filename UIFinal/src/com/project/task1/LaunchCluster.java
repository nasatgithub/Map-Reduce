package com.project.task1;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
//import com.amazonaws.services.elasticmapreduce.model.Application;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.elasticmapreduce.util.StepFactory;

public class LaunchCluster {

	public static void main(String[] args) {
		AWSCredentials credentials = new BasicAWSCredentials("AKIAIS6HZ576JP2DQVRA", "KxOkDMA1V+CFfnqCnVisQuHekyEQ2EM7SvqWlQiM");
		AmazonElasticMapReduceClient emr = new AmazonElasticMapReduceClient(credentials);

		StepFactory stepFactory = new StepFactory();
		
//		Application spark = new Application()
//				.withName("Spark")
//				.withVersion("1.5.2");
		
		String[] params = {"s3://mr-project/Dataset","s3://mr-project/Output-task1"}; 
		
//		List<StepConfig> stepConfigs = new ArrayList<StepConfig>();
		
		HadoopJarStepConfig reviews = new HadoopJarStepConfig()
				.withJar("s3://mr-project/task1.jar")
				.withArgs(params);

		StepConfig enabledebugging = new StepConfig()
				.withName("Enable debugging")
				.withActionOnFailure("TERMINATE_JOB_FLOW")
				.withHadoopJarStep(stepFactory.newEnableDebuggingStep());
		
		StepConfig customJar = new StepConfig()
				.withName("Top 5 Helpful reviewers")
				.withActionOnFailure("CONTINUE")
				.withHadoopJarStep(reviews);

		RunJobFlowRequest request = new RunJobFlowRequest()
				.withReleaseLabel("emr-4.0.0")
//				.withApplications(spark)
				.withName("Task 1")
				.withSteps(enabledebugging,customJar)
				.withLogUri("s3://log-project/Task1/")
				.withServiceRole("EMR_DefaultRole")
				.withJobFlowRole("EMR_EC2_DefaultRole")
				.withInstances(new JobFlowInstancesConfig()
//				.withEc2KeyName("keypair")
				.withInstanceCount(11)
				.withTerminationProtected(true)
				.withKeepJobFlowAliveWhenNoSteps(false)
				.withMasterInstanceType("m1.medium")
				.withSlaveInstanceType("m1.medium"));

		RunJobFlowResult result = emr.runJobFlow(request);
		System.out.println(result.toString());

	}

}
