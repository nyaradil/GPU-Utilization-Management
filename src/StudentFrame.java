import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.io.IOException;

public class StudentFrame extends JFrame {

private GpuSystem system;
private JTextField titleField;
private JTextField applicationField;
private JTextArea descriptionArea;
private String selectedTaskSize;
private JTextField searchField;
private JTextField jobIdField;
private JTextArea balanceArea;
private JTextArea jobsArea;
private JButton submitButton;
private JButton advanceButton;
private JLabel simulationTimeLabel;


public StudentFrame(GpuSystem system) {
    super("Student Dashboard");

    this.system = system;
    this.selectedTaskSize = "Small";


    titleField = new JTextField(20);
    applicationField = new JTextField(20);
    descriptionArea = new JTextArea(3, 20);

    searchField = new JTextField(20);
    jobIdField = new JTextField(8);

    balanceArea = new JTextArea(8, 22);
    jobsArea = new JTextArea(12, 55);
    
    advanceButton = new JButton("Advance Minute");

    advanceButton.setToolTipText(
        "Advances the shared simulation for all students."
    );
    
    simulationTimeLabel = new JLabel("Simulation time: N/A");

    balanceArea.setEditable(false);
    jobsArea.setEditable(false);

    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    
    JButton chooseSizeButton = new JButton("Choose task size");
    submitButton = new JButton("Submit job");
    JButton searchButton = new JButton("Search");
    JButton cancelButton = new JButton("Cancel job");
    JButton logoutButton = new JButton("Log out");

    
    submitButton.setEnabled(false);

    setLayout(new BorderLayout(10, 10));

    
    JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
    formPanel.setBorder(
        BorderFactory.createTitledBorder("Submit a job")
    );

    formPanel.add(new JLabel("Job title:"));
    formPanel.add(titleField);

    formPanel.add(new JLabel("Application name:"));
    formPanel.add(applicationField);

    formPanel.add(new JLabel("Description:"));
    formPanel.add(new JScrollPane(descriptionArea));

    formPanel.add(chooseSizeButton);
    formPanel.add(submitButton);

    add(formPanel, BorderLayout.NORTH);

 
    JScrollPane jobsScroll = new JScrollPane(jobsArea);
    jobsScroll.setBorder(
        BorderFactory.createTitledBorder("My jobs")
    );

    JScrollPane balanceScroll = new JScrollPane(balanceArea);
    balanceScroll.setBorder(
        BorderFactory.createTitledBorder("GPU minutes")
    );

    JPanel displayPanel = new JPanel(new BorderLayout(10, 10));
    displayPanel.add(jobsScroll, BorderLayout.CENTER);
    displayPanel.add(balanceScroll, BorderLayout.EAST);

    add(displayPanel, BorderLayout.CENTER);


    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    searchPanel.add(new JLabel("Search my jobs:"));
    searchPanel.add(searchField);
    searchPanel.add(searchButton);

    JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    cancelPanel.add(new JLabel("Job ID:"));
    cancelPanel.add(jobIdField);
    cancelPanel.add(cancelButton);
    cancelPanel.add(advanceButton);
    cancelPanel.add(simulationTimeLabel);
    cancelPanel.add(logoutButton);

    JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
    bottomPanel.add(searchPanel);
    bottomPanel.add(cancelPanel);

    add(bottomPanel, BorderLayout.SOUTH);

 
    chooseSizeButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            handleChooseTaskSize();
        }
    });

    submitButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            handleSubmit();
        }
    });

    searchButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            handleSearch();
        }
    });

    cancelButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            handleCancel();
        }
    });
    
    advanceButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            handleAdvanceMinute();
        }
    });

    logoutButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            handleLogout();
        }
    });

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    restoreJobDraft();
    refreshView();
    pack();
    setLocationRelativeTo(null);
}


