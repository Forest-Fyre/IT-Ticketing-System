package org.seng2050.A3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.time.LocalDate;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.sql.PreparedStatement;
import java.util.stream.Collectors;

	//record used to record the count of issues currently assigned to staff
	record staffCounts(
		int count,
		String username
	) {}	
	
	//record used to record the uncompleted issues, with their date, username, and title
	record unCompletedIssues(
		long date,
		String username,
		String title
	) {}

@Controller
public class StatsController {
	
	//database connection
    @Autowired
    private DataSource dataSource;
	
	//main method that returns all statistic objects
	@GetMapping("/statistics")
	public ModelAndView getStats(
        @AuthenticationPrincipal UserDetails userDetails
        ) throws Exception {
		var mav = new ModelAndView("statistics");
		//queries for all category counts
		String softwareQuery = categoryQuery("Software");
		String networkQuery = categoryQuery("Network");
		String hardwareQuery = categoryQuery("Hardware");
		String emailQuery =  categoryQuery("Email");
		String accountQuery = categoryQuery("Account");	
		//used to return all software category numbers
		try (Connection connection1 = this.dataSource.getConnection();
		Statement statement1 = connection1.createStatement();
		ResultSet softwareResults = statement1.executeQuery(softwareQuery)) {
			if (softwareResults.next()) { 
				int softwareNum = softwareResults.getInt("result");
				mav.addObject("softwareNum", softwareNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//used to return all network category numbers
		try (Connection connection2 = this.dataSource.getConnection();
		Statement statement2 = connection2.createStatement();
		ResultSet networkResults = statement2.executeQuery(networkQuery)) {
			if (networkResults.next()) { 
				int networkNum = networkResults.getInt("result");
				mav.addObject("networkNum", networkNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//used to return all hardware category numbers
		try (Connection connection3 = this.dataSource.getConnection();
		Statement statement3 = connection3.createStatement();
		ResultSet hardwareResults = statement3.executeQuery(hardwareQuery)) {
			if (hardwareResults.next()) { 
				int hardwareNum = hardwareResults.getInt("result");
				mav.addObject("hardwareNum", hardwareNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//used to return all email category numbers
		try (Connection connection4 = this.dataSource.getConnection();
		Statement statement4 = connection4.createStatement();
		ResultSet emailResults = statement4.executeQuery(emailQuery)) {
			if (emailResults.next()) { 
				int emailNum = emailResults.getInt("result");
				mav.addObject("emailNum", emailNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//used to return all account category numbers
		try (Connection connection5 = this.dataSource.getConnection();
		Statement statement5 = connection5.createStatement();
		ResultSet accountResults = statement5.executeQuery(accountQuery)) {
			if (accountResults.next()) { 
				int accountNum = accountResults.getInt("result");
				mav.addObject("accountNum", accountNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//returns all status query numbers
		String newQuery = statusQuery("New");
		String inProgressQuery = statusQuery("In Progress");
		String completedQuery = statusQuery("Completed");
		String resolvedQuery = statusQuery("Resolved");
		String thirdPQuery = statusQuery("Waiting on Third Party");
		String reporterQuery = statusQuery("Waiting on Reporter");
		//used to return all new status numbers
		try (Connection connection12 = this.dataSource.getConnection();
		Statement statement12 = connection12.createStatement();
		ResultSet New_Results = statement12.executeQuery(newQuery)) {
			if (New_Results.next()) {
				int newNum = New_Results.getInt("result");
				mav.addObject("newNum", newNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//used to return all progress status numbers
		try (Connection connection6 = this.dataSource.getConnection();
		Statement statement6 = connection6.createStatement();
		ResultSet progress_Results = statement6.executeQuery(inProgressQuery)) {
			if (progress_Results.next()) {
				int progressNum = progress_Results.getInt("result");
				mav.addObject("progressNum", progressNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//used to return all complete status numbers
		try (Connection connection7 = this.dataSource.getConnection();
		Statement statement7 = connection7.createStatement();
		ResultSet Complete_Results = statement7.executeQuery(completedQuery)) {
			if (Complete_Results.next()) {
				int completeNum = Complete_Results.getInt("result");
				mav.addObject("completeNum", completeNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//used to return all resolved status numbers
		try (Connection connection8 = this.dataSource.getConnection();
		Statement statement8 = connection8.createStatement();
		ResultSet Resolve_Results = statement8.executeQuery(resolvedQuery)) {
			if (Resolve_Results.next()) {
				int resolvedNum = Resolve_Results.getInt("result");
				mav.addObject("resolvedNum", resolvedNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//used to return all thirdParty status numbers
		try (Connection connection9 = this.dataSource.getConnection();
		Statement statement9 = connection9.createStatement();
		ResultSet ThirdP_Results = statement9.executeQuery(thirdPQuery)) {
			if (ThirdP_Results.next()) {
				int ThirdPNum = ThirdP_Results.getInt("result");
				mav.addObject("ThirdPNum", ThirdPNum);
			}
		}
		//used to return all waiting on reporter status numbers
		try (Connection connection10 = this.dataSource.getConnection();
		Statement statement10 = connection10.createStatement();
		ResultSet Reporter_Results = statement10.executeQuery(reporterQuery)) {
			if (Reporter_Results.next()) {
				int reporterNum = Reporter_Results.getInt("result");
				mav.addObject("reporterNum", reporterNum);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//query used to return all status and the amount of issues they are working on
		String staffQuery = staffCountQuery();
		List<staffCounts> countsStaff = new LinkedList<>();
		try (Connection connection11 = this.dataSource.getConnection();
		Statement statement11 = connection11.createStatement();
		ResultSet staff_Results = statement11.executeQuery(staffQuery)) {
			while (staff_Results.next()) {
				int staffCount = staff_Results.getInt("count");
				String username = staff_Results.getString("username");
				staffCounts newStaff = new staffCounts(staffCount, username);
				countsStaff.add(newStaff);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//returns how many times the knowledge base has been viewed
		int viewCount  = 0;
		String KBViewQuery = "SELECT count FROM KBNum WHERE ID = 'KB1';";
		try (Connection connection13 = this.dataSource.getConnection();
		Statement statement13 = connection13.createStatement();
		ResultSet results13 = statement13.executeQuery(KBViewQuery)) {
			if (results13.next()) {
				viewCount = results13.getInt("count");
				mav.addObject("viewCount", viewCount);
			}
		}
		
		//returns all the un completed issues from database
		List<unCompletedIssues> issueList = new LinkedList<>();
		//query includes start date for ordering
		String unsolvedIssuesQuery = "SELECT username, Title, date AS startDate " +
									"FROM Issue " +
									"WHERE [Status] != 'Resolved' " +
									"ORDER BY startDate;";
		try (Connection connection14 = this.dataSource.getConnection();
		Statement statement14 = connection14.createStatement();
		ResultSet results14 = statement14.executeQuery(unsolvedIssuesQuery)) {
			//only returns the top 5 unordered issues
			for (int i=0; i < 5; i++) {
				if (results14.next()) {
					long date = results14.getLong("startDate");
					String username = results14.getString("username");
					String title = results14.getString("Title");
					unCompletedIssues currentIssue = new unCompletedIssues(date, username, title);
					issueList.add(currentIssue);
				}
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		long totalTime = 0;
		int size = 0;
		//query used to get the average time it takes to complete an issue
		String avgTimeQuery = "SELECT [Date], endDate FROM Issue WHERE endDate IS NOT NULL;";
		try (Connection connection15 = this.dataSource.getConnection();
		Statement statement15 = connection15.createStatement();
		ResultSet results15 = statement15.executeQuery(avgTimeQuery)) {
			while (results15.next()) {
				long startDate = results15.getLong("Date");
				long endDate = results15.getLong("endDate");
				long issueTimeMilliSeconds = endDate - startDate;
				totalTime += issueTimeMilliSeconds;
				size++;
			}
		}  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//calculate the average with size and totalTime
		if (size > 0) {
			long avg = totalTime / size;
			avg = avg / 1000;
			mav.addObject("avgTime", avg);
		} else {
			//if no issues have been completed return zero
			mav.addObject("avgTime", 0);
		}

		//add all objects to view for html use
		mav.addObject("unCompletedIssues", issueList);
		mav.addObject("staffCounts", countsStaff);		
		String role = userDetails.getAuthorities().stream()
		.map(GrantedAuthority::getAuthority)
		.collect(Collectors.joining(", "));
		mav.addObject("role", role);

		boolean isManager = role.contains("MANAGER");
		boolean isIt = role.contains("IT");
		boolean isUser = role.contains("USER");
        mav.addObject("isManager", isManager);
        mav.addObject("isUser", isUser);
        mav.addObject("isIt", isIt);
		mav.addObject("username", userDetails.getUsername());

        return mav;
	}
	//query used to return the amount of times a category appears in database
	public String categoryQuery(String type) {
		return "SELECT COUNT(*) AS result FROM Issue ise JOIN CategoryIssueRelationship catRel ON ise.IssueID = catRel.IssueID" +
		" JOIN Category cat on catRel.CategoryID = cat.CategoryID WHERE cat.Category = '" + type + "';";
	}
	//query used to return the amount of times a status appears in the database
	public String statusQuery(String type) {
		return "SELECT COUNT(*) AS result FROM Issue WHERE [Status] = '" + type + "';";
	}
	//query used to return all status and the amount of issues they are working on
	public String staffCountQuery() {
		return "SELECT ITStaff.username, COUNT(ITStaffIssueRelationship.IssueID) AS count FROM ITStaff" +
		" LEFT JOIN ITStaffIssueRelationship ON ITStaff.EmployeeID = ITStaffIssueRelationship.EmployeeID GROUP BY ITStaff.username;";
	}
}

	