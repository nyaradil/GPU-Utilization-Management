import java.util.ArrayList;

public class Scheduler {

private ArrayList<Job> jobs;
private GPU[] gpus;
private SimulationClock clock;

public Scheduler(ArrayList<Job> jobs, GPU[] gpus, SimulationClock clock) {
	this.jobs = jobs;
	this.gpus = gpus;
	this.clock = clock;
}


void dispatchWaitingJobs() {
    int currentMinute = clock.getElapsedMinutes();
    
    for (int i = 0; i < gpus.length; i++) {
        GPU gpu = gpus[i];

        if (gpu.isAvailable()) {
            Job job = findFirstQueuedJob();

            if (job == null) {
                return; 
            }

            job.start(gpu.getGpuId(), currentMinute);
            gpu.assignJob(job);
        }
    }
}

void advanceMinute() {

    clock.advanceOneMinute();
    int currentMinute = clock.getElapsedMinutes();

    for (int i = 0; i < gpus.length; i++) {
        GPU gpu = gpus[i];

        if (!gpu.isAvailable()) {
            Job job = gpu.getCurrentJob();

            if (job.getRemainingMinutes(currentMinute) == 0) {
                job.complete(currentMinute);
                gpu.releaseJob();
            }
        }
    }
    
    dispatchWaitingJobs();
}

void cancelJob(Job job, String actor, String reason) {

    int currentMinute = clock.getElapsedMinutes();

    if (job.getStatus().equals("Queued")) {

        job.cancel(currentMinute, actor, reason);

    } else if (job.getStatus().equals("Running")) {

        GPU gpu = findGpuById(job.getAssignedGpuId());

        job.cancel(currentMinute, actor, reason);
        gpu.releaseJob();
      
        dispatchWaitingJobs();

    } else {
        throw new IllegalStateException(
            "Only queued or running jobs can be cancelled."
        );
    }
}

public int getQueuePosition(Job job) {
    int position = 0;

    for (int i = 0; i < jobs.size(); i++) {
        Job currentJob = jobs.get(i);

        if (currentJob.getStatus().equals("Queued")) {
            position++;

            if (currentJob == job) {
                return position;
            }
        }
    }

    return -1;
}



public int predictStartMinute(Job target) { 

    if (!target.getStatus().equals("Queued")) {
        return -1;
    }

    int currentMinute = clock.getElapsedMinutes();
    int[] availableAt = new int[gpus.length];


    for (int i = 0; i < gpus.length; i++) {
        if (gpus[i].isAvailable()) {
            availableAt[i] = currentMinute;
        } else {
            availableAt[i] = gpus[i].getExpectedFinishMinute();
        }
    }


    for (int i = 0; i < jobs.size(); i++) {
        Job job = jobs.get(i);

        if (job.getStatus().equals("Queued")) {


            int earliestIndex = 0;

            for (int g = 1; g < availableAt.length; g++) {
                if (availableAt[g] < availableAt[earliestIndex]) {
                    earliestIndex = g;
                }
            }

            int predictedStart = availableAt[earliestIndex];

            if (job == target) {
                return predictedStart;
            }

            availableAt[earliestIndex] =
                predictedStart + job.getEstimatedDuration();
        }
    }

    return -1; 
}


public int getPredictedFinishMinute(Job job) {
    String status = job.getStatus();

    if (status.equals("Queued")) {
        int predictedStart = predictStartMinute(job);

        if (predictedStart == -1) {
            return -1;
        }

        return predictedStart + job.getEstimatedDuration();

    } else if (status.equals("Running")) {
        return job.getStartMinute()
                + job.getEstimatedDuration();

    } else if (status.equals("Completed")) {
        return job.getFinishMinute();

    } else {
        return -1; 
    }
}

public int getRemainingWait(Job job) {

    if (!job.getStatus().equals("Queued")) {
        return -1;
    }

    int predictedStart = predictStartMinute(job);

    if (predictedStart == -1) {
        return -1;
    }

    return predictedStart - clock.getElapsedMinutes();
}


private Job findFirstQueuedJob() {
    for (int i = 0; i < jobs.size(); i++) {
        Job job = jobs.get(i);

        if (job.getStatus().equals("Queued")) {
            return job;
        }
    }

    return null;
}





private GPU findGpuById(int id) {
    for (int i = 0; i < gpus.length; i++) {
        if (gpus[i].getGpuId() == id) {
            return gpus[i];
        }
    }

    throw new IllegalArgumentException(
        "GPU not found: " + id
    );
}
	



}