package database;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class EgccConnector {
	
	public static void main (String[] args) throws ParseException {
		
		// Scanner used throughout the code
		Scanner in = new Scanner(System.in);
		// credentials of the user who logs in (only works with users from the database)
		String user;
		String pass;
		// num is used in the switch statement, nowhere else
		int num;
		// placed this here because I use it a lot
		int itemID;
		// loggedInternet keeps you on the main Screen
		boolean loggedInternet = true;
		// loggedDatabase keeps you inside one users stuff
		boolean loggedDatabase = true;
		// try catch block to check the if the connection works
		try {
			// connection to the database - (Change this for your stuff)
			EgccConnector data = new EgccConnector("u208369","p208369","schema208369");
			
			while (loggedInternet) {
				// home screen
				System.out.println("Welcome to eGCC");
				System.out.println("---------------");
				System.out.println("Please login below:");
				
				// self explanatory
				System.out.print("Username: ");
				user = in.next();
				System.out.print("Password: ");
				pass = in.next();
				
				// if statement to check if the user is within the database
				if (data.login(user,pass)) {
					System.out.println("Login Successful! Welcome " + user);
					// keeping it logged in unless told otherwise
					loggedDatabase = true;
					while (loggedDatabase) {
						
						// main screen for messing with the database
						System.out.println("\nPlease enter one of the values below:");
						System.out.print("0: Change my password \n"
										  + "1: View items I bid on \n"
										  + "2: View my items \n"
										  + "3: View my purcheses \n"
										  + "4: Search by keyword \n"
										  + "5: View seller rating \n"
										  + "6: Put a new item up for auction \n"
										  + "7: Ship an item \n"
										  + "8: View highest bid \n"
										  + "9: Place a bid \n"
										  + "10: Rate a seller \n"
										  + "11: Close an auction \n"
										  + "12: Exit eGCC \n"
										  + "Input a number: ");
						num = in.nextInt();
						switch (num) {
							case 0: System.out.print("Please enter new password: ");
									String newPass = in.next();
									data.changePassword(user,newPass);
									break;
							case 1:
								data.viewMyBiddingItems();
								break;
							case 2:
								data.viewMyItems();
								break;
							case 3:
								data.viewMyPurchases();
								break;
							case 4:
								System.out.print("Enter keyword: ");
								String word = in.next();
								data.searchByKeyword(word);
								break;
							case 5:
								System.out.print("Enter the ID of the item whose seller rating you are looking for: ");
								int id = in.nextInt();
								System.out.println("\nRating: " + data.viewSellerRating(id) + "\n");
								break;
							case 6:
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
								Date todaysDate = sdf.parse(sdf.format(new Date()));
								
								String[] categories;
								System.out.print("Enter the title:");
								String title = in.next();
								System.out.print("Enter the starting bid:");
								Double startBid = in.nextDouble();
								System.out.print("Enter the endDate (format yyyy-MM-dd): ");
								String endDate = in.next();
								Date endDate2 = sdf.parse(endDate);
								if (endDate2.before(todaysDate)) {
									System.out.println("End Date must be after todays date");
									break;
								}
								
								System.out.print("Enter number of categories for this item: ");
								categories = new String[in.nextInt()];
								
								for (int i = 0; i < categories.length; i++) {
									System.out.print("Enter next category: ");
									categories[i] = in.next();
								}
								if (data.putItem(title,startBid,endDate,categories)) {
									System.out.println("\nItem has been placed for auction");
								}
								break;
							case 7:
								System.out.print("Enter item ID of the item you wish to ship: ");
								itemID = in.nextInt();
								if (data.shipItem(itemID)) {
									System.out.println("\nItem has been shipped");
								}
								break;
							case 8:
								System.out.print("Enter item ID of the item of which you want to see the highest bid: ");
								itemID = in.nextInt();
								System.out.println("\nHighest bid of " + itemID + " is " + data.viewHighestBid(itemID));
								break;
							case 9:
								System.out.print("Enter itemID of the item you wish to bid on: ");
								itemID = in.nextInt();
								System.out.print("Enter bid price: ");
								double bidNum = in.nextDouble();
								if (data.placeBid(itemID,bidNum)) {
									System.out.println("\nThe bid was successfully placed");
								}
								break;
							case 10:
								System.out.print("Enter seller ID of the seller you wish to give a rating: ");
								int sellerID = in.nextInt();
								System.out.print("Enter rating: ");
								double rating = in.nextDouble();
								if (data.rateSeller(sellerID,rating)) {
									System.out.println("\nRating has been placed");
								}
								break;
							case 11:
								System.out.println("Enter itemID of the auction you want to close: ");
								itemID = in.nextInt();
								if (data.closeAuction(itemID)) {
									System.out.println("\nAuction has been closed");
								}
								break;
							case 12:
								loggedDatabase = false;
								loggedInternet = false;
								if (data.closeConnection())
									System.out.println("\nGoodbye, we home you had a good time! :)");
								break;
						}
					}
				}
				else {
					System.out.println("Login failed, please try again");
				}	
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		in.close();
		
	}

    // Object that stores the connection to the database
    Connection conn;
    PreparedStatement stmt; // closed in the final method
    PreparedStatement insertStmt; // closed throughout the code
    PreparedStatement updateStmt; // closed throughout the code
    
    // this item is used only in creating the new item
    int item;
    // userID of the egccuser once they login
    int userID;
    /**
     * Connects to the database with the following information
     * @param username
     * @param password
     * @param schema
     */
    public EgccConnector(String username, String password, String schema) {
    	try {
    		// connects using whatever credentials you give it
    		conn = DriverManager.getConnection("jdbc:mysql://COMPDBS300/"+schema,username,password); 
    	} catch (Exception e) {
    		System.out.println("\nConnection to the database failed");
    	}
    }

    /**
     * Logs the user in with the following credentials. If the user exists it will log them in and save UserID as that user's ID
     * @param username
     * @param password
     * @return True if login was successful | False otherwise
     */
    public boolean login(String username, String password) {
    	try {
    		// select statement that retieves the information of the user with said usename and password
    		String select = "SELECT * FROM egccuser WHERE username = ? AND password = ?";
    		// you'll see a lot of these
    		stmt = conn.prepareStatement(select);
    		// puts username and password into those ?'s
    		stmt.setString(1,username);
    		stmt.setString(2,password);
    		// creates a resultset of the query
    		ResultSet rs = stmt.executeQuery();
    		
    		// checking if the user exists in the database
    		if (rs.next()) {
    			// setting the userID equal to the user who entered their credentials
    			userID = rs.getInt(1);
    			return true;
    		}
    	} catch (Exception e) {
    		System.out.println("\nLogin failed");
    	}
	return false;
    }
    /**
     * Changes the password of the user who is logged in
     * @param username
     * @param newPassword
     * @return True if password changed | False otherwise
     */
    public boolean changePassword( String username, String newPassword) {
    	try {
    		
    		// updates the password for the user with the entered username
    		String update = "UPDATE egccUser SET password = ?  WHERE username = ?";
    		// grabs all the information from the specified user to check the password
    		String select = "SELECT * FROM egccUser WHERE username = ?";
    		// prepares each statement with the SQL commands
    		stmt = conn.prepareStatement(select);
    		updateStmt = conn.prepareStatement(update);
    		// using set methods to place values in for the ?
    		stmt.setString(1,username);
    		updateStmt.setString(1,newPassword);
    		updateStmt.setString(2,username);
    		// creating a resultSet with the select statement to save the values
    		ResultSet selectRS = stmt.executeQuery();
    		// checks if both of the statements properly run
    		if (selectRS.next() && updateStmt.executeUpdate() != -1) {
    			// if statement to check if the updated password equals the newPassword value
    			if (selectRS.getString(1).equals(newPassword)) {
    				System.out.println("\nPassword has been changed\n");
    				updateStmt.close();
    				return true;
    			}
    		}
    	} catch (Exception e) {
    		System.out.println("\nPassword did not change correctly");
    	}
	return false;
    }
    /**
     * View the items that the user (who is currently logged in) has bid on
     */
    public void viewMyBiddingItems() {
    	try {
    		// select statement to get the items that the user has bid on
    		String select = "SELECT * FROM item WHERE itemID IN (SELECT itemID FROM bid WHERE BuyerID = ?)";
    		// preparing the statement with the SQL script
    		stmt  = conn.prepareStatement(select);
    		// setting values in for the ?'s
    		stmt.setInt(1,userID);
    		// creating a resultSet for the select statement values
    		ResultSet rs = stmt.executeQuery();
    		// while loop looping through all of the values of the resultSet
    		System.out.println("");
    		
    		String format = "%-8s %-25s %-75s %-15s %-15s %-15s %-15s %-15s";
    		
    		String columnNames = String.format(format, "ItemID", " | " + "Title", " | " + "Description", " | "+ "Starting Bid", 
					 " | "  + "Highest Bid", " | " + "End Date", " | " + "SellerID",  " | " + "Status");
    		String tableBorder = "";
    		for (int i = 0; i < columnNames.toCharArray().length; i++) {
    			tableBorder += "=";
    		}
    		System.out.println(columnNames);
    		System.out.println(tableBorder);
    		while (rs.next()) {
    			// printing all the items that the user has bid on
    			System.out.println(String.format(format, rs.getInt("ItemID"), " | " + rs.getString("title"), " | " + rs.getString("description"), " | $" + rs.getBigDecimal("startingBid"), 
    							 " | $"  + rs.getBigDecimal("highestBid"), " | " + rs.getDate("endDate"), " | " + rs.getInt("SellerID"), " | " + rs.getString("status")));
    		}
    		System.out.println("");
    		
    	} catch (Exception e) {
    		System.out.println("\n Error - " + e.getMessage());
    	}
    }
    /**
     * View the items that the user (who is currently logged in) already has
     */
    public void viewMyItems() {
    	try {
    		// the select statement for retrieving all of the items the user sells
    		String select = "SELECT * FROM item WHERE sellerID = ?";
    		// preparing the statement with the SQL query
    		stmt = conn.prepareStatement(select);
    		// placing a value in for the ? in the SQL statement 
    		stmt.setInt(1,userID);
    		// creating a resultSet that takes in the results from the select statement
    		ResultSet rs = stmt.executeQuery();
    		// while statement to loop over the resultSet
    		System.out.println("");
    		String format = "%-8s %-25s %-75s %-15s %-15s %-15s %-15s %-15s";
    		
    		String columnNames = String.format(format, "ItemID", " | " + "Title", " | " + "Description", " | "+ "Starting Bid", 
					 " | "  + "Highest Bid", " | " + "End Date", " | " + "SellerID",  " | " + "Status");
    		String tableBorder = "";
    		for (int i = 0; i < columnNames.toCharArray().length; i++) {
    			tableBorder += "=";
    		}
    		System.out.println(columnNames);
    		System.out.println(tableBorder);
    		while (rs.next()) {
    			// printing out all the values of the items that the user has put up for auction
    			System.out.println(String.format(format, rs.getInt("ItemID"), " | " + rs.getString("title"), " | " + rs.getString("description"), " | $" 
    							+ rs.getBigDecimal("startingBid"), " | $" + rs.getBigDecimal("highestBid"), " | " + rs.getDate("endDate"), 
    							 " | " + rs.getInt("SellerID"), " | " + rs.getString("status")));
    		}
    		System.out.println("");
    		
    	} catch (Exception e) {
    		System.out.println("\nError - " + e.getMessage());
    	}
    }
    
    /**
     * View the purchases that the user (who is currently logged in) has already purchased
     */
    public void viewMyPurchases() {
    	try {
    		// select statement to select (title, description, price, categoryID, dateSold, dateShipped)
    		String select = "SELECT title, description, price, categoryID, dateSold, dateShipped FROM item JOIN\n" + 
    						"	itemCategory USING (itemID) JOIN purchase USING (itemID) WHERE buyerID = ?";
    		// preparing the statement with the SQL query
    		stmt = conn.prepareStatement(select);
    		// adding a value in for the ?
    		stmt.setInt(1,userID);
    		// creating a ResultSet with from the following Query
    		ResultSet rs = stmt.executeQuery();
    		// while statement looping over the resultSet
    		System.out.println("");
    		String format = "%-25s %-75s %-15s %-15s %-15s %-15s";
    		
    		String columnNames = String.format(format, "Title", " | " + "Description", " | " + "Price", " | "+ "Category ID", 
					 " | "  + "Date Sold", " | " + "Date Shipped");
    		String tableBorder = "";
    		for (int i = 0; i < columnNames.toCharArray().length; i++) {
    			tableBorder += "=";
    		}
    		System.out.println(columnNames);
    		System.out.println(tableBorder);
    		while (rs.next()) {
    			// printing all the items that the user has purchased 
    			System.out.println(String.format(format, rs.getString("title"), " | " + rs.getString("description"), " | $" + rs.getDouble("price"),
						    " | " + rs.getInt("categoryID"), " | " + rs.getDate("dateSold"), " | " + rs.getDate("dateShipped")));
    		}
    		System.out.println("");
    		
    	} catch (Exception e){
    		System.out.println("\nError - " + e.getMessage());
    	}
    }

    /**
     * Prints the items that contain the keyword the user entered
     * @param keyword
     */
    public void searchByKeyword(String keyword) {
    	try {
    		// select statement to retrieve all the values of item where the description is like the keyword
    		String select = "SELECT * FROM item WHERE description LIKE ?";
    		// preparing the statement with teh SQL code
    		stmt = conn.prepareStatement(select);
    		// setting a value in for the ? with the %% on each side of the word
    		stmt.setString(1,"%" + keyword + "%");
    		ResultSet rs = stmt.executeQuery();
    		
    		System.out.println("");
    		String format = "%-8s %-25s %-75s %-15s %-15s %-15s %-15s %-15s";
    		
    		String columnNames = String.format(format, "ItemID", " | " + "Title", " | " + "Description", " | "+ "Starting Bid", 
					 " | "  + "Highest Bid", " | " + "End Date", " | " + "SellerID",  " | " + "Status");
    		String tableBorder = "";
    		for (int i = 0; i < columnNames.toCharArray().length; i++) {
    			tableBorder += "=";
    		}
    		System.out.println(columnNames);
    		System.out.println(tableBorder);
    		while (rs.next()) {
    			// printing out all the values of the item that contains the keyword the user entered
    			System.out.println(String.format(format, rs.getInt("ItemID"), " | " + rs.getString("title"), " | " + rs.getString("description"), " | $" + rs.getDouble("startingBid"), 
    						" | $" + rs.getDouble("highestBid"), " | " + rs.getDate("endDate"), " | " + rs.getInt("SellerID"), " | " + rs.getString("status")));
    		}
    		System.out.println("");
    		
    	} catch (Exception e){
    		System.out.println("\nError - " + e.getMessage());
    	}
    }

    /**
     * Gets the seller rating of the person who is selling the item the user is checking
     * @param itemID
     * @return seller rating
     */
    public double viewSellerRating(int itemID) {
    	try {
    		// creating a value for the seller's rating
    		double sellerRating = 0.0;
    		// selecting the highest rating from the seller rating for the user who is selling the item
    		String select = "SELECT AVG(rating) FROM sellerrating WHERE SellerID IN (SELECT sellerID FROM item WHERE itemID = ?)";
    		// preparing the statement with the SQL query shown above
    		stmt = conn.prepareStatement(select);
    		// setting the ? in the SQL statement with ItemID
    		stmt.setInt(1,itemID);
    		// creating a ResultSet with the values retrieved from the select query
    		ResultSet rs = stmt.executeQuery();
    		// while loop to loop over the values of the resultSet
    		while (rs.next()) {
    			sellerRating = rs.getBigDecimal("AVG(rating)").doubleValue();
    		}
    		return sellerRating;
    	} catch (Exception e){
    		System.out.println("\nError - " + e.getMessage());
    	}
	// return the rating of the seller that is selling the item
	return 0.0;
    }
    
    /**
     * Places a new item up for auction with all of the following credentials
     * @param title
     * @param startingBid
     * @param endDate
     * @param categories
     * @return True if the item is placed | False otherwise
     */
    public boolean putItem(String title, double startingBid, String endDate, String categories[]) {
    	try {
    		// this variable is only used because of the for loop used to check if the statements insert
    		boolean check = false;
    		// getting the highest itemID in the database to create a new itemID
    		String getItemID = "SELECT MAX(ItemID) FROM item";
    		// creating a new prepared statement to get the ItemID
    		PreparedStatement ItemID = conn.prepareStatement(getItemID);
    		// creating a ResultSet to retrieve the ItemID
    		ResultSet rs = ItemID.executeQuery();
    		// if the ResultSet has information we retrieve the max itemID
    		if (rs.next()) {
    			// setting the value equal to item
    			item = rs.getInt("MAX(itemID)");
    		}
    		// insert statement to add the following values into the item table
    		String insert = "INSERT INTO item VALUES(?, ?, null, ?, null, ?,?,'open')";
    		String select = "SELECT ID FROM category WHERE description = ?";
    		String insertCategory = "INSERT INTO category (ID, description) VALUES(?,?)";
    		// creating the prepared statement with the following 
    		insertStmt = conn.prepareStatement(insert);
    		stmt = conn.prepareStatement(select);
    		updateStmt = conn.prepareStatement(insertCategory);
    		// setting the ?'s to the following values given in these statements
    		insertStmt.setInt(1,item+1);
    		insertStmt.setString(2,title);
    		insertStmt.setDouble(3,startingBid);
    		insertStmt.setString(4,endDate);;
    		insertStmt.setInt(5,userID);
    		
    		// if the statement executes return true;
    		if (insertStmt.execute()) {
    			check = true;
    		} else { check = false; }
    		// for loop to add each category the user asked to be added
    		for (int i = 0; i < categories.length; i++) {
    			// getting the categoryID if it exists and setting that to a ResultSet
    			stmt.setString(1,categories[i]);
    			ResultSet results = stmt.executeQuery();
    			int categoryID = results.getInt("ID");
    			// creating an insert statement to add the category with the following description and ID
    			updateStmt.setInt(1,categoryID);
    			updateStmt.setString(2,categories[i]);
    			// if the statement executes return true
    			if (updateStmt.execute()) {
    				check = true;
    			} else { check = false; }
    		}
    		ItemID.close();
    		insertStmt.close();
    		updateStmt.close();
    		return check;
  
    	} catch (Exception e){
    		System.out.println("\nPlacing the item was unsuccessful");
    	}
	return false;
    }
    
    /**
     * 
     * @param itemID
     * @return True if the item is shipped | False otherwise
     */
    public boolean shipItem(int itemID) {
    	try {
    		// two update statements to change the values inside of the SQL database
    		String update = "UPDATE item SET status = 'shipped' WHERE itemID = ?";
    		String update2 = "UPDATE purchase SET dateshipped = sysdate() WHERE itemID = ?";
    		// preparing the stmt and update statement with the SQL statements
    		stmt = conn.prepareStatement(update);
    		updateStmt = conn.prepareStatement(update2);
    		// setting values in for the ?'s in the SQL statements
    		stmt.setInt(1,itemID);
    		updateStmt.setInt(1,itemID);
    		// if statements to see if both of the statements execute if so return true
    		if (stmt.executeUpdate() != -1 && updateStmt.executeUpdate() != -1) {
    			updateStmt.close();
    			return true;
    		}
    		
    	} catch (Exception e){
    		System.out.println("\nShipping the item was unsuccessful");
    	}
	return false;
    }
    
    /**
     * Retrieves the highest bid of the item the user is checking
     * @param itemID
     * @return Highest bid of the item
     */
    public double viewHighestBid (int itemID) {
    	try {
    		// setting a value in for the new bid
    		double bid = 0.0;
    		// writing the SQL statement to retrieve the highest bidder in the database
    		String select = "SELECT MAX(currentBid) FROM bid WHERE itemID = ?";
    		// preparing the statement with the SQL command
    		stmt = conn.prepareStatement(select);
    		// setting values in for the ?'s
    		stmt.setInt(1,itemID);
    		// creating a result set from the select statement
    		ResultSet rs = stmt.executeQuery();
    		// while statement to loop through the results
    		while (rs.next()) {
    			// setting bid equal to the highest value of currentBid
    			bid =rs.getInt("MAX(currentBid)");
    		}
    		return bid;
    		
    	} catch (Exception e){
    		System.out.println("\nError - " + e.getMessage());
    	}
    	return 0.0;
    }

    /**
     * Places a new bid on an item
     * @param itemID
     * @param bidValue
     * @return True if the bid is placed | False otherwise
     */
    public boolean placeBid(int itemID, double bidValue) {
    	try {
    		// insert statement to place a new bid on the specified item
    		String insert = "INSERT INTO bid (BuyerID, itemID, currentBid) VALUES(?,?,?)";
    		// preparing the SQL statement into the prepared statement
    		insertStmt = conn.prepareStatement(insert);
    		// inserting values in for the ?'s in the SQL statement
    		insertStmt.setInt(1,userID);
    		insertStmt.setInt(2,itemID);
    		insertStmt.setDouble(3,bidValue);
    		// if the statement executes return true
    		if (insertStmt.execute()) {
    			return true;
    		}
    		
    	} catch (Exception e){
    		System.out.println("\nPlacing the bid was unsuccessful");
    	}
	return false;
    }

    /**
     * Places a new rating on a seller
     * @param sellerID
     * @param rating
     * @return True if the rating is placed | False otherwise
     */
    public boolean rateSeller(int sellerID, double rating) {
    	try {
    		// insert statement to place a new rating on the specified seller of the item being views
    		String insert = "INSERT INTO sellerrating VALUES (?,?,?,null,sysdate())";
    		// preparing the SQL statement into the prepared statement
    		insertStmt = conn.prepareStatement(insert);
    		// adding values in for the ?'s in the SQL statement
    		insertStmt.setInt(1,userID);
    		insertStmt.setInt(2,sellerID);
    		insertStmt.setDouble(3,rating);
    		// if the statement executes return true
    		if (insertStmt.execute()) {
    			insertStmt.close();
    			return true;
    		}
    		
    	} catch (Exception e){
    		System.out.println("\nPlacing the rating was unsuccessful");
    	}
	return false;
    }	

    /**
     * Closes the auction of the itemID
     * @param itemID
     * @return True if the auction is closed | False otherwise
     */
    public boolean closeAuction (int itemID) {
    	try {
    		// update statement to change the status of the item to sold
    		String update = "UPDATE item SET STATUS = 'sold' WHERE itemID = ?";
    		// inserting the item into the 
    		String insert = "INSERT INTO purchase VALUES (("
    				+ "SELECT buyerID FROM bid WHERE itemID = ? AND currentbid IN ("
    				+ "SELECT MAX(currentbid) FROM bid WHERE itemID = ?),?,(SELECT MAX(currentbid) FROM bid WHERE itemID = ?),sysDate(),null)";
    		// preparing the update and insert statements with the SQL code above
    		updateStmt = conn.prepareStatement(update);
    		insertStmt = conn.prepareStatement(insert);
    		// adding values into the ?'s in both of the statements\
    		updateStmt.setInt(1,itemID);
    		insertStmt.setInt(1,itemID);
    		insertStmt.setInt(2,itemID);
    		insertStmt.setInt(3,itemID);
    		insertStmt.setInt(4,itemID);
    		// if statement to check if both of the statements executed
    		if (updateStmt.executeUpdate() != -1 && insertStmt.execute()) {
    			updateStmt.close();
    			insertStmt.close();
    			return true;
    		}
    	} catch (Exception e){
    		System.out.println("\nClosing the auction was unsuccessful");
    	}
	return false;
    }

    /**
     * Closes the connection and the stmt prepared statement
     * @return True if connections are closed | False otherwise
     */
    public boolean closeConnection() { 
    	try {
    		// closing all of the statements and the connection
    		conn.close();
    		stmt.close();
    	
    	} catch (Exception e){
    		System.out.println(e.getMessage());
    		// if they do not all close this returns false
    		return false;
    	}
	return true;
    }

}