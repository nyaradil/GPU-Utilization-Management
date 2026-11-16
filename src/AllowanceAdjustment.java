
public class AllowanceAdjustment {
	private int minutesDelta;
	private String reason;
	private String adminUsername;
	private int recordedMinute;
	
	public AllowanceAdjustment(int delta, String reason, String admin, int minute) {
		minutesDelta = delta;
		this.reason = reason;
		adminUsername = admin;
		recordedMinute = minute;
	}

	public int getMinutesDelta() {
		return minutesDelta;
	}

	public String getReason() {
		return reason;
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public int getRecordedMinute() {
		return recordedMinute;
	}
	
	
}
