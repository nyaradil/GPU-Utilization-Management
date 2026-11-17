public class GPU {
	private int gpuId;
	private Job currentJob;
	
	public GPU(int id) {
		if (id < 1 || id > 4) {
			throw new IllegalArgumentException("Invalid GPU ID");
		}
		gpuId = id;
		currentJob = null;
	}
	
	public int getGpuId() {
		return gpuId;
	}
	public boolean isAvailable() {
		return currentJob == null;
	}
	public int getCurrentJobId() {
		if (currentJob == null) {
			return -1;
		}
		return currentJob.getJobId();
	}
	public Job getCurrentJob() {
		return currentJob;
	}
	
	public void assignJob(Job job) {
		//Assign a job to a certain GPU
		if (job == null) {
			throw new IllegalArgumentException("Job cannot be null");
		}
		
		if (!isAvailable()) {
			throw new IllegalStateException("GPU is already occupied");
		}
		if(!job.getStatus().equals("Running") || job.getAssignedGpuId() != gpuId) {
			throw new IllegalArgumentException("Invalid job assignment");
		}
		
		currentJob = job;
	}
	
	public void releaseJob() {
		//This methid empties the GPU after the job is completed or cancelled
		if(isAvailable()) {
			throw new IllegalStateException("GPU is already available");
		}
		
		currentJob = null;
	}
	
	public int getRemainingMinutes(int now) {
		if (isAvailable()) {
			return 0;
		}
		return currentJob.getRemainingMinutes(now);
	}
	
	public int getExpectedFinishMinute() {
		if (isAvailable()) {
			return -1;
		}
		
		return currentJob.getStartMinute() + currentJob.getEstimatedDuration();
	}
}
