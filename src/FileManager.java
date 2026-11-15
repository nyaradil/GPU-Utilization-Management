import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

// Saves complete simulation snapshots with only Scanner and PrintWriter.
public class FileManager {
    private String fileName;

    public FileManager() { this("gpu_data.txt"); }
    public FileManager(String name) { fileName = name; }
    public String getFileName() { return fileName; }

    // Character codes preserve spaces, newlines and punctuation without split().
    private void writeText(PrintWriter out, String text) {
        if (text == null) text = "";
        out.println(text.length());
        for (int i = 0; i < text.length(); i++) out.println((int) text.charAt(i));
    }

    private String readText(Scanner in) {
        int length = in.nextInt();
        if (length < 0 || length > 100000)
            throw new IllegalArgumentException("Invalid text length.");
        String text = "";
        for (int i = 0; i < length; i++) {
            int c = in.nextInt();
            if (c < 0 || c > 65535)
                throw new IllegalArgumentException("Invalid character code.");
            text += (char) c;
        }
        return text;
    }

    public void save(Storage data) throws FileNotFoundException {
        if (data == null || fileName == null || fileName.length() == 0)
            throw new IllegalArgumentException("Missing storage or file name.");
        PrintWriter out = new PrintWriter(fileName);
        out.println("GPU_PROJECT_V2");
        SimulationClock clock = data.getClock();
        out.println(clock.isInitialized() ? 1 : 0);
        out.println(clock.getDay()); out.println(clock.getMonth()); out.println(clock.getYear());
        out.println(clock.getHour()); out.println(clock.getMinute());
        out.println(clock.getElapsedMinutes());

        out.println(data.getStudents().size());
        for (Student s : data.getStudents()) {
            writeText(out, s.getUsername()); writeText(out, s.getName());
            writeText(out, s.getPasswordForStorage());
            writeText(out, s.getPhone()); writeText(out, s.getEmail());
            TimeAllowance a = s.getAllowance();
            out.println(a.getAllocated()); out.println(a.getUsed()); out.println(a.getReserved());
        }

        out.println(data.getRequests().size());
        for (AccessRequest r : data.getRequests()) {
            writeText(out, r.getUniversityId()); writeText(out, r.getName());
            writeText(out, r.getEmail()); writeText(out, r.getPhone());
            writeText(out, r.getChosenPassword()); writeText(out, r.getStatus());
            writeText(out, r.getRejectionReason());
        }

        out.println(data.getAdjustments().size());
        for (String[] a : data.getAdjustments()) {
            for (int i = 0; i < 5; i++) writeText(out, a[i]);
        }

        out.println(data.getJobs().size());
        for (Job j : data.getJobs()) {
            out.println(j.getJobId()); writeText(out, j.getStudentId());
            writeText(out, j.getTitle()); writeText(out, j.getApplicationName());
            writeText(out, j.getDescription()); writeText(out, j.getTaskSize());
            out.println(j.getSubmissionTime()); writeText(out, j.getStatus());
            out.println(j.getAssignedGpuId()); out.println(j.getStartTime());
            out.println(j.getFinishTime()); out.println(j.getCancellationTime());
            writeText(out, j.getCancelledBy()); writeText(out, j.getCancellationReason());
            writeText(out, j.getResult());
        }

        out.println(4);
        for (GPU gpu : data.getGpus()) {
            out.println(gpu.getGpuId());
            Job job = gpu.getCurrentJob();
            out.println(job == null ? -1 : job.getJobId());
        }
        out.println("END");
        out.close();
    }

    public Storage load() throws FileNotFoundException {
        Scanner in = new Scanner(new File(fileName));
        try {
            if (!in.next().equals("GPU_PROJECT_V2"))
                throw new IllegalArgumentException("Unknown save-file format.");
            Storage data = new Storage();
            int initialized = in.nextInt();
