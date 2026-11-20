
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

// Central controller: GUI classes call this, not Job or GPU directly.
public class GpuSystem {
    private ArrayList<User> users;
    private ArrayList<AccessRequest> requests;
    private ArrayList<Job> jobs;
    private GPU[] gpus;
    private SimulationClock clock;
    private Scheduler scheduler;
    private Storage storage;
    private User currentUser;
    private int nextJobId;

    // Full Constructor: all screens must share this same GpuSystem instance.
    public GpuSystem(Storage storage) {
        if (storage == null) throw new IllegalArgumentException("Storage is required.");
        this.storage = storage;
        users = new ArrayList<User>();
        users.add(new Administrator());
        requests = new ArrayList<AccessRequest>();
        jobs = new ArrayList<Job>();
        gpus = new GPU[4];
        for (int i = 0; i < 4; i++) gpus[i] = new GPU(i + 1);
        clock = new SimulationClock();
        scheduler = new Scheduler(jobs, gpus, clock);
        currentUser = null;
        nextJobId = 1;
    }

    public void load() throws IOException { storage.loadInto(this); }
    public void save() throws IOException { storage.save(this); }

    private boolean filled(String text) {
        if (text == null) return false;
        for (int i = 0; i < text.length(); i++) if (text.charAt(i) > ' ') return true;
        return false;
    }

    private User findUser(String username) {
        if (username == null) return null;
        for (User user : users) if (username.equals(user.getUsername())) return user;
        return null;
    }

    private Job findJob(int id) {
        for (Job job : jobs) if (job.getJobId() == id) return job;
        return null;
    }

    private Student requireStudent() {
        if (!(currentUser instanceof Student))
            throw new IllegalArgumentException("Student login required.");
        return (Student) currentUser;
    }

    private void requireAdministrator() {
        if (!(currentUser instanceof Administrator))
            throw new IllegalArgumentException("Administrator login required.");
    }

    // Register a pending request; only approved students become users.
    public void registerStudent(String id, String name, String email,
                                String phone, String password) throws IOException {
        if (!filled(id) || !filled(name) || !filled(email) ||
                !filled(phone) || !filled(password))
            throw new IllegalArgumentException("All registration fields are required.");
        for (int i = 0; i < id.length(); i++)
            if (id.charAt(i) < '0' || id.charAt(i) > '9')
                throw new IllegalArgumentException("University ID must contain digits.");
        int at = email.indexOf('@');
        if (at < 1 || email.indexOf('.', at + 1) <= at + 1 ||
                email.substring(at + 1).indexOf('@') >= 0)
            throw new IllegalArgumentException("Invalid email.");
        int phoneDigits = 0;
        for (int i = 0; i < phone.length(); i++) {
            char c = phone.charAt(i);
            if (c >= '0' && c <= '9') phoneDigits++;
            else if (c != '+' && c != '-' && c != ' ' && c != '(' && c != ')')
                throw new IllegalArgumentException("Invalid phone number.");
        }
        if (phoneDigits == 0) throw new IllegalArgumentException("Phone needs digits.");
        for (AccessRequest request : requests)
            if (id.equals(request.getUniversityId()))
                throw new IllegalArgumentException("University ID already requested.");
        if (findUser(id) != null)
            throw new IllegalArgumentException("Account already exists.");
        requests.add(new AccessRequest(id, name, email, phone, password));
        save();
    }

    public User login(String username, String password) {
        if (currentUser != null) throw new IllegalArgumentException("Log out first.");
        User user = findUser(username);
        if (user == null || password == null || !user.passwordMatches(password))
            throw new IllegalArgumentException("Invalid credentials or account not approved.");
        currentUser = user;
        return user;
    }

    public void logout() { currentUser = null; }
    public User getCurrentUser() { return currentUser; }

    private AccessRequest pendingRequest(String id) {
        for (AccessRequest r : requests)
            if (r.getUniversityId().equals(id) && r.isPending()) return r;
        throw new IllegalArgumentException("Pending request not found.");
    }

    // Student constructor order follows the teammate version supplied earlier.
    public void approveRequest(String id, int initialMinutes) throws IOException {
        requireAdministrator();
        if (initialMinutes < 0) throw new IllegalArgumentException("Negative allocation.");
        AccessRequest request = pendingRequest(id);
        if (findUser(id) != null) throw new IllegalArgumentException("Already approved.");
        Student student = new Student(id, request.getName(),
                request.getChosenPassword(), request.getPhone(), request.getEmail());
        student.getAllowance().addAdjustment(new AllowanceAdjustment(
                initialMinutes, "Initial approval", currentUser.getUsername(),
                isClockInitialized() ? clock.getElapsedMinutes() : -1));
        users.add(student);
        request.markApproved();
        save();
    }

