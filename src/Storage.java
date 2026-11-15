import java.util.ArrayList;

// Holds the live data shared by the system, scheduler and file manager.
public class Storage {
    private Administrator administrator;
    private ArrayList<Student> students;
    private ArrayList<AccessRequest> requests;
    private ArrayList<Job> jobs;
    private ArrayList<String[]> adjustments;
    private GPU[] gpus;
    private SimulationClock clock;

    public Storage() {
        administrator = new Administrator();
        students = new ArrayList<Student>();
        requests = new ArrayList<AccessRequest>();
        jobs = new ArrayList<Job>();
        adjustments = new ArrayList<String[]>();
        gpus = new GPU[4];
        for (int i = 0; i < 4; i++) {
            gpus[i] = new GPU(i + 1);
        }
        clock = new SimulationClock();
    }

    // Package access: only backend classes should use these live objects.
    Administrator getAdministrator() { return administrator; }
    ArrayList<Student> getStudents() { return students; }
    ArrayList<AccessRequest> getRequests() { return requests; }
    ArrayList<Job> getJobs() { return jobs; }
    ArrayList<String[]> getAdjustments() { return adjustments; }
    GPU[] getGpus() { return gpus; }
    SimulationClock getClock() { return clock; }
    void setClock(SimulationClock value) { clock = value; }

    Student findStudent(String id) {
        if (id == null) return null;
        for (Student student : students) {
            if (id.equals(student.getUsername())) return student;
        }
        return null;
    }

    AccessRequest findRequest(String id) {
        if (id == null) return null;
        for (AccessRequest request : requests) {
            if (id.equals(request.getUniversityId())) return request;
        }
        return null;
    }

    Job findJob(int id) {
        for (Job job : jobs) {
            if (job.getJobId() == id) return job;
        }
        return null;
    }

    int nextJobId() {
        int maximum = 0;
        for (Job job : jobs) {
            if (job.getJobId() > maximum) maximum = job.getJobId();
        }
        if (maximum == Integer.MAX_VALUE) return -1;
        return maximum + 1;
    }

    // Keep the one queue in FCFS order, breaking submission-time ties by job ID.
    void addJob(Job newJob) {
        int index = 0;
        while (index < jobs.size()) {
            Job current = jobs.get(index);
            if (newJob.getSubmissionTime() < current.getSubmissionTime()
                    || (newJob.getSubmissionTime() == current.getSubmissionTime()
                    && newJob.getJobId() < current.getJobId())) break;
            index++;
        }
        jobs.add(index, newJob);
    }
}