private void refreshView() {
	
	simulationTimeLabel.setText(
		    "Simulation time: " + system.getCurrentTimeText()
		);

    String studentId = system.getCurrentUser().getUsername();

    int[] balance = system.getBalance(studentId);

    balanceArea.setText(
        "Allocated: " + balance[0] + " minutes\n"
        + "Used: " + balance[1] + " minutes\n"
        + "Reserved: " + balance[2] + " minutes\n"
        + "Available: " + balance[3] + " minutes"
    );

    submitButton.setEnabled(
        system.isClockInitialized() && balance[3] > 0
    );
    
    advanceButton.setEnabled(system.isClockInitialized());

    ArrayList<Job> visibleJobs =
        system.getVisibleJobs(searchField.getText().trim());

 
    jobsArea.setText("");

    if (visibleJobs.isEmpty()) {
        jobsArea.setText("No jobs found.");
        return;
    }

    for (int i = 0; i < visibleJobs.size(); i++) {
        Job job = visibleJobs.get(i);
        int jobId = job.getJobId();
        String status = job.getStatus();

        jobsArea.append(
        	    "Job ID: " + jobId + "\n"
        	    + "Title: " + job.getTitle() + "\n"
        	    + "Application: " + job.getApplicationName() + "\n"
        	    + "Status: " + status + "\n"
        	    + "Submitted: "
        	    + system.formatMinute(job.getSubmissionMinute()) + "\n"
        	);

        if (job.getAssignedGpuId() == -1) {
            jobsArea.append("GPU: Not assigned\n");
        } else {
            jobsArea.append("GPU: " + job.getAssignedGpuId() + "\n");
        }

        if (status.equals("Queued")) {
            jobsArea.append(
                "Queue position: " + system.getQueuePosition(jobId) + "\n"
                + "Predicted start: "
                + system.formatMinute(
                    system.getPredictedStartMinute(jobId)
                ) + "\n"
                + "Predicted finish: "
                + system.formatMinute(
                    system.getPredictedFinishMinute(jobId)
                ) + "\n"
                + "Remaining wait: "
                + system.getRemainingWait(jobId) + " minutes\n"
            );

        } else {
        	jobsArea.append(
        		    "Actual start: "
        		    + system.formatMinute(job.getStartMinute()) + "\n"
        		    + "Total queue wait: "
        		    + job.getTotalQueueWait() + " minutes\n"
        		);

            if (status.equals("Running")) {
                jobsArea.append(
                    "Predicted finish: "
                    + system.formatMinute(
                        system.getPredictedFinishMinute(jobId)
                    ) + "\n"
                );
            } else {
                jobsArea.append(
                    "Execution ended: "
                    + system.formatMinute(job.getFinishMinute()) + "\n"
                    + "Allowance charged: "
                    + job.getChargeMinutes() + " minutes\n"
                    + "Result: " + job.getResultSummary() + "\n"
                );
            }
        }

        if (status.equals("Cancelled")) {
            jobsArea.append(
                "Cancelled at: "
                + system.formatMinute(job.getCancellationMinute()) + "\n"
                + "Cancelled by: " + job.getCancelledBy() + "\n"
                + "Reason: " + job.getCancellationReason() + "\n"
            );
        }

        jobsArea.append("\n");
    }

    
    jobsArea.setCaretPosition(0);
}

private void handleAdvanceMinute() {

    try {
        system.advanceMinute();
        refreshView();

    } catch (IllegalArgumentException ex) {
        showError(ex.getMessage());

    } catch (IllegalStateException ex) {
        showError(ex.getMessage());

    } catch (IOException ex) {
        refreshView();

        showError(
            "The changes could not be saved:\n"
                + ex.getMessage()
                + "\nCheck the current simulation time "
                + "before advancing again."
        );
    }
}

private void handleChooseTaskSize() {

    String[] sizes = {
        "Small", "Medium", "Large", "Very large"
    };

    String choice = (String) JOptionPane.showInputDialog(
        this,
        "Choose a task size:",
        "Task size",
        JOptionPane.QUESTION_MESSAGE,
        null,
        sizes,
        selectedTaskSize
    );

    if (choice == null) {
        return;
    }

    selectedTaskSize = choice;

    int duration;

    if (selectedTaskSize.equals("Small")) {
        duration = 5;
    } else if (selectedTaskSize.equals("Medium")) {
        duration = 15;
    } else if (selectedTaskSize.equals("Large")) {
        duration = 30;
    } else {
        duration = 60;
    }

    JOptionPane.showMessageDialog(
        this,
        "Task size: " + selectedTaskSize
            + "\nDuration: " + duration + " GPU minutes",
        "Task duration",
        JOptionPane.INFORMATION_MESSAGE
    );
}

