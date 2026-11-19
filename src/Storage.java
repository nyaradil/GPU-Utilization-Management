
import java.util.ArrayList;

// Stores the live objects shared by GpuSystem, Scheduler and FileManager.
public class Storage {

    private Administrator administrator;
    private ArrayList<Student> students;
    private ArrayList<AccessRequest> requests;
    private ArrayList<Job> jobs;
    private ArrayList<String[]> adjustments;
    private GPU[] gpus;
    private SimulationClock clock;

    // Default Constructor
    public Storage() {
        administrator = new Administrator();

        students = new ArrayList<Student>();
        requests = new ArrayList<AccessRequest>();
        jobs = new ArrayList<Job>();
        adjustments = new ArrayList<String[]>();

        // The project requires exactly four GPUs.
        gpus = new GPU[4];
        for (int i = 0; i < 4; i++) {
            gpus[i] = new GPU(i + 1);
        }

        clock = new SimulationClock();
    }

    // Package-access methods used by backend classes.
    Administrator getAdministrator() { return administrator; }
    ArrayList<Student> getStudents() { return students; }
    ArrayList<AccessRequest> getRequests() { return requests; }
    ArrayList<Job> getJobs() { return jobs; }
    ArrayList<String[]> getAdjustments() { return adjustments; }
    GPU[] getGpus() { return gpus; }
    SimulationClock getClock() { return clock; }

    void setClock(SimulationClock newClock) {
        clock = newClock;
    }

    // Find an approved student by university ID.
    Student findStudent(String id) {
        if (id == null) return null;

        for (Student student : students) {
            if (id.equals(student.getUsername())) {
                return student;
            }
        }
        return null;
    }

    // Find a registration request.
    AccessRequest findRequest(String id) {
        if (id == null) return null;

        for (AccessRequest request : requests) {
            if (id.equals(request.getUniversityId())) {
                return request;
            }
        }
        return null;
    }

    // Find a job by its generated ID.
    Job findJob(int id) {
        for (Job job : jobs) {
            if (job.getJobId() == id) {
                return job;
            }
        }
        return null;
    }

    // Generate the next unique job ID.
    int nextJobId() {
        int largest = 0;

        for (Job job : jobs) {
            if (job.getJobId() > largest) {
                largest = job.getJobId();
            }
        }

        if (largest == Integer.MAX_VALUE) return -1;
        return largest + 1;
    }

    // Keep jobs ordered by submission time, then job ID (FCFS).
    void addJob(Job newJob) {
        int position = 0;

        while (position < jobs.size()) {
            Job current = jobs.get(position);

            if (newJob.getSubmissionTime() < current.getSubmissionTime()
                    || (newJob.getSubmissionTime() == current.getSubmissionTime()
                    && newJob.getJobId() < current.getJobId())) {
                break;
            }
            position++;
        }

        jobs.add(position, newJob);
    }
}
