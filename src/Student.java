

public class Student extends User{

private String email;
private String phone;
private TimeAllowance allowance;


public Student(String id, String name, String password, String phone, String email) { 
//inherited username is the university id
	super(id, name, password);
	this.phone = phone;
	this.email = email;
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
		return this.Student;
	}

@Override
	public boolean canCancel(Job job) {
		if (job.id.equals(id)) {
			return true;
		}
		return false;
	}

}


