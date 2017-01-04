package sciCon.model;

public class Post {
	private User author;
	private String content;

	public Post(User author, String content) {
		this.author = author;
		this.content = content;
	}

	public User getAuthor() {
		return author;
	}

	public String getContent() {
		return content;
	}
}
