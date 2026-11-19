
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

// Saves and restores the system using basic text-file operations.
public class FileManager implements Storage {
    private String filePath;

    // Full Constructor
    public FileManager(String path) {
        if (path == null || path.length() == 0)
            throw new IllegalArgumentException("File path required.");

        filePath = path;
    }

    // Write length and character codes to preserve spaces and line breaks.
    private void writeText(PrintWriter out, String text) {
        if (text == null) text = "";

        out.println(text.length());

        for (int i = 0; i < text.length(); i++)
            out.println((int) text.charAt(i));
    }

    private String readText(Scanner in) {
        int length = in.nextInt();

        if (length < 0 || length > 100000)
            throw new IllegalArgumentException("Invalid text length.");

        String text = "";

        for (int i = 0; i < length; i++) {
            int code = in.nextInt();

            if (code < 0 || code > 65535)
                throw new IllegalArgumentException(
                        "Invalid character code.");

            text += (char) code;
        }

        return text;
    }

    private int readCount(Scanner in) {
        int count = in.nextInt();

        if (count < 0 || count > 100000)
            throw new IllegalArgumentException("Invalid record count.");

        return count;
    }

    private void writeAdjustment(PrintWriter out,
                                 AllowanceAdjustment record) {
        out.println(record.getMinutesDelta());
        writeText(out, record.getReason());
        writeText(out, record.getAdminUsername());
        out.println(record.getRecordedMinute());
    }

    private AllowanceAdjustment readAdjustment(Scanner in) {
        int minutes = in.nextInt();
        String reason = readText(in);
        String admin = readText(in);
        int minute = in.nextInt();

        return new AllowanceAdjustment(minutes, reason, admin, minute);
    }

    // Save an administrator or student account.
    private void writeUser(PrintWriter out, User user) {
        out.println(user instanceof Administrator ? "A" : "S");

        writeText(out, user.getUsername());
        writeText(out, user.getName());
        writeText(out, user.getPasswordForStorage());

        if (user instanceof Student) {
            Student student = (Student) user;

            writeText(out, student.getEmail());
            writeText(out, student.getPhone());

            ArrayList<AllowanceAdjustment> history =
                    student.getAllowance().getAdjustments();

            out.println(history.size());

            for (AllowanceAdjustment a : history)
                writeAdjustment(out, a);
        }
    }

    // Restore the correct subclass of User.
    private User readUser(Scanner in) {
        String type = in.next();
        String id = readText(in);
        String name = readText(in);
        String password = readText(in);

        if ("A".equals(type)) {
            Administrator admin = new Administrator();

            if (!id.equals(admin.getUsername())
                    || !name.equals(admin.getName())
                    || !admin.passwordMatches(password))
                throw new IllegalArgumentException(
                        "Invalid administrator account.");

            return admin;
        }

        if (!"S".equals(type))
            throw new IllegalArgumentException("Invalid user type.");

        String email = readText(in);
        String phone = readText(in);

        // Student constructor follows UML order:
        // id, name, email, phone, password.
        Student student = new Student(id, name, email, phone, password);

        int count = readCount(in);

        for (int i = 0; i < count; i++)
            student.getAllowance().addAdjustment(
                    readAdjustment(in));

        return student;
    }

    private void writeRequest(PrintWriter out, AccessRequest request) {
        writeText(out, request.getUniversityId());
        writeText(out, request.getName());
        writeText(out, request.getEmail());
        writeText(out, request.getPhone());
        writeText(out, request.getChosenPassword());
        writeText(out, request.getStatus());
        writeText(out, request.getRejectionReason());
    }

    private AccessRequest readRequest(Scanner in) {
        AccessRequest request = new AccessRequest(
                readText(in), readText(in),
                readText(in), readText(in), readText(in));

        request.restoreReview(readText(in), readText(in));
        return request;
    }

    // Save a job and all its recorded execution details.
    private void writeJob(PrintWriter out, Job job) {
        out.println(job.getJobId());
        writeText(out, job.getOwnerId());
        writeText(out, job.getTitle());
        writeText(out, job.getApplicationName());
        writeText(out, job.getDescription());
        writeText(out, job.getTaskSize());

        out.println(job.getSubmissionMinute());
        writeText(out, job.getStatus());

        out.println(job.getGpuId());
        out.println(job.getStartMinute());
        out.println(job.getFinishMinute());
        out.println(job.getCancellationMinute());

        writeText(out, job.getCancelledBy());
        writeText(out, job.getCancellationReason());
        writeText(out, job.getResultSummary());
    }

