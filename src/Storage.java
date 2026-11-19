
import java.io.IOException;

// The UML storage contract. FileManager provides the implementation.
public interface Storage {
    void loadInto(GpuSystem system) throws IOException;
    void save(GpuSystem system) throws IOException;
}
