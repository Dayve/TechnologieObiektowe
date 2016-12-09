package sciCon.model;

import java.time.LocalDate;

public interface Validator {
	default public int isConferenceValid(Conference c) {
		int retCode = 0;

		String startTime = c.getStartTime();
		String endTime = c.getEndTime();
		LocalDate date = c.getDate();
		double dStart = 0;
		double dEnd = 0;

		if (date.isBefore(LocalDate.now())) {
			retCode |= 1;
		}

		if (startTime.indexOf(":") > 0) {
			startTime = startTime.replaceFirst(":", ".");
		}
		
		dStart = Float.parseFloat(startTime);

		if (endTime.indexOf(":") > 0) {
			endTime = endTime.replaceFirst(":", ".");
		}

		dEnd = Float.parseFloat(endTime);

		if (dStart >= dEnd) {
			retCode |= 2;
		}

		if (dStart >= 24.00 || dEnd >= 24.00) {
			retCode |= 4;
		}

		return retCode;
	}

	default public int isUserValid(User u) {

		int retCode = 0;

		String login = u.getLogin();
		String password = u.getPassword();
		String name = u.getName();
		String surname = u.getSurname();

		if (!(login.matches("[a-zA-Z0-9_]*")) || login.length() < 3) {
			retCode |= 1;
		}

		if (password.length() < 6) {
			retCode |= 2;
		}

		if (name.length() < 2 || surname.length() < 2) {
			retCode |= 4;
		}

		return retCode;
	}

	default public String interpretValidationCode(int validationCode, String... messages) {
		String retMessage = "";
		int messagesLength = messages.length;

		if (validationCode == 0) {
			retMessage = messages[0];
		} else {
			for (int i = 1; i < messagesLength; i++) {

				// if bit is 1 then append corresponding message from arguments
				if ((validationCode & i) == i) {
					retMessage += messages[i] + " \n";
				}

				// remove the last character ("\n")
				if(retMessage.length() > 0 && retMessage.charAt(retMessage.length() - 1) == '\n') {
					retMessage = retMessage.substring(0, retMessage.length() - 1);
				}
				
			}
		}
		return retMessage;
	}
}