    private Job readJob(Scanner in) {
        int id = in.nextInt();

        String owner = readText(in);
        String title = readText(in);
        String app = readText(in);
        String description = readText(in);
        String size = readText(in);

        int submitted = in.nextInt();

        Job job = new Job(
                id, owner, title, app, description, size, submitted);

        String status = readText(in);
        int gpu = in.nextInt();
        int start = in.nextInt();
        int finish = in.nextInt();
        int cancelled = in.nextInt();

        String actor = readText(in);
        String reason = readText(in);
        String result = readText(in);

        // Restore the recorded state without replaying past execution.
        job.restoreExecution(status, gpu, start, finish,
                cancelled, actor, reason, result);

        return job;
    }

    // Save the entire application state to a text file.
    public void save(GpuSystem system) throws IOException {
        if (system == null)
            throw new IllegalArgumentException("System required.");

        PrintWriter out = new PrintWriter(filePath);

        try {
            out.println("GPU_PROJECT_V4");

            // Save the clock and its initial date.
            SimulationClock clock = system.getClockForStorage();

            out.println(clock.isInitialized() ? 1 : 0);
            out.println(clock.getElapsedMinutes());

            if (clock.isInitialized()) {
                LocalDateTime date = clock.getInitialDateTime();

                out.println(date.getYear());
                out.println(date.getMonthValue());
                out.println(date.getDayOfMonth());
                out.println(date.getHour());
                out.println(date.getMinute());
            }

            out.println(system.getNextJobIdForStorage());

            // Save all approved users.
            ArrayList<User> users = system.getUsersForStorage();

            out.println(users.size());

            for (User user : users)
                writeUser(out, user);

            // Save pending, approved, and rejected requests.
            ArrayList<AccessRequest> requests =
                    system.getRequestsForStorage();

            out.println(requests.size());

            for (AccessRequest request : requests)
                writeRequest(out, request);

            // Save queued, running, completed, and cancelled jobs.
            ArrayList<Job> jobs = system.getJobsForStorage();

            out.println(jobs.size());

            for (Job job : jobs)
                writeJob(out, job);

            // Save the running job assigned to each GPU.
            GPU[] gpus = system.getGpusForStorage();

            out.println(gpus.length);

            for (GPU gpu : gpus) {
                Job running = gpu.getCurrentJob();
                out.println(
                        running == null ? -1 : running.getJobId());
            }

            out.println("END");

            // Detect errors that PrintWriter may otherwise hide.
            if (out.checkError())
                throw new IOException("Could not write save file.");

        } finally {
            out.close();
        }
    }

    // Load the saved application without advancing simulation time.
    public void loadInto(GpuSystem system) throws IOException {
        if (system == null)
            throw new IllegalArgumentException("System required.");

        Scanner in = new Scanner(new File(filePath));

        try {
            if (!"GPU_PROJECT_V4".equals(in.next()))
                throw new IllegalArgumentException(
                        "Unknown save format.");

            int ready = in.nextInt();
            int elapsed = in.nextInt();

            LocalDateTime initial = null;

            // Restore the initial date if the clock was initialized.
            if (ready == 1) {
                int year = in.nextInt();
                int month = in.nextInt();
                int day = in.nextInt();
                int hour = in.nextInt();
                int minute = in.nextInt();

                initial = LocalDateTime.of(
                        year, month, day, hour, minute);

            } else if (ready != 0) {
                throw new IllegalArgumentException(
                        "Invalid clock flag.");
            }

            int nextId = in.nextInt();

            // Restore all user accounts.
            ArrayList<User> users = new ArrayList<User>();

            int count = readCount(in);

            for (int i = 0; i < count; i++)
                users.add(readUser(in));

            // Restore registration requests.
            ArrayList<AccessRequest> requests =
                    new ArrayList<AccessRequest>();

            count = readCount(in);

            for (int i = 0; i < count; i++)
                requests.add(readRequest(in));

            // Restore job histories.
            ArrayList<Job> jobs = new ArrayList<Job>();

            count = readCount(in);

            for (int i = 0; i < count; i++)
                jobs.add(readJob(in));

            // Restore four GPU assignments.
            if (in.nextInt() != 4)
                throw new IllegalArgumentException(
                        "Four GPUs required.");

            int[] gpuJobIds = new int[4];

            for (int i = 0; i < 4; i++)
                gpuJobIds[i] = in.nextInt();

            if (!"END".equals(in.next()))
                throw new IllegalArgumentException(
                        "Missing END marker.");

            if (in.hasNext())
                throw new IllegalArgumentException(
                        "Unexpected extra data.");

            // GpuSystem validates the loaded data before replacing
            // the application's existing objects.
            system.restoreData(users, requests, jobs, gpuJobIds,
                    initial, elapsed, ready == 1, nextId);

        } catch (RuntimeException e) {
            throw new IOException(
                    "Invalid save file: " + e.getMessage());

        } finally {
            in.close();
        }
    }
}
