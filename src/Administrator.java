
public class Administrator extends User {

    public Administrator() {
        super("admin", "admin", "gpu2026");
    }

    @Override
    public String getRole() {
        return "Administrator";
    }

    @Override
    public boolean canCancel(Job job) {
        return true;
    }
}