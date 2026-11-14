import java.awt.JobAttributes;
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

//review//

public int predictStartMinute(Job target) { 

    if (!target.getStatus().equals("Queued")) {
        return -1;
    }

    int currentMinute = clock.getElapsedMinutes();
    int[] availableAt = new int[gpus.length];

    // Copy when each GPU will become available.
    for (int i = 0; i < gpus.length; i++) {
        if (gpus[i].isAvailable()) {
            availableAt[i] = currentMinute;
        } else {
            availableAt[i] = gpus[i].getExpectedFinishMinute();
        }
    }

    // Process queued jobs in FCFS order.
    for (int i = 0; i < jobs.size(); i++) {
        Job job = jobs.get(i);

        if (job.getStatus().equals("Queued")) {

            // Find the GPU with the earliest availability.
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

            // Reserve time only in the temporary schedule.
            availableAt[earliestIndex] =
                predictedStart + job.getEstimatedDuration();
        }
    }

    return -1; // Target was not found in the queue.
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
	


}
