        add(passwordField);
        add(loginButton);
        add(registerButton);

        // Both buttons use the same event-handling method.
        loginButton.addActionListener(this);
        registerButton.addActionListener(this);
    }

    // Handle clicks on the Login and Register buttons.
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == loginButton) {
            login();
        } else if (event.getSource() == registerButton) {
            register();
        }
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Check empty fields before asking GpuSystem to log in.
        if (username.length() == 0 || password.length() == 0) {
            JOptionPane.showMessageDialog(this, "Enter your username and password.");
            return;
        }

        if (!system.login(username, password)) {
            JOptionPane.showMessageDialog(this, system.getLastError());
            passwordField.setText("");
            return;
        }

        // Open the correct dashboard with the SAME GpuSystem object.
        // The two dashboard classes must have constructors taking GpuSystem.
        try {
            if (system.isAdministrator()) {
                new AdminFrame(system).setVisible(true);
            } else if (system.isStudent()) {
                new StudentFrame(system).setVisible(true);
            } else {
                system.logout();
                JOptionPane.showMessageDialog(this, "Unknown account role.");
                return;
            }
            dispose();
        } catch (RuntimeException exception) {
            system.logout();
            JOptionPane.showMessageDialog(this, "Could not open the dashboard.");
        }
    }

    private void register() {
        // Collect all registration details in one simple dialog.
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(6, 2, 5, 5));
        form.add(new JLabel("University ID:"));
        form.add(idField);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Password:"));
        form.add(newPasswordField);
        form.add(new JLabel("Confirm password:"));
        form.add(confirmField);

        int choice = JOptionPane.showConfirmDialog(this, form,
                "Student Registration", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String password = new String(newPasswordField.getPassword());
        String confirmation = new String(confirmField.getPassword());

        // The two passwords must match before submitting the request.
        if (!password.equals(confirmation)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        // GpuSystem validates the details and prevents duplicate requests.
        if (system.registerStudent(idField.getText().trim(),
                nameField.getText().trim(), emailField.getText().trim(),
                phoneField.getText().trim(), password)) {
            JOptionPane.showMessageDialog(this,
                    "Registration submitted. Wait for administrator approval.");
        } else {
            JOptionPane.showMessageDialog(this, system.getLastError());
        }
    }
}
