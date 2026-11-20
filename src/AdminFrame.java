import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class AdminFrame extends JFrame implements ActionListener {
	private GpuSystem system;
	private JTextField requestIdField;
    private JTextField initialMinutesField;
    private JTextField studentIdField;
    private JTextField minutesField;
    private JTextArea adjustmentReasonArea;
    private JTextArea cancellationReasonArea;
    private JTextField jobIdField;
    private JTextArea dashboardArea;
    private JTextArea requestsArea;
    private JSpinner datePicker;
    private JSpinner timePicker;
    private JButton initializeButton;
    private JButton advanceButton;
    private JButton approveButton;
    private JButton rejectButton;
    private JButton findStudentButton;
    private JButton grantButton;
    private JButton setAllocationButton;
    private JButton cancelButton;
    private JButton refreshButton;
    private JButton logoutButton;
    
    public AdminFrame(GpuSystem system) {
    		this.system = system;
    	
    		setTitle("GPU Management System - Administrator");
    		setSize(850, 600);
    		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		
    		requestIdField = new JTextField(15);
        initialMinutesField = new JTextField("100", 10);
        studentIdField = new JTextField(15);
        minutesField = new JTextField(10);
        adjustmentReasonArea = new JTextArea(4, 20);
        cancellationReasonArea = new JTextArea(4, 20);
        jobIdField = new JTextField(10);
        dashboardArea = new JTextArea();
        requestsArea = new JTextArea(12, 50);

        dashboardArea.setEditable(false);
        requestsArea.setEditable(false);
        adjustmentReasonArea.setLineWrap(true);
        adjustmentReasonArea.setWrapStyleWord(true);
        cancellationReasonArea.setLineWrap(true);
        cancellationReasonArea.setWrapStyleWord(true);
        
        // Create spinners for selecting the initial simulation date and time (add up and down arrows to allow the user to control date and time)
        datePicker = new JSpinner(new SpinnerDateModel());
        timePicker = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(datePicker, "dd/MM/yyyy");
        dateEditor.getFormat().setLenient(false);
        datePicker.setEditor(dateEditor);
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timePicker, "HH:mm");
        timeEditor.getFormat().setLenient(false);
        timePicker.setEditor(timeEditor);
        
        //Initializing all buttons
        approveButton = new JButton("Approve Request");
        rejectButton = new JButton("Reject Request");
        findStudentButton = new JButton("Find Student");
        grantButton = new JButton("Grant Minutes");
        setAllocationButton = new JButton("Set Allocation");
        cancelButton = new JButton("Cancel Job");
        refreshButton = new JButton("Refresh Dashboard");
        initializeButton = new JButton("Initialize Clock");
        advanceButton = new JButton("Advance Minute");
        logoutButton = new JButton("Logout");
        
        //Add actionListener to all buttons
        approveButton.addActionListener(this);
        rejectButton.addActionListener(this);
        findStudentButton.addActionListener(this);
        grantButton.addActionListener(this);
        setAllocationButton.addActionListener(this);
        cancelButton.addActionListener(this);
        refreshButton.addActionListener(this);
        initializeButton.addActionListener(this);
        advanceButton.addActionListener(this);
        logoutButton.addActionListener(this);

        advanceButton.setEnabled(system.isClockInitialized()); //The advance button works only if the clock is initialized
        initializeButton.setEnabled(!system.isClockInitialized());
        
        JTabbedPane tabs = new JTabbedPane(); //Creates tabs so the user can navigate between them easily
        
        // Registration Requests tab
        JPanel requestPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        requestPanel.add(new JLabel("University ID:"));
        requestPanel.add(requestIdField);
        requestPanel.add(new JLabel("Initial Allocation (minutes):"));
        requestPanel.add(initialMinutesField);
        requestPanel.add(approveButton);
        requestPanel.add(rejectButton);
        JPanel requestsTab = new JPanel(new BorderLayout(10, 10));
        requestsTab.add(requestPanel, BorderLayout.NORTH);
        requestsTab.add(new JScrollPane(requestsArea), BorderLayout.CENTER);
        tabs.addTab("Registration Requests", requestsTab);
        
        // Student Management tab
        JPanel studentPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        studentPanel.add(new JLabel("Student University ID:"));
        studentPanel.add(studentIdField);
        studentPanel.add(findStudentButton);
        studentPanel.add(new JLabel(""));
        studentPanel.add(new JLabel("Minutes:"));
        studentPanel.add(minutesField);
        studentPanel.add(grantButton);
        studentPanel.add(setAllocationButton);
        studentPanel.add(new JLabel("Reason:"));
        studentPanel.add(new JScrollPane(adjustmentReasonArea));
        tabs.addTab("Student Management", studentPanel);
        
        // Simulation Controls tab
        JPanel simulationPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        simulationPanel.add(new JLabel("Select Initial Date:"));
        simulationPanel.add(datePicker);
        simulationPanel.add(new JLabel("Select Initial Time:"));
        simulationPanel.add(timePicker);
        simulationPanel.add(initializeButton);
        simulationPanel.add(advanceButton);
        tabs.addTab("Simulation", simulationPanel);
        
        // Job Cancellation tab
        JPanel cancellationPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        cancellationPanel.add(new JLabel("Job ID:"));
        cancellationPanel.add(jobIdField);
        cancellationPanel.add(new JLabel("Cancellation Reason:"));
        cancellationPanel.add(new JScrollPane(cancellationReasonArea));
        cancellationPanel.add(cancelButton);
        tabs.addTab("Job Management", cancellationPanel);
        
        // Dashboard (Main tab)
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.add(new JScrollPane(dashboardArea),BorderLayout.CENTER);
        dashboardPanel.add(refreshButton, BorderLayout.SOUTH);
        tabs.addTab("Dashboard", dashboardPanel);
        //Logout button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutButton);
        add(tabs, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        refreshView();
        setVisible(true);
    }
    
    //Handle button events
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == approveButton) {
            handleApprove();
        }
        else if (e.getSource() == rejectButton) {
            handleReject();
        }
        else if (e.getSource() == findStudentButton) {
            handleFindStudent();
        }
        else if (e.getSource() == grantButton) {
            handleGrant();
        }
        else if (e.getSource() == setAllocationButton) {
            handleSetAllocation();
        }
        else if (e.getSource() == initializeButton) {
            handleInitializeClock();
        }
        else if (e.getSource() == advanceButton) {
            handleAdvanceMinute();
        }
        else if (e.getSource() == cancelButton) {
            handleCancel();
        }
        else if (e.getSource() == refreshButton) {
            refreshView();
        }
        else if (e.getSource() == logoutButton) {
            handleLogout();
        }
    }
    
    public void refreshView() {
    		// Refresh dashboard information
        try {
            String dashboard = "";
            dashboard += "SIMULATION TIME\n";
            dashboard += system.getCurrentTimeText() + "\n\n";
            dashboard += "GPU STATUS\n";
            dashboard += system.getGpuSummary() + "\n\n";
            dashboard += "JOB COUNTS\n";
            int[] counts = system.getJobCounts();
            dashboard += "Queued: " + counts[0] + "\n";
            dashboard += "Running: " + counts[1] + "\n";
            dashboard += "Completed: " + counts[2] + "\n";
            dashboard += "Cancelled: " + counts[3] + "\n\n";
            dashboard += "Busy GPUs: " + system.getBusyGpuCount() + " / 4\n";
            double utilization = system.getUtilizationPercent();

            if (utilization == -1) {
                dashboard += "Utilization: N/A\n";
            }
            else {
                dashboard += "Utilization: " + utilization + "%\n";
            }

            ArrayList<AccessRequest> requests = system.getRequests();
            dashboard += "\nRegistration requests: " + requests.size() + "\n";

            String requestDetails = "";
            dashboard += "\nSTUDENT BALANCES\n";

            for (AccessRequest request : requests) {
                requestDetails += "University ID: " + request.getUniversityId()
                        + "\nName: " + request.getName()
                        + "\nEmail: " + request.getEmail()
                        + "\nPhone: " + request.getPhone()
                        + "\nChosen password: " + request.getChosenPassword()
                        + "\nStatus: " + request.getStatus() + "\n";

                if (request.getStatus().equals("Rejected"))
                    requestDetails += "Rejection reason: "
                            + request.getRejectionReason() + "\n";

                requestDetails += "\n";

                if (request.getStatus().equals("Approved")) {
                    int[] balance = system.getBalance(request.getUniversityId());
                    dashboard += request.getUniversityId()
                            + " | Allocated: " + balance[0]
                            + " | Used: " + balance[1]
                            + " | Reserved: " + balance[2]
                            + " | Available: " + balance[3] + "\n";
                }
            }

            requestsArea.setText(requests.isEmpty()
                    ? "No registration requests." : requestDetails);

            dashboard += "\nALL JOBS\n";
            ArrayList<Job> jobs = system.getVisibleJobs("");

            if (jobs.isEmpty()) dashboard += "No jobs submitted.\n";

            for (Job job : jobs) {
                int jobId = job.getJobId();

                dashboard += "Job " + jobId
                        + " | Student: " + job.getStudentId()
                        + " | Application: " + job.getApplicationName()
                        + " | Status: " + job.getStatus() + "\n";

                dashboard += "GPU: " + (job.getAssignedGpuId() == -1
                        ? "Not assigned" : "" + job.getAssignedGpuId())
                        + " | Submitted: "
                        + system.formatMinute(job.getSubmissionMinute()) + "\n";

                if (job.getStatus().equals("Queued")) {
                    dashboard += "Queue position: " + system.getQueuePosition(jobId)
                            + " | Predicted start: "
                            + system.formatMinute(system.getPredictedStartMinute(jobId))
                            + " | Predicted finish: "
                            + system.formatMinute(system.getPredictedFinishMinute(jobId))
                            + " | Remaining wait: " + system.getRemainingWait(jobId)
                            + " minutes\n";
                } else {
                    dashboard += "Started: " + system.formatMinute(job.getStartMinute());

                    if (job.getStatus().equals("Running")) {
                        dashboard += " | Predicted finish: "
                                + system.formatMinute(system.getPredictedFinishMinute(jobId));
                    } else {
                        dashboard += " | Execution ended: "
                                + system.formatMinute(job.getFinishMinute())
                                + " | Runtime: " + job.getChargeMinutes()
                                + " | Queue wait: " + job.getTotalQueueWait()
                                + " | Charged: " + job.getChargeMinutes()
                                + "\nResult: " + job.getResultSummary();
                    }

                    dashboard += "\n";
                }

                if (job.getStatus().equals("Cancelled"))
                    dashboard += "Cancelled at: "
                            + system.formatMinute(job.getCancellationMinute())
                            + " | By: " + job.getCancelledBy()
                            + " | Reason: " + job.getCancellationReason() + "\n";

                dashboard += "\n";
            }

            dashboardArea.setText(dashboard);
            initializeButton.setEnabled(!system.isClockInitialized());
            advanceButton.setEnabled(system.isClockInitialized());

        }
        catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    

    public void handleApprove() {
    		// Approve a registration request
        try {
            String id = requestIdField.getText().trim();
            int minutes = Integer.parseInt(initialMinutesField.getText().trim());

            if (id.isEmpty() || minutes < 0) {
                showError("Enter a valid student ID and initial minutes.");
                return;
            }

            system.approveRequest(id, minutes);
            JOptionPane.showMessageDialog(this, "Registration request approved.");

            refreshView();
        }
        catch (NumberFormatException ex) {
            showError("Initial minutes must be a valid number.");
        }
        catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    public void handleReject() {
    		// Reject a registration request	
        try {
            String id = requestIdField.getText().trim();
            String reason = JOptionPane.showInputDialog(this, "Rejection reason:");
            if (reason == null) return;
            reason = reason.trim();
            
            if (id.isEmpty() || reason.isEmpty()) {
                showError("Enter the request ID and rejection reason.");
                return;
            }
            system.rejectRequest(id, reason);
            JOptionPane.showMessageDialog(this, "Registration request rejected.");

            refreshView();
        }
        catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    public void handleFindStudent() {
    	 	// Find a student and display their information
        try {
            String id = studentIdField.getText().trim();
            if (id.isEmpty()) {
                showError("Enter a student university ID.");
                return;
            }

            Student student = system.findStudent(id);

            if (student == null) {
                showError("Student not found.");
                return;
            }

            int[] balance = system.getBalance(id);
            String result = "Student ID: " + id + "\n\n";

            result += "Allocated Minutes: " + balance[0] + "\n";
            result += "Used Minutes: " + balance[1] + "\n";
            result += "Reserved Minutes: " + balance[2] + "\n";
            result += "Available Minutes: " + balance[3] + "\n\n";
            result += "Student Jobs:\n";

            ArrayList<Job> jobs = system.getVisibleJobs("");

            for (Job job : jobs) {
                if (job.getStudentId().equals(id)) {
                    result += "Job ID: " + job.getJobId() + " | Title: " + job.getTitle() + " | Status: " + job.getStatus() + "\n";
                }
            }

            JOptionPane.showMessageDialog(this, result);
        }
        catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    public void handleGrant() {
    		// Grant additional minutes to a student
        try {
            String id = studentIdField.getText().trim();
            int amount = Integer.parseInt(minutesField.getText().trim());
            String reason = adjustmentReasonArea.getText().trim();
            if (id.isEmpty() || amount <= 0 || reason.isEmpty()) {
                showError("Enter a student ID, positive minutes, and a reason.");
                return;
            }

            system.grantMinutes(id, amount, reason);
            JOptionPane.showMessageDialog(this, "Additional minutes granted successfully.");

            refreshView();
        }
        catch (NumberFormatException ex) {
            showError("Minutes must be a valid number.");
        }
        catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    public void handleSetAllocation() {
    		// Change a student's total allocated minutes
        try {
            String id = studentIdField.getText().trim();
            int newTotal = Integer.parseInt(minutesField.getText().trim());
            String reason = adjustmentReasonArea.getText().trim();

            if (id.isEmpty() || newTotal < 0 || reason.isEmpty()) {
                showError("Enter a student ID, valid allocation, and a reason.");
                return;
            }

            system.setAllocatedMinutes(id, newTotal, reason);
            JOptionPane.showMessageDialog(this, "Student allocation updated successfully.");

            refreshView();
        }
        catch (NumberFormatException ex) {
            showError("Minutes must be a valid number.");
        }
        catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    public LocalDateTime readSelectedDateTime() {
    		// Read the selected date and time
        try {
            datePicker.commitEdit();
            timePicker.commitEdit();
        } catch (java.text.ParseException ex) {
            throw new IllegalArgumentException("Enter a valid date and time.");
        }

        Date selectedDate = (Date) datePicker.getValue();
        Date selectedTime = (Date) timePicker.getValue();
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(selectedDate);
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTime(selectedTime);
        LocalDate date = LocalDate.of(dateCalendar.get(Calendar.YEAR), dateCalendar.get(Calendar.MONTH) + 1, dateCalendar.get(Calendar.DAY_OF_MONTH));
        LocalTime time = LocalTime.of(timeCalendar.get(Calendar.HOUR_OF_DAY), timeCalendar.get(Calendar.MINUTE));

        return LocalDateTime.of(date, time);
    }
    
    public void handleInitializeClock() {
    		// Initialize the simulation clock
        try {
            LocalDateTime selected = readSelectedDateTime();
            system.initializeClock(selected);
            JOptionPane.showMessageDialog(this, "Simulation clock initialized.");

            refreshView();
        }
        catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    public void handleAdvanceMinute() {
    		// Advance the simulation by one minute
        try {
            system.advanceMinute();
            refreshView();
        }
        catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    public void handleCancel() {
    		// Cancel a job
        try {
            int id = Integer.parseInt(jobIdField.getText().trim());
            String reason = cancellationReasonArea.getText().trim();

            if (reason.isEmpty()) {
                showError("Enter a cancellation reason.");
                return;
            }

            system.cancelJob(id, reason);
            JOptionPane.showMessageDialog(this, "Job cancelled successfully.");

            refreshView();
        }
        catch (NumberFormatException ex) {
            showError("Job ID must be a valid number.");
        }
        catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    public void handleLogout() {
    		// Log out and return to the login screen
        system.logout();
        dispose(); //Closes the current window
        new LoginFrame(system).setVisible(true);
    }
    
    public void showError(String message) {
    		// Display error messages
        if (message == null || message.trim().isEmpty()) {
        		message = "An unexpected error occurred.";
        }
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
}