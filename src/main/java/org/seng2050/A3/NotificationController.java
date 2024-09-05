package org.seng2050.A3;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.stream.Collectors;

import javax.sql.DataSource;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.text.SimpleDateFormat;


@Controller
public class NotificationController {

	//database connection
    @Autowired
    private DataSource dataSource;

	//record used to record notifications to/from database
    record Notification(
        int notificationID,
        String date,
        String status,
        int precedence,
        String message,
        String username
    ) {}

	//method used to return homepage notifications
    @GetMapping("/notifications")
    public ModelAndView homepage(
        @AuthenticationPrincipal UserDetails userDetails, 
        Model model) throws Exception {
        
        
        List<Notification> notiList = new LinkedList<>();
        String currentUsername = userDetails.getUsername();
        //query used to return unseen attributes for logged in username
        String query = "SELECT notificationID, [Date], [Status], Precedence, [Message], username FROM Notification WHERE username = ?";
        query += " AND status = 'unseen'"; //where notification status == unseen
        query += " ORDER BY Precedence DESC, [Date] DESC;";
    
        try (Connection connection = this.dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            
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
					//adds notfication to notification list
                    notiList.add(new Notification(notificationID, stringDate, status, precedence, message, username));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                String error = "An error has occurred: " + e.getMessage();
                return new ModelAndView("redirect:/error?error=" + error);
            }
        }  catch (SQLException e) {
			e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
        
        
        var mav = new ModelAndView("notifications");
        mav.addObject("username", currentUsername);
        String roles = getRoles(userDetails);
        
        boolean isManager = roles.contains("MANAGER");
		boolean isIt = roles.contains("IT");
		boolean isUser = roles.contains("USER");
        model.addAttribute("isManager", isManager);
        model.addAttribute("isUser", isUser);
        model.addAttribute("isIt", isIt);
        mav.addObject("notiList", notiList);
        
        return mav; 
    }

	//gets the roles of current signed in user
    public String getRoles(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
    }

	//method used to mark notification as seen
    @PostMapping("/Notification_mark_seen")
    public String markAsSeen(@RequestParam("notificationID") int notificationID) {
        try (Connection connection = this.dataSource.getConnection()) {
			//query changes notification status to seen 
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
        return "redirect:/notifications"; 
    }
  
}
