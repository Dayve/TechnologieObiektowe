package sciCon.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class Conference implements Serializable {

	private static final long serialVersionUID = -6259050915073534863L;
	
	private int id;
	private ArrayList<User> organizers = new ArrayList<User>();
	private ArrayList<User> prelectors = new ArrayList<User>();
	private ArrayList<User> participants = new ArrayList<User>();
	private ArrayList<User> sponsors = new ArrayList<User>();
	private ArrayList<User> pending = new ArrayList<User>();
	
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

	public LocalDate getDate() {
		return startTime.toLocalDate();
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
	
	public ArrayList<User> getParticipantsList() {
		ArrayList<User> ret = new ArrayList<User>(organizers);
		ret.addAll(prelectors);
		ret.addAll(sponsors);
		ret.addAll(participants);
		return ret;
	}
	
	public ArrayList<User> getPendingList() {
		return pending;
	}
	
	@Override
	public String toString() {
		String organizersStr = "";
		Iterator<User> it = organizers.iterator();
		while(it.hasNext()) {
			User o = it.next();
			organizersStr += o.getName() + " " + o.getSurname();
			if(it.hasNext()) {
				organizersStr += ", ";
			}
		}

		String ret = "Temat: " + subject + "\nOrganizatorzy: " + organizersStr
		+ "\nCzas rozpoczęcia: " + startTime.toString().replace("T", ", godz. ")
		+ "\nCzas zakończenia: " + endTime.toString().replace("T", ", godz. ") + "\nMiejsce: " + place + "\nPlan: " + agenda;
		if (this.description != null) {
			ret += "\nOpis: " + description;
		}
		return ret;
	}
	
	public static Comparator<Conference> confDateComparator = new Comparator<Conference>() {

		public int compare(Conference c1, Conference c2) {
		   LocalDateTime ldt1 = c1.getStartTime();
		   LocalDateTime ldt2 = c2.getStartTime();
		   int ret = 0;

		   if(ldt1.isBefore(ldt2)) {
			   ret = 1;
		   } else {
			   if(ldt1.isAfter(ldt2)) {
				   ret = -1;
			   }
		   }
		   return ret;

	    }};
	    
}