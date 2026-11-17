
public abstract class User {
	
private String username;
private String name;
private String password;


public User(String username, String name, String password) {

    if (username == null || username.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "Username cannot be empty."
        );
    }

    if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "Name cannot be empty."
        );
    }

    if (password == null || password.trim().isEmpty()) {
        throw new IllegalArgumentException(
            "Password cannot be empty."
        );
    }

    this.username = username.trim();
    this.name = name.trim();
    this.password = password;
}


public String getUsername() {
	return username;
}


public String getName() {
	return name;
}



public boolean passwordMatches(String input) {
    return password != null && password.equals(input);
}

public abstract String getRole();

public abstract boolean canCancel(Job job);

String getPasswordForStorage() {
    return password;
}

}