    public void rejectRequest(String id, String reason) throws IOException {
        requireAdministrator();
        AccessRequest request = pendingRequest(id);
        request.markRejected(reason);
        save();
    }

    public Student findStudent(String id) {
        if (currentUser == null || (! (currentUser instanceof Administrator)
                && !currentUser.getUsername().equals(id)))
            throw new IllegalArgumentException("Access denied.");
        User user = findUser(id);
        if (user instanceof Student) return (Student) user;
        return null;
    }

    public ArrayList<AccessRequest> getRequests() {
        requireAdministrator();
        return new ArrayList<AccessRequest>(requests);
    }

    public ArrayList<Job> getVisibleJobs(String search) {
        if (currentUser == null) throw new IllegalArgumentException("Login required.");
        ArrayList<Job> visible = new ArrayList<Job>();
        String term = search == null ? "" : search.toLowerCase();
        for (Job job : jobs) {
            if (!(currentUser instanceof Administrator) &&
                    !currentUser.getUsername().equals(job.getStudentId())) continue;
            if (("" + job.getJobId()).indexOf(term) >= 0 ||
                    job.getTitle().toLowerCase().indexOf(term) >= 0 ||
                    job.getApplicationName().toLowerCase().indexOf(term) >= 0 ||
                    job.getStatus().toLowerCase().indexOf(term) >= 0) visible.add(job);
        }
        return visible;
    }

    public Job getJobDetails(int jobId) {
        if (currentUser == null) throw new IllegalArgumentException("Login required.");
        Job job = findJob(jobId);
        if (job == null || (!(currentUser instanceof Administrator) &&
                !currentUser.getUsername().equals(job.getStudentId())))
            throw new IllegalArgumentException("Job not found or access denied.");
        return job;
    }

    // Balances are calculated from the permanent adjustments and job history.
    public int[] getBalance(String studentId) {
        Student student = findStudent(studentId);
        if (student == null) throw new IllegalArgumentException("Student not found.");
        TimeAllowance a = student.getAllowance();
        return new int[]{a.getAllocatedMinutes(),
                a.getUsedMinutes(jobs, studentId),
                a.getReservedMinutes(jobs, studentId),
                a.getAvailableMinutes(jobs, studentId)};
    }

    public void grantMinutes(String id, int amount, String reason) throws IOException {
        requireAdministrator();
        if (amount <= 0) throw new IllegalArgumentException("Grant must be positive.");
        Student student = findStudent(id);
        if (student == null) throw new IllegalArgumentException("Student not found.");
        long total = (long) student.getAllowance().getAllocatedMinutes() + amount;
        if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("Allocation too large.");
        setAllocatedMinutes(id, (int) total, reason);
    }

