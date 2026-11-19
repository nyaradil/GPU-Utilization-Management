
import java.io.FileNotFoundException;

// Tests Storage, GpuSystem and FileManager without the Swing interface.
public class TestDriver3 {

    private static int passed = 0;
    private static int failed = 0;

    // Display whether each test passed or failed.
    private static void check(String name, boolean result) {
        if (result) {
            System.out.println("PASS: " + name);
            passed++;
        } else {
            System.out.println("FAIL: " + name);
            failed++;
        }
    }

    public static void main(String[] args) {

        GpuSystem system = new GpuSystem();

        // TEST 1: Student registration and login.
        check("Reject incorrect admin password",
                !system.login("admin", "wrong"));

        boolean registered = true;

        // Register five students.
        for (int i = 1; i <= 5; i++) {
            boolean ok = system.registerStudent(
                    "100" + i,
                    "Student " + i,
                    "student" + i + "@ku.ac.ae",
                    "050000000" + i,
                    "pass" + i);

            if (!ok) {
                registered = false;
            }
        }

        check("Register five students", registered);

        // Check duplicate registration.
        check("Reject duplicate university ID",
                !system.registerStudent(
                        "1001", "Duplicate",
                        "duplicate@ku.ac.ae",
                        "0501234567", "pass"));

        // Students cannot log in before approval.
        check("Unapproved student cannot log in",
                !system.login("1001", "pass1"));

        check("Administrator can log in",
                system.login("admin", "gpu2026"));

        // TEST 2: Clock and student approval.
        check("Initialize simulation clock",
                system.initializeClock(18, 11, 2026, 10, 0));

        boolean approved = true;

        // Approve all five students.
        for (int i = 1; i <= 5; i++) {
            if (!system.approveRequest("100" + i)) {
                approved = false;
            }
        }

        check("Approve five students", approved);

        check("Prevent repeated approval",
                !system.approveRequest("1001"));

        check("Five students appear in admin records",
                system.getStudentRows().size() == 5);

        // Test granting extra GPU minutes.
        check("Grant extra GPU minutes",
                system.grantExtraTime(
                        "1001", 10, "Test allocation"));

        check("Student allocation updated",
                system.getStudentRows().get(0)[4].equals("110"));

        // Administrators cannot submit student jobs.
        check("Administrator cannot submit student jobs",
                system.submitJob(
                        "Wrong role",
                        "TestApp",
                        "Should be rejected",
                        "Small") == -1);

        system.logout();

        // TEST 3: Submit 20 jobs.
        int accepted = 0;
        boolean studentLogins = true;

        for (int i = 1; i <= 5; i++) {

            if (!system.login("100" + i, "pass" + i)) {
                studentLogins = false;
                continue;
            }

            // Each student submits four small jobs.
            for (int j = 1; j <= 4; j++) {

                int jobId = system.submitJob(
                        "Job " + i + "-" + j,
                        "TestApp",
                        "Small simulated job",
                        "Small");

                if (jobId > 0) {
                    accepted++;
                }
            }

            system.logout();
        }

        check("All five students can log in",
                studentLogins);

        check("Twenty jobs accepted",
                accepted == 20);

        check("Administrator can log in again",
                system.login("admin", "gpu2026"));

        // Check GPU scheduling.
        int[] counts = system.getJobCounts();

        check("Four running and sixteen queued",
                counts[1] == 4 && counts[0] == 16);

        check("Four GPUs are busy",
                system.getBusyGpuCount() == 4);

        // Check FCFS queue order.
        check("Fifth job is first in queue",
                system.getQueuePosition(5) == 1);

        check("Fifth job starts at simulated minute five",
                system.getPredictedStart(5) == 5);

        // Utilization is undefined at simulation minute zero.
        check("Utilization is N/A at minute zero",
                system.getUtilization() == -1);

        // TEST 4: Job cancellation.
        // Queued cancellation should not charge GPU minutes.
        check("Cancel queued job",
                system.cancelJob(
                        20, "Test queued cancellation"));

        check("Queued cancellation has no charge",
                system.getChargedMinutes(20) == 0);

        // Advance the simulation by two minutes.
        check("Advance first minute",
                system.advanceMinute());

        check("Advance second minute",
                system.advanceMinute());

        check("All GPUs were busy for first two minutes",
                system.getUtilization() == 100.0);

        // Running cancellation should charge elapsed runtime.
        check("Cancel running job",
                system.cancelJob(
                        1, "Test running cancellation"));

        check("Running cancellation charges two minutes",
                system.getChargedMinutes(1) == 2);

        // TEST 5: Job completion.
        boolean advanced = true;

        // Advance enough minutes to finish the remaining jobs.
        for (int i = 0; i < 50; i++) {
            if (!system.advanceMinute()) {
                advanced = false;
            }
        }

        check("Simulation continues successfully",
                advanced);

        counts = system.getJobCounts();

        check("Eighteen completed and two cancelled",
                counts[2] == 18 && counts[3] == 2);

        check("No jobs remain queued or running",
                counts[0] == 0 && counts[1] == 0);

        check("All GPUs are free",
                system.getBusyGpuCount() == 0);

        // TEST 6: FileManager save and load.
        // Use a separate test file to protect the application's saved data.
        try {

            Storage sample = new Storage();

            sample.getClock().initialize(
                    18, 11, 2026, 10, 0);

            // Create an approved registration request.
            AccessRequest request = new AccessRequest(
                    "9001",
                    "File Test",
                    "filetest@ku.ac.ae",
                    "0501234567",
                    "pass");

            request.markApproved();
            sample.getRequests().add(request);

            // Create an approved student with reserved minutes.
            Student student = new Student(
                    "9001",
                    "File Test",
                    "pass",
                    "0501234567",
                    "filetest@ku.ac.ae");

            student.setAllowance(
                    new TimeAllowance(100, 0, 5));

            sample.getStudents().add(student);

            // Create a running job.
            Job job = new Job(
                    1,
                    "9001",
                    "Save Test",
                    "TestApp",
                    "Running job",
                    "Small",
                    0);

            job.start(1, 0);

            sample.addJob(job);
            sample.getGpus()[0].assignJob(job);

            // Save and reload the simulation.
            FileManager manager = new FileManager(
                    "test_driver3_data.txt");

            manager.save(sample);

            Storage loaded = manager.load();

            // Verify that the data was restored.
            check("Restore approved student",
                    loaded.findStudent("9001") != null);

            check("Restore running job",
                    loaded.findJob(1) != null
                    && loaded.findJob(1).getStatus().equals("Running"));

            check("Restore GPU assignment",
                    loaded.getGpus()[0].getCurrentJob() != null);

            check("Restore reserved minutes",
                    loaded.findStudent("9001")
                    .getAllowance().getReserved() == 5);

        } catch (FileNotFoundException e) {

            check("Save and load test file", false);
            System.out.println(e.getMessage());

        } catch (RuntimeException e) {

            check("Save and load valid data", false);
            System.out.println(e.getMessage());
        }

        // Display the final testing results.
        System.out.println();
        System.out.println("Tests passed: " + passed);
        System.out.println("Tests failed: " + failed);
    }
}
