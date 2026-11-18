
import java.io.FileNotFoundException;
import java.util.ArrayList;

// Controls accounts, jobs, allowances and the GPU simulation.
public class GpuSystem {

    private Storage storage;
    private FileManager fileManager;
    private Scheduler scheduler;
    private User currentUser;
    private String lastError;

    // Default Constructor
    public GpuSystem() {
        storage = new Storage();
        fileManager = new FileManager();
        scheduler = new Scheduler(
                storage.getJobs(), storage.getGpus(), storage.getClock());
        currentUser = null;
        lastError = "";
    }

    public String getLastError() { return lastError; }

    public String getCurrentUsername() {
        if (currentUser == null) return "";
        return currentUser.getUsername();
    }

    public boolean isAdministrator() {
        return currentUser instanceof Administrator;
    }

    public boolean isStudent() {
        return currentUser instanceof Student;
    }

    public boolean isClockInitialized() {
        return storage.getClock().isInitialized();
    }

    private boolean fail(String message) {
        lastError = message;
        return false;
    }

    private boolean requireAdmin() {
        if (!isAdministrator()) {
            return fail("Administrator login required.");
        }
        return true;
    }

    private boolean filled(String value) {
        if (value == null) return false;

        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) > ' ') return true;
        }
        return false;
    }

    // Required task sizes and simulated durations.
    private int durationFor(String size) {
        if ("Small".equals(size)) return 5;
        if ("Medium".equals(size)) return 15;
        if ("Large".equals(size)) return 30;
        if ("Very large".equals(size)) return 60;
        return -1;
    }

    // Register a student. Approval is required before login.
    public boolean registerStudent(String id, String name, String email,
                                   String phone, String password) {
        lastError = "";

        if (!filled(id) || !filled(name) || !filled(email)
                || !filled(phone) || !filled(password)) {
            return fail("All fields are required.");
        }

        for (int i = 0; i < id.length(); i++) {
            if (id.charAt(i) < '0' || id.charAt(i) > '9') {
                return fail("University ID must contain digits only.");
            }
        }

        int at = email.indexOf('@');
        if (at <= 0 || at >= email.length() - 1
                || email.substring(at + 1).indexOf('@') != -1
                || email.substring(at + 1).indexOf('.') <= 0) {
            return fail("Invalid email address.");
        }

        int digits = 0;
        for (int i = 0; i < phone.length(); i++) {
            char c = phone.charAt(i);

            if (c >= '0' && c <= '9') {
                digits++;
            } else if (c != '+' && c != '-' && c != ' '
                    && c != '(' && c != ')') {
                return fail("Invalid phone number.");
            }
        }

        if (digits == 0) {
            return fail("Phone number must contain digits.");
        }

        if (storage.findStudent(id) != null
                || storage.findRequest(id) != null) {
            return fail("This university ID is already registered.");
        }

        try {
            storage.getRequests().add(
                    new AccessRequest(id, name, email, phone, password));
        } catch (IllegalArgumentException e) {
            return fail(e.getMessage());
        }

        return true;
    }

    // Authenticate either an administrator or an approved student.
    public boolean login(String username, String password) {
        lastError = "";

        if (currentUser != null) return fail("Log out first.");
        if (username == null || password == null) {
            return fail("Enter credentials.");
        }

        // Polymorphism: User can refer to Administrator or Student.
        User user = storage.getAdministrator();

        if (!username.equals(user.getUsername())) {
            user = storage.findStudent(username);
        }

        if (user == null || !user.passwordMatches(password)) {
            return fail("Invalid credentials or account not approved.");
        }

        currentUser = user;
        return true;
    }

    public void logout() {
        currentUser = null;
        lastError = "";
    }

    // Approve a request with the default 100-minute allowance.
    public boolean approveRequest(String id) {
        return approveRequest(id, 100);
    }

    public boolean approveRequest(String id, int minutes) {
        lastError = "";
        if (!requireAdmin()) return false;

        AccessRequest request = storage.findRequest(id);

        if (request == null || !request.isPending()
                || storage.findStudent(id) != null) {
            return fail("Only a pending request can be approved.");
        }

        if (minutes < 0) {
            return fail("Allocated minutes cannot be negative.");
        }

        Student student = new Student(
                request.getUniversityId(), request.getName(),
                request.getChosenPassword(), request.getPhone(),
                request.getEmail());

        // Student.java must provide setAllowance(TimeAllowance).
        student.setAllowance(new TimeAllowance(minutes, 0, 0));

        storage.getStudents().add(student);
        request.markApproved();
        return true;
    }

    public boolean rejectRequest(String id, String reason) {
        lastError = "";
        if (!requireAdmin()) return false;

        AccessRequest request = storage.findRequest(id);

        if (request == null || !request.isPending()) {
            return fail("Only a pending request can be rejected.");
        }

        request.markRejected(reason);
        return true;
    }

    // Adjust a student's total allocation and record the change.
    public boolean setAllocatedMinutes(String id, int total, String reason) {
        lastError = "";
        if (!requireAdmin()) return false;

        Student student = storage.findStudent(id);

        if (student == null || !filled(reason)) {
            return fail("Student and reason required.");
        }

        TimeAllowance allowance = student.getAllowance();

        if (total < 0
                || (long) total < (long) allowance.getUsed()
                        + allowance.getReserved()) {
            return fail("Allocation cannot be below used plus reserved minutes.");
        }

        if (total == allowance.getAllocated()) {
            return fail("Allocation has not changed.");
        }

        int change = total - allowance.getAllocated();
        allowance.setAllocated(total);

        int time = -1;
        if (isClockInitialized()) {
            time = storage.getClock().getElapsedMinutes();
        }

        // Record: student ID, change, reason, admin, simulated minute.
        storage.getAdjustments().add(
                new String[]{id, "" + change, reason, "admin", "" + time});

        return true;
    }

    public boolean grantExtraTime(String id, int minutes, String reason) {
        lastError = "";
        if (!requireAdmin()) return false;

        Student student = storage.findStudent(id);

        if (student == null || minutes <= 0) {
            return fail("Invalid student or amount.");
        }

        int total = student.getAllowance().getAllocated();

        if (minutes > Integer.MAX_VALUE - total) {
            return fail("Allocation too large.");
        }

        return setAllocatedMinutes(id, total + minutes, reason);
    }

    // Initialize the simulated date and time once.
    public boolean initializeClock(int day, int month, int year,
                                   int hour, int minute) {
        lastError = "";
        if (!requireAdmin()) return false;

        if (isClockInitialized() || storage.getJobs().size() > 0) {
            return fail("Clock already initialized; use Advance Minute.");
        }

        if (!storage.getClock().initialize(day, month, year, hour, minute)) {
            return fail("Invalid date or time.");
        }

        return true;
    }

    // Submit a job and reserve its full duration.
    // Returns the job ID, or -1 if submission fails.
    public int submitJob(String title, String application,
                         String description, String size) {
        lastError = "";

        if (!isStudent()) {
            fail("Student login required.");
            return -1;
        }

        if (!isClockInitialized()) {
            fail("Initialize the clock first.");
            return -1;
        }

        int minutes = durationFor(size);

        if (!filled(title) || !filled(application)
                || !filled(description) || minutes < 0) {
            fail("Complete all job details and choose a valid task size.");
            return -1;
        }

        TimeAllowance allowance = ((Student) currentUser).getAllowance();

        // An exact allowance match is accepted.
        if (minutes > allowance.getAvailable()) {
            fail("Not enough available minutes.");
            return -1;
        }

        int id = storage.nextJobId();
        if (id < 1) {
            fail("Job ID limit reached.");
            return -1;
        }

        int now = storage.getClock().getElapsedMinutes();

        // Avoid overflowing simulated execution times.
        long latest = (long) now + minutes;

        for (Job job : storage.getJobs()) {
            if (job.getStatus().equals("Queued")
                    || job.getStatus().equals("Running")) {
                latest += job.getEstimatedDuration();
            }
        }

        if (latest > Integer.MAX_VALUE) {
            fail("Simulation time limit reached.");
            return -1;
        }

        Job job = new Job(id, currentUser.getUsername(),
                title, application, description, size, now);

        storage.addJob(job);
        allowance.setReserved(allowance.getReserved() + minutes);

        // Scheduler assigns available GPUs in increasing ID order.
        scheduler.dispatchWaitingJobs();
        return id;
    }

    // Release the reservation and charge actual execution time.
    private void settle(Job job, int charge) {
        TimeAllowance allowance =
                storage.findStudent(job.getStudentId()).getAllowance();

        allowance.setReserved(
                allowance.getReserved() - job.getEstimatedDuration());

        allowance.setUsed(allowance.getUsed() + charge);
    }

    // Advance the simulation by one minute.
    public boolean advanceMinute() {
        lastError = "";
        if (!requireAdmin()) return false;

        if (!isClockInitialized()) {
            return fail("Initialize the clock first.");
        }

        if (storage.getClock().getElapsedMinutes() == Integer.MAX_VALUE) {
            return fail("Simulation time limit reached.");
        }

        // Remember which jobs were running before the clock advances.
        Job[] runningBefore = new Job[4];

        for (int i = 0; i < 4; i++) {
            runningBefore[i] = storage.getGpus()[i].getCurrentJob();
        }

        scheduler.advanceMinute();

        // Charge jobs that completed during this minute.
        for (int i = 0; i < 4; i++) {
            if (runningBefore[i] != null
                    && runningBefore[i].getStatus().equals("Completed")) {
                settle(runningBefore[i],
                        runningBefore[i].getEstimatedDuration());
            }
        }

        return true;
    }

    // Cancel a queued or running job with a recorded reason.
    public boolean cancelJob(int jobId, String reason) {
        lastError = "";

        Job job = visibleJob(jobId);
        if (job == null) return false;

        if (!filled(reason)) {
            return fail("Cancellation reason required.");
        }

        String status = job.getStatus();

        if (!status.equals("Queued") && !status.equals("Running")) {
            return fail("Only queued or running jobs can be cancelled.");
        }

        int charge = 0;

        // Queued cancellation is free; running cancellation charges runtime.
        if (status.equals("Running")) {
            charge = storage.getClock().getElapsedMinutes()
                    - job.getStartTime();
        }

        scheduler.cancelJob(job, currentUser.getUsername(), reason);
        settle(job, charge);
        return true;
    }

    // Enforce student ownership when accessing jobs.
    private Job visibleJob(int id) {
        Job job = storage.findJob(id);

        if (currentUser == null || job == null
                || (!isAdministrator()
                && !job.getStudentId().equals(currentUser.getUsername()))) {
            fail("Job not found or access denied.");
            return null;
        }

        return job;
    }

    public int getQueuePosition(int id) {
        Job job = visibleJob(id);
        if (job == null) return -1;

        if (!job.getStatus().equals("Queued")) return 0;
        return scheduler.getQueuePosition(job);
    }

    public int getPredictedStart(int id) {
        Job job = visibleJob(id);
        if (job == null) return -1;

        if (job.getStatus().equals("Queued")) {
            return scheduler.predictStartMinute(job);
        }
        return job.getStartTime();
    }

    public int getPredictedFinish(int id) {
        Job job = visibleJob(id);
        if (job == null) return -1;

        if (job.getStatus().equals("Queued")) {
            return scheduler.predictStartMinute(job)
                    + job.getEstimatedDuration();
        }

        if (job.getStatus().equals("Running")) {
            return job.getStartTime() + job.getEstimatedDuration();
        }

        return job.getFinishTime();
    }

    public int getRemainingWait(int id) {
        Job job = visibleJob(id);
        if (job == null) return -1;

        if (!job.getStatus().equals("Queued")) return 0;

        return getPredictedStart(id)
                - storage.getClock().getElapsedMinutes();
    }

    // Runtime excludes all time spent waiting in the queue.
    private int runtime(Job job) {
        if (job.getStartTime() < 0) return 0;

        int end = job.getFinishTime();

        if (job.getStatus().equals("Running")) {
            end = storage.getClock().getElapsedMinutes();
        }

        return end - job.getStartTime();
    }

    public int getRuntime(int id) {
        Job job = visibleJob(id);
        if (job == null) return -1;
        return runtime(job);
    }

    public int getChargedMinutes(int id) {
        Job job = visibleJob(id);
        if (job == null) return -1;

        if (job.getStatus().equals("Completed")
                || job.getStatus().equals("Cancelled")) {
            return runtime(job);
        }
        return 0;
    }

    public int getTotalQueueWait(int id) {
        Job job = visibleJob(id);
        if (job == null) return -1;

        if (job.getStartTime() >= 0) {
            return job.getStartTime() - job.getSubmissionTime();
        }

        if (job.getStatus().equals("Cancelled")) {
            return job.getCancellationTime() - job.getSubmissionTime();
        }

        return storage.getClock().getElapsedMinutes()
                - job.getSubmissionTime();
    }

    // Return job information as copies for the Swing tables.
    public ArrayList<String[]> getJobRows() {
        ArrayList<String[]> rows = new ArrayList<String[]>();

        if (currentUser == null) {
            fail("Login required.");
            return rows;
        }

        for (Job job : storage.getJobs()) {
            if (!isAdministrator()
                    && !job.getStudentId().equals(currentUser.getUsername())) {
                continue;
            }

            int id = job.getJobId();

            rows.add(new String[]{
                    "" + id,
                    job.getStudentId(),
                    job.getTitle(),
                    job.getApplicationName(),
                    job.getDescription(),
                    job.getTaskSize(),
                    "" + job.getEstimatedDuration(),
                    job.getStatus(),
                    "" + job.getAssignedGpuId(),
                    formatMinute(job.getSubmissionTime()),
                    formatMinute(job.getStartTime()),
                    formatMinute(job.getFinishTime()),
                    "" + getQueuePosition(id),
                    formatMinute(getPredictedStart(id)),
                    formatMinute(getPredictedFinish(id)),
                    "" + getRemainingWait(id),
                    "" + runtime(job),
                    "" + getChargedMinutes(id),
                    formatMinute(job.getCancellationTime()),
                    job.getCancelledBy(),
                    job.getCancellationReason(),
                    job.getResult(),
                    "" + getTotalQueueWait(id)
            });
        }

        return rows;
    }

    // Search visible jobs by ID, title, application or status.
    public ArrayList<String[]> searchJobs(String text) {
        ArrayList<String[]> found = new ArrayList<String[]>();
        if (text == null) return found;

        String needle = text.toLowerCase();

        for (String[] row : getJobRows()) {
            if (row[0].indexOf(needle) >= 0
                    || row[2].toLowerCase().indexOf(needle) >= 0
                    || row[3].toLowerCase().indexOf(needle) >= 0
                    || row[7].toLowerCase().indexOf(needle) >= 0) {
                found.add(row);
            }
        }

        return found;
    }

    // Students see only their own balance; admins see every student.
    public ArrayList<String[]> getStudentRows() {
        ArrayList<String[]> rows = new ArrayList<String[]>();

        if (currentUser == null) {
            fail("Login required.");
            return rows;
        }

        for (Student student : storage.getStudents()) {
            if (!isAdministrator()
                    && !student.getUsername().equals(currentUser.getUsername())) {
                continue;
            }

            TimeAllowance a = student.getAllowance();

            rows.add(new String[]{
                    student.getUsername(), student.getName(),
                    student.getEmail(), student.getPhone(),
                    "" + a.getAllocated(), "" + a.getUsed(),
                    "" + a.getReserved(), "" + a.getAvailable()
            });
        }

        return rows;
    }

    public ArrayList<String[]> searchStudents(String id) {
        ArrayList<String[]> found = new ArrayList<String[]>();
        if (!requireAdmin() || id == null) return found;

        for (String[] row : getStudentRows()) {
            if (row[0].indexOf(id) >= 0) {
                found.add(row);
            }
        }

        return found;
    }

    // Registration requests are visible to the administrator only.
    public ArrayList<String[]> getRequestRows() {
        ArrayList<String[]> rows = new ArrayList<String[]>();
        if (!requireAdmin()) return rows;

        for (AccessRequest r : storage.getRequests()) {
            rows.add(new String[]{
                    r.getUniversityId(), r.getName(),
                    r.getEmail(), r.getPhone(),
                    r.getChosenPassword(), r.getStatus(),
                    r.getRejectionReason()
            });
        }

        return rows;
    }

    // Show the history of allowance changes.
    public ArrayList<String[]> getAdjustmentRows() {
        ArrayList<String[]> rows = new ArrayList<String[]>();
        if (!requireAdmin()) return rows;

        for (String[] a : storage.getAdjustments()) {
            rows.add(new String[]{
                    a[0], a[1], a[2], a[3],
                    formatMinute(Integer.parseInt(a[4]))
            });
        }

        return rows;
    }

    // GPU dashboard: ID, availability, current job, remaining, finish.
    public ArrayList<String[]> getGpuRows() {
        ArrayList<String[]> rows = new ArrayList<String[]>();
        if (!requireAdmin()) return rows;

        for (GPU gpu : storage.getGpus()) {
            Job job = gpu.getCurrentJob();

            if (job == null) {
                rows.add(new String[]{
                        "" + gpu.getGpuId(), "Free", "-", "0", "-"
                });
            } else {
                rows.add(new String[]{
                        "" + gpu.getGpuId(),
                        "Busy",
                        "" + job.getJobId(),
                        "" + (job.getEstimatedDuration() - runtime(job)),
                        formatMinute(gpu.getExpectedFinishMinute())
                });
            }
        }

        return rows;
    }

    // Counts: Queued, Running, Completed, Cancelled.
    public int[] getJobCounts() {
        int[] counts = new int[4];
        if (currentUser == null) return counts;

        for (Job job : storage.getJobs()) {
            if (!isAdministrator()
                    && !job.getStudentId().equals(currentUser.getUsername())) {
                continue;
            }

            if (job.getStatus().equals("Queued")) counts[0]++;
            else if (job.getStatus().equals("Running")) counts[1]++;
            else if (job.getStatus().equals("Completed")) counts[2]++;
            else if (job.getStatus().equals("Cancelled")) counts[3]++;
        }

        return counts;
    }

    public int getBusyGpuCount() {
        if (!requireAdmin()) return -1;

        int count = 0;
        for (GPU gpu : storage.getGpus()) {
            if (!gpu.isAvailable()) count++;
        }
        return count;
    }

    // Total busy GPU minutes divided by total available GPU minutes.
    public double getUtilization() {
        if (!requireAdmin()) return -1;

        int elapsed = storage.getClock().getElapsedMinutes();
        if (elapsed == 0) return -1; // GUI should display N/A.

        long busyMinutes = 0;

        for (Job job : storage.getJobs()) {
            busyMinutes += runtime(job);
        }

        return 100.0 * busyMinutes / (4.0 * elapsed);
    }

    public String getCurrentTime() {
        if (!isClockInitialized()) return "Not initialized";
        return formatMinute(storage.getClock().getElapsedMinutes());
    }

    // Convert simulated minutes to dd/MM/yyyy HH:mm.
    public String formatMinute(int offset) {
        if (!isClockInitialized() || offset < 0) return "-";

        SimulationClock c = storage.getClock();

        int day = c.getDay();
        int month = c.getMonth();
        int year = c.getYear();

        long total = (long) c.getHour() * 60 + c.getMinute() + offset;

        int hour = (int) ((total / 60) % 24);
        int minute = (int) (total % 60);
        long days = total / 1440;

        while (days > 0) {
            int remaining = daysInMonth(month, year) - day;

            if (days <= remaining) {
                day += (int) days;
                days = 0;
            } else {
                days -= remaining + 1;
                day = 1;
                month++;

                if (month == 13) {
                    month = 1;
                    year++;
                }
            }
        }

        return two(day) + "/" + two(month) + "/" + year
                + " " + two(hour) + ":" + two(minute);
    }

    private String two(int number) {
        if (number < 10) return "0" + number;
        return "" + number;
    }

    private int daysInMonth(int month, int year) {
        if (month == 2) {
            if (year % 400 == 0
                    || (year % 4 == 0 && year % 100 != 0)) {
                return 29;
            }
            return 28;
        }

        if (month == 4 || month == 6
                || month == 9 || month == 11) {
            return 30;
        }

        return 31;
    }

    // Save the complete simulation.
    public boolean save() {
        lastError = "";

        try {
            fileManager.save(storage);
            return true;
        } catch (FileNotFoundException e) {
            return fail("Cannot save file: " + e.getMessage());
        } catch (RuntimeException e) {
            return fail("Cannot save data: " + e.getMessage());
        }
    }

    // Restore data and reconnect the Scheduler to the restored objects.
    public boolean load() {
        lastError = "";

        if (isStudent()) {
            return fail("Log out before loading data.");
        }

        try {
            Storage loaded = fileManager.load();

            storage = loaded;
            scheduler = new Scheduler(
                    storage.getJobs(), storage.getGpus(), storage.getClock());

            currentUser = null;
            return true;

        } catch (FileNotFoundException e) {
            return fail("Cannot open save file: " + e.getMessage());
        } catch (RuntimeException e) {
            return fail("Invalid save file: " + e.getMessage());
        }
    }
}
