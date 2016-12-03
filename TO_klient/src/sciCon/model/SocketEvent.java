package sciCon.model;

import java.io.Serializable;
import java.util.ArrayList;

public class SocketEvent implements Serializable {

	private static final long serialVersionUID = -1416783796467274992L;
	private String name;
	private Object[] data;

	public SocketEvent(String name, Object... data) {
		this.name = name;
		this.data = data;
	}

	/*
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> ArrayList<T> getObjects(Class<T> classy) {
		ArrayList<T> aL = new ArrayList<T>();
		for (int i = 0; i < data.length; i++) {
			if (classy.getClass().isAssignableFrom(data[i].getClass())) {
				aL.add((T) data[i]);
			}
		}
		return aL;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getObject(Class<T> classy) {
		T obj = null;
		for (int i = 0; i < data.length; i++) {
			if (classy.isInstance(data[i])) {
				obj = (T) data[i];
			}
		}
		return obj;
	}

	public String getName() {
		return name;
	}
}
