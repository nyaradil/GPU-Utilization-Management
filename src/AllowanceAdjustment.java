public class AllowanceAdjustment {
	private int minutesDelta;
	private String reason;
	private String adminUsername;
	private int recordedMinute;
	
	public AllowanceAdjustment(int delta, String reason, String admin, int minute) {
		if(reason == null || reason.trim().isEmpty() || admin == null || admin.trim().isEmpty() || minute < -1) { //The -1 was accepted as it can be because the simulation clock is not initialized yet
			//Check that the adjustment information is valid
			throw new IllegalArgumentException("Invalid allowance adjustment information");
		}
		
		minutesDelta = delta;
		this.reason = reason.trim();
		adminUsername = admin.trim();
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