private void handleSubmit() {

    int duration;

    if (selectedTaskSize.equals("Small")) {
        duration = 5;
    } else if (selectedTaskSize.equals("Medium")) {
        duration = 15;
    } else if (selectedTaskSize.equals("Large")) {
        duration = 30;
    } else {
        duration = 60;
    }

    int answer = JOptionPane.showConfirmDialog(
        this,
        "Submit this job?\n"
            + "Task size: " + selectedTaskSize + "\n"
            + "Duration: " + duration + " GPU minutes",
        "Confirm submission",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE
    );

    if (answer != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        Job submittedJob = system.submitJob(
            titleField.getText(),
            applicationField.getText(),
            descriptionArea.getText(),
            selectedTaskSize
        );
        
        Student student = (Student) system.getCurrentUser();
        student.clearJobDraft();
        
        restoreJobDraft();
        refreshView();

        titleField.setText("");
        applicationField.setText("");
        descriptionArea.setText("");

        refreshView();

        JOptionPane.showMessageDialog(
            this,
            "Job " + submittedJob.getJobId()
                + " submitted successfully."
        );

    } catch (IllegalArgumentException ex) {
        showError(ex.getMessage());

    } catch (IllegalStateException ex) {
        showError(ex.getMessage());

    } catch (IOException ex) {
        refreshView();

        showError(
            "The changes could not be saved:\n"
                + ex.getMessage()
                + "\nCheck the jobs list before retrying."
        );
    }
}

private void handleSearch() {
    refreshView();
}

private void handleCancel() {

    try {
        int jobId = Integer.parseInt(
            jobIdField.getText().trim()
        );

        String reason = JOptionPane.showInputDialog(
            this,
            "Reason for cancelling job " + jobId + ":",
            "Cancel job",
            JOptionPane.QUESTION_MESSAGE
        );

        // Cancel or closing the dialog stops the operation.
        if (reason == null) {
            return;
        }

        reason = reason.trim();

        if (reason.isEmpty()) {
            throw new IllegalArgumentException(
                "A cancellation reason is required."
            );
        }

        system.cancelJob(jobId, reason);

        jobIdField.setText("");
        refreshView();

        JOptionPane.showMessageDialog(
            this,
            "Job " + jobId + " cancelled successfully."
        );

    } catch (NumberFormatException ex) {
        showError("Enter a valid numeric job ID.");

    } catch (IllegalArgumentException ex) {
        showError(ex.getMessage());

    } catch (IllegalStateException ex) {
        showError(ex.getMessage());

    } catch (IOException ex) {
        refreshView();

        showError(
            "The changes could not be saved:\n"
                + ex.getMessage()
                + "\nCheck the jobs list before retrying."
        );
    }
}

private void handleLogout() {
	
	 Student student = (Student) system.getCurrentUser();

	    student.saveJobDraft(
	        titleField.getText(),
	        applicationField.getText(),
	        descriptionArea.getText(),
	        selectedTaskSize
	    );

    system.logout();

    dispose();

    LoginFrame loginFrame = new LoginFrame(system);
    loginFrame.setVisible(true);
}

private void showError(String message) {
    JOptionPane.showMessageDialog(
        this,
        message,
        "Error",
        JOptionPane.ERROR_MESSAGE
    );
}

private void restoreJobDraft() {
    Student student = (Student) system.getCurrentUser();

    titleField.setText(student.getDraftTitle());
    applicationField.setText(student.getDraftApplication());
    descriptionArea.setText(student.getDraftDescription());
    selectedTaskSize = student.getDraftTaskSize();
}

}
