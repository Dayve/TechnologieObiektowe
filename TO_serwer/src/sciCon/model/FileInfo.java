package sciCon.model;

import java.io.Serializable;

public class FileInfo implements Serializable {

	private static final long serialVersionUID = -1222285932832117578L;
	
	private String filename;
	private String description;
	private User author;
	private int targetConferenceId;
	
	public FileInfo() {
		filename = new String();
		description = new String();
		author = null;
		targetConferenceId = -1;
	}
	
	public FileInfo(String filename, String description, User author, int targetConferenceId) {
		super();
		this.filename = filename;
		this.description = description;
		this.author = author;
		this.targetConferenceId = targetConferenceId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public int getTargetConferenceId() {
		return targetConferenceId;
	}

	public void setTargetConferenceId(int targetConferenceId) {
		this.targetConferenceId = targetConferenceId;
	}

	@Override
	public String toString() {
		return "FileInfo [filename=" + filename + ", description=" + description + ", author=" + author
				+ ", targetConferenceId=" + targetConferenceId + "]";
	}
}
