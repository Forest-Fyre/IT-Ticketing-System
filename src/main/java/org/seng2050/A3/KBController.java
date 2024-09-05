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
public class KBController {

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
   
    //method used to return all completed knowledge base issues
	@GetMapping("/KB")
	public ModelAndView showIssues(
        @RequestParam(required = false) String titleFilter, 
        @AuthenticationPrincipal UserDetails userDetails,
        Model model
        ) throws Exception {

		List<issue2> issues = new LinkedList<>();
		//adds a page view upon page loading
		String viewQuery = "UPDATE KBNum SET count = count + 1 WHERE ID = 'KB1';";
		try (Connection connectionV = this.dataSource.getConnection();
		PreparedStatement statementV = connectionV.prepareStatement(viewQuery)) {
			statementV.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}	
		
		//selects all Issue details where status is resolved
        String query = "SELECT IssueID, Title, Description, Status, date, username FROM Issue WHERE Status = ?";
		//query used to check title filter
		if (titleFilter != null && !titleFilter.isEmpty()) {
			query += " AND Title LIKE ?";
		}
		query += "ORDER BY [Date] DESC;";
		try (Connection connection1 = this.dataSource.getConnection();
		PreparedStatement statement = connection1.prepareStatement(query)) {
			statement.setString(1, "Resolved");
			if (titleFilter != null && !titleFilter.isEmpty()) {
				statement.setString(2, '%' + titleFilter + '%');
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
			statement.close();
		}  catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
        var mav = new ModelAndView("KB");
		//objects used to for corresponding html
		mav.addObject("role", getRoles(userDetails));
        mav.addObject("issues", issues);
		String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
		mav.addObject("role", role);

		boolean isManager = role.contains("MANAGER");
		boolean isIt = role.contains("IT");
		boolean isUser = role.contains("USER");
        model.addAttribute("isManager", isManager);
        model.addAttribute("isUser", isUser);
        model.addAttribute("isIt", isIt);
		mav.addObject("username", userDetails.getUsername());

		
        return mav;
    }

	public String getRoles(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
    }
}