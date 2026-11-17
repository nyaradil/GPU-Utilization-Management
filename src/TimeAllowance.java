import java.util.ArrayList;

public class TimeAllowance {
	private ArrayList<AllowanceAdjustment> adjustments;
	
	public TimeAllowance() {
		adjustments = new ArrayList<AllowanceAdjustment>();
	}
	
	public void addAdjustment(AllowanceAdjustment record) {
		if (record == null) {
			throw new IllegalArgumentException("Adjustment cannot be null");
		}
		adjustments.add(record);
	}
	
	public int getAllocatedMinutes() {
		int total = 0;
		
		for (AllowanceAdjustment adjustment: adjustments) {
			total += adjustment.getMinutesDelta();
		}
		
		return total;
	}
	
	public int getUsedMinutes(ArrayList<Job>jobs, String owner) {
		if (jobs == null || owner == null) {
			throw new IllegalArgumentException("Invalid job information");
		}
		int total = 0;
		
		for (Job job : jobs) {
			if (owner.equals(job.getStudentId())) {
				if (job.getStatus().equals("Completed") || (job.getStatus().equals("Cancelled") && job.getStartMinute() != -1)) {
	                    total += job.getChargeMinutes();
	                }
			}
		}
		return total;
	}
	public int getReservedMinutes(ArrayList<Job> jobs, String owner) {

        if (jobs == null || owner == null) {
            throw new IllegalArgumentException("Invalid job information.");
        }

        int total = 0;

        for (Job job : jobs) {

            if (owner.equals(job.getStudentId())) {

                if (job.getStatus().equals("Queued") ||
                    job.getStatus().equals("Running")) {

                    total += job.getEstimatedDuration();
                }
            }
        }

        return total;
    }

    public int getAvailableMinutes(ArrayList<Job> jobs, String owner) {

        int allocated = getAllocatedMinutes();
        int used = getUsedMinutes(jobs, owner);
        int reserved = getReservedMinutes(jobs, owner);

        return allocated - used - reserved;
    }

    public boolean canSubmit(int duration, ArrayList<Job> jobs, String owner) {

        if (duration <= 0 || jobs == null || owner == null) {
            return false;
        }

        return getAvailableMinutes(jobs, owner) >= duration;
    }

    public ArrayList<AllowanceAdjustment> getAdjustments() {
        return new ArrayList<AllowanceAdjustment>(adjustments);
    }
}
