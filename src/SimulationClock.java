import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimulationClock {
	private LocalDateTime initialDateTime;
	private int elapsedMinutes;
	private boolean initialized;
	private DateTimeFormatter formatter;
	
	public SimulationClock() {
		initialDateTime = null;
		elapsedMinutes = 0;
		initialized = false;
		formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"); //ofPattern() is a method in DateTimeFormatter class which allows us to format the date and time as we want
	}
	public void initialize(LocalDateTime selected) { //Taked the initial time and date the user wants (selected) and makes it the initial time
		if (initialized) {
			throw new IllegalStateException("Clock is already initialized");
		}
		if (selected == null) {
			throw new IllegalArgumentException("Invalid initial date and time");
		}
		initialDateTime = selected.withSecond(0).withNano(0);
		elapsedMinutes = 0;
		initialized = true;
	}
	
	public void advanceOneMinute() {
		if (!initialized) {
			throw new IllegalStateException("Clock is not initialized");
		}
		elapsedMinutes++;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public int getElapsedMinutes() {
		return elapsedMinutes;
	}
	
	public LocalDateTime getInitialDateTime() {
		return initialDateTime;
	}
	
	public LocalDateTime getCurrentDateTime() {
		if (!initialized) {
			return null;
		}
		return initialDateTime.plusMinutes(elapsedMinutes);
	}
	
	public String formatMinute(int minute) {
		if (!initialized || minute < 0) {
			return "N/A";
		}
		LocalDateTime dateTime = initialDateTime.plusMinutes(minute);
		return dateTime.format(formatter);
	}
	
	public void restore(LocalDateTime initial, int elapsed, boolean ready) {
		// This method restores the the clock of the user if the app is closed and opened again
		if (elapsed < 0 || (ready && initial == null) || (!ready && (initial != null || elapsed != 0))) {
			//Throw an exception if the elapsed time is negative (impossible) or the clock is initialized while there is no initial date and time
			throw new IllegalArgumentException("Invalid saved clock information");
		}
		
		if (initial != null) {
			initialDateTime =  initial.withSecond(0).withNano(0);
		}
		else {
			initialDateTime = null;
		}
		
		elapsedMinutes = elapsed;
		initialized = ready;
	}
}
