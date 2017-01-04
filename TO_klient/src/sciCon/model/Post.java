package sciCon.model;

import java.time.LocalDateTime;

public class Post {
	private User author;
	private String content;
	

	private LocalDateTime time;

	public Post(User author, String content, LocalDateTime time) {
		this.author = author;
		this.content = content;
		this.time = time;
	}

	public User getAuthor() {
		return author;
	}

	public String getContent() {
		return content;
	}
	
	public LocalDateTime getTime() {
		return time;
	}
}
