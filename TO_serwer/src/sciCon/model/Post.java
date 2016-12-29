package sciCon.model;

public class Post {
	private String username;
	private String content;

	public Post(String username, String content) {
		this.username = username;
		this.content = content;
	}

	public String getUsername() {
		return username;
	}

	public String getContent() {
		return content;
	}

	@Override public String toString() {
		return "Post [username=" + username + ", content=" + content + "]";
	}
}
