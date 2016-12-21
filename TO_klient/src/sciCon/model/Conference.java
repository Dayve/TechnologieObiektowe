package sciCon.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Conference implements Serializable {

	private static final long serialVersionUID = -6259050915073534863L;
	
	private int id;
	private ArrayList<User> organizers = new ArrayList<User>();
	
	private String name;
	private String subject;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private String place;
	private String description;
	private String agenda;
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getName() {
		return name;
	}

	public String getSubject() {
		return subject;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
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
	
	public int getId() {
		return id;
	}
	
	public User getFirstOrganizer() {
		return organizers.get(0);
	}
	
	public ArrayList<User> getOrganizers() {
		return organizers;
	}
	
	public Conference(int id, String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, User organizer) {
		this( name, subject,  startTime,  endTime,  place,
				description,  agenda, organizer);
		this.id = id;
	}
	
	public Conference(String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, User organizer) {
		this.name = name;
		this.subject = subject;
		this.startTime = startTime;
		this.endTime = endTime;
		this.place = place;
		this.description = description;
		this.agenda = agenda;
		organizers.add(organizer);
	}
	
	public Conference(String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers) {
		this.name = name;
		this.subject = subject;
		this.startTime = startTime;
		this.endTime = endTime;
		this.place = place;
		this.description = description;
		this.agenda = agenda;
		this.organizers = organizers;
	}
	
	public Conference(int id, String name, String subject, LocalDateTime startTime, LocalDateTime endTime, String place,
			String description, String agenda, ArrayList<User> organizers) {
		this( name, subject,  startTime,  endTime,  place,
				description,  agenda, organizers);
		this.id = id;
	}
	
	@Override
	public String toString() {
		String organizersStr = "";
		for(User o: organizers) {
			organizersStr += o.getName() + " " + o.getSurname() + ", ";
		}
		String ret = "\nTemat: " + subject + "\nOrganizatorzy: " + organizersStr
		+ "\nCzas rozpoczęcia: " + startTime.toString().replace("T", ", godz. ")
		+ "\nCzas zakończenia: " + endTime.toString().replace("T", ", godz. ") + "\nMiejsce: " + place + "\nPlan: " + agenda;
		if (this.description != null) {
			ret += "\nOpis: " + description;
		}
		return ret;
	}
}