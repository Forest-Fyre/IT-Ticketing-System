package org.seng2050.A3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

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

@Controller
public class IssuesController {
	
	//database connection
    @Autowired
    private DataSource dataSource;
	
	//record used to record issues to/from database
   record issue2(
   int issueID,
	String title,
	String description,
	String status,
    long date,
    String username
   ) {}
	
	//method used to show all issues
	@GetMapping("/issues")
	public ModelAndView showIssues(
        @RequestParam(required = false) String titleFilter, 
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
        ) throws Exception {

		List<issue2> issues = new LinkedList<>();
        //returns current signed in username
        String currentUsername = userDetails.getUsername();

		String role = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(", "));
		boolean manager = false;
		String query = "";
		//query used to return all roles
		if (role.equals("ROLE_MANAGER")) {
			manager = true;
			query ="SELECT IssueID, Title, Description, Status, date, username FROM Issue WHERE Status = 'New' ";
		} else if (role.equals("ROLE_IT")) {
			query = "SELECT Issue.IssueID, Issue.Title, Issue.Description, Issue.Status, Issue.Date, Issue.username" +
					" FROM Issue JOIN ITStaffIssueRelationship ON Issue.IssueID = ITStaffIssueRelationship.IssueID JOIN ITStaff" +
					"ON ITStaffIssueRelationship.EmployeeID = ITStaff.EmployeeID WHERE ITStaff.username = ?";	
		} else {
			query = "SELECT IssueID, Title, Description, Status, date, username FROM Issue WHERE username = ?";
		}
        
		if (titleFilter != null && !titleFilter.isEmpty()) {
			query += " AND Title LIKE ?";
		}
		//query orders by date
		query += "ORDER BY [Date] DESC;";
		try (Connection connection = this.dataSource.getConnection();
		PreparedStatement statement = connection.prepareStatement(query)) {
			if (!manager) { statement.setString(1, currentUsername); }
			//if titleFilter is clicked, order by filter
			if (titleFilter != null && !titleFilter.isEmpty()) {
				if (manager) { statement.setString(1, '%' + titleFilter + '%'); }
				else { statement.setString(2, '%' + titleFilter + '%'); }
			}
			try (ResultSet results = statement.executeQuery()) {
				while (results.next()) {
					int issueID = results.getInt("IssueID");
					String title = results.getString("Title");
					String description = results.getString("Description");
					String status = results.getString("Status");
					long date = results.getLong("date");
					String username = results.getString("username");
					issues.add(new issue2(issueID, title, description, status, date, username));
				}
			}  catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
			return new ModelAndView("redirect:/error?error=" + error);
		}    
		
        var mav = new ModelAndView("issues");;
		
		mav.addObject("role", role);
		//objects used for html 
		boolean isManager = role.contains("MANAGER");
		boolean isIt = role.contains("IT");
		boolean isUser = role.contains("USER");
        model.addAttribute("isManager", isManager);
        model.addAttribute("isUser", isUser);
        model.addAttribute("isIt", isIt);
        mav.addObject("issues", issues);
		mav.addObject("username", userDetails.getUsername());

        return mav;
    }

	//method used to return a users assigned issues
	@GetMapping("/a_issues")
	public ModelAndView showa_issues(
        @RequestParam(required = false) String titleFilter, 
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
        ) throws Exception {

		List<issue2> issues = new LinkedList<>();
        
        String currentUsername = userDetails.getUsername();

		String role = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(", "));
		//query used to return all assigned issues
		String query = "SELECT Issue.IssueID, Issue.Title, Issue.Description, Issue.Status, Issue.Date, Issue.username " +
            "FROM Issue " +
            "JOIN ITStaffIssueRelationship ON Issue.IssueID = ITStaffIssueRelationship.IssueID " +
            "JOIN ITStaff ON ITStaffIssueRelationship.EmployeeID = ITStaff.EmployeeID " +
            "WHERE ITStaff.username = ?";
        if (titleFilter != null && !titleFilter.isEmpty()) {
			query += " AND Title LIKE ?";
		}
		//orders query by date
		query += "ORDER BY [Date] DESC;";
		try (Connection connection = this.dataSource.getConnection();
		PreparedStatement statement = connection.prepareStatement(query)) {
			statement.setString(1, currentUsername);
			if (titleFilter != null && !titleFilter.isEmpty()) {
				statement.setString(2, '%' + titleFilter + '%');
			}
			try (ResultSet results = statement.executeQuery()) {
				while (results.next()) {
					int issueID = results.getInt("IssueID");
					String title = results.getString("Title");
					String description = results.getString("Description");
					String status = results.getString("Status");
					long date = results.getLong("Date");
					String username = results.getString("username");
					issues.add(new issue2(issueID, title, description, status, date, username));
				}
			}  catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
			return new ModelAndView("redirect:/error?error=" + error);
		}		
        var mav = new ModelAndView("a_issues");;
		
		mav.addObject("role", role);
		//objects used for html 
		boolean isManager = role.contains("MANAGER");
		boolean isIt = role.contains("IT");
		boolean isUser = role.contains("USER");
        model.addAttribute("isManager", isManager);
        model.addAttribute("isUser", isUser);
        model.addAttribute("isIt", isIt);
		mav.addObject("username", userDetails.getUsername());

        mav.addObject("a_issues", issues);
        return mav;
    }

	//method used to return all un assigned issues
	@GetMapping("/ua_issues")
	public ModelAndView showua_issues(
        @RequestParam(required = false) String titleFilter, 
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
        ) throws Exception {

		List<issue2> issues = new LinkedList<>();
        
		String role = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(", "));
		//query that returns all new issues
		String query = "SELECT IssueID, Title, Description, Status, Date, username FROM Issue WHERE Status = 'New'";
		
		if (titleFilter != null && !titleFilter.isEmpty()) {
			query += " AND Title LIKE ?";
		}
		//orders all by date
		query += "ORDER BY [Date] DESC;";
		try (Connection connection = this.dataSource.getConnection();
		PreparedStatement statement = connection.prepareStatement(query)) {
			if (titleFilter != null && !titleFilter.isEmpty()) {
				statement.setString(1, '%' + titleFilter + '%');
			}
			try (ResultSet results = statement.executeQuery()) {
				while (results.next()) {
					int issueID = results.getInt("IssueID");
					String title = results.getString("Title");
					String description = results.getString("Description");
					String status = results.getString("Status");
					long date = results.getLong("Date");
					String username = results.getString("username");
					issues.add(new issue2(issueID, title, description, status, date, username));
				}
			}  catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
			}
		}  catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
			return new ModelAndView("redirect:/error?error=" + error);
		}     
		
        var mav = new ModelAndView("ua_issues");;
		//returns all object needed for html
		boolean isManager = role.contains("MANAGER");
		boolean isIt = role.contains("IT");
		boolean isUser = role.contains("USER");
        model.addAttribute("isManager", isManager);
        model.addAttribute("isUser", isUser);
        model.addAttribute("isIt", isIt);
        mav.addObject("ua_issues", issues);
		mav.addObject("username", userDetails.getUsername());
        return mav;
    }
}