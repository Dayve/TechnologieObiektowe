package sciCon.model;

import java.time.LocalDate;
import java.util.Calendar;

public interface Validator {
	default public int isConferenceValid(Conference c) {
		int retCode = 0;

		String startTime = c.getStartTime();
		String endTime = c.getEndTime();
		
		if(startTime.length() != 5 || endTime.length() != 5) {
			return 1; // user didn't fill at least one hour combo box
		}
		
		LocalDate date = c.getDate();
		
		Calendar cal = Calendar.getInstance();
		int hourNow = cal.get(Calendar.HOUR_OF_DAY);
		int minNow = cal.get(Calendar.MINUTE);
		
		int startHr = Integer.parseInt(startTime.substring(0, 2));
		int startMin = Integer.parseInt(startTime.substring(3, 5));
		int endHr = Integer.parseInt(endTime.substring(0, 2));
		int endMin = Integer.parseInt(endTime.substring(3, 5));

		//the start time is less than one hour from now
		if (date.isBefore(LocalDate.now()) ||  // posted date is before now
				(date.isEqual(LocalDate.now()) && (startHr < hourNow || 
						(startHr == hourNow && startMin < minNow)))) {
			retCode |= 2;
		}

		//starts later than finishes
		if (startHr > endHr || (startHr == endHr && startMin > endMin)) {
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
		int bit = 1;
		
		if (validationCode == 0) {
			retMessage = messages[0];
		} else {
			for (int counter = 1; counter < messagesLength; bit *= 2, counter++) {
				// if bit is 1 then append corresponding message from arguments
				if ((validationCode & bit) == bit) {
					retMessage += messages[counter] + " \n";
				}
			}
			// remove the last character ("\n") if there is one or more message
			if(retMessage.length() > 0 && retMessage.charAt(retMessage.length() - 1) == '\n') {
				retMessage = retMessage.substring(0, retMessage.length() - 1);
			}
		}
		return retMessage;
	}
}
