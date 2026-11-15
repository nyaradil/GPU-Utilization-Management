import java.io.FileNotFoundException;
import java.util.ArrayList;

// Domain controller. JFrame must call this class, not change Job/GPU directly.
public class GpuSystem {
    private Storage storage;
    private FileManager files;
    private Scheduler scheduler;
    private User currentUser;
    private String lastError;

    public GpuSystem() {
        storage = new Storage();
        files = new FileManager();
        scheduler = new Scheduler(storage.getJobs(), storage.getGpus(), storage.getClock());
        currentUser = null;
        lastError = "";
    }

    public String getLastError() { return lastError; }
    public String getCurrentUsername() {
        return currentUser == null ? "" : currentUser.getUsername();
    }
    public boolean isAdministrator() { return currentUser instanceof Administrator; }
    public boolean isStudent() { return currentUser instanceof Student; }
    public boolean isClockInitialized() { return storage.getClock().isInitialized(); }
    private boolean fail(String error) { lastError = error; return false; }
    private boolean admin() {
        if (!isAdministrator()) return fail("Administrator login required.");
        return true;
    }
    private boolean filled(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) > ' ') return true;
        return false;
    }

    public boolean registerStudent(String id, String name, String email,
                                   String phone, String password) {
        lastError = "";
        if (!filled(id) || !filled(name) || !filled(email)
                || !filled(phone) || !filled(password)) return fail("All fields are required.");
        for (int i = 0; i < id.length(); i++) {
            if (id.charAt(i) < '0' || id.charAt(i) > '9')
                return fail("University ID must contain digits only.");
        }
        int at = email.indexOf('@');
        if (at <= 0 || at >= email.length() - 1
                || email.substring(at + 1).indexOf('@') != -1
                || email.substring(at + 1).indexOf('.') <= 0)
            return fail("Invalid email address.");
        int digits = 0;
        for (int i = 0; i < phone.length(); i++) {
            char c = phone.charAt(i);
            if (c >= '0' && c <= '9') digits++;
            else if (c != '+' && c != '-' && c != ' ' && c != '(' && c != ')')
                return fail("Invalid phone number.");
        }
        if (digits == 0) return fail("Phone number must contain digits.");
        if (storage.findStudent(id) != null || storage.findRequest(id) != null)
            return fail("This university ID is already registered.");
        try {
            storage.getRequests().add(new AccessRequest(id, name, email, phone, password));
        } catch (IllegalArgumentException e) {
            return fail(e.getMessage());
        }
        return true;
    }

    public boolean login(String username, String password) {
        lastError = "";
        if (currentUser != null) return fail("Log out first.");
        if (username == null || password == null) return fail("Enter credentials.");
        User user = storage.getAdministrator();
        if (!username.equals(user.getUsername())) user = storage.findStudent(username);
        if (user == null || !user.passwordMatches(password))
            return fail("Invalid credentials or account not approved.");
        currentUser = user;
        return true;
    }

    public void logout() { currentUser = null; lastError = ""; }

    public boolean approveRequest(String id) { return approveRequest(id, 100); }
    public boolean approveRequest(String id, int minutes) {
        lastError = "";
        if (!admin()) return false;
        AccessRequest request = storage.findRequest(id);
        if (request == null || !request.isPending() || storage.findStudent(id) != null)
            return fail("Only a pending request can be approved.");
        if (minutes < 0) return fail("Negative allocation is not allowed.");
        Student student = new Student(request.getUniversityId(), request.getName(),
                request.getChosenPassword(), request.getPhone(), request.getEmail());
        // Teammate's Student class needs setAllowance(TimeAllowance).
        student.setAllowance(new TimeAllowance(minutes, 0, 0));
        storage.getStudents().add(student);