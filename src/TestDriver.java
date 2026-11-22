import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class TestDriver {

    // Run separately from Main. All sample preparation is inside this method.
    public static void main(String[] args) throws IOException {

        String fileName = "sample_gpu_state.txt";
        if (new File(fileName).exists()) {
            System.out.println("Move the existing " + fileName + " before running again.");
            return;
        }

        GpuSystem system = new GpuSystem(new FileManager(fileName));

        System.out.println("=== REGISTER STUDENTS ===");

        // 1. Register five students, one pending request, and one rejected example.
        system.registerStudent("59001", "AA", "aa@email.com", "1234", "aa59001");
        System.out.println("Registered AA / 59001 - request pending.");
        system.registerStudent("59002", "AB", "ab@email.com", "1234", "ab59002");
        System.out.println("Registered AB / 59002 - request pending.");
        system.registerStudent("59003", "AC", "ac@email.com", "1234", "ac59003");
        System.out.println("Registered AC / 59003 - request pending.");
        system.registerStudent("59004", "AD", "ad@email.com", "1234", "ad59004");
        System.out.println("Registered AD / 59004 - request pending.");
        system.registerStudent("59005", "AE", "ae@email.com", "1234", "ae59005");
        System.out.println("Registered AE / 59005 - request pending.");
        system.registerStudent("59006", "AF", "af@email.com", "1234", "af59006");
        System.out.println("Registered AF / 59006 - request pending.");
        system.registerStudent("59007", "AG", "ag@email.com", "1234", "ag59007");
        System.out.println("Registered AG / 59007 - request pending.");

        System.out.println("\n=== APPROVE ACCOUNTS ===");

        // 2. Approval creates the Student objects and records their allowances.
        system.login("admin", "gpu2026");
        system.approveRequest("59001", 100);
        System.out.println("Approved 59001 with 100 GPU minutes.");
        system.approveRequest("59002", 120);
        System.out.println("Approved 59002 with 120 GPU minutes.");
        system.approveRequest("59003", 100);
        System.out.println("Approved 59003 with 100 GPU minutes.");
        system.approveRequest("59004", 100);
        System.out.println("Approved 59004 with 100 GPU minutes.");
        system.approveRequest("59005", 100);
        System.out.println("Approved 59005 with 100 GPU minutes.");
        system.rejectRequest("59007", "Demonstration: incomplete university verification");
        System.out.println("Rejected AG / 59007: incomplete university verification.");
        System.out.println("AF / 59006 remains pending.");
        // AF / 59006 stays pending.

        // References to the actual approved students owned by the system.
        Student student1 = system.findStudent("59001");
        Student student2 = system.findStudent("59002");
        Student student3 = system.findStudent("59003");
        Student student4 = system.findStudent("59004");
        Student student5 = system.findStudent("59005");

        system.initializeClock(LocalDateTime.of(2026, 11, 22, 9, 0));
        System.out.println("\nClock initialized: " + system.getCurrentTimeText());
        System.out.println("Utilization: N/A at time zero.");
        system.setAllocatedMinutes("59003", 80,
                "Boundary test: four planned jobs total exactly 80 minutes");
        System.out.println("Adjusted AC / 59003 allocation to "
                + system.getBalance("59003")[0] + " minutes. Reason recorded.");
        system.logout();

        System.out.println("\n=== SUBMIT JOBS ===");

        // 3. First Small job for each student: jobs 1-5.
        // Jobs 1-4 immediately occupy GPUs 1-4; job 5 waits.
        system.login(student1.getUsername(), "aa59001");
        Job job1 = system.submitJob("AA-Job-1", "Python",
                "Demonstration job 1 for AA.\nSimulated GPU workload.", "Small");
        System.out.println("Created job " + job1.getJobId()
                + " | Student: " + job1.getStudentId()
                + " | Size: " + job1.getTaskSize()
                + " | Duration: " + job1.getEstimatedDuration() + " minutes"
                + " | Status: " + job1.getStatus()
                + " | GPU: " + (job1.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job1.getAssignedGpuId()));
        system.logout();

        system.login(student2.getUsername(), "ab59002");
        Job job2 = system.submitJob("AB-Job-1", "Python",
                "Demonstration job 1 for AB.\nSimulated GPU workload.", "Small");
        System.out.println("Created job " + job2.getJobId()
                + " | Student: " + job2.getStudentId()
                + " | Size: " + job2.getTaskSize()
                + " | Duration: " + job2.getEstimatedDuration() + " minutes"
                + " | Status: " + job2.getStatus()
                + " | GPU: " + (job2.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job2.getAssignedGpuId()));
        system.logout();

        system.login(student3.getUsername(), "ac59003");
        Job job3 = system.submitJob("AC-Job-1", "Python",
                "Demonstration job 1 for AC.\nSimulated GPU workload.", "Small");
        System.out.println("Created job " + job3.getJobId()
                + " | Student: " + job3.getStudentId()
                + " | Size: " + job3.getTaskSize()
                + " | Duration: " + job3.getEstimatedDuration() + " minutes"
                + " | Status: " + job3.getStatus()
                + " | GPU: " + (job3.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job3.getAssignedGpuId()));
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        Job job4 = system.submitJob("AD-Job-1", "Python",
                "Demonstration job 1 for AD.\nSimulated GPU workload.", "Small");
        System.out.println("Created job " + job4.getJobId()
                + " | Student: " + job4.getStudentId()
                + " | Size: " + job4.getTaskSize()
                + " | Duration: " + job4.getEstimatedDuration() + " minutes"
                + " | Status: " + job4.getStatus()
                + " | GPU: " + (job4.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job4.getAssignedGpuId()));
        system.logout();

        system.login(student5.getUsername(), "ae59005");
        Job job5 = system.submitJob("AE-Job-1", "Python",
                "Demonstration job 1 for AE.\nSimulated GPU workload.", "Small");
        System.out.println("Created job " + job5.getJobId()
                + " | Student: " + job5.getStudentId()
                + " | Size: " + job5.getTaskSize()
                + " | Duration: " + job5.getEstimatedDuration() + " minutes"
                + " | Status: " + job5.getStatus()
                + " | GPU: " + (job5.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job5.getAssignedGpuId()));
        system.logout();

        // 4. Second job for each student, Medium: jobs 6-10.
        system.login(student1.getUsername(), "aa59001");
        Job job6 = system.submitJob("AA-Job-2", "Python",
                "Demonstration job 2 for AA.\nSimulated GPU workload.", "Medium");
        System.out.println("Created job " + job6.getJobId()
                + " | Student: " + job6.getStudentId()
                + " | Size: " + job6.getTaskSize()
                + " | Duration: " + job6.getEstimatedDuration() + " minutes"
                + " | Status: " + job6.getStatus()
                + " | GPU: " + (job6.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job6.getAssignedGpuId()));
        system.logout();

        system.login(student2.getUsername(), "ab59002");
        Job job7 = system.submitJob("AB-Job-2", "Python",
                "Demonstration job 2 for AB.\nSimulated GPU workload.", "Medium");
        System.out.println("Created job " + job7.getJobId()
                + " | Student: " + job7.getStudentId()
                + " | Size: " + job7.getTaskSize()
                + " | Duration: " + job7.getEstimatedDuration() + " minutes"
                + " | Status: " + job7.getStatus()
                + " | GPU: " + (job7.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job7.getAssignedGpuId()));
        system.logout();

        system.login(student3.getUsername(), "ac59003");
        Job job8 = system.submitJob("AC-Job-2", "Python",
                "Demonstration job 2 for AC.\nSimulated GPU workload.", "Medium");
        System.out.println("Created job " + job8.getJobId()
                + " | Student: " + job8.getStudentId()
                + " | Size: " + job8.getTaskSize()
                + " | Duration: " + job8.getEstimatedDuration() + " minutes"
                + " | Status: " + job8.getStatus()
                + " | GPU: " + (job8.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job8.getAssignedGpuId()));
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        Job job9 = system.submitJob("AD-Job-2", "Python",
                "Demonstration job 2 for AD.\nSimulated GPU workload.", "Medium");
        System.out.println("Created job " + job9.getJobId()
                + " | Student: " + job9.getStudentId()
                + " | Size: " + job9.getTaskSize()
                + " | Duration: " + job9.getEstimatedDuration() + " minutes"
                + " | Status: " + job9.getStatus()
                + " | GPU: " + (job9.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job9.getAssignedGpuId()));
        system.logout();

        system.login(student5.getUsername(), "ae59005");
        Job job10 = system.submitJob("AE-Job-2", "Python",
                "Demonstration job 2 for AE.\nSimulated GPU workload.", "Medium");
        System.out.println("Created job " + job10.getJobId()
                + " | Student: " + job10.getStudentId()
                + " | Size: " + job10.getTaskSize()
                + " | Duration: " + job10.getEstimatedDuration() + " minutes"
                + " | Status: " + job10.getStatus()
                + " | GPU: " + (job10.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job10.getAssignedGpuId()));
        system.logout();

        // 5. Third job for each student, Large: jobs 11-15.
        system.login(student1.getUsername(), "aa59001");
        Job job11 = system.submitJob("AA-Job-3", "Python",
                "Demonstration job 3 for AA.\nSimulated GPU workload.", "Large");
        System.out.println("Created job " + job11.getJobId()
                + " | Student: " + job11.getStudentId()
                + " | Size: " + job11.getTaskSize()
                + " | Duration: " + job11.getEstimatedDuration() + " minutes"
                + " | Status: " + job11.getStatus()
                + " | GPU: " + (job11.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job11.getAssignedGpuId()));
        system.logout();

        system.login(student2.getUsername(), "ab59002");
        Job job12 = system.submitJob("AB-Job-3", "Python",
                "Demonstration job 3 for AB.\nSimulated GPU workload.", "Large");
        System.out.println("Created job " + job12.getJobId()
                + " | Student: " + job12.getStudentId()
                + " | Size: " + job12.getTaskSize()
                + " | Duration: " + job12.getEstimatedDuration() + " minutes"
                + " | Status: " + job12.getStatus()
                + " | GPU: " + (job12.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job12.getAssignedGpuId()));
        system.logout();

        system.login(student3.getUsername(), "ac59003");
        Job job13 = system.submitJob("AC-Job-3", "Python",
                "Demonstration job 3 for AC.\nSimulated GPU workload.", "Large");
        System.out.println("Created job " + job13.getJobId()
                + " | Student: " + job13.getStudentId()
                + " | Size: " + job13.getTaskSize()
                + " | Duration: " + job13.getEstimatedDuration() + " minutes"
                + " | Status: " + job13.getStatus()
                + " | GPU: " + (job13.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job13.getAssignedGpuId()));
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        Job job14 = system.submitJob("AD-Job-3", "Python",
                "Demonstration job 3 for AD.\nSimulated GPU workload.", "Large");
        System.out.println("Created job " + job14.getJobId()
                + " | Student: " + job14.getStudentId()
                + " | Size: " + job14.getTaskSize()
                + " | Duration: " + job14.getEstimatedDuration() + " minutes"
                + " | Status: " + job14.getStatus()
                + " | GPU: " + (job14.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job14.getAssignedGpuId()));
        system.logout();

        system.login(student5.getUsername(), "ae59005");
        Job job15 = system.submitJob("AE-Job-3", "Python",
                "Demonstration job 3 for AE.\nSimulated GPU workload.", "Large");
        System.out.println("Created job " + job15.getJobId()
                + " | Student: " + job15.getStudentId()
                + " | Size: " + job15.getTaskSize()
                + " | Duration: " + job15.getEstimatedDuration() + " minutes"
                + " | Status: " + job15.getStatus()
                + " | GPU: " + (job15.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job15.getAssignedGpuId()));
        system.logout();

        // Rejected attempts do not add jobs or consume job IDs.
        System.out.println("\n=== OVER-LIMIT SUBMISSION TEST ===");
        system.login(student1.getUsername(), "aa59001");
        System.out.println("AA available: " + system.getBalance("59001")[3]
                + " minutes (expected 50). Attempting a 60-minute job.");
        try {
            system.submitJob("Over-limit test", "Python",
                    "This 60-minute job should be rejected", "Very large");
            throw new AssertionError("FAIL: Over-limit submission was accepted.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Rejected as expected: " + ex.getMessage());
        }
        System.out.println("AA available after rejection: " + system.getBalance("59001")[3]
                + " minutes (expected 50, unchanged).");
        system.logout();

        // 6. AA needs 60 available for the fourth job, but currently has only 50.
        system.login("admin", "gpu2026");
        system.grantMinutes("59001", 10,
                "AA needs 110 total minutes for the four demonstration jobs");
        System.out.println("Granted AA 10 extra minutes. Reason recorded.");
        System.out.println("AA allocated: " + system.getBalance("59001")[0]
                + " (expected 110) | Available: " + system.getBalance("59001")[3]
                + " (expected 60).");
        system.logout();

        // Fourth jobs: Very large for AA/AB, Large for AC/AD/AE. Jobs 16-20.
        system.login(student1.getUsername(), "aa59001");
        Job job16 = system.submitJob("AA-Job-4", "Python",
                "Demonstration job 4 for AA.\nSimulated GPU workload.", "Very large");
        System.out.println("Created job " + job16.getJobId()
                + " | Student: " + job16.getStudentId()
                + " | Size: " + job16.getTaskSize()
                + " | Duration: " + job16.getEstimatedDuration() + " minutes"
                + " | Status: " + job16.getStatus()
                + " | GPU: " + (job16.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job16.getAssignedGpuId()));
        system.logout();

        system.login(student2.getUsername(), "ab59002");
        Job job17 = system.submitJob("AB-Job-4", "Python",
                "Demonstration job 4 for AB.\nSimulated GPU workload.", "Very large");
        System.out.println("Created job " + job17.getJobId()
                + " | Student: " + job17.getStudentId()
                + " | Size: " + job17.getTaskSize()
                + " | Duration: " + job17.getEstimatedDuration() + " minutes"
                + " | Status: " + job17.getStatus()
                + " | GPU: " + (job17.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job17.getAssignedGpuId()));
        system.logout();

        system.login(student3.getUsername(), "ac59003");
        Job job18 = system.submitJob("AC-Job-4", "Python",
                "Demonstration job 4 for AC.\nSimulated GPU workload.", "Large");
        System.out.println("Created job " + job18.getJobId()
                + " | Student: " + job18.getStudentId()
                + " | Size: " + job18.getTaskSize()
                + " | Duration: " + job18.getEstimatedDuration() + " minutes"
                + " | Status: " + job18.getStatus()
                + " | GPU: " + (job18.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job18.getAssignedGpuId()));
        // This is an exact allowance match: AC now has 0 available minutes.
        System.out.println("AC available after exact-match job: " + system.getBalance("59003")[3]
                + " minutes (expected 0).");
        try {
            system.submitJob("Zero-allowance test", "Python",
                    "This new job should be rejected at zero available", "Small");
            throw new AssertionError("FAIL: Submission at zero available was accepted.");
        } catch (IllegalArgumentException ex) {
            System.out.println("Zero-allowance submission rejected as expected: " + ex.getMessage());
        }
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        Job job19 = system.submitJob("AD-Job-4", "Python",
                "Demonstration job 4 for AD.\nSimulated GPU workload.", "Large");
        System.out.println("Created job " + job19.getJobId()
                + " | Student: " + job19.getStudentId()
                + " | Size: " + job19.getTaskSize()
                + " | Duration: " + job19.getEstimatedDuration() + " minutes"
                + " | Status: " + job19.getStatus()
                + " | GPU: " + (job19.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job19.getAssignedGpuId()));
        system.logout();

        system.login(student5.getUsername(), "ae59005");
        Job job20 = system.submitJob("AE-Job-4", "Python",
                "Demonstration job 4 for AE.\nSimulated GPU workload.", "Large");
        System.out.println("Created job " + job20.getJobId()
                + " | Student: " + job20.getStudentId()
                + " | Size: " + job20.getTaskSize()
                + " | Duration: " + job20.getEstimatedDuration() + " minutes"
                + " | Status: " + job20.getStatus()
                + " | GPU: " + (job20.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job20.getAssignedGpuId()));
        system.logout();

        System.out.println("\n=== ADVANCE TIME AND CANCEL JOBS ===");

        // 7. Advance to minute 2. AD cancels a queued job: charge 0.
        system.login("admin", "gpu2026");
        for (int i = 0; i < 2; i++) {
            system.advanceMinute();
            int[] minuteCounts = system.getJobCounts();
            System.out.println("Advanced to " + system.getCurrentTimeText()
                    + " | Queued: " + minuteCounts[0]
                    + " | Running: " + minuteCounts[1]
                    + " | Completed: " + minuteCounts[2]
                    + " | Cancelled: " + minuteCounts[3]);
        }
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        system.cancelJob(job19.getJobId(), "AD: queued job no longer needed");
        System.out.println("Student queued cancellation: job " + job19.getJobId()
                + " | Charged: " + job19.getChargeMinutes() + " (expected 0)"
                + " | Total queue wait: " + job19.getTotalQueueWait()
                + " | By: " + job19.getCancelledBy()
                + " | Reason: " + job19.getCancellationReason());
        system.logout();

        // 8. Advance to minute 7.
        // Jobs 1-4 complete at minute 5; jobs 5-8 then start.
        system.login("admin", "gpu2026");
        for (int i = 0; i < 5; i++) {
            system.advanceMinute();
            int[] minuteCounts = system.getJobCounts();
            System.out.println("Advanced to " + system.getCurrentTimeText()
                    + " | Queued: " + minuteCounts[0]
                    + " | Running: " + minuteCounts[1]
                    + " | Completed: " + minuteCounts[2]
                    + " | Cancelled: " + minuteCounts[3]);
        }
        system.logout();

        // AE cancels running job 5: waited 5 minutes, executed 2, charged 2.
        system.login(student5.getUsername(), "ae59005");
        system.cancelJob(job5.getJobId(), "AE: stop running small job after two minutes");
        System.out.println("Student running cancellation: job " + job5.getJobId()
                + " | Charged: " + job5.getChargeMinutes() + " (expected 2)"
                + " | Total queue wait: " + job5.getTotalQueueWait()
                + " | By: " + job5.getCancelledBy()
                + " | Reason: " + job5.getCancellationReason());
        system.logout();

        // Admin cancels one queued job and one running job.
        system.login("admin", "gpu2026");
        system.cancelJob(job20.getJobId(), "Administrator: remove duplicate queued work");
        System.out.println("Administrator queued cancellation: job " + job20.getJobId()
                + " | Charged: " + job20.getChargeMinutes() + " (expected 0)"
                + " | Total queue wait: " + job20.getTotalQueueWait()
                + " | By: " + job20.getCancelledBy()
                + " | Reason: " + job20.getCancellationReason());
        system.cancelJob(job6.getJobId(), "Administrator: stop running job after two minutes");
        System.out.println("Administrator running cancellation: job " + job6.getJobId()
                + " | Charged: " + job6.getChargeMinutes() + " (expected 2)"
                + " | Total queue wait: " + job6.getTotalQueueWait()
                + " | By: " + job6.getCancelledBy()
                + " | Reason: " + job6.getCancellationReason());
        system.save();

        System.out.println("Job 9 status: " + job9.getStatus()
                + " | GPU: " + job9.getAssignedGpuId()
                + " | Start minute: " + job9.getStartMinute()
                + " (expected Running, GPU 1, minute 7).");
        System.out.println("Job 10 status: " + job10.getStatus()
                + " | GPU: " + job10.getAssignedGpuId()
                + " | Start minute: " + job10.getStartMinute()
                + " (expected Running, GPU 2, minute 7).");

        // 9. Print actual results alongside the expected values.
        System.out.println("\n=== FINAL SUMMARY ===");
        int[] counts = system.getJobCounts();
        System.out.println("Time: " + system.getCurrentTimeText());
        System.out.println("Queued: " + counts[0] + " (expected 8)");
        System.out.println("Running: " + counts[1] + " (expected 4)");
        System.out.println("Completed: " + counts[2] + " (expected 4)");
        System.out.println("Cancelled: " + counts[3] + " (expected 4)");
        System.out.println("Utilization: " + system.getUtilizationPercent() + "% (expected 100%)");
        System.out.println("Total accepted jobs: " + system.getVisibleJobs("").size()
                + " (expected 20; rejected attempts created no jobs).");

        System.out.println("\n=== FINAL STUDENT BALANCES ===");
        Student[] students = {student1, student2, student3, student4, student5};
        int[][] expectedBalances = {
            {110, 7, 90, 13}, {120, 5, 105, 10}, {80, 5, 75, 0},
            {100, 5, 45, 50}, {100, 2, 45, 53}
        };
        for (int i = 0; i < students.length; i++) {
            int[] balance = system.getBalance(students[i].getUsername());
            System.out.println(students[i].getName() + " / " + students[i].getUsername()
                    + " | Allocated: " + balance[0] + " (expected " + expectedBalances[i][0] + ")"
                    + " | Used: " + balance[1] + " (expected " + expectedBalances[i][1] + ")"
                    + " | Reserved: " + balance[2] + " (expected " + expectedBalances[i][2] + ")"
                    + " | Available: " + balance[3] + " (expected " + expectedBalances[i][3] + ")");
        }

        System.out.println("\n=== FINAL QUEUED JOBS ===");
        Job[] queuedJobs = {job11, job12, job13, job14, job15, job16, job17, job18};
        int[] expectedStarts = {20, 20, 22, 22, 50, 50, 52, 52};
        for (int i = 0; i < queuedJobs.length; i++) {
            int id = queuedJobs[i].getJobId();
            System.out.println("Job " + id
                    + " | Position: " + system.getQueuePosition(id) + " (expected " + (i + 1) + ")"
                    + " | Predicted start: " + system.formatMinute(system.getPredictedStartMinute(id))
                    + " | Remaining wait: " + system.getRemainingWait(id)
                    + " (expected " + (expectedStarts[i] - 7) + " minutes)");
        }

        System.out.println("\nSample saved to: " + new File(fileName).getAbsolutePath());
        system.logout();
    }
}
