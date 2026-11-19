
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                Storage storage = new FileManager("gpu_state.txt");
                GpuSystem system = new GpuSystem(storage);

                try {
                    system.load();

                    LoginFrame loginFrame = new LoginFrame(system);
                    loginFrame.setVisible(true);

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                        null,
                        "Could not load the application data:\n"
                            + ex.getMessage(),
                        "Startup error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }
}