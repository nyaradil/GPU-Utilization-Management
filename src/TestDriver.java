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

        // 1. Register five students, one pending request, and one rejected example.
        system.registerStudent("59001", "AA", "aa@email.com", "1234", "aa59001");
        system.registerStudent("59002", "AB", "ab@email.com", "1234", "ab59002");
        system.registerStudent("59003", "AC", "ac@email.com", "1234", "ac59003");
        system.registerStudent("59004", "AD", "ad@email.com", "1234", "ad59004");
        system.registerStudent("59005", "AE", "ae@email.com", "1234", "ae59005");
        system.registerStudent("59006", "AF", "af@email.com", "1234", "af59006");
        system.registerStudent("59007", "AG", "ag@email.com", "1234", "ag59007");

        // 2. Approval creates the Student objects and records their allowances.
        system.login("admin", "gpu2026");
        system.approveRequest("59001", 100);
        system.approveRequest("59002", 120);
        system.approveRequest("59003", 100);
        system.approveRequest("59004", 100);
        system.approveRequest("59005", 100);
        system.rejectRequest("59007", "Demonstration: incomplete university verification");
        // AF / 59006 stays pending.

        // References to the actual approved students owned by the system.
        Student student1 = system.findStudent("59001");
        Student student2 = system.findStudent("59002");
        Student student3 = system.findStudent("59003");
        Student student4 = system.findStudent("59004");
        Student student5 = system.findStudent("59005");

        system.initializeClock(LocalDateTime.of(2026, 11, 22, 9, 0));
        system.setAllocatedMinutes("59003", 80,
                "Boundary test: four planned jobs total exactly 80 minutes");
        system.logout();

        // 3. First Small job for each student: jobs 1-5.
        // Jobs 1-4 immediately occupy GPUs 1-4; job 5 waits.
        system.login(student1.getUsername(), "aa59001");
        Job job1 = system.submitJob("AA-Job-1", "Python",
                "Demonstration job 1 for AA.\nSimulated GPU workload.", "Small");
        system.logout();

        system.login(student2.getUsername(), "ab59002");
        Job job2 = system.submitJob("AB-Job-1", "Python",
                "Demonstration job 1 for AB.\nSimulated GPU workload.", "Small");
        system.logout();

        system.login(student3.getUsername(), "ac59003");
        Job job3 = system.submitJob("AC-Job-1", "Python",
                "Demonstration job 1 for AC.\nSimulated GPU workload.", "Small");
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        Job job4 = system.submitJob("AD-Job-1", "Python",
                "Demonstration job 1 for AD.\nSimulated GPU workload.", "Small");
        system.logout();

        system.login(student5.getUsername(), "ae59005");
        Job job5 = system.submitJob("AE-Job-1", "Python",
                "Demonstration job 1 for AE.\nSimulated GPU workload.", "Small");
        system.logout();

        // 4. Second job for each student, Medium: jobs 6-10.
        system.login(student1.getUsername(), "aa59001");
        Job job6 = system.submitJob("AA-Job-2", "Python",
                "Demonstration job 2 for AA.\nSimulated GPU workload.", "Medium");
        system.logout();

        system.login(student2.getUsername(), "ab59002");
        Job job7 = system.submitJob("AB-Job-2", "Python",
                "Demonstration job 2 for AB.\nSimulated GPU workload.", "Medium");
        system.logout();

        system.login(student3.getUsername(), "ac59003");
        Job job8 = system.submitJob("AC-Job-2", "Python",
                "Demonstration job 2 for AC.\nSimulated GPU workload.", "Medium");
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        Job job9 = system.submitJob("AD-Job-2", "Python",
                "Demonstration job 2 for AD.\nSimulated GPU workload.", "Medium");
        system.logout();

        system.login(student5.getUsername(), "ae59005");
        Job job10 = system.submitJob("AE-Job-2", "Python",
                "Demonstration job 2 for AE.\nSimulated GPU workload.", "Medium");
        system.logout();

        // 5. Third job for each student, Large: jobs 11-15.
        system.login(student1.getUsername(), "aa59001");
        Job job11 = system.submitJob("AA-Job-3", "Python",
                "Demonstration job 3 for AA.\nSimulated GPU workload.", "Large");
        system.logout();

        system.login(student2.getUsername(), "ab59002");
        Job job12 = system.submitJob("AB-Job-3", "Python",
                "Demonstration job 3 for AB.\nSimulated GPU workload.", "Large");
        system.logout();

        system.login(student3.getUsername(), "ac59003");
        Job job13 = system.submitJob("AC-Job-3", "Python",
                "Demonstration job 3 for AC.\nSimulated GPU workload.", "Large");
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        Job job14 = system.submitJob("AD-Job-3", "Python",
                "Demonstration job 3 for AD.\nSimulated GPU workload.", "Large");
        system.logout();

        system.login(student5.getUsername(), "ae59005");
        Job job15 = system.submitJob("AE-Job-3", "Python",
                "Demonstration job 3 for AE.\nSimulated GPU workload.", "Large");
        system.logout();

        // 6. AA needs 60 available for the fourth job, but currently has only 50.
        system.login("admin", "gpu2026");
        system.grantMinutes("59001", 10,
                "AA needs 110 total minutes for the four demonstration jobs");
        system.logout();

        // Fourth jobs: Very large for AA/AB, Large for AC/AD/AE. Jobs 16-20.
        system.login(student1.getUsername(), "aa59001");
        Job job16 = system.submitJob("AA-Job-4", "Python",
                "Demonstration job 4 for AA.\nSimulated GPU workload.", "Very large");
        system.logout();

        system.login(student2.getUsername(), "ab59002");
        Job job17 = system.submitJob("AB-Job-4", "Python",
                "Demonstration job 4 for AB.\nSimulated GPU workload.", "Very large");
        system.logout();

        system.login(student3.getUsername(), "ac59003");
        Job job18 = system.submitJob("AC-Job-4", "Python",
                "Demonstration job 4 for AC.\nSimulated GPU workload.", "Large");
        // This is an exact allowance match: AC now has 0 available minutes.
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        Job job19 = system.submitJob("AD-Job-4", "Python",
                "Demonstration job 4 for AD.\nSimulated GPU workload.", "Large");
        system.logout();

        system.login(student5.getUsername(), "ae59005");
        Job job20 = system.submitJob("AE-Job-4", "Python",
                "Demonstration job 4 for AE.\nSimulated GPU workload.", "Large");
        system.logout();

        // 7. Advance to minute 2. AD cancels a queued job: charge 0.
        system.login("admin", "gpu2026");
        for (int i = 0; i < 2; i++) system.advanceMinute();
        system.logout();

        system.login(student4.getUsername(), "ad59004");
        system.cancelJob(job19.getJobId(), "AD: queued job no longer needed");
        system.logout();

        // 8. Advance to minute 7.
        // Jobs 1-4 complete at minute 5; jobs 5-8 then start.
        system.login("admin", "gpu2026");
        for (int i = 0; i < 5; i++) system.advanceMinute();
        system.logout();

        // AE cancels running job 5: waited 5 minutes, executed 2, charged 2.
        system.login(student5.getUsername(), "ae59005");
        system.cancelJob(job5.getJobId(), "AE: stop running small job after two minutes");
        system.logout();

        // Admin cancels one queued job and one running job.
        system.login("admin", "gpu2026");
        system.cancelJob(job20.getJobId(), "Administrator: remove duplicate queued work");
        system.cancelJob(job6.getJobId(), "Administrator: stop running job after two minutes");
        system.save();

        // 9. Print results.
        int[] counts = system.getJobCounts();
        System.out.println("Time: " + system.getCurrentTimeText());
        System.out.println("Queued: " + counts[0] + " (expected 8)");
        System.out.println("Running: " + counts[1] + " (expected 4)");
        System.out.println("Completed: " + counts[2] + " (expected 4)");
        System.out.println("Cancelled: " + counts[3] + " (expected 4)");
        System.out.println("Utilization: " + system.getUtilizationPercent() + "% (expected 100%)");
        System.out.println("Saved to: " + new File(fileName).getAbsolutePath());
        system.logout();
    }
}
