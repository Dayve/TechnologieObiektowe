package sciCon.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Conference implements Serializable {

	private static final long serialVersionUID = -6259050915073534863L;
	
	private String name;
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getName() {
		return name;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getSubject() {
		return subject;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getPlace() {
		return place;
	}

	public String getDescription() {
		return description;
	}

	public String getAgenda() {
		return agenda;
	}

	private LocalDate date;
	private String subject;
	private String startTime;
	private String endTime;
	private String place;
	private String description;
	private String agenda;
	
	public Conference(String name, LocalDate date) {
		this.date = date;
		this.name = name;
	}

	public Conference(String name, LocalDate date, String subject, String startTime, String endTime, String place,
			String description, String agenda) {
		this.date = date;
		this.name = name;
		this.subject = subject;
		this.startTime = startTime;
		this.endTime = endTime;
		this.place = place;
		this.description = description;
		this.agenda = agenda;
	}
}