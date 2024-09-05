package org.seng2050.A3;
import java.util.Date;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Controller
public class ReportController {
	
	//database connection
    @Autowired
    private DataSource dataSource;
	
	//record used to record categories to/from database
    record Category(int CategoryID, String Category) {}

	//record used to record sub categories to/from database
    record SubCategory(int SubCategoryID, int CategoryID, String SubCategory) {}

	//method used to return categories from database for report selection
    @GetMapping("/report")
    public ModelAndView showIssues(@AuthenticationPrincipal UserDetails userDetails) throws Exception {
        List<Category> categories = new LinkedList<>();
        List<SubCategory> subCategories = new LinkedList<>();

        try (Connection connection = this.dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String categoryQuery = "SELECT CategoryID, Category FROM Category";
            try (ResultSet categoryResults = statement.executeQuery(categoryQuery)) {
                while (categoryResults.next()) {
                    int categoryID = categoryResults.getInt("CategoryID");
                    String category = categoryResults.getString("Category");
                    categories.add(new Category(categoryID, category));
                }
            }  catch (SQLException e) {
                e.printStackTrace();
                String error = "An error has occurred: " + e.getMessage();
                return new ModelAndView("redirect:/error?error=" + error);
            }
			//returns all sub categories aswell
            String subcategoryQuery = "SELECT SubCategoryID, CategoryID, SubCategory FROM SubCategory";
            try (ResultSet subcategoryResults = statement.executeQuery(subcategoryQuery)) {
                while (subcategoryResults.next()) {
                    int subCategoryID = subcategoryResults.getInt("SubCategoryID");
                    int categoryID = subcategoryResults.getInt("CategoryID");
                    String subCategory = subcategoryResults.getString("SubCategory");
                    subCategories.add(new SubCategory(subCategoryID, categoryID, subCategory));
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
		//modelview used to return all objects to html page
        var mav = new ModelAndView("report");
        mav.addObject("Categories", categories);
        mav.addObject("SubCategories", subCategories);
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

	//method used to submit report form to database
    @PostMapping("/SubmitReport")
    public String handleFormSubmission(
            @RequestParam String title,
            @RequestParam int[] categories,
            @RequestParam int[] subCategories,
            @RequestParam String description,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        try (Connection connection = this.dataSource.getConnection()) {
            //Gets date 
            Date currentDate = new Date();
            long timeInMillis = currentDate.getTime();

            //Gets unique ID
            int id = getId(timeInMillis);
            String username = userDetails.getUsername();


            //1ST insert statement inserts issue 
            String firstQuery = "INSERT INTO Issue (IssueID, Title, Description, Status, date, username) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement firstStatement = connection.prepareStatement(firstQuery)) {
    
                //Sets parameters
                firstStatement.setInt(1, id);
                firstStatement.setString(2, title);
                firstStatement.setString(3, description);
                firstStatement.setString(4, "New");
                firstStatement.setLong(5, timeInMillis);
                firstStatement.setString(6, username);
    
                //Execute 
                firstStatement.executeUpdate();
            }
    
            //2ND insert statement, inserts categories
            String secondQuery = "INSERT INTO CategoryIssueRelationship (IssueID, CategoryID) VALUES (?, ?)";
            try (PreparedStatement secondStatement = connection.prepareStatement(secondQuery)) {
                for (int category : categories) {
                    //Sets parameters
                    secondStatement.setInt(1, id);
                    secondStatement.setInt(2, category);    
                    //Batches statements into relation table
                    secondStatement.addBatch();
                }
                secondStatement.executeBatch();//Executes insert statement
            }
            

            //3RD statement, inserts sub categories
            String thirdQuery = "INSERT INTO SubcategoryIssueRelationship (IssueID, SubCategoryID) VALUES (?, ?)";
            try (PreparedStatement thirdStatement = connection.prepareStatement(thirdQuery)) {
                for (int subcategory : subCategories) {
                    //Sets parameters
                    thirdStatement.setInt(1, id); 
                    thirdStatement.setInt(2, subcategory);    
                    //Batches statements
                    thirdStatement.addBatch();
                }
                thirdStatement.executeBatch();//Execute
            }

            //4th statement, insert default notification when an issue is reported
            String fourthQuery = "INSERT INTO [Notification] (NotificationID, [Date], [Status], Precedence, [Message], username) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement fourthStatement = connection.prepareStatement(fourthQuery)) {
                    //Sets parameters
                    fourthStatement.setInt(1, id); 
                    String status = "unseen";
                    Date date = new Date();
                    fourthStatement.setLong(2, date.getTime());  
                    fourthStatement.setString(3, status);  
                    fourthStatement.setInt(4, 1);  // default precedence to 1 

                    String message = "Your issue has been recieved, you will be notified at various stages of progress";
                    
                    fourthStatement.setString(5, message); 
                    fourthStatement.setString(6, username); 
            
                    //Batches statements
                    fourthStatement.addBatch();
                    fourthStatement.executeBatch(); //Execute
            }

    
        } catch (SQLException e) {
            e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return "redirect:/error?error=" + error;
        } catch (Exception e) {
            e.printStackTrace();
            String error = "An error has occurred: " + e.getMessage();
        	return "redirect:/error?error=" + error;
        }
    
        return "redirect:/issues";
    }
    
    //returns unique id using unix timestamp
    public int getId(long timeInMillis) {
        //convert time to a string getting the last 10 digits
        String timeString = Long.toString(timeInMillis);
        String last10Digits = timeString.length() > 10 ? timeString.substring(timeString.length() - 10) : timeString;
    
        //convert the 10 digits back to an int
        int issueID;
        try {
            issueID = Integer.parseInt(last10Digits);
        } catch (NumberFormatException e) {
            issueID = (int) (timeInMillis % Integer.MAX_VALUE); //fallback
        }
    
        return issueID;
    }
    
}
