
public abstract class User {
	
private String username;
private String name;
private String password;


public User(String username, String name, String password) {
	this.username = username;
	this.name = name;
	this.password = password;
}


public String getUsername() {
	return username;
}


public String getName() {
	return name;
}



public boolean passwordMatches(String input) {
	if (input.equals(password)) {
		return true;
	}
	return false;
}

public abstract String getRole();

public abstract boolean canCancel();

String getPasswordForStorage() {
    return password;
}

	
}
