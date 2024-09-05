package org.seng2050.A3;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class HomeController {

    @Autowired
    private DataSource dataSource;
	//used to record notification objects to/from database
    record Notification(
        int notificationID,
        String date,
        String status,
        int precedence,
        String message,
        String username
    ) {}
	//used to record issue objects to/from database
    record issue(
        int issueID,
        String title,
        String description,
        String status,
        String date,
        String username
    ) {}

    
	//home screen modelview
    @GetMapping("/homepage")
    public String homepage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("username", userDetails.getUsername());
        String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        model.addAttribute("roles", roles);
        
        List<Notification> notiList = new LinkedList<>();
        String currentUsername = userDetails.getUsername();
        //gets the top 3 notifications for the home page
        String query = "SELECT TOP 3 notificationID, [Date], [Status], Precedence, [Message], username FROM Notification WHERE username = ?";
        query += " AND status = 'unseen'"; //where notification status == unseen
        query += " ORDER BY Precedence DESC, [Date] DESC;";
    
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
            // create our notification list getting the top 3 most receet/highest precendence notifications
            statement.setString(1, currentUsername);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    int notificationID = results.getInt("notificationID");
                    long dateMS = results.getLong("Date");
                    String status = results.getString("Status");
                    int precedence = results.getInt("Precedence");
                    String message = results.getString("Message");
                    String username = results.getString("username");

                    Date date = new Date(dateMS);
                    SimpleDateFormat ddmmyyyy = new SimpleDateFormat("yyyy-MM-dd");
                    String stringDate = ddmmyyyy.format(date);

                    notiList.add(new Notification(notificationID, stringDate, status, precedence, message, username));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                String error = "An error has occurred: " + e.getMessage();
                return "redirect:/error?error=" + error;
            }
        }  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return "redirect:/error?error=" + error;
		}

        // create our list containing the most recent issue
        List<issue> issueList = new LinkedList<>();
        query = "SELECT TOP 1 IssueID, Title, Description, Status, Date, username FROM Issue WHERE username = ? ORDER BY [Date] DESC";
        query += ";";
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
			//returns issues ordered by date
            statement.setString(1, currentUsername);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    int issueID = results.getInt("IssueID");
                    String title = results.getString("Title");
                    String description = results.getString("Description");
                    String status = results.getString("Status");
                    long dateMS = results.getLong("Date");

                    Date date = new Date(dateMS);
                    SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd-MM-yyyy"); //aaa = am/pm z = timezone(shortform)
                    String stringDate = ddmmyyyy.format(date);

                    String username = results.getString("username");
                    issueList.add(new issue(issueID, title, description, status, stringDate, username));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                String error = "An error has occurred: " + e.getMessage();
                return "redirect:/error?error=" + error;
            }
        }  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return "redirect:/error?error=" + error;
		}


        // role attribute checks
        boolean isManager = roles.contains("MANAGER");
		boolean isIt = roles.contains("IT");
		boolean isUser = roles.contains("USER");
        model.addAttribute("isManager", isManager);
        model.addAttribute("isUser", isUser);
        model.addAttribute("isIt", isIt);
        model.addAttribute("notiList", notiList);
        model.addAttribute("issueList", issueList);
        return "homepage";
    }
  
    @GetMapping("/login")
    public String login() {
        return "login";
    }

	//used to mark a notification as seen 
    @PostMapping("/mark_seen")
    public String markAsSeen(@RequestParam("notificationID") int notificationID) {
        try (Connection connection = this.dataSource.getConnection()) {
			String query = "UPDATE Notification SET Status = 'seen' WHERE notificationID = ?;";
			try (PreparedStatement statementI = connection.prepareStatement(query)) {
				statementI.setInt(1, notificationID);	
				statementI.executeUpdate();
				statementI.close();
			}  catch (SQLException e) {
				e.printStackTrace();
			}
			connection.close();
		}  catch (SQLException e) {
			e.printStackTrace();
		}
		//redirects to homepage
        return "redirect:/homepage"; 
    }
}
