public class Job {
	private int jobId;
	private String studentId;
	private String title;
	private String applicationName;
	private String description;
	private String taskSize;
	private int estimateDuration;
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
		jobId = id;
		studentId = owner;
		this.title = title;
		applicationName = app;
		this.description = description;
		taskSize = size;
		submissionMinute = submitted;
	}
	
	public void start(int gpuId, int now) {
		
	}
	
	public void complete(int now) {
		
	}
	
	public void cancel(int now, String actor, String reason) {
		
	}
	
	public int getExcutedMinutes(int now) {
		return
	}
	
	public int getChargeMinutes() {
		return
	}
	
	public int getRemainingMinutes(int now) {
		return
	}
	
	public int getTotalQueueWait() {
		return
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

	public int getEstimateDuration() {
		return estimateDuration;
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
	
	public void restoreExcution(String state, int gpu, int start, int finish, int cancelled, String actor, String reason, String result) {
		
	}
}
