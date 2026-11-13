
public class AccessRequest {
	
private String universityId;
private String name;
private String email;
private String phone;
private String chosenPassword;
private String status;
private String rejectionReason;



public AccessRequest(String id, String name, String email,
            String phone, String password) {

if (id == null || id.trim().isEmpty()) {
throw new IllegalArgumentException(
   "University ID cannot be empty."
);
}

if (name == null || name.trim().isEmpty()) {
throw new IllegalArgumentException(
   "Name cannot be empty."
);
}

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

if (password == null || password.trim().isEmpty()) {
throw new IllegalArgumentException(
   "Password cannot be empty."
);
}

this.universityId = id.trim();
this.name = name.trim();
this.email = email.trim();
this.phone = phone.trim();
this.chosenPassword = password;
this.status = "Pending";
this.rejectionReason = "";
}

public boolean isPending() {
    return status.equals("Pending");
}

void markApproved() {
    if (!isPending()) {
        throw new IllegalStateException(
            "Only pending requests can be approved."
        );
    }

    this.status = "Approved";
}

void markRejected(String reason) {
    if (!isPending()) {
        throw new IllegalStateException(
            "Only pending requests can be rejected."
        );
    }

    // Allow an empty reason, since the brief does not require one.
    if (reason == null) {
        reason = "";
    }

    this.status = "Rejected";
    this.rejectionReason = reason.trim();
}

public String getUniversityId() {
	return universityId;
}

public String getName() {
	return name;
}

public String getEmail() {
	return email;
}

public String getPhone() {
	return phone;
}

public String getChosenPassword() {
	return chosenPassword;
}

public String getStatus() {
	return status;
}

public String getRejectionReason() {
	return rejectionReason;
}

void restoreReview(String state, String reason) {

    if (!"Pending".equals(state)
            && !"Approved".equals(state)
            && !"Rejected".equals(state)) {

        throw new IllegalArgumentException(
            "Invalid saved request status."
        );
    }

    this.status = state;
    this.rejectionReason = "";

    if (state.equals("Rejected") && reason != null) {
        this.rejectionReason = reason.trim();
    }
}

}


