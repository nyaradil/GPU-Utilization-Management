
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

// One controller owns the data; screens never schedule jobs directly.
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

    // Full Constructor
    public GpuSystem(Storage storage) {
        if (storage == null)
            throw new IllegalArgumentException("Storage required.");

        this.storage = storage;
        users = new ArrayList<User>();
        users.add(new Administrator());
        requests = new ArrayList<AccessRequest>();
        jobs = new ArrayList<Job>();

        // Create the four required GPUs.
        gpus = new GPU[4];
        for (int i = 0; i < 4; i++)
            gpus[i] = new GPU(i + 1);

        clock = new SimulationClock();
        scheduler = new Scheduler(jobs, gpus, clock);
        currentUser = null;
        nextJobId = 1;
    }

    public void load() throws IOException {
        storage.loadInto(this);
    }

    public void save() throws IOException {
        storage.save(this);
    }

    private boolean filled(String text) {
        if (text == null) return false;
        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) > ' ') return true;
        return false;
    }

    private User findUser(String username) {
        if (username == null) return null;
        for (User user : users)
            if (username.equals(user.getUsername())) return user;
        return null;
    }

    private Job findJob(int jobId) {
        for (Job job : jobs)
            if (job.getJobId() == jobId) return job;
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

    // Registration creates a request, not an approved account.
    public void registerStudent(String id, String name, String email,
                                String phone, String password) throws IOException {
        if (!filled(id) || !filled(name) || !filled(email)
                || !filled(phone) || !filled(password))
            throw new IllegalArgumentException("All fields are required.");

        // University ID must contain digits only.
        for (int i = 0; i < id.length(); i++)
            if (id.charAt(i) < '0' || id.charAt(i) > '9')
                throw new IllegalArgumentException(
                        "University ID must contain digits only.");

        // Basic email validation.
        int at = email.indexOf('@');
        if (at < 1 || email.indexOf('.', at + 1) <= at + 1
                || email.substring(at + 1).indexOf('@') >= 0)
            throw new IllegalArgumentException("Invalid email.");

        // Basic phone validation.
        int digits = 0;
        for (int i = 0; i < phone.length(); i++) {
            char c = phone.charAt(i);
            if (c >= '0' && c <= '9') digits++;
            else if (c != '+' && c != '-' && c != ' '
                    && c != '(' && c != ')')
                throw new IllegalArgumentException("Invalid phone number.");
        }
        if (digits == 0)
            throw new IllegalArgumentException("Phone must contain digits.");

        // Prevent duplicate accounts and requests.
        if (findUser(id) != null)
            throw new IllegalArgumentException("Account already exists.");

        for (AccessRequest request : requests)
            if (id.equals(request.getUniversityId()))
                throw new IllegalArgumentException(
                        "University ID already requested.");

        requests.add(new AccessRequest(id, name, email, phone, password));
        save();
    }

    // User is a polymorphic reference to Student or Administrator.
    public User login(String username, String password) {
        if (currentUser != null)
            throw new IllegalArgumentException("Log out first.");

        User user = findUser(username);
        if (user == null || password == null
                || !user.passwordMatches(password))
            throw new IllegalArgumentException(
                    "Invalid credentials or account not approved.");

        currentUser = user;
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Approve a student and record the initial GPU-minute allocation.
    public void approveRequest(String id, int initialMinutes)
            throws IOException {
        requireAdministrator();

        if (initialMinutes < 0)
            throw new IllegalArgumentException("Invalid allocation.");

        AccessRequest request = null;
        for (AccessRequest r : requests)
            if (r.getUniversityId().equals(id) && r.isPending())
                request = r;

        if (request == null || findUser(id) != null)
            throw new IllegalArgumentException("Pending request not found.");

        // Student constructor follows UML order:
        // id, name, email, phone, password.
        Student student = new Student(id, request.getName(),
                request.getEmail(), request.getPhone(),
                request.getChosenPassword());

        student.getAllowance().addAdjustment(new AllowanceAdjustment(
                initialMinutes, "Initial approval",
                currentUser.getUsername(),
                isClockInitialized() ? clock.getElapsedMinutes() : -1));

        users.add(student);
        request.markApproved();
        save();
    }

    public void rejectRequest(String id, String reason)
            throws IOException {
        requireAdministrator();

        for (AccessRequest request : requests) {
            if (request.getUniversityId().equals(id)
                    && request.isPending()) {
                request.markRejected(reason);
                save();
                return;
            }
        }
        throw new IllegalArgumentException("Pending request not found.");
    }

    // Administrators see any student; students see only themselves.
    public Student findStudent(String id) {
        if (currentUser == null
                || (!(currentUser instanceof Administrator)
                && !currentUser.getUsername().equals(id)))
            throw new IllegalArgumentException("Access denied.");

        User user = findUser(id);
        if (user instanceof Student) return (Student) user;
        return null;
    }

    public ArrayList<AccessRequest> getRequests() {
        requireAdministrator();

        // Return a separate list instead of the internal list.
        ArrayList<AccessRequest> copy = new ArrayList<AccessRequest>();
        for (AccessRequest request : requests)
            copy.add(request);
        return copy;
    }

    // Search jobs visible to the logged-in user.
    public ArrayList<Job> getVisibleJobs(String search) {
        if (currentUser == null)
            throw new IllegalArgumentException("Login required.");

        String term = search == null ? "" : search.toLowerCase();
        ArrayList<Job> visible = new ArrayList<Job>();

        for (Job job : jobs) {
            if (!(currentUser instanceof Administrator)
                    && !job.getOwnerId().equals(currentUser.getUsername()))
                continue;

            if (("" + job.getJobId()).indexOf(term) >= 0
                    || job.getTitle().toLowerCase().indexOf(term) >= 0
                    || job.getApplicationName().toLowerCase().indexOf(term) >= 0
                    || job.getStatus().toLowerCase().indexOf(term) >= 0)
                visible.add(job);
        }
        return visible;
    }

    public Job getJobDetails(int jobId) {
        if (currentUser == null)
            throw new IllegalArgumentException("Login required.");

        Job job = findJob(jobId);

        if (job == null || (!(currentUser instanceof Administrator)
                && !job.getOwnerId().equals(currentUser.getUsername())))
            throw new IllegalArgumentException(
                    "Job not found or access denied.");

        return job;
    }

    // Calculate balances from adjustments and job records.
    public int[] getBalance(String studentId) {
        Student student = findStudent(studentId);
        if (student == null)
            throw new IllegalArgumentException("Student not found.");

        TimeAllowance a = student.getAllowance();

        // Order: allocated, used, reserved, available.
        return new int[]{
                a.getAllocatedMinutes(),
                a.getUsedMinutes(jobs, studentId),
                a.getReservedMinutes(jobs, studentId),
                a.getAvailableMinutes(jobs, studentId)
        };
    }

    public void grantMinutes(String id, int amount, String reason)
            throws IOException {
        requireAdministrator();

        if (amount <= 0)
            throw new IllegalArgumentException("Grant must be positive.");

        Student student = findStudent(id);
        if (student == null)
            throw new IllegalArgumentException("Student not found.");

        long total = (long) student.getAllowance()
                .getAllocatedMinutes() + amount;

        if (total > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Allocation too large.");

        setAllocatedMinutes(id, (int) total, reason);
    }

    public void setAllocatedMinutes(String id, int newTotal,
                                    String reason) throws IOException {
        requireAdministrator();

        if (!filled(reason))
            throw new IllegalArgumentException("Reason required.");

        Student student = findStudent(id);
        if (student == null)
            throw new IllegalArgumentException("Student not found.");

        int[] balance = getBalance(id);

        // Do not reduce allocation below already used/reserved minutes.
        if (newTotal < 0
                || (long) newTotal < (long) balance[1] + balance[2])
            throw new IllegalArgumentException(
                    "Cannot reduce below used plus reserved.");

        long delta = (long) newTotal - balance[0];

        if (delta < Integer.MIN_VALUE || delta > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Adjustment too large.");

        if (delta == 0) return;

        student.getAllowance().addAdjustment(new AllowanceAdjustment(
                (int) delta, reason, currentUser.getUsername(),
                isClockInitialized() ? clock.getElapsedMinutes() : -1));
        save();
    }

    // Initialize the simulation clock only once.
    public void initializeClock(LocalDateTime selected)
            throws IOException {
        requireAdministrator();

        if (selected == null || clock.isInitialized())
            throw new IllegalArgumentException(
                    "Choose a date/time only once.");

        clock.initialize(selected);
        save();
    }

    public boolean isClockInitialized() {
        return clock.isInitialized();
    }

    public String getCurrentTimeText() {
        if (!clock.isInitialized()) return "N/A";
        return clock.formatMinute(clock.getElapsedMinutes());
    }

    public String formatMinute(int minute) {
        if (!clock.isInitialized() || minute < 0) return "-";
        return clock.formatMinute(minute);
    }

    // Submit a job if the student has enough available GPU minutes.
    public Job submitJob(String title, String app, String description,
                         String size) throws IOException {
        Student student = requireStudent();

        if (!clock.isInitialized())
            throw new IllegalArgumentException("Initialize clock first.");

        if (!filled(title) || !filled(app) || !filled(description))
            throw new IllegalArgumentException("Complete all job fields.");

        int duration;

        if ("Small".equals(size)) duration = 5;
        else if ("Medium".equals(size)) duration = 15;
        else if ("Large".equals(size)) duration = 30;
        else if ("Very large".equals(size)) duration = 60;
        else throw new IllegalArgumentException("Invalid task size.");

        if (!student.getAllowance().canSubmit(
                duration, jobs, student.getUsername()))
            throw new IllegalArgumentException(
                    "Insufficient available minutes.");

        if (nextJobId == Integer.MAX_VALUE)
            throw new IllegalArgumentException("Job ID limit reached.");

        Job job = new Job(nextJobId, student.getUsername(),
                title, app, description, size,
                clock.getElapsedMinutes());

        nextJobId++;

        // Submission times never go backwards.
        // Increasing IDs break ties for FCFS scheduling.
        jobs.add(job);

        scheduler.dispatchWaitingJobs();
        save();
        return job;
    }

    // Advance all GPU jobs by exactly one simulated minute.
    public void advanceMinute() throws IOException {
        requireAdministrator();

        if (!clock.isInitialized())
            throw new IllegalArgumentException("Initialize clock first.");

        if (clock.getElapsedMinutes() == Integer.MAX_VALUE)
            throw new IllegalArgumentException(
                    "Simulation time limit reached.");

        scheduler.advanceMinute();
        save();
    }

    // Cancel a job after checking ownership and its current state.
    public void cancelJob(int jobId, String reason)
            throws IOException {
        Job job = getJobDetails(jobId);

        if (!filled(reason))
            throw new IllegalArgumentException("Reason required.");

        if (!currentUser.canCancel(job))
            throw new IllegalArgumentException("Access denied.");

        if (!"Queued".equals(job.getStatus())
                && !"Running".equals(job.getStatus()))
            throw new IllegalArgumentException(
                    "Job cannot be cancelled.");

        scheduler.cancelJob(job, currentUser.getUsername(), reason);
        save();
    }

    // Queue and waiting-time calculations belong to Scheduler.
    public int getQueuePosition(int jobId) {
        return scheduler.getQueuePosition(getJobDetails(jobId));
    }

    public int getPredictedStartMinute(int jobId) {
        Job job = getJobDetails(jobId);

        if ("Queued".equals(job.getStatus()))
            return scheduler.predictStartMinute(job);

        return job.getStartMinute();
    }

    public int getPredictedFinishMinute(int jobId) {
        Job job = getJobDetails(jobId);

        if ("Queued".equals(job.getStatus()))
            return scheduler.predictStartMinute(job)
                    + job.getEstimatedDuration();

        if ("Running".equals(job.getStatus()))
            return job.getStartMinute() + job.getEstimatedDuration();

        return job.getFinishMinute();
    }

    public int getRemainingWait(int jobId) {
        Job job = getJobDetails(jobId);

        if (!"Queued".equals(job.getStatus())) return 0;
        return scheduler.getRemainingWait(job);
    }

    // Counts: queued, running, completed, cancelled.
    public int[] getJobCounts() {
        if (currentUser == null)
            throw new IllegalArgumentException("Login required.");

        int[] counts = new int[4];

        for (Job job : getVisibleJobs("")) {
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
        for (GPU gpu : gpus)
            if (!gpu.isAvailable()) busy++;

        return busy;
    }

    // Return GPU information for the administrator dashboard.
    public String getGpuSummary() {
        requireAdministrator();

        String summary = "";

        for (GPU gpu : gpus) {
            Job job = gpu.getCurrentJob();

            summary += "GPU " + gpu.getGpuId() + ": ";

            if (job == null) {
                summary += "Free\n";
            } else {
                summary += "Job " + job.getJobId()
                        + ", remaining "
                        + gpu.getRemainingMinutes(
                                clock.getElapsedMinutes())
                        + ", finish "
                        + formatMinute(gpu.getExpectedFinishMinute())
                        + "\n";
            }
        }

        return summary;
    }

    // Busy GPU minutes divided by four times elapsed minutes.
    public double getUtilizationPercent() {
        requireAdministrator();

        int elapsed = clock.getElapsedMinutes();
        if (elapsed == 0) return -1; // N/A at time zero.

        long busy = 0;

        for (Job job : jobs)
            busy += job.getExecutedMinutes(elapsed);

        return 100.0 * busy / (4.0 * elapsed);
    }

    // Package-access methods used only by FileManager.
    ArrayList<User> getUsersForStorage() { return users; }
    ArrayList<AccessRequest> getRequestsForStorage() { return requests; }
    ArrayList<Job> getJobsForStorage() { return jobs; }
    GPU[] getGpusForStorage() { return gpus; }
    SimulationClock getClockForStorage() { return clock; }
    int getNextJobIdForStorage() { return nextJobId; }

    // Stage loaded data first.
    // Replace live data only after validation succeeds.
    void restoreData(ArrayList<User> loadedUsers,
                     ArrayList<AccessRequest> loadedRequests,
                     ArrayList<Job> loadedJobs,
                     int[] gpuJobIds,
                     LocalDateTime initial,
                     int elapsed, boolean ready, int nextId) {

        if (loadedUsers == null || loadedRequests == null
                || loadedJobs == null || gpuJobIds == null
                || gpuJobIds.length != 4 || nextId < 1)
            throw new IllegalArgumentException("Invalid saved data.");

        GpuSystem candidate = new GpuSystem(storage);

        candidate.users = loadedUsers;
        candidate.requests = loadedRequests;
        candidate.jobs = loadedJobs;
        candidate.nextJobId = nextId;
        candidate.clock.restore(initial, elapsed, ready);
        candidate.scheduler = new Scheduler(
                candidate.jobs, candidate.gpus, candidate.clock);

        // Reconnect running jobs to their GPUs.
        for (int i = 0; i < 4; i++) {
            if (gpuJobIds[i] == -1) continue;

            Job job = candidate.findJob(gpuJobIds[i]);

            if (job == null || !"Running".equals(job.getStatus())
                    || job.getGpuId() != i + 1)
                throw new IllegalArgumentException(
                        "Invalid GPU assignment.");

            candidate.gpus[i].assignJob(job);
        }

        candidate.validateLoadedState();

        // All checks passed: replace the application's data.
        users = candidate.users;
        requests = candidate.requests;
        jobs = candidate.jobs;
        gpus = candidate.gpus;
        clock = candidate.clock;
        scheduler = candidate.scheduler;
        nextJobId = candidate.nextJobId;
        currentUser = null;
    }

    // Basic checks prevent corrupted saved records becoming live data.
    private void validateLoadedState() {
        if (!clock.isInitialized() && jobs.size() > 0)
            throw new IllegalArgumentException(
                    "Jobs require an initialized clock.");

        if (clock.getElapsedMinutes() < 0)
            throw new IllegalArgumentException("Invalid simulation time.");

        int admins = 0;

        // Validate users and prevent duplicate usernames.
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);

            if (user == null || !filled(user.getUsername()))
                throw new IllegalArgumentException("Invalid account.");

            if (user instanceof Administrator) admins++;

            for (int j = 0; j < i; j++)
                if (user.getUsername().equals(
                        users.get(j).getUsername()))
                    throw new IllegalArgumentException(
                            "Duplicate account.");
        }

        if (admins != 1)
            throw new IllegalArgumentException(
                    "One administrator required.");

        // Validate registration requests.
        for (int i = 0; i < requests.size(); i++) {
            AccessRequest request = requests.get(i);

            if (request == null)
                throw new IllegalArgumentException("Invalid request.");

            for (int j = 0; j < i; j++)
                if (request.getUniversityId().equals(
                        requests.get(j).getUniversityId()))
                    throw new IllegalArgumentException(
                            "Duplicate request.");

            boolean approved = "Approved".equals(request.getStatus());
            boolean hasAccount = findUser(
                    request.getUniversityId()) instanceof Student;

            if (approved != hasAccount)
                throw new IllegalArgumentException(
                        "Request and account do not match.");
        }

        // Validate student allowances and approvals.
        for (User user : users) {
            if (user instanceof Student) {
                Student student = (Student) user;

                if (student.getAllowance() == null)
                    throw new IllegalArgumentException(
                            "Missing allowance.");

                int[] balance = new int[]{
                        student.getAllowance().getAllocatedMinutes(),
                        student.getAllowance().getUsedMinutes(
                                jobs, user.getUsername()),
                        student.getAllowance().getReservedMinutes(
                                jobs, user.getUsername()),
                        student.getAllowance().getAvailableMinutes(
                                jobs, user.getUsername())
                };

                if (balance[0] < 0 || balance[1] < 0
                        || balance[2] < 0 || balance[3] < 0)
                    throw new IllegalArgumentException(
                            "Invalid allowance.");

                boolean approved = false;

                for (AccessRequest request : requests)
                    if (request.getUniversityId().equals(
                            user.getUsername())
                            && "Approved".equals(request.getStatus()))
                        approved = true;

                if (!approved)
                    throw new IllegalArgumentException(
                            "Approval record missing.");
            }
        }

        // Validate jobs and FCFS ordering.
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);

            if (job == null || job.getJobId() < 1
                    || job.getJobId() >= nextJobId
                    || !(findUser(job.getOwnerId()) instanceof Student)
                    || job.getSubmissionMinute() < 0
                    || job.getSubmissionMinute() > clock.getElapsedMinutes())
                throw new IllegalArgumentException("Invalid job.");

            for (int j = 0; j < i; j++)
                if (job.getJobId() == jobs.get(j).getJobId())
                    throw new IllegalArgumentException(
                            "Duplicate job ID.");

            if (i > 0 && (job.getSubmissionMinute()
                    < jobs.get(i - 1).getSubmissionMinute()
                    || (job.getSubmissionMinute()
                    == jobs.get(i - 1).getSubmissionMinute()
                    && job.getJobId()
                    < jobs.get(i - 1).getJobId())))
                throw new IllegalArgumentException(
                        "Jobs are not in FCFS order.");

            if ("Running".equals(job.getStatus())) {
                int id = job.getGpuId();

                if (id < 1 || id > 4
                        || gpus[id - 1].getCurrentJob() != job)
                    throw new IllegalArgumentException(
                            "Running job has no GPU.");

            } else if ("Queued".equals(job.getStatus())) {
                if (job.getGpuId() != -1)
                    throw new IllegalArgumentException(
                            "Queued job has a GPU.");

            } else if (!"Completed".equals(job.getStatus())
                    && !"Cancelled".equals(job.getStatus())) {
                throw new IllegalArgumentException(
                        "Unknown job status.");
            }
        }

        // Validate the four GPU assignments.
        for (int i = 0; i < 4; i++) {
            if (gpus[i].getGpuId() != i + 1)
                throw new IllegalArgumentException("Invalid GPU ID.");

            Job running = gpus[i].getCurrentJob();

            if (running == null) {
                for (Job job : jobs)
                    if ("Queued".equals(job.getStatus()))
                        throw new IllegalArgumentException(
                                "Queued job while GPU is free.");

            } else if (!"Running".equals(running.getStatus())) {
                throw new IllegalArgumentException(
                        "GPU is not running a valid job.");
            }
        }
    }
}
