public class Job {
	private int jobId;
	private String studentId;
	private String title;
	private String applicationName;
	private String description;
	private String taskSize;
	private int estimatedDuration;
	private int submissionMinute;
	private String status;
	private int assignedGpuId;
	private int startMinute;
	private int finishMinute;
	private int cancellationMinute;
	private String cancelledBy;
	private String cancellationReason;
	private String resultSummary;
	
	public Job(int id, String owner, String title, String app, String description, String size, int submitted) {
		//Check that all job information is valid
		if (id <= 0 || owner == null || owner.trim().isEmpty() || title == null || title.trim().isEmpty() || app == null || app.trim().isEmpty() || description == null || description.trim().isEmpty() || size == null || submitted < 0) {
			// Throw an Exception to handle the invalid data
			throw new IllegalArgumentException("Invalid job information");
		}
		
		jobId = id;
		studentId = owner;
		this.title = title;
		applicationName = app;
		this.description = description;
		taskSize = size;
		submissionMinute = submitted;
		
		//Check the size of the submitted job and assign its right estimated duration
		if (size.equals("Small")) {
			estimatedDuration = 5;
		}
		else if (size.equals("Medium")) {
			estimatedDuration = 15;
		}
		else if (size.equals("Large")) {
			estimatedDuration = 30;
		}
		else if (size.equals("Very large")) {
			estimatedDuration = 60;
		}
		else {
			//Throw an exception if the task sizs is non of the above options which makes it invalid
			throw new IllegalArgumentException("Invalid task size");
		}
		
		status = "Queued";
		//Make all the attributes related to the GPU = -1 until it is assigned to one and the job starts processing
		assignedGpuId = -1;
		startMinute = -1;
		finishMinute = -1;
		
		//Initialize all the attributes related to the cancellation of the task to -1 and empty string until it gets cancelled if it does
		cancellationMinute = -1;
		cancelledBy = "";
		cancellationReason = "";
		resultSummary = "";
	}
	
	public void start(int gpuId, int now) {
		// This method is used to start a new task
		if (!status.equals("Queued")) {
			//Throws and exception if the task wasn't in the queue
			throw new IllegalStateException("Job is not queued");
		}
		if (gpuId < 1 || gpuId > 4 || now < submissionMinute) {
			//Throw an exception if the GPU ID is invalid (not between 1 and 4) or the start time of the task is before the submitted time
			throw new IllegalArgumentException("Invalid start information");
		}
		
		assignedGpuId = gpuId;
		startMinute = now;
		status = "Running";
	}
	
	public void complete(int now) {
		//This method is used to complete a running task
		if (!status.equals("Running")) {
			//Checks if a the task is already running or not, if not, it throws an exception
			throw new IllegalStateException("Job is not running");
		}
		
		if (now < (startMinute + estimatedDuration)) {
			//Throws an exception if the task is not completed yet (the current time is less than the estimated time after the starting time)
			throw new IllegalStateException("Job has not finished yet");
		}
		
		finishMinute = now;
		status = "Completed";
		resultSummary = "Job completed successfully";
	}
	
	public void cancel(int now, String actor, String reason) {
		//This method is used to cancel a task
		if (!status.equals("Queued") && !status.equals("Running")) {
			//Throws an exception if the task is not running or in the queue (Does not exist yet)
			throw new IllegalStateException("Job cannot be cancelled");
		}
		if (actor == null || actor.trim().isEmpty() || now < submissionMinute) {
			//Throws an exception if the person who cancelled the task is not valid or the current time is less than the submission time (Impossible in the normal flow of the program) 
			throw new IllegalArgumentException("Invalid cancellation information");
		}
		
		cancellationMinute = now;
		cancelledBy = actor;
		cancellationReason = reason == null ? "" : reason.trim(); //If there is no reason added, it is assigned empty
		
		if (status.equals("Running")) {
			finishMinute = now;
		}
		
		status = "Cancelled";
		resultSummary = (cancellationReason.isEmpty())? "Job cancelled" : ("Job cancelled: " + cancellationReason);
	}
	
	public int getExecutedMinutes(int now) {
		//Returns how many minutes are excuted
		if (startMinute == -1) {
			//The task haven't started yet
			return 0;
		}
		if (status.equals("Running")) {
			//If the task is still running
			return (now < startMinute)? 0: now - startMinute;
		}
		if (status.equals("Completed") || status.equals("Cancelled")) {
			//If the task is finished or cancelled
			return finishMinute - startMinute;
		}
		
		return 0;
	}
	
	public int getChargeMinutes() {
		//Returns the minutes the student is charged for based on the status of the task
		if (status.equals("Completed")) {
			//Returns the full duration of the task if it is completed
			return estimatedDuration;
		}
		if (status.equals("Cancelled") && startMinute != -1) {
			//Return the time spent on the task if it was cancelled while running
			return finishMinute - startMinute;
		}
		return 0;
	}
	
	public int getRemainingMinutes(int now) {
		//Returns the time left for the task to be completed
		if (status.equals("Queued")) {
			return estimatedDuration;
		}
		if (status.equals("Running")) {
			int remaining = estimatedDuration - (now - startMinute);
			return (remaining < 0) ? 0: remaining;
		}
		return 0;
	}
	
	public int getTotalQueueWait() {
		//Returns how much time the task waited in the queue
		if (status.equals("Queued")) {
			//The task is still in the queue
			return -1;
		}
		if (startMinute != -1) {
			return startMinute - submissionMinute;
		}
		if (status.equals("Cancelled")) {
			//If the task was cancelled while it is in the queue
			return cancellationMinute - submissionMinute;
		}
		
		return -1;
	}

	public int getJobId() {
		return jobId;
	}

	public String getStudentId() {
		return studentId;
	}

	public String getTitle() {
		return title;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getDescription() {
		return description;
	}

	public String getTaskSize() {
		return taskSize;
	}
	
	public int getWorkloadUnits() {
		return estimatedDuration;
	}

	public int getEstimatedDuration() {
		return estimatedDuration;
	}

	public int getSubmissionMinute() {
		return submissionMinute;
	}

	public String getStatus() {
		return status;
	}

	public int getAssignedGpuId() {
		return assignedGpuId;
	}

	public int getStartMinute() {
		return startMinute;
	}

	public int getFinishMinute() {
		return finishMinute;
	}

	public int getCancellationMinute() {
		return cancellationMinute;
	}

	public String getCancelledBy() {
		return cancelledBy;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public String getResultSummary() {
		return resultSummary;
	}
	
	public void restoreExecution(String state, int gpu, int start, int finish, int cancelled, String actor, String reason, String result) {
		//If the user closes the app, it has to be restored as is when the user opens the app again
		if (state == null || (!state.equals("Queued") && !state.equals("Running") && !state.equals("Completed") && !state.equals("Cancelled"))) {
			//Throws an exception if the task does not have a history and was never submitted
			throw new IllegalArgumentException("Invalid job status");
		}
		
		status = state;
		assignedGpuId = gpu;
		startMinute = start;
		finishMinute = finish;
		cancellationMinute = cancelled;
		cancelledBy = actor;
		cancellationReason = reason;
		resultSummary = result;
	}
}
