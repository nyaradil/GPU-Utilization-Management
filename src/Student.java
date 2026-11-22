
public class Student extends User {

    private String email;
    private String phone;
    private TimeAllowance allowance;
    private String draftTitle = "";
    private String draftApplication = "";
    private String draftDescription = "";
    private String draftTaskSize = "Small";

    public Student(String id, String name, String email,
                   String phone, String password) {

        super(id, name, password);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Email cannot be empty."
            );
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Phone number cannot be empty."
            );
        }

        this.email = email.trim();
        this.phone = phone.trim();
        this.allowance = new TimeAllowance();
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public TimeAllowance getAllowance() {
        return allowance;
    }

    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public boolean canCancel(Job job) {
        return getUsername().equals(job.getStudentId());
    }
    
    public void saveJobDraft(String title, String application,
            String description, String taskSize) {
		draftTitle = title;
		draftApplication = application;
		draftDescription = description;
		draftTaskSize = taskSize;
	}
    
    public String getDraftTitle() {
        return draftTitle;
    }
    
    public String getDraftApplication() {
        return draftApplication;
    }
    
    public String getDraftDescription() {
        return draftDescription;
    }
    
    public String getDraftTaskSize() {
        return draftTaskSize;
    }
    
    public void clearJobDraft() {
        saveJobDraft("", "", "", "Small");
    }       
}