package Replica2;

import java.util.List;
import java.util.Map;

public class CommonOutput {
	public static final String general_fail = "failed";
	public static final String general_success = "successful";
	public static final String addEvent_fail_cannot_decrease_capacity = "Cannot decrease capacity";
	public static final String addMovieSlot_success_added = "Movie Slot added successfully";
	public static final String addMovieSlot_fail_another_server = "can not add movie slot to different server";

	public static final String addMovieSlot_success_capacity_updated = "Movie Slot capacity updated successfully";
	public static final String removeMovieSlot_fail_no_such_movieShow = "No such event";
	public static final String removeMovieSlot_success_removed = "Movie Slot removed successfully";
	public static final String removeMovieSlot_fail_another_server = "can not remove movie slot to different server";

	public static final String bookMovieShow_fail_no_such_movieShow = "No such Movie Show";
	public static final String bookMovieShow_fail_already_booked_movieShow = "You have booked already";
	public static final String bookMovieShow_success_booked = "Movie Show booked Successfully";

	public static final String bookMovieShow_no_capacity = "Movie show is full";
	public static final String bookMovieShow_fail_weekly_limit = "Weekly limit reached";
	public static final String cancelMovieShow_fail_not_registered_in_movieShow = "You are not registered in movie show";
	public static final String cancelMovieShow_fail_overcanceled_movieShow = "Asked number of Tickets are more than booked";
	public static final String cancelMovieShow_success_movieShow = "Movie show canceled Successful";
	public static final String cancelMovieShow_fail_no_such_movieShow = "No such event";
	public static final String exchangeTicket_fail_no_such_movieShow = "No such Movie";
	public static final String exchangeTicket_fail_not_registered_in_movieShow = "You are not registered in event";
	private static final String SUCCESS = "Success:";
	private static final String FAIL = "Fail:";

	private static String standardOutput(boolean isSuccess, String method, String output) {
		if (isSuccess)
			return SUCCESS + method + " > " + output;
		else
			return FAIL + method + " > " + output;
	}

	public static String addMovieSlotOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			if (reason == null) {
				reason = general_success;
			}
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "addMovieSlot", reason);
	}

	/**
	 * Format of each string in allEventIDsWithCapacity --> MovieID+ one space + remainingCapacity
	 */
	public static String listMovieShowAvailabilityOutput(boolean isSuccess, List<String> allEventIDsWithCapacity, String reason) {
		if (isSuccess) {
			reason = general_success;
			if (allEventIDsWithCapacity.size() > 0) {
				StringBuilder reasonBuilder = new StringBuilder();
				for (String event :
						allEventIDsWithCapacity) {
					if (event.length() > 10) {
						reasonBuilder.append(event).append("@");
					}
				}
				if (reasonBuilder.length() > 0)
					reason = reasonBuilder.toString();
				if (reason.endsWith("@"))
					reason = reason.substring(0, reason.length() - 1);
			}
		} else {
			reason = general_fail;
		}
		return standardOutput(isSuccess, "listMovieShowAvailability", reason);
	}

	public static String removeMovieSlotOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "removeMovieSlot", reason);
	}

	public static String bookMovieTicketsOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "bookMovieTickets", reason);
	}

	//Format for output EventType+ one space + EventID
	public static String getBookingScheduleOutput(boolean isSuccess, Map<String, Map<String, Integer>> movieShows, String reason) {
		if (isSuccess) {
			reason = general_success;
			if (movieShows.size() > 0) {
				StringBuilder reasonBuilder = new StringBuilder();
				for (String movieName :
						movieShows.keySet()) {
					for (String movieID :
							movieShows.get(movieName).keySet()) {
						reasonBuilder.append(movieName).append(" ").append(movieID).append("@");
					}
				}
				reason = reasonBuilder.toString();
				if (!reason.equals(""))
					reason = reason.substring(0, reason.length() - 1);
			}
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "getBookingSchedule", reason);
	}

	public static String cancelMovieTicketsOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "cancelMovieTickets", reason);
	}


	public static String exchangeTicketsOutput(boolean isSuccess, String reason) {
		if (isSuccess) {
			reason = general_success;
		} else {
			if (reason == null) {
				reason = general_fail;
			}
		}
		return standardOutput(isSuccess, "exchangeTickets", reason);
	}
}