    public void setAllocatedMinutes(String id, int newTotal, String reason) throws IOException {
        requireAdministrator();
        if (!filled(reason)) throw new IllegalArgumentException("Reason required.");
        Student student = findStudent(id);
        if (student == null) throw new IllegalArgumentException("Student not found.");
        int[] balance = getBalance(id);
        if (newTotal < 0 || (long) newTotal < (long) balance[1] + balance[2])
            throw new IllegalArgumentException("Cannot reduce below used plus reserved.");
        long delta = (long) newTotal - balance[0];
        if (delta < Integer.MIN_VALUE || delta > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Adjustment too large.");
        if (delta == 0) return;
        student.getAllowance().addAdjustment(new AllowanceAdjustment(
                (int) delta, reason, currentUser.getUsername(),
                isClockInitialized() ? clock.getElapsedMinutes() : -1));
        save();
    }

    public void initializeClock(LocalDateTime selected) throws IOException {
        requireAdministrator();
        if (selected == null || clock.isInitialized())
            throw new IllegalArgumentException("Choose a date/time only once.");
        clock.initialize(selected);
        save();
    }

    public boolean isClockInitialized() { return clock.isInitialized(); }
    public String getCurrentTimeText() {
        return clock.isInitialized() ? clock.formatMinute(clock.getElapsedMinutes()) : "N/A";
    }
    public String formatMinute(int minute) {
        return minute < 0 || !clock.isInitialized() ? "-" : clock.formatMinute(minute);
    }

    public Job submitJob(String title, String app, String description,
                         String size) throws IOException {
        Student student = requireStudent();
        if (!clock.isInitialized()) throw new IllegalArgumentException("Initialize clock first.");
        if (!filled(title) || !filled(app) || !filled(description))
            throw new IllegalArgumentException("Complete the job fields.");
        int duration = 0;
        if ("Small".equals(size)) duration = 5;
        else if ("Medium".equals(size)) duration = 15;
        else if ("Large".equals(size)) duration = 30;
        else if ("Very large".equals(size)) duration = 60;
        else throw new IllegalArgumentException("Invalid task size.");
        if (!student.getAllowance().canSubmit(duration, jobs, student.getUsername()))
            throw new IllegalArgumentException("Insufficient available minutes.");
        if (nextJobId == Integer.MAX_VALUE)
            throw new IllegalArgumentException("Job ID limit reached.");
        Job job = new Job(nextJobId, student.getUsername(), title, app,
                description, size, clock.getElapsedMinutes());
        nextJobId++;
        // The queue is kept in FCFS order by submission minute and ID.
        int position = 0;
        while (position < jobs.size() &&
                (jobs.get(position).getSubmissionMinute() < job.getSubmissionMinute() ||
                (jobs.get(position).getSubmissionMinute() == job.getSubmissionMinute() &&
                jobs.get(position).getJobId() < job.getJobId()))) position++;
        jobs.add(position, job);
        scheduler.dispatchWaitingJobs();
        save();
        return job;
    }

    public void advanceMinute() throws IOException {
        requireAdministrator();
        if (!clock.isInitialized()) throw new IllegalArgumentException("Initialize clock first.");
        if (clock.getElapsedMinutes() == Integer.MAX_VALUE)
            throw new IllegalArgumentException("Simulation time limit reached.");
        scheduler.advanceMinute();
        save();
    }

    public void cancelJob(int jobId, String reason) throws IOException {
        Job job = getJobDetails(jobId);
        if (!filled(reason)) throw new IllegalArgumentException("Reason required.");
        if (!currentUser.canCancel(job)) throw new IllegalArgumentException("Access denied.");
        if (!"Queued".equals(job.getStatus()) && !"Running".equals(job.getStatus()))
            throw new IllegalArgumentException("Job cannot be cancelled.");
        scheduler.cancelJob(job, currentUser.getUsername(), reason);
        save();
    }

    public int getQueuePosition(int jobId) {
        return scheduler.getQueuePosition(getJobDetails(jobId));
    }
    public int getPredictedStartMinute(int jobId) {
        Job job = getJobDetails(jobId);
        return "Queued".equals(job.getStatus()) ?
                scheduler.predictStartMinute(job) : job.getStartMinute();
    }
    public int getPredictedFinishMinute(int jobId) {
        Job job = getJobDetails(jobId);
        if ("Queued".equals(job.getStatus()))
            return scheduler.predictStartMinute(job) + job.getEstimatedDuration();
        if ("Running".equals(job.getStatus()))
            return job.getStartMinute() + job.getEstimatedDuration();
        return job.getFinishMinute();
    }
    public int getRemainingWait(int jobId) {
        Job job = getJobDetails(jobId);
        return "Queued".equals(job.getStatus()) ?
                scheduler.predictStartMinute(job) - clock.getElapsedMinutes() : 0;
    }

    public int[] getJobCounts() {
        requireAdministrator();
        int[] counts = new int[4];
        for (Job job : jobs) {
            if ("Queued".equals(job.getStatus())) counts[0]++;
            else if ("Running".equals(job.getStatus())) counts[1]++;
            else if ("Completed".equals(job.getStatus())) counts[2]++;
            else if ("Cancelled".equals(job.getStatus())) counts[3]++;
        }
        return counts;
    }
    public int getBusyGpuCount() {
        requireAdministrator();
        int busy = 0;
        for (GPU gpu : gpus) if (!gpu.isAvailable()) busy++;
        return busy;
    }
    public String getGpuSummary() {
        requireAdministrator();
        String text = "";
        for (GPU gpu : gpus) {
            Job job = gpu.getCurrentJob();
            text += "GPU " + gpu.getGpuId() + ": ";
            if (job == null) text += "Free\n";
            else text += "Job " + job.getJobId() + ", remaining " +
                    job.getRemainingMinutes(clock.getElapsedMinutes()) +
                    ", finish " + formatMinute(gpu.getExpectedFinishMinute()) + "\n";
        }
        return text;
    }
    public double getUtilizationPercent() {
        requireAdministrator();
        int elapsed = clock.getElapsedMinutes();
        if (elapsed == 0) return -1;
        long busy = 0;
        for (Job job : jobs) busy += job.getExecutedMinutes(elapsed);
        return 100.0 * busy / (4.0 * elapsed);
    }

    // Package-private access is reserved for the FileManager implementation.
    ArrayList<User> getUsersForStorage() { return users; }
    ArrayList<AccessRequest> getRequestsForStorage() { return requests; }
    ArrayList<Job> getJobsForStorage() { return jobs; }
    GPU[] getGpusForStorage() { return gpus; }
    SimulationClock getClockForStorage() { return clock; }
    int getNextJobIdForStorage() { return nextJobId; }

    // Restore complete snapshots without advancing the simulated clock.
    void restoreData(ArrayList<User> loadedUsers, ArrayList<AccessRequest> loadedRequests,
                     ArrayList<Job> loadedJobs, int[] gpuJobIds, LocalDateTime initial,
                     int elapsed, boolean ready, int nextId) {
        if (loadedUsers == null || loadedRequests == null || loadedJobs == null ||
                gpuJobIds == null || gpuJobIds.length != 4 || nextId < 1)
            throw new IllegalArgumentException("Invalid saved data.");
        // Stage the restored objects before replacing the live system.
        GPU[] restoredGpus = new GPU[4];
        for (int i = 0; i < 4; i++) restoredGpus[i] = new GPU(i + 1);
        SimulationClock restoredClock = new SimulationClock();
        restoredClock.restore(initial, elapsed, ready);
        for (int i = 0; i < 4; i++) {
            if (gpuJobIds[i] == -1) continue;
            Job running = null;
            for (Job job : loadedJobs)
                if (job.getJobId() == gpuJobIds[i]) running = job;
            if (running == null || !"Running".equals(running.getStatus()) ||
                    running.getAssignedGpuId() != i + 1)
                throw new IllegalArgumentException("Invalid GPU assignment.");
            restoredGpus[i].assignJob(running);
        }
        ArrayList<User> oldUsers = users;
        ArrayList<AccessRequest> oldRequests = requests;
        ArrayList<Job> oldJobs = jobs;
        GPU[] oldGpus = gpus;
        SimulationClock oldClock = clock;
        Scheduler oldScheduler = scheduler;
        int oldId = nextJobId;
        users = loadedUsers;
        requests = loadedRequests;
        jobs = loadedJobs;
        gpus = restoredGpus;
        clock = restoredClock;
        nextJobId = nextId;
        scheduler = new Scheduler(jobs, gpus, clock);
        try {
            validateLoadedState();
        } catch (RuntimeException e) {
            users = oldUsers;
            requests = oldRequests;
            jobs = oldJobs;
            gpus = oldGpus;
            clock = oldClock;
            scheduler = oldScheduler;
            nextJobId = oldId;
            throw e;
        }
        currentUser = null;
    }

    private void validateLoadedState() {
        int admins = 0;
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u == null) throw new IllegalArgumentException("Invalid user.");
            if (u instanceof Administrator) admins++;
            for (int j = 0; j < i; j++)
                if (u.getUsername().equals(users.get(j).getUsername()))
                    throw new IllegalArgumentException("Duplicate username.");
            if (u instanceof Student) {
                Student s = (Student) u;
                int[] b = getBalanceForStorage(s);
                if (b[0] < 0 || b[1] < 0 || b[2] < 0 || b[3] < 0)
                    throw new IllegalArgumentException("Invalid allowance.");
            }
        }
        if (admins != 1) throw new IllegalArgumentException("One administrator required.");
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            if (job == null || job.getJobId() < 1 || job.getJobId() >= nextJobId ||
                    !(findUser(job.getStudentId()) instanceof Student))
                throw new IllegalArgumentException("Invalid job.");
            for (int j = 0; j < i; j++)
                if (job.getJobId() == jobs.get(j).getJobId())
                    throw new IllegalArgumentException("Duplicate job ID.");
            if ("Running".equals(job.getStatus())) {
                int gpuId = job.getAssignedGpuId();
                if (gpuId < 1 || gpuId > 4 || gpus[gpuId - 1].getCurrentJob() != job)
                    throw new IllegalArgumentException("Running job not assigned.");
            }
        }
        for (GPU gpu : gpus) if (gpu.isAvailable())
            for (Job job : jobs) if ("Queued".equals(job.getStatus()))
                throw new IllegalArgumentException("Queued job while GPU free.");
    }

    private int[] getBalanceForStorage(Student s) {
        TimeAllowance a = s.getAllowance();
        String id = s.getUsername();
        return new int[]{a.getAllocatedMinutes(), a.getUsedMinutes(jobs, id),
                a.getReservedMinutes(jobs, id), a.getAvailableMinutes(jobs, id)};
    }
}
