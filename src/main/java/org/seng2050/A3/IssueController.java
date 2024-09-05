package org.seng2050.A3;
import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Random;
import java.sql.PreparedStatement;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
@Controller
public class IssueController {

    @Autowired
    private DataSource dataSource;
		
	//used to record issues objects to/from database
   record issue2(
	int issueID,
	String title,
	String description,
	String status,
    long date,
    String username
   ) {}
   
   //used to record issue categories objects to/from database
   record issueCategory(
	int categoryID,
	String category
   ) {}
   
   //used to record issue sub categories objects to/from database
   record issueSubCategory(
	int categoryID,
	String subCategory
   ) {}
   
   //used to record issue comment objects to/from database
   record comment2(
	int commentID,
	LocalDate date,
	String username,
	String authority,
	String commentData,
	int isHighlighted
   ) {}

	//method used to return the issue and its comments to html
	@GetMapping("/issue")
	@PostMapping("/issue")
	public ModelAndView getIssueGetComments(
		@RequestParam(required = false) String issueID, 
		@AuthenticationPrincipal UserDetails userDetails,
		HttpSession session) throws Exception {

		session.setAttribute("issueID", issueID); //sets the session attribute to current issueID

		List<comment2> comments = new LinkedList<>();		

		//returns the issue comments that connects to the issueID input parameter
		String query = "SELECT commentID, date, Comment, username, isHighlighted FROM Comment WHERE issueID = ?;";
		try (Connection connection1 = this.dataSource.getConnection();
		PreparedStatement statement1 = connection1.prepareStatement(query)) {
			statement1.setInt(1, Integer.parseInt(issueID));
			try (ResultSet results1 = statement1.executeQuery()) {
				while (results1.next()) {
					int commentID = results1.getInt("commentID");
					int dateInt = results1.getInt("date");
					String commentUsername = results1.getString("username");
					String stringDate = String.valueOf(dateInt);
					if (stringDate.length() == 7) {
						stringDate = "0" + stringDate;
					}
					String commentData = results1.getString("Comment");
					int isHighlighted = results1.getBoolean("isHighlighted") ? 1 : 0;
					DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMyyyy");
					LocalDate date = LocalDate.parse(stringDate, format);

					String authority = "";
					//returns the authority of comment username
					String authQuery = "SELECT a.authority FROM authorities a WHERE a.username = ?;";
					try (PreparedStatement statement5 = connection1.prepareStatement(authQuery)) {
						statement5.setString(1, commentUsername);
						try (ResultSet results5 = statement5.executeQuery()) {
							if (results5.next()) {
								authority = results5.getString("authority");
							}
						} catch (SQLException e) {
							e.printStackTrace();
							String error = "An error has occurred: " + e.getMessage();
        					return new ModelAndView("redirect:/error?error=" + error);
							
						}
						statement5.close();
					} catch (SQLException e) {
						e.printStackTrace();
						String error = "An error has occurred: " + e.getMessage();
        				return new ModelAndView("redirect:/error?error=" + error);
						
						
					}
					//adds comment to comment list
					comments.add(new comment2(commentID, date, commentUsername, authority, commentData, isHighlighted));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
				
			}
			statement1.close();
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//returns the current issue that is linked with the foreign key of issueID
		issue2 currentIssue = null;
		query = "SELECT IssueID, Title, Description, Status, date, username FROM Issue WHERE IssueID = ?;";
		try (Connection connection2 = this.dataSource.getConnection();
		PreparedStatement statement2 = connection2.prepareStatement(query)) {
			statement2.setInt(1, Integer.parseInt(issueID));
			try (ResultSet results2 = statement2.executeQuery()) {
				if (results2.next()) {
					int issuesID = results2.getInt("IssueID");
					String title = results2.getString("Title");
					String description = results2.getString("Description");
					String status = results2.getString("Status");
					long date = results2.getLong("date");
					String username = results2.getString("username");
					currentIssue = new issue2(issuesID, title, description, status, date, username);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
				
			}
			statement2.close();
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
			
		}
		//returns the categories linked to the issueID
		List<issueCategory> categories = new LinkedList<>();
		query = "SELECT cat.CategoryID, cat.Category FROM Category cat JOIN CategoryIssueRelationship rel ON cat.CategoryID = rel.CategoryID " +
			"WHERE rel.IssueID = ?;";
		try (Connection connection3 = this.dataSource.getConnection();
		PreparedStatement statement3 = connection3.prepareStatement(query)) {
			statement3.setInt(1, Integer.parseInt(issueID));
			try (ResultSet results3 = statement3.executeQuery()) {
				while (results3.next()) {
					int categoryID = results3.getInt("CategoryID");
					String category = results3.getString("Category");
					categories.add(new issueCategory(categoryID, category)); //adds to category list
				}
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
			}
			statement3.close();
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}

		//returns the sub categories linked to the issueID
		List<issueSubCategory> subCategories = new LinkedList<>();
		String innerQuery = "SELECT subcat.SubCategory, subcat.CategoryID FROM SubCategory subcat JOIN SubcategoryIssueRelationship sir ON subcat.SubCategoryID = sir.SubCategoryID" + 
		" WHERE sir.IssueID =?;";
		try (Connection connection4 = this.dataSource.getConnection();
		PreparedStatement statement4 = connection4.prepareStatement(innerQuery)) {
			statement4.setInt(1, Integer.parseInt(issueID));
			try (ResultSet results4 = statement4.executeQuery()) {
				while (results4.next()) {
					int catID = results4.getInt("CategoryID");
					String subCategory = results4.getString("SubCategory");
					subCategories.add(new issueSubCategory(catID, subCategory)); //adds to sub category list
				}
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
			}
			statement4.close();
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		//	returns IT staff who can be assigned to work on issue
		List<String> staffUsernames = new LinkedList<>();
		try (Connection connection5 = this.dataSource.getConnection();
		Statement statement = connection5.createStatement()) {
			String StaffQuery = "SELECT username FROM ITStaff";
			try (ResultSet staffResults = statement.executeQuery(StaffQuery)) {
				while (staffResults.next()) {
					String username = staffResults.getString("username");
					staffUsernames.add(username);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		// returns the employeeID connected to the http session username signed in
		String signedinUsername = userDetails.getUsername();
		int usernameEmployeeID = 0;
		String queryF = "SELECT EmployeeID FROM ITStaff WHERE username = ?;";
		try (Connection connectionF = this.dataSource.getConnection();
		PreparedStatement statementF = connectionF.prepareStatement(queryF)) {
			statementF.setString(1, signedinUsername);
			try (ResultSet resultsF = statementF.executeQuery()) {
				if (resultsF.next()) { 
					usernameEmployeeID = resultsF.getInt("EmployeeID");
				}
			} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
	
		// checks if a user is currently assigned to the issue with the issueID parameter
		boolean userAssigned  = false;
		String queryW = "SELECT * FROM ITStaffIssueRelationship WHERE IssueID = ? AND EmployeeID = ?;";
		try (Connection connectionW = this.dataSource.getConnection();
		PreparedStatement statementW = connectionW.prepareStatement(queryW)) {
			statementW.setInt(1, Integer.parseInt(issueID));
			statementW.setInt(2, usernameEmployeeID);
			try (ResultSet resultsW = statementW.executeQuery()) {
				if (resultsW.next()) { userAssigned = true; }
			} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
			} 
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		
		
		//all objects that are returned to issue.html
		var mav = new ModelAndView("issue");
		mav.addObject("isAssigned", userAssigned);
		mav.addObject("signedinUsername", signedinUsername);
		mav.addObject("comments", comments);
		mav.addObject("issue", currentIssue);
		mav.addObject("categories", categories);
		mav.addObject("subCategories", subCategories);
		mav.addObject("unassignedStaff", staffUsernames);
		String role = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(", "));
		mav.addObject("role", role); //returns the role of signed in user

		boolean isManager = role.contains("MANAGER");
		boolean isIt = role.contains("IT");
		boolean isUser = role.contains("USER");
        mav.addObject("isManager", isManager);
        mav.addObject("isUser", isUser);
        mav.addObject("isIt", isIt);
		mav.addObject("username", userDetails.getUsername());

		return mav;
	}

	//used to a comment to the current Issue
	@PostMapping("/issue")
	public ModelAndView addComment(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam String addComment,
		HttpSession session) throws Exception {
			
		addComment = addComment.trim();
		String issueID = (String) session.getAttribute("issueID");	
		if (addComment.length() == 0) { return getIssueGetComments(issueID, userDetails, session); }
		String username = userDetails.getUsername();	

		String date = LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
		int intDate = Integer.parseInt(date);
		int commentID =  0;
		//query used to check if new commentID is valid
		String checkQuery = "SELECT * FROM Comment WHERE commentID = ?;";
		try (Connection connection3 = dataSource.getConnection();
			PreparedStatement statement1 = connection3.prepareStatement(checkQuery)) {
			Date currentDate = new Date();
			do {
				long timeInMillis = currentDate.getTime();
				commentID = getId(timeInMillis);
				statement1.setInt(1, commentID);
				try (ResultSet resultSet1 = statement1.executeQuery()) {
					//if valid break loop, else regenerate a new commentID
					if (!resultSet1.next()) {
						break; 
					}
				} catch (SQLException e) {
					e.printStackTrace();
					String error = "An error has occurred: " + e.getMessage();
					return new ModelAndView("redirect:/error?error=" + error);
				}
			} while (true);
			statement1.close();
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
			return new ModelAndView("redirect:/error?error=" + error);
		}
		//	query used to insert a new comment into database
		String query = "INSERT INTO Comment (commentID, issueID, date, Comment, isHighlighted, username) VALUES (?, ?, ?, ?, 0, ?);";
		try (Connection connection4 = dataSource.getConnection();
		PreparedStatement statement2 = connection4.prepareStatement(query)) {
			statement2.setInt(1, commentID);
			statement2.setInt(2, Integer.parseInt(issueID));
			statement2.setInt(3, intDate);
			statement2.setString(4, addComment);
			statement2.setString(5, username);
			statement2.executeUpdate();
			//sends notifications out to user and assigned staff
			NotifyIssueWorker(Integer.parseInt(issueID), 2, "Issue has recieved additional comment, please review", userDetails.getUsername(), connection4);
			NotifyIssueCreator(Integer.parseInt(issueID), 2, "Issue has recieved additional comment, please review", userDetails.getUsername(), connection4);
			statement2.close();
		}  catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		String IssueUsername = "";
		String IssueStatus = "";
		
		//query used to return the username and status connected to the issue 
		String nameQuery = "SELECT username, status FROM Issue WHERE issueID = ?;";
		try (Connection connectionL = dataSource.getConnection();
		PreparedStatement statementL = connectionL.prepareStatement(nameQuery)) {
			statementL.setInt(1, Integer.parseInt(issueID));
			try (ResultSet results10 = statementL.executeQuery()) {
				if (results10.next()) {
					IssueUsername = results10.getString("username");
					IssueStatus = results10.getString("status");	//this variable is used to check issue status
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
		
		//if the issue isn't new and the username returned is the reported user, when a comment is added, change
		// status to 'Waiting on Third Party'
		if (IssueUsername != "" && !IssueStatus.equals("New") && IssueUsername.equals(username)) {	
			try (Connection connection = this.dataSource.getConnection()) {
				String queryStat = "UPDATE Issue SET Status = 'Waiting on Third Party' WHERE IssueID = ?;";
				try (PreparedStatement statementI = connection.prepareStatement(queryStat)) {
					statementI.setInt(1, Integer.parseInt(issueID));
					statementI.executeUpdate();
					NotifyIssueWorker(Integer.parseInt(issueID), 2, "Issue status updated (waiting on third party)", userDetails.getUsername(), connection);
					NotifyIssueCreator(Integer.parseInt(issueID), 2, "Issue status updated (waiting on third party)", userDetails.getUsername(), connection);
					statementI.close();
				}  catch (SQLException e) {
					e.printStackTrace();
					String error = "An error has occurred: " + e.getMessage();
					return new ModelAndView("redirect:/error?error=" + error);
				}
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
				return new ModelAndView("redirect:/error?error=" + error);
			}
		} //if the issue isn't new and the username returned is the reported user, when a comment is added, change status
		// to 'Waiting on Reporter'
		else if (IssueUsername != "" && !IssueStatus.equals("New") && !IssueUsername.equals(username)) {
			try (Connection connection = this.dataSource.getConnection()) {
				String queryStat = "UPDATE Issue SET Status = 'Waiting on Reporter' WHERE IssueID = ?;";
				try (PreparedStatement statementI = connection.prepareStatement(queryStat)) {
					statementI.setInt(1, Integer.parseInt(issueID));
					statementI.executeUpdate();
					NotifyIssueWorker(Integer.parseInt(issueID), 2, "Issue status updated (waiting on reporter)", userDetails.getUsername(), connection);
					NotifyIssueCreator(Integer.parseInt(issueID), 2, "Issue requires additional information (waiting on reporter)", userDetails.getUsername(), connection);
					statementI.close();
				}  catch (SQLException e) {
					e.printStackTrace();
					String error = "An error has occurred: " + e.getMessage();
					return new ModelAndView("redirect:/error?error=" + error);
				}
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
				return new ModelAndView("redirect:/error?error=" + error);
			}	
		}

		// Redirect to the same page to show the updated comments
		return getIssueGetComments(issueID, userDetails, session);
	}

	// method used to highlight a comment/ check highlighted comments
	@PostMapping("/highlightComment")
	public ModelAndView highlightComment(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam int commentID,
		@RequestParam String isHighlighted,
		HttpSession session) throws Exception {	
			
		String issueID = (String) session.getAttribute("issueID");
		int highlighted = "1".equals(isHighlighted) ? 0 : 1;

		String query = "UPDATE Comment " + 
						"SET isHighlighted = ?"+ 
						" WHERE CommentID = ?;";
		try (Connection connection = dataSource.getConnection();
		PreparedStatement statement2 = connection.prepareStatement(query)) {
			statement2.setInt(1, highlighted);
			statement2.setInt(2, commentID);
			
			statement2.executeUpdate();
			statement2.close();
		}  catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);

		}

		// Redirect to the same page to show the updated comments
		return getIssueGetComments(issueID, userDetails, session);
	}

	//manager assigns staff to an issue
	@PostMapping("/assignStaff")
	public ModelAndView assignStaff(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam String username,
		HttpSession session) throws Exception {
			
		String issueID = (String) session.getAttribute("issueID");	

		int employeeID = 0;
		//query returns ITStaff username from session to select
		String idQuery = "SELECT EmployeeID FROM ITStaff WHERE username = ?";
		try (Connection connection9 = this.dataSource.getConnection();
		PreparedStatement statement9 = connection9.prepareStatement(idQuery)) {
			statement9.setString(1, username);
			try (ResultSet results = statement9.executeQuery()) {
				if (results.next()) {
					employeeID = results.getInt("EmployeeID");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
			}
		} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
		}
		//inserts the EmployeeID and IssueID into relationship table to record assignment		
		try (Connection connection = this.dataSource.getConnection()) {
			String query = "INSERT INTO ITStaffIssueRelationship (EmployeeID, IssueID) VALUES (?, ?);";
			try (PreparedStatement statementS = connection.prepareStatement(query)) {
				statementS.setInt(1, employeeID);
				statementS.setInt(2,  Integer.parseInt(issueID));
				statementS.executeUpdate();
				statementS.close();
				//changes issue status to 'In Progress'
				String newStatusQuery = "UPDATE Issue SET Status = 'In Progress' WHERE IssueID = ?;";
				try (PreparedStatement statementX = connection.prepareStatement(newStatusQuery)) {
					statementX.setInt(1, Integer.parseInt(issueID));
					statementX.executeUpdate();
					//send notifications out to connected users
					NotifyIssueWorker(Integer.parseInt(issueID), 2, "You have been assigned work", userDetails.getUsername(), connection);
					NotifyIssueCreator(Integer.parseInt(issueID), 2, "Your issue is being worked on", userDetails.getUsername(), connection);
					statementX.close();
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
			connection.close();
		}  catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
		}
		return getIssueGetComments(issueID, userDetails, session);
	}

	//staff self assign to an issue 
	@PostMapping("/selfAssign")
	public ModelAndView selfAssign(
		@AuthenticationPrincipal UserDetails userDetails,
		HttpSession session) throws Exception {
			
		String issueID = (String) session.getAttribute("issueID");  

		String username = (String) userDetails.getUsername(); 

		int employeeID = 0;
		//query returns ITStaff username from session to select
		String idQuery = "SELECT EmployeeID FROM ITStaff WHERE username = ?";
		try (Connection connection9 = this.dataSource.getConnection();
		PreparedStatement statement9 = connection9.prepareStatement(idQuery)) {
			statement9.setString(1, username);
			try (ResultSet results = statement9.executeQuery()) {
				if (results.next()) {
					employeeID = results.getInt("EmployeeID");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
				return new ModelAndView("redirect:/error?error=" + error);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
			return new ModelAndView("redirect:/error?error=" + error);
		}
				
		try (Connection connection = this.dataSource.getConnection()) {
			String query = "INSERT INTO ITStaffIssueRelationship (EmployeeID, IssueID) VALUES (?, ?);";
			try (PreparedStatement statementS = connection.prepareStatement(query)) {
				statementS.setInt(1, employeeID);
				statementS.setInt(2,  Integer.parseInt(issueID));
				statementS.executeUpdate();
				statementS.close();
				//changes issue status to 'In Progress'
				String newStatusQuery = "UPDATE Issue SET Status = 'In Progress' WHERE IssueID = ?;";
				try (PreparedStatement statementX = connection.prepareStatement(newStatusQuery)) {
					statementX.setInt(1, Integer.parseInt(issueID));
					statementX.executeUpdate();
					//send notifications out to connected users
					NotifyIssueWorker(Integer.parseInt(issueID), 2, "You have been assigned a new issue to complete", userDetails.getUsername(), connection);
					NotifyIssueCreator(Integer.parseInt(issueID), 2, "Your issue is being worked on", userDetails.getUsername(), connection);
					statementX.close();
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
			connection.close();
		}  catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
			return new ModelAndView("redirect:/error?error=" + error);
		}

		return getIssueGetComments(issueID, userDetails, session);
	}

	//method used to change issue status to complete
	@PostMapping("/completeStatus")
	public ModelAndView completeStatus(
		@AuthenticationPrincipal UserDetails userDetails,
		HttpSession session) throws Exception {
			
		String issueID = (String) session.getAttribute("issueID");	
			
		try (Connection connection = this.dataSource.getConnection()) {
			String Statquery = "UPDATE Issue SET Status = 'Completed' WHERE IssueID = ?;";
			try (PreparedStatement statementI = connection.prepareStatement(Statquery)) {
				statementI.setInt(1, Integer.parseInt(issueID));
				statementI.executeUpdate();
				//send notifications out to connected users
				NotifyIssueWorker(Integer.parseInt(issueID), 2, "You have submitted a solution, awaiting approval", userDetails.getUsername(), connection);
				NotifyIssueCreator(Integer.parseInt(issueID), 2, "Issue requires accept or reject of solution", userDetails.getUsername(), connection);
				statementI.close();
			}  catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
			}
			connection.close();
		}  catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		return getIssueGetComments(issueID, userDetails, session);
	}
	
	//method used to change issue status to accepted form complete
	@PostMapping("/acceptSolution")
	public ModelAndView acceptSolution(
		@RequestParam String issueID,
		@AuthenticationPrincipal UserDetails userDetails,
		HttpSession session) throws Exception {
			
		String issueIDSession = (String) session.getAttribute("issueID");	

		if (!issueIDSession.equals(issueID) || issueIDSession == null) {
			throw new IllegalArgumentException("Issue ID is invalid for request");
		}	
		Date currentDate = new Date();
        long timeInMillis = currentDate.getTime();
		
		try (Connection connection = this.dataSource.getConnection()) {
			//changes issue status to 'resolved' and set endDate to current date
			String query = "UPDATE Issue SET Status = 'resolved', endDate = ? WHERE IssueID = ?;";
			try (PreparedStatement statementI = connection.prepareStatement(query)) {
				statementI.setLong(1, timeInMillis);
				statementI.setInt(2, Integer.parseInt(issueID));
				statementI.executeUpdate();
				//send notifications out to connected users
				NotifyIssueWorker(Integer.parseInt(issueID), 2, "Issue resolved and added to knowledge base", userDetails.getUsername(), connection);
				NotifyIssueCreator(Integer.parseInt(issueID), 2, "You have accepted the solution, issue added to knowledge base", userDetails.getUsername(), connection);
				statementI.close();
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
				return new ModelAndView("redirect:/error?error=" + error);
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
			return new ModelAndView("redirect:/error?error=" + error);
		}

		//removes the relationship table to signify issue is over
		try (Connection connection2 = this.dataSource.getConnection()) {		
			String staffQuery = "DELETE FROM ITStaffIssueRelationship WHERE IssueID = ?;"; 
			try (PreparedStatement statementZ = connection2.prepareStatement(staffQuery)) {
				statementZ.setInt(1, Integer.parseInt(issueID));	
				statementZ.executeUpdate();
				statementZ.close();
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
		return getIssueGetComments(issueID, userDetails, session);
	}

	// rejects the accepted solution
	@PostMapping("/rejectSolution")
	public ModelAndView rejectSolution(
		@RequestParam String issueID,
		@AuthenticationPrincipal UserDetails userDetails,
		HttpSession session) throws Exception {
			
		String issueIDSession = (String) session.getAttribute("issueID");	

		if (!issueIDSession.equals(issueID) || issueIDSession == null) {
			throw new IllegalArgumentException("Issue ID is invalid for request");
		}	
		
		try (Connection connection = this.dataSource.getConnection()) {
			//changes the issue status to not accepted
			String query = "UPDATE Issue SET Status = 'Not Accepted' WHERE IssueID = ?;";
			try (PreparedStatement statementI = connection.prepareStatement(query)) {
				statementI.setInt(1, Integer.parseInt(issueID));	
				statementI.executeUpdate();
				//send notifications out to connected users
				NotifyIssueWorker(Integer.parseInt(issueID), 2, "Issue rejected, please review the issue", userDetails.getUsername(), connection);
				NotifyIssueCreator(Integer.parseInt(issueID), 2, "You have rejected the solution, IT will submit another solution", userDetails.getUsername(), connection);
				statementI.close();
			}  catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
        		return new ModelAndView("redirect:/error?error=" + error);
			}
			connection.close();
		}  catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
        	return new ModelAndView("redirect:/error?error=" + error);
		}
		return getIssueGetComments(issueID, userDetails, session);
	}
	
	//method used to create a notification for database
	public void createNotification(int id, int precedence, String inputMessage, String inputUsername, Connection connection){
		// get the issues Title for the message
		String issueTitle = "SELECT Title FROM Issue WHERE IssueID = ?;";
		String title = "";
		String username = "";
		try (PreparedStatement issueQuery = connection.prepareStatement(issueTitle)) {
			try (ResultSet results = issueQuery.executeQuery()) {
					while (results.next()) {
						title = results.getString("Title");
						username = results.getString("username");
					}
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
			}
		} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
		}

		// create the notification for input username
		String fourthQuery = "INSERT INTO [Notification] (NotificationID, [Date], Precedence, [Message], username) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement fourthStatement = connection.prepareStatement(fourthQuery)) {
				//Sets parameters
				Date date = new Date();

				int uniqueid = getId(date.getTime());
				fourthStatement.setInt(1, uniqueid);	

				fourthStatement.setLong(2, date.getTime());
				fourthStatement.setInt(3, precedence);  

				fourthStatement.setString(4, "Issue: " + title + " " + inputMessage); 
				fourthStatement.setString(5, inputUsername); 
		
				//Batches statements
				fourthStatement.addBatch();
				fourthStatement.executeBatch(); //Executes insert statement
				fourthStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			String error = "An error has occurred: " + e.getMessage();
		}

		// create the notification issue username (if not equal)
		if(username != inputUsername){
			String fifthQuery = "INSERT INTO [Notification] (NotificationID, [Date], Precedence, [Message], username) VALUES (?, ?, ?, ?, ?)";
			try (PreparedStatement fifthStatement = connection.prepareStatement(fifthQuery)) {
					//Sets parameters
					Date date = new Date();

					int uniqueid = getId(date.getTime());
					fifthStatement.setInt(1, uniqueid);	

					fifthStatement.setLong(2, date.getTime());
					fifthStatement.setInt(3, precedence);  

					fifthStatement.setString(4, title + " " + inputMessage); 
					fifthStatement.setString(5, username); 
			
					//Batches statements
					fifthStatement.addBatch();
					fifthStatement.executeBatch(); //Executes insert statement
					fifthStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
			}
		}
	}

	//method used to notify the issue creator
	public void NotifyIssueCreator(int id, int precedence, String inputMessage, String inputUsername, Connection connection){
		String issueTitle = "SELECT Title FROM Issue WHERE IssueID = ?;";
		String username = getIssueUsername(id);
		String fifthQuery = "INSERT INTO [Notification] (NotificationID, [Date], [Status], Precedence, [Message], username) VALUES (?, ?, ?, ?, ?, ?)";
			try (PreparedStatement fifthStatement = connection.prepareStatement(fifthQuery)) {
					//Sets parameters
					Date date = new Date();
					String status = "unseen";
					int uniqueid = getId(date.getTime());
					fifthStatement.setInt(1, --uniqueid); //change unique id value to avoid primary key conflict

					fifthStatement.setLong(2, date.getTime());
					fifthStatement.setString(3, status);
					fifthStatement.setInt(4, precedence);  

					fifthStatement.setString(5, "Issue: " + getTitle(id) + " - " + inputMessage); 
					fifthStatement.setString(6, username); 
			
					//Batches statements
					fifthStatement.addBatch();
					fifthStatement.executeBatch(); //Executes insert statement
					fifthStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
			}
	}
	//method used to notify the assigned staff
	public void NotifyIssueWorker(int id, int precedence, String inputMessage, String inputUsername, Connection connection){
		String username = getIssueStaffUsername(id);
		String fifthQuery = "INSERT INTO [Notification] (NotificationID, [Date], [Status], Precedence, [Message], username) VALUES (?, ?, ?, ?, ?, ?)";
			try (PreparedStatement fifthStatement = connection.prepareStatement(fifthQuery)) {
					//Sets parameters
					Date date = new Date();
					String status = "unseen";
					int uniqueid = getId(date.getTime());
					fifthStatement.setInt(1, uniqueid);	

					fifthStatement.setLong(2, date.getTime());
					fifthStatement.setString(3, status);
					fifthStatement.setInt(4, precedence);  

					fifthStatement.setString(5, "Issue: " + getTitle(id) + " - " + inputMessage); 
					fifthStatement.setString(6, username); 
			
					//Batches statements
					fifthStatement.addBatch();
					fifthStatement.executeBatch(); //Executes insert statement
					fifthStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
				String error = "An error has occurred: " + e.getMessage();
			}
	}

	//method used to get an id
	public int getId(long timeInMillis) {
        //convert time to a string getting the last 10 digits
        String timeString = Long.toString(timeInMillis);
        String last10Digits = timeString.length() > 10 ? timeString.substring(timeString.length() - 10) : timeString;
    
        //onvert the 10 digits back to an integer
        int issueID;
        try {
            issueID = Integer.parseInt(last10Digits);
        } catch (NumberFormatException e) {
            issueID = (int) (timeInMillis % Integer.MAX_VALUE); //fallback
        }
    
        return issueID;
    }
	
	//method used to get the issue username
	public String getIssueUsername(int issueID) {
		String username = null; 
		//Make connection
		try (Connection connection = this.dataSource.getConnection()) {
			String query = "SELECT username FROM Issue WHERE IssueID = ?;";
			try (PreparedStatement statementI = connection.prepareStatement(query)) {
				statementI.setInt(1, issueID); // Set the issueID parameter
	
				// Execute the query and get the ResultSet
				try (ResultSet results = statementI.executeQuery()) {
					if (results.next()) {
						// Retrieve the username from the ResultSet
						username = results.getString("username");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(); 
		}
	
		return username; //return null
	}

	// given an issue id return the usernme of the staff member assigned to issue
	public String getIssueStaffUsername(int issueID) {
		String username = ""; 
		int employeeID = 0;
		try (Connection connection = this.dataSource.getConnection()) {
			String query = "SELECT EmployeeID FROM ITStaffIssueRelationship WHERE IssueID = ?;";
			try (PreparedStatement statementI = connection.prepareStatement(query)) {
				statementI.setInt(1, issueID); 
				try (ResultSet results = statementI.executeQuery()) {
					if (results.next()) {
						employeeID = results.getInt("EmployeeID");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(); 
		}
		// use employeeID to get username
		try (Connection connection = this.dataSource.getConnection()) {
			String query = "SELECT username FROM ITStaff WHERE EmployeeID = ?;";
			try (PreparedStatement statementI = connection.prepareStatement(query)) {
				statementI.setInt(1, employeeID); 
				try (ResultSet results = statementI.executeQuery()) {
					if (results.next()) {
						username = results.getString("username");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(); 
		}
		return username; //return null
	}

	//method used to get the title connected to issueID parameter
	public String getTitle(int issueID){
		String title = "";
		try (Connection connection = this.dataSource.getConnection()) {
			String query = "SELECT Title FROM Issue WHERE IssueID = ?;";
			try (PreparedStatement statementI = connection.prepareStatement(query)) {
				statementI.setInt(1, issueID); 
				try (ResultSet results = statementI.executeQuery()) {
					if (results.next()) {
						title = results.getString("Title");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(); 
		}
		return title;
	}

}