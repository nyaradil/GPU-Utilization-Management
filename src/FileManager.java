
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

// Saves and restores the system's data using text files.
public class FileManager {

    private String fileName;

    // Default Constructor
    public FileManager() {
        this("gpu_data.txt");
    }

    // Full Constructor
    public FileManager(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    // Store character codes so text can contain spaces and newlines.
    private void writeText(PrintWriter out, String value) {
        if (value == null) value = "";

        out.println(value.length());

        for (int i = 0; i < value.length(); i++) {
            out.println((int) value.charAt(i));
        }
    }

    private String readText(Scanner in) {
        int length = in.nextInt();

        if (length < 0 || length > 100000) {
            throw new IllegalArgumentException("Invalid text length.");
        }

        String value = "";

        for (int i = 0; i < length; i++) {
            int code = in.nextInt();

            if (code < 0 || code > 65535) {
                throw new IllegalArgumentException("Invalid character code.");
            }

            value += (char) code;
        }

        return value;
    }

    private int readCount(Scanner in) {
        int count = in.nextInt();

        if (count < 0 || count > 100000) {
            throw new IllegalArgumentException("Invalid record count.");
        }

        return count;
    }

    // Save all accounts, jobs, balances, GPUs and clock information.
    public void save(Storage data) throws FileNotFoundException {
        if (data == null || fileName == null || fileName.length() == 0) {
            throw new IllegalArgumentException("Missing storage or file name.");
        }

        validate(data);

        PrintWriter out = new PrintWriter(fileName);

        try {
            out.println("GPU_PROJECT_V2");

            SimulationClock clock = data.getClock();

            out.println(clock.isInitialized() ? 1 : 0);
            out.println(clock.getDay());
            out.println(clock.getMonth());
            out.println(clock.getYear());
            out.println(clock.getHour());
            out.println(clock.getMinute());
            out.println(clock.getElapsedMinutes());

            // Save approved students and their allowances.
            out.println(data.getStudents().size());

            for (Student student : data.getStudents()) {
                writeText(out, student.getUsername());
                writeText(out, student.getName());
                writeText(out, student.getPasswordForStorage());
                writeText(out, student.getPhone());
                writeText(out, student.getEmail());

                TimeAllowance a = student.getAllowance();

                out.println(a.getAllocated());
                out.println(a.getUsed());
                out.println(a.getReserved());
            }

            // Save all registration requests and review decisions.
            out.println(data.getRequests().size());

            for (AccessRequest request : data.getRequests()) {
                writeText(out, request.getUniversityId());
                writeText(out, request.getName());
                writeText(out, request.getEmail());
                writeText(out, request.getPhone());
                writeText(out, request.getChosenPassword());
                writeText(out, request.getStatus());
                writeText(out, request.getRejectionReason());
            }

            // Save the allowance adjustment history.
            out.println(data.getAdjustments().size());

            for (String[] adjustment : data.getAdjustments()) {
                for (int i = 0; i < 5; i++) {
                    writeText(out, adjustment[i]);
                }
            }

            // Save all jobs, including completed and cancelled jobs.
            out.println(data.getJobs().size());

            for (Job job : data.getJobs()) {
                out.println(job.getJobId());
                writeText(out, job.getStudentId());
                writeText(out, job.getTitle());
                writeText(out, job.getApplicationName());
                writeText(out, job.getDescription());
                writeText(out, job.getTaskSize());

                out.println(job.getSubmissionTime());
                writeText(out, job.getStatus());

                out.println(job.getAssignedGpuId());
                out.println(job.getStartTime());
                out.println(job.getFinishTime());
                out.println(job.getCancellationTime());

                writeText(out, job.getCancelledBy());
                writeText(out, job.getCancellationReason());
                writeText(out, job.getResult());
            }

            // Save current assignments for the four GPUs.
            out.println(4);

            for (GPU gpu : data.getGpus()) {
                out.println(gpu.getGpuId());

                Job job = gpu.getCurrentJob();
                out.println(job == null ? -1 : job.getJobId());
            }

            out.println("END");

        } finally {
            out.close();
        }
    }

    // Load the saved simulation without advancing simulated time.
    public Storage load() throws FileNotFoundException {
        Scanner in = new Scanner(new File(fileName));

        try {
            if (!in.next().equals("GPU_PROJECT_V2")) {
                throw new IllegalArgumentException("Unknown file format.");
            }

            Storage data = new Storage();

            // Restore the initial date and elapsed simulation time.
            int initialized = in.nextInt();

            int day = in.nextInt();
            int month = in.nextInt();
            int year = in.nextInt();
            int hour = in.nextInt();
            int minute = in.nextInt();
            int elapsed = in.nextInt();

            if ((initialized != 0 && initialized != 1) || elapsed < 0) {
                throw new IllegalArgumentException("Invalid clock data.");
            }

            data.setClock(new SimulationClock(
                    day, month, year, hour, minute,
                    elapsed, initialized == 1));

            // Restore approved students.
            int count = readCount(in);

            for (int i = 0; i < count; i++) {
                String id = readText(in);
                String name = readText(in);
                String password = readText(in);
                String phone = readText(in);
                String email = readText(in);

                int allocated = in.nextInt();
                int used = in.nextInt();
                int reserved = in.nextInt();

                Student student = new Student(
                        id, name, password, phone, email);

                student.setAllowance(
                        new TimeAllowance(allocated, used, reserved));

                data.getStudents().add(student);
            }

            // Restore registration requests.
            count = readCount(in);

            for (int i = 0; i < count; i++) {
                AccessRequest request = new AccessRequest(
                        readText(in), readText(in), readText(in),
                        readText(in), readText(in));

                String status = readText(in);
                String reason = readText(in);

                request.restoreReview(status, reason);
                data.getRequests().add(request);
            }

            // Restore allowance adjustment history.
            count = readCount(in);

            for (int i = 0; i < count; i++) {
                String[] adjustment = new String[5];

                for (int j = 0; j < 5; j++) {
                    adjustment[j] = readText(in);
                }

                data.getAdjustments().add(adjustment);
            }

            // Restore jobs and their execution histories.
            count = readCount(in);

            for (int i = 0; i < count; i++) {
                int id = in.nextInt();

                String owner = readText(in);
                String title = readText(in);
                String app = readText(in);
                String description = readText(in);
                String size = readText(in);

                int submitted = in.nextInt();
                String status = readText(in);

                int gpuId = in.nextInt();
                int start = in.nextInt();
                int finish = in.nextInt();
                int cancelled = in.nextInt();

                String by = readText(in);
                String reason = readText(in);
                String result = readText(in);

                Job job = new Job(id, owner, title,
                        app, description, size, submitted);

                if (status.equals("Running")
                        || status.equals("Completed")
                        || (status.equals("Cancelled") && start >= 0)) {
                    job.start(gpuId, start);
                }

                if (status.equals("Completed")) {
                    job.complete(finish);
                } else if (status.equals("Cancelled")) {
                    job.cancel(cancelled, by, reason);
                } else if (!status.equals("Queued")
                        && !status.equals("Running")) {
                    throw new IllegalArgumentException("Invalid job status.");
                }

                // Verify that the restored history matches the saved data.
                if (!status.equals(job.getStatus())
                        || job.getAssignedGpuId() != gpuId
                        || job.getStartTime() != start
                        || job.getFinishTime() != finish
                        || job.getCancellationTime() != cancelled) {
                    throw new IllegalArgumentException("Invalid job history.");
                }

                // Job.java must provide a result-restoration setter.
                job.setResult(result);

                data.addJob(job);
            }

            // Restore GPU assignments.
            if (in.nextInt() != 4) {
                throw new IllegalArgumentException("Four GPUs required.");
            }

            for (int i = 0; i < 4; i++) {
                int gpuId = in.nextInt();
                int runningId = in.nextInt();

                if (gpuId != i + 1) {
                    throw new IllegalArgumentException("Invalid GPU ID.");
                }

                if (runningId != -1) {
                    Job job = data.findJob(runningId);

                    if (job == null
                            || !job.getStatus().equals("Running")
                            || job.getAssignedGpuId() != gpuId) {
                        throw new IllegalArgumentException(
                                "Invalid GPU assignment.");
                    }

                    data.getGpus()[i].assignJob(job);
                }
            }

            if (!in.next().equals("END")) {
                throw new IllegalArgumentException("Missing END marker.");
            }

            validate(data);
            return data;

        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Invalid save file: " + e.getMessage());
        } finally {
            in.close();
        }
    }

    // Check important relationships before saving or loading data.
    private void validate(Storage data) {
        SimulationClock clock = data.getClock();

        if (clock == null || data.getGpus() == null
                || data.getGpus().length != 4) {
            throw new IllegalArgumentException("Missing clock or GPUs.");
        }

        if (clock.getElapsedMinutes() < 0
                || (!clock.isInitialized()
                && (clock.getElapsedMinutes() != 0
                || data.getJobs().size() != 0))) {
            throw new IllegalArgumentException("Invalid simulation clock.");
        }

        // Validate the initial calendar date.
        if (clock.isInitialized()) {
            SimulationClock check = new SimulationClock();

            if (!check.initialize(
                    clock.getDay(), clock.getMonth(), clock.getYear(),
                    clock.getHour(), clock.getMinute())) {
                throw new IllegalArgumentException(
                        "Invalid initial date or time.");
            }
        }

        // Check student IDs and allowance balances.
        for (int i = 0; i < data.getStudents().size(); i++) {
            Student student = data.getStudents().get(i);

            if (student == null || student.getAllowance() == null) {
                throw new IllegalArgumentException("Invalid student.");
            }

            for (int j = 0; j < i; j++) {
                if (student.getUsername().equals(
                        data.getStudents().get(j).getUsername())) {
                    throw new IllegalArgumentException("Duplicate student ID.");
                }
            }

            long used = 0;
            long reserved = 0;

            for (Job job : data.getJobs()) {
                if (!job.getStudentId().equals(student.getUsername())) {
                    continue;
                }

                if (job.getStatus().equals("Queued")
                        || job.getStatus().equals("Running")) {
                    reserved += job.getEstimatedDuration();
                } else if (job.getStartTime() >= 0) {
                    used += (long) job.getFinishTime() - job.getStartTime();
                }
            }

            TimeAllowance a = student.getAllowance();

            if (a.getAllocated() < 0
                    || used != a.getUsed()
                    || reserved != a.getReserved()
                    || used < 0 || reserved < 0
                    || used + reserved > a.getAllocated()) {
                throw new IllegalArgumentException("Invalid allowance balance.");
            }
        }

        // Check registration requests.
        for (int i = 0; i < data.getRequests().size(); i++) {
            AccessRequest r = data.getRequests().get(i);

            for (int j = 0; j < i; j++) {
                if (r.getUniversityId().equals(
                        data.getRequests().get(j).getUniversityId())) {
                    throw new IllegalArgumentException("Duplicate request ID.");
                }
            }

            if (r.getStatus().equals("Approved")
                    != (data.findStudent(r.getUniversityId()) != null)) {
                throw new IllegalArgumentException("Request/account mismatch.");
            }
        }

        for (Student s : data.getStudents()) {
            AccessRequest request = data.findRequest(s.getUsername());

            if (request == null
                    || !request.getStatus().equals("Approved")) {
                throw new IllegalArgumentException("Approved request missing.");
            }
        }

        // Validate job IDs, durations, statuses and execution times.
        for (int i = 0; i < data.getJobs().size(); i++) {
            Job job = data.getJobs().get(i);

            if (job.getJobId() <= 0
                    || data.findStudent(job.getStudentId()) == null
                    || job.getSubmissionTime() < 0
                    || job.getSubmissionTime() > clock.getElapsedMinutes()) {
                throw new IllegalArgumentException("Invalid job record.");
            }

            for (int j = 0; j < i; j++) {
                if (job.getJobId() == data.getJobs().get(j).getJobId()) {
                    throw new IllegalArgumentException("Duplicate job ID.");
                }
            }

            int duration = job.getEstimatedDuration();

            if (!(job.getTaskSize().equals("Small") && duration == 5)
                    && !(job.getTaskSize().equals("Medium") && duration == 15)
                    && !(job.getTaskSize().equals("Large") && duration == 30)
                    && !(job.getTaskSize().equals("Very large") && duration == 60)) {
                throw new IllegalArgumentException(
                        "Invalid job size or duration.");
            }

            String status = job.getStatus();
            int start = job.getStartTime();
            int finish = job.getFinishTime();
            int now = clock.getElapsedMinutes();

            if (status.equals("Queued")) {
                if (start != -1 || finish != -1
                        || job.getAssignedGpuId() != -1) {
                    throw new IllegalArgumentException(
                            "Queued job has execution details.");
                }
            } else if (status.equals("Running")) {
                if (start < job.getSubmissionTime() || start > now
                        || now - start >= duration || finish != -1
                        || job.getAssignedGpuId() < 1
                        || job.getAssignedGpuId() > 4) {
                    throw new IllegalArgumentException("Invalid running job.");
                }

                if (data.getGpus()[job.getAssignedGpuId() - 1]
                        .getCurrentJob() != job) {
                    throw new IllegalArgumentException(
                            "Running job not on its GPU.");
                }
            } else if (status.equals("Completed")) {
                if (start < job.getSubmissionTime()
                        || finish - start != duration
                        || finish > now
                        || job.getAssignedGpuId() < 1
                        || job.getAssignedGpuId() > 4) {
                    throw new IllegalArgumentException(
                            "Invalid completed job.");
                }
            } else if (status.equals("Cancelled")) {
                if (job.getCancellationTime() < job.getSubmissionTime()
                        || job.getCancellationTime() > now
                        || job.getCancelledBy().length() == 0
                        || job.getCancellationReason().length() == 0) {
                    throw new IllegalArgumentException(
                            "Invalid cancellation.");
                }

                if (start == -1) {
                    if (finish != -1 || job.getAssignedGpuId() != -1) {
                        throw new IllegalArgumentException(
                                "Queued cancellation has GPU data.");
                    }
                } else if (start < job.getSubmissionTime()
                        || finish != job.getCancellationTime()
                        || finish - start >= duration
                        || finish < start
                        || job.getAssignedGpuId() < 1
                        || job.getAssignedGpuId() > 4) {
                    throw new IllegalArgumentException(
                            "Invalid running cancellation.");
                }
            } else {
                throw new IllegalArgumentException("Unknown job status.");
            }
        }

        // Check the four GPUs and their current jobs.
        for (int i = 0; i < 4; i++) {
            GPU gpu = data.getGpus()[i];

            if (gpu == null || gpu.getGpuId() != i + 1) {
                throw new IllegalArgumentException(
                        "GPU IDs must be 1 to 4.");
            }

            Job running = gpu.getCurrentJob();

            if (running != null
                    && (!running.getStatus().equals("Running")
                    || running.getAssignedGpuId() != i + 1)) {
                throw new IllegalArgumentException(
                        "GPU state does not match job.");
            }

            if (running == null) {
                for (Job job : data.getJobs()) {
                    if (job.getStatus().equals("Queued")) {
                        throw new IllegalArgumentException(
                                "Queued job while a GPU is free.");
                    }
                }
            }
        }

        // Check saved allowance adjustment records.
        for (String[] a : data.getAdjustments()) {
            if (a == null || a.length != 5
                    || data.findStudent(a[0]) == null) {
                throw new IllegalArgumentException(
                        "Invalid adjustment record.");
            }

            Integer.parseInt(a[1]);
            Integer.parseInt(a[4]);
        }
    }
}
