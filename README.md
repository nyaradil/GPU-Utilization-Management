# GPU Utilization Management

A Java Swing application that simulates job scheduling across four GPUs.
Students submit jobs within their GPU-minute allowance, while administrators
approve accounts, manage allowances, and monitor activity.

## Requirements

- JDK 25
- Eclipse IDE for Java Developers with Java 25 support

## Setup and Run

1. Clone or download this repository.
2. In Eclipse, select File → Import → General →
   Existing Projects into Workspace.
3. Select the repository folder and finish importing.
4. Ensure the project uses Java 25.
5. Right-click src/Main.java → Run As → Java Application.

Administrator login:
- Username: admin
- Password: gpu2026

On a fresh start, the administrator must initialize the simulation clock.
Students must register and receive approval before submitting jobs.

## Sample Data

1. Use the included sample file, or run TestDriver to regenerate it after moving the existing sample.
2. In Main.java, change:
   new FileManager("gpu_state.txt")
   to:
   new FileManager("sample_gpu_state.txt")
3. Run Main.java.

The sample contains five approved students, pending/rejected requests,
and 20 jobs across all four states.

Example student login:
- Username: 59001
- Password: aa59001

TestDriver will not overwrite an existing sample file.
Main and TestDriver should use the same working directory.

## Saved Data

Changes are saved automatically to the file selected in Main.java.
Simulation time advances only through the Advance Minute button.

To return to normal data, close the app and switch Main.java back to
gpu_state.txt. To start fresh, move, delete or rename that existing file first.
