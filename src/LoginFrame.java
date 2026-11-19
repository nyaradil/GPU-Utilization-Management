
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

// The login screen delegates account rules to GpuSystem.
public class LoginFrame extends JFrame implements ActionListener {
    private GpuSystem system;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField registrationIdField;
    private JTextField registrationNameField;
    private JTextField registrationEmailField;
    private JTextField registrationPhoneField;
    private JPasswordField registrationPasswordField;
    private JLabel statusLabel;
    private JButton loginButton;
    private JButton registerButton;

    // Full Constructor: receive the shared application controller.
    public LoginFrame(GpuSystem system) {
        this.system = system;
        setTitle("GPU Utilization Management - Login");
        setSize(420, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 8, 8));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        statusLabel = new JLabel(" ");

        add(new JLabel("Username / University ID:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
        add(registerButton);
        add(statusLabel);
        add(new JLabel(""));

        loginButton.addActionListener(this);
        registerButton.addActionListener(this);

        registrationIdField = new JTextField();
        registrationNameField = new JTextField();
        registrationEmailField = new JTextField();
        registrationPhoneField = new JTextField();
        registrationPasswordField = new JPasswordField();
    }

    // Both buttons use the same listener.
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == loginButton) handleLogin();
        else if (event.getSource() == registerButton) handleRegister();
    }

    private void handleLogin() {
        try {
            User user = system.login(usernameField.getText(),
                    new String(passwordField.getPassword()));

            // The dashboards must receive the SAME GpuSystem object.
            if (user instanceof Administrator) {
                new AdminFrame(system).setVisible(true);
            } else if (user instanceof Student) {
                new StudentFrame(system).setVisible(true);
            } else {
                throw new IllegalArgumentException("Unknown account type.");
            }
            dispose();
        } catch (RuntimeException e) {
            system.logout();
            showError(e.getMessage());
        }
    }

    private void handleRegister() {
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("University ID:"));
        form.add(registrationIdField);
        form.add(new JLabel("Name:"));
        form.add(registrationNameField);
        form.add(new JLabel("Email:"));
        form.add(registrationEmailField);
        form.add(new JLabel("Phone:"));
        form.add(registrationPhoneField);
        form.add(new JLabel("Password:"));
        form.add(registrationPasswordField);

        int choice = JOptionPane.showConfirmDialog(this, form,
                "Student Registration", JOptionPane.OK_CANCEL_OPTION);
        if (choice != JOptionPane.OK_OPTION) return;

        try {
            system.registerStudent(registrationIdField.getText(),
                    registrationNameField.getText(),
                    registrationEmailField.getText(),
                    registrationPhoneField.getText(),
                    new String(registrationPasswordField.getPassword()));

            registrationPasswordField.setText("");
            statusLabel.setText("Request pending approval.");
            JOptionPane.showMessageDialog(this,
                    "Registration submitted. Wait for administrator approval.");
        } catch (IOException e) {
            showError("Could not save registration: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        statusLabel.setText("Please correct the error.");
        JOptionPane.showMessageDialog(this, message);
    }
}
