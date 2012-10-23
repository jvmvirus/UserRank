package com.userrank;

import java.sql.*;
import java.util.Date;
import Jama.*;

public class UserRank {
	private int number_user = 0;
	private int number_sole = 0;
	private Connection connect = null;
	private Statement statement = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	private double start_time = 0;
	private double end_time = 0;
	
	public void execute() throws Exception {
		start_time = getCurrentTime();
		
		// configuration database
		initDatabase();
				
		// drop tables if exist
		dropTables();
		
		// create all value for calculating user rank
		createTableUserRank();
		
		createTableRelatedUser();
		
		initTableUserRank();    		
		initTableRelatedUser();
		
		removeRelatedThemSelves();
		
		updateValueUserRank();   		
		
		updateNumberRelatedUsers();

		// create system of linear equations and resolve them
		createTableSOLE();
		
		initTableSOLE();
		
		updateOwnerIdSOLE();
		
		initValueSOLE();
		
		resolveSOLE();
		
		// call reset init value of users who have none like
		resetValueOfUserNoneLikeIsTrue();
		
		// close database
		close();
		
		end_time = getCurrentTime();
		
		double total_time = end_time - start_time;
		System.out.println("total time is : " + total_time + " seconds");
	}
	
	/**
	 * init functions
	 * @throws Exception
	 */
	private void initDatabase() throws Exception {
		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "younet_me";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "root"; 
		String password = "";
		  
		try {
			Class.forName(driver);
		      
			// Setup the connection with the DB
		    connect = DriverManager.getConnection(url+dbName,userName,password);

		    // Statements allow to issue SQL queries to the database
		    statement = connect.createStatement();
		 } catch (Exception e) {
		      throw e;
		 }
		
	}
	
	public void readDataBase() throws Exception {
	    try {
	      // This will load the MySQL driver, each DB has its own driver
	      Class.forName("com.mysql.jdbc.Driver");
	      // Setup the connection with the DB
	      connect = DriverManager
	          .getConnection("jdbc:mysql://localhost/feedback?"
	              + "user=sqluser&password=sqluserpw");

	      // Statements allow to issue SQL queries to the database
	      statement = connect.createStatement();
	      // Result set get the result of the SQL query
	      resultSet = statement
	          .executeQuery("select * from FEEDBACK.COMMENTS");

	      // PreparedStatements can use variables and are more efficient
	      preparedStatement = connect
	          .prepareStatement("insert into  FEEDBACK.COMMENTS values (default, ?, ?, ?, ? , ?, ?)");
	      // "myuser, webpage, datum, summery, COMMENTS from FEEDBACK.COMMENTS");
	      // Parameters start with 1
	      preparedStatement.setString(1, "Test");
	      preparedStatement.setString(2, "TestEmail");
	      preparedStatement.setString(3, "TestWebpage");
	      //preparedStatement.setDate(4, new java.sql.Date(9, 11, 2011) );
	      preparedStatement.setString(5, "TestSummary");
	      preparedStatement.setString(6, "TestComment");
	      preparedStatement.executeUpdate();

	      preparedStatement = connect
	          .prepareStatement("SELECT myuser, webpage, datum, summery, COMMENTS from FEEDBACK.COMMENTS");
	      resultSet = preparedStatement.executeQuery();

	      // Remove again the insert comment
	      preparedStatement = connect
	      .prepareStatement("delete from FEEDBACK.COMMENTS where myuser= ? ; ");
	      preparedStatement.setString(1, "Test");
	      preparedStatement.executeUpdate();
	      
	      resultSet = statement
	      .executeQuery("select * from FEEDBACK.COMMENTS");
	      
	    } catch (Exception e) {
	      throw e;
	    } finally {
	      close();
	    }

	}
	
	
	
/*-----------------------------------------------------------------
| main functions
------------------------------------------------------------------*/
	/**
  	* drop tables
	* @throws SQLException 
  	*/
  	private void dropTables() throws SQLException {
  		
  		String sql = "DROP TABLE IF EXISTS `younet_me`.`engine4_user_rank`;";
   		statement.executeUpdate(sql);
  		
  		sql = "DROP TABLE IF EXISTS `younet_me`.`engine4_related_user`;";
   		statement.executeUpdate(sql);
			
   		sql = "DROP TABLE IF EXISTS `younet_me`.`engine4_sole`;";
   		statement.executeUpdate(sql);   		
  	}
  	
  	/**
  	 * create tables
  	 * @throws SQLException 
  	 */
  	private void createTableUserRank() throws SQLException { 
  		String sql = "CREATE TABLE IF NOT EXISTS `younet_me`.`engine4_user_rank` ("
		  				+" `id` int(11) NOT NULL,"
		  				+" `value` double DEFAULT NULL,"
						+" `number_related_users` int(11) DEFAULT NULL,"
						+" `none_like` tinyint(1) DEFAULT NULL,"
						+"	  PRIMARY KEY (`id`)"
						+"	) ENGINE=MyISAM DEFAULT CHARSET=latin1;";
		statement.executeUpdate(sql);		
  	}
  	
  	private void createTableRelatedUser() throws SQLException {
  		String sql = "CREATE TABLE IF NOT EXISTS `engine4_related_user` ("
					  +"`id` int(11) NOT NULL,"
					  +"`owner_id` int(11) DEFAULT NULL,"
					  +"`poster_id` int(11) DEFAULT NULL,"
					  +"PRIMARY KEY (`id`)"
					  +") ENGINE=MyISAM DEFAULT CHARSET=latin1;";
		statement.executeUpdate(sql);		
  	}
  	
  	private void createTableSOLE() throws SQLException { // create table system of linear equations
		String sql = "CREATE TABLE IF NOT EXISTS `younet_me`.`engine4_sole` ("
					  +"`id` int(11) NOT NULL,"
					  +"`owner_id` int(11) DEFAULT NULL,"
					  +"PRIMARY KEY (`id`)"
					  +") ENGINE=MyISAM DEFAULT CHARSET=latin1;";
		statement.executeUpdate(sql);		
	
  		// create columns in system of linear equations Ax = B
	    sql = "ALTER TABLE engine4_sole";
	    String name_before = "owner_id";
	    boolean first_time = true;
	    
	    String subsql = "select owner_id from younet_me.engine4_related_user group by owner_id order by owner_id";
		resultSet = statement.executeQuery(subsql);		
	    
		while ( resultSet.next() ) {
			String index = resultSet.getString("owner_id");
			String name_after = "user_" + index;
			
			if (first_time == true) {
	    		first_time = false;
	    		sql += " ADD COLUMN " + name_after + " DOUBLE NULL AFTER " + name_before;
	    	} else {
	    		sql += ", ADD COLUMN " + name_after + " DOUBLE NULL AFTER " + name_before;
	    	}
			
			name_before = name_after;
		}
		
	    
	    sql += ", ADD COLUMN B DOUBLE NULL AFTER " + name_before;
	    
	    //System.out.println(sql);
	    
		statement.executeUpdate(sql);		
  	}
  	/**
  	 * end create tables
  	 */
  	
  	/**
  	 * init values of tables
  	 * @throws SQLException 
  	 */
  	private void initTableUserRank() throws SQLException {  		
  		// set default value of none like column	
  		int number_user = getNumberUser();
	  	for (int i = 1; i <= number_user; i++) {
	    	String sql = "INSERT IGNORE INTO `younet_me`.`engine4_user_rank` (id, none_like) VALUES("+ i +",0)";
			statement.executeUpdate(sql);		
	    }
  	}
  	
  	private void initTableRelatedUser() throws SQLException {
  		  	
	  	String sql = "	(Select subject_id as owner_id, poster_id" 
	  				 +"	from younet_me.engine4_activity_likes INNER JOIN younet_me.engine4_activity_actions "
	  				 +"	ON action_id = resource_id)"
	  				+" UNION"
	  				+" (Select subject_id as owner_id, poster_id"
	  				+"	from "
					+"	(Select t1.resource_id, t2.poster_id from younet_me.engine4_activity_comments  as t1 INNER JOIN (Select resource_type, resource_id, poster_id from younet_me.engine4_core_likes, younet_me.engine4_user_rank where id = poster_id) as t2 "
					+"	On t1.comment_id = t2.resource_id and t2.resource_type = 'activity_comment') as a1 inner join younet_me.engine4_activity_actions on a1.resource_id = action_id)"
					+"	UNION"
					+"	(Select owner_id, poster_id from younet_me.engine4_album_albums  as t1 INNER JOIN (Select resource_type, resource_id, poster_id from younet_me.engine4_core_likes, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.album_id = t2.resource_id and t2.resource_type = 'advalbum_album') "
					+"	UNION"
					+"	(Select owner_id, poster_id from younet_me.engine4_album_photos  as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_likes, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.photo_id = t2.resource_id and t2.resource_type = 'advalbum_photo')"
					+"	UNION"
					+"	(Select owner_id, poster_id from younet_me.engine4_blog_blogs  as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_likes, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.blog_id = t2.resource_id and t2.resource_type = 'blog')"
					+"	UNION"
					+"	(Select t1.poster_id as owner_id, t2.poster_id from younet_me.engine4_core_comments  as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_likes, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.comment_id = t2.resource_id and t2.resource_type = 'core_comment')"
					+"	UNION"
					+"	(Select t1.user_id as owner_id, poster_id from  younet_me.engine4_poll_polls as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_likes, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.poll_id = t2.resource_id and t2.resource_type = 'poll')"
					+"	UNION"
					+"	(Select owner_id, poster_id from  younet_me.engine4_video_videos as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_likes, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.video_id = t2.resource_id and t2.resource_type = 'video')"

					+"	UNION"	
					+"	(Select subject_id as owner_id, poster_id" 
					+"		from younet_me.engine4_activity_comments INNER JOIN younet_me.engine4_activity_actions" 
					+"		ON action_id = resource_id)"
					+"	UNION"
					+"	(Select owner_id, poster_id from younet_me.engine4_album_albums  as t1 INNER JOIN (Select resource_type, resource_id, poster_id from younet_me.engine4_core_comments, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.album_id = t2.resource_id and t2.resource_type = 'advalbum_album') "
					+"	UNION"
					+"	(Select owner_id, poster_id from younet_me.engine4_album_photos  as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_comments, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.photo_id = t2.resource_id and t2.resource_type = 'advalbum_photo')"
					+" UNION"
					+"	(Select user_id as owner_id, poster_id from younet_me.engine4_group_photos  as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_comments, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.photo_id = t2.resource_id and t2.resource_type = 'advgroup_photo')"
					+"	UNION"
					+"	(Select owner_id, poster_id from younet_me.engine4_blog_blogs  as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_comments, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.blog_id = t2.resource_id and t2.resource_type = 'blog')"
					+"	UNION"
					+"	(Select t1.user_id as owner_id, poster_id from  younet_me.engine4_poll_polls as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_comments, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.poll_id = t2.resource_id and t2.resource_type = 'poll')"
					+"	UNION"
					+"	(Select owner_id, poster_id from  younet_me.engine4_video_videos as t1 INNER JOIN  (Select resource_type, resource_id, poster_id from younet_me.engine4_core_comments, younet_me.engine4_user_rank where id = poster_id) as t2"
					+"		On t1.video_id = t2.resource_id and t2.resource_type = 'video')	"
						
					+"	ORDER BY owner_id, poster_id";
	  		  	
  		int i = 1;
		
  		resultSet = statement.executeQuery(sql);		
	    
  		Statement stmt = connect.createStatement();
		while ( resultSet.next() ) {
			
			int owner_id = resultSet.getInt("owner_id");
			int poster_id = resultSet.getInt("poster_id");
			
			sql = "INSERT IGNORE INTO `younet_me`.`engine4_related_user` (id, owner_id, poster_id) VALUES("+ i +", "+owner_id+", "+poster_id+" )";
			
			stmt.executeUpdate(sql);	
				
			i++;
		}
		
  	}
  	
  	private void initTableSOLE() throws SQLException {  		
	  	/*
	     * init values in system of linear equations Ax = B
	     * 
	     */	    
		int number_sole = getNumberSOLE(); 
	    
		Statement stmt = connect.createStatement();
	    for (int sole_id = 1; sole_id <= number_sole;sole_id++) { 
		    String sql = "INSERT IGNORE INTO engine4_sole (id";
		    int number_col = 0;
		    
		    String subSql = "select owner_id from younet_me.engine4_related_user group by owner_id order by owner_id";
		    resultSet = statement.executeQuery(subSql);
		    while ( resultSet.next() ) {
		    	int index = resultSet.getInt("owner_id");
		    	number_col++;
		    	sql += ", user_"+index;
		    }
		    
		    // column b
		    sql += ", B";
		    
		    sql += ") VALUE("+sole_id;
		    
		    number_col++;  // + 1 => B column
		    
		    for (int i = 0; i < number_col; i++) {
		    	sql += ", 0";
		    }
		    
		    sql += ")";
	    
		    //System.out.println(sql);
		    stmt.executeUpdate(sql);
	    }
  	}
  	
  	/**
  	 * init SOLE
  	 * @throws SQLException 
  	 */
  	private void initValueSOLE() throws SQLException {	  	
		/*
	     *  set diagonal of matrix is 1
	     *  set value = 1 / number_user, value is init value of user who have none like
	     */
	    double number_user = getNumberUser();
	    double value = 1 / number_user;   
		
		int number_sole = getNumberSOLE();	    
	    for (int i = 1; i <= number_sole; i++) {
	    	int owner_id;
	    	String sql = "select owner_id from younet_me.engine4_sole where id ="+i;
	    	resultSet = statement.executeQuery(sql);
	    	resultSet.next();
	    	owner_id = resultSet.getInt("owner_id");
	    	
	    	//
	     	//  set diagonal of matrix is 1
	     	//
	    	String sql2 = "UPDATE younet_me.engine4_sole SET `user_"+owner_id+"`=1 WHERE `id`="+i;
	    	statement.executeUpdate(sql2);
	    	
	    	
	    	String sql1 = "select poster_id from younet_me.engine4_related_user where owner_id = "+owner_id;
	    	
	    	
	    	// set value of B colum is 0 in SOLE Ax = B
	    	double B_value = 0;
	    	
	    	resultSet = statement.executeQuery(sql1);
	    	Statement stmt = connect.createStatement();
	    		
	    	while ( resultSet.next() ) { 
	    		int none_like = 1;
		    	int poster_id = resultSet.getInt("poster_id");
		    	String sql3 = "Select none_like, number_related_users from younet_me.engine4_user_rank where id="+poster_id;
		    		
		    	int number_related_users = 1; // not set 0 because divide by zero
		    	ResultSet resultSet1 = stmt.executeQuery(sql3);
		    	resultSet1.next();
		    	none_like = resultSet1.getInt("none_like");
		    	number_related_users = resultSet1.getInt("number_related_users");
		    			    		
		    	if (none_like == 1) {
		    		B_value += value / number_related_users;    	
		    		//echo "B: $B_value <br/>";		
		    	} else {
		    		String sql4 = "UPDATE engine4_sole SET `user_"+poster_id+"`= -1/"+number_related_users+" WHERE `id`="+i;
		    			//echo $sql . "<br/>";
		    		Statement stmt2 = connect.createStatement();
		    			
		    		stmt2.executeUpdate(sql4);
		    	}
	    	}
	    	
	    	sql = "UPDATE engine4_sole SET `B`="+B_value+" WHERE `id`="+i;

	    	//System.out.println(sql);
	    	
	    	statement.executeUpdate(sql);
	    
	    }
	      	
  	}
  	
  	private Matrix initMainMatrix() throws SQLException {	
  		  	
	    // Ax = B
	    // init A matrix from database
		int number_sole = getNumberSOLE();
	    
	    int m_row = 0;
	    int m_col = 0;	    
	    
	    double[][] M = new double[number_sole][number_sole];
	    
	    for (int i = 1; i <= number_sole; i++) {
	    	String sql = "select * from younet_me.engine4_sole where id = " + i;
	    	resultSet = statement.executeQuery(sql);
	    	resultSet.next();
	    	int offset = 2;
	    	
	    	for (m_col = offset; m_col < number_sole + offset; m_col++) {
    			// matrix column must be subtracted offset
    			 double val = resultSet.getDouble(m_col + 1);
    			 M[m_row][m_col - offset] = val;
    		}
	    	
	    	m_row++;
	    }  	
	    
	    //printMatrix(M);
	    
	    return new Matrix(M);
	    
  	}
  	
  	/**
  	 * reduce noise by the way we remove rows which users like themselves
  	 * @throws SQLException 
  	 */
  	private void removeRelatedThemSelves() throws SQLException {
	    String sql = "DELETE FROM `younet_me`.`engine4_related_user` WHERE `owner_id`=`poster_id`";
  		statement.executeUpdate(sql);		
  	}
  	
  	/**
  	 * update value table user rank
  	 */
  	private void updateValueUserRank() throws SQLException {
  		double number_user = getNumberUser();
  		
  		String sql = "select user_id from `younet_me`.`engine4_users` where user_id not in"
  					+" (select owner_id from `younet_me`.`engine4_related_user`"
					+"	group by owner_id)";
  		resultSet = statement.executeQuery(sql);
  	 	//set value of none like column
  		Statement stmt = connect.createStatement();
  		while ( resultSet.next() ) {
  			int user_id = resultSet.getInt("user_id");
  			String subSql = "UPDATE younet_me.engine4_user_rank SET none_like=1 WHERE id="+user_id;
  			stmt.executeUpdate(subSql);
  		}
  		
	    
	    // set value = 1 / number_user, value is init value of user who have none like
	  	double value = 1 / number_user; 
	    
	  	sql = "select id from `younet_me`.`engine4_user_rank` where none_like = 1";
	  	resultSet = statement.executeQuery(sql);
	  	while ( resultSet.next() ) {
	  		int user_id = resultSet.getInt("id"); 
	  		String subSql = "UPDATE younet_me.engine4_user_rank SET value="+value+" WHERE id="+user_id;
	  		stmt.executeUpdate(subSql);
	  	}
		
  	}
  	
  	/**
  	 * update value number_related_users column in younet_me.engine4_user_rank table (number person whom user likes)
  	 * @throws SQLException 
  	 */
  	private void updateNumberRelatedUsers() throws SQLException {
  		String sql = "select poster_id, number_user from" 
					 +"	(Select poster_id, count(*) as number_user from younet_me.engine4_related_user"
					 +"	group by poster_id) as a1" 
					 +"	join younet_me.engine4_user_rank"
					 +"	on a1.poster_id = id";
  		
  		resultSet = statement.executeQuery(sql);
  		Statement stmt = connect.createStatement();
  		while ( resultSet.next() ) {
  			int number_user = resultSet.getInt("number_user");
  			int poster_id = resultSet.getInt("poster_id");
  			String subSql = "UPDATE younet_me.engine4_user_rank SET number_related_users="+number_user+" WHERE id="+poster_id;
  			stmt.executeUpdate(subSql);
  		}
  		
  	}
	
  	/**
  	 * update value of owner_id column in SOLE
  	 * @throws SQLException 
  	 */
  	private void updateOwnerIdSOLE() throws SQLException {
	  	int sole_id = 1;
	  	String sql = "select owner_id from younet_me.engine4_related_user group by owner_id order by owner_id";
	  	resultSet = statement.executeQuery(sql);
	  	Statement stmt = connect.createStatement();
	  	while ( resultSet.next() ){
	  		int owner_id = resultSet.getInt("owner_id");
	  		String subSql = "UPDATE engine4_sole SET owner_id="+owner_id+" WHERE id="+sole_id;
	  		sole_id++;
	  		stmt.executeUpdate(subSql);
	  	}
	    
  	}
  	
  	/**
  	 * reset all value of users who have none like is 1
  	 * @throws SQLException 
  	 */
  	private void resetValueOfUserNoneLikeIsTrue() throws SQLException {
	  	String sql = "UPDATE younet_me.engine4_user_rank SET value=0 WHERE none_like=1";
  		statement.executeUpdate(sql);
  	}
  	
  	/**
  	 * 	Resolve system of linear equations
  	 * 	use Crame's rule to resolve SOLE
  	 * @throws SQLException 
  	 */
  	private void resolveSOLE() throws SQLException {
  		
  		// calculate determinant of main matrix  
    	
    	Matrix matrix_sole = initMainMatrix();
    	double Det_M = matrix_sole.det();
    	System.out.println("det matrix: " + Det_M);
    	
    	
    	int number_sole = getNumberSOLE();
    	double[][] Mx = new double[number_sole][number_sole];
    	
	    for (int user_i = 0; user_i < number_sole; user_i++) {
	    
		    // init Mx matrix from database		    
		    int m_row = 0;
		    int m_col = 0;
		 	for (int i = 1; i <= number_sole; i++) {
		 		String sql2 = "select * from younet_me.engine4_sole where id = "+i;
		 		resultSet = statement.executeQuery(sql2);
		 		resultSet.next();
		 		int offset = 2;
		 		
		 		for (m_col = offset; m_col < number_sole + offset; m_col++) {
	    			// matrix column must be subtracted offset
	    			Mx[m_row][m_col - offset] = resultSet.getDouble(m_col + 1);
	    		}
	    		
	    		// remove value in j column by value of B column (Crame's rule)
	    		Mx[m_row][user_i] = resultSet.getDouble(number_sole + offset + 1);
		    
		    	m_row++;
		    }
					  	
		  	Matrix matrix_sole_x = new Matrix(Mx);		    
		  	
		  	double Det_Mx = matrix_sole_x.det();
		  	
		  	//System.out.println("Det_Mx: " + Det_Mx);
		  	
		  	// use Crame's rule to resolve SOLE
		  	double result = Det_Mx / Det_M;
		  	
		  	//System.out.println("result: " + result);
		  	
		  	int id = user_i + 1;
		  	String sql1 = "select owner_id from younet_me.engine4_sole where id = "+id;
		  	Statement stmt = connect.createStatement();
		  	resultSet = stmt.executeQuery(sql1);
		  	while ( resultSet.next() ) {
		  		int owner_id = resultSet.getInt("owner_id");
		  		String sql = "UPDATE `younet_me`.`engine4_user_rank` SET `value`="+result+" WHERE `id`="+owner_id;
		  		statement.executeUpdate(sql);
		  	}
		  	
		  	
	    }
	    
  	
  	}
  	
  	
/*-----------------------------------------------------------------
| end main
------------------------------------------------------------------*/
	
	/**
	 *  helper functions
	 */
	private double getCurrentTime() {
		double time = 0;
		Date date = new Date();
		time = date.getTime() / 1000;
		return time;
	}
	
	// You need to close the resultSet
	private void close() {
		    try {
		      if (resultSet != null) {
		        resultSet.close();
		      }

		      if (statement != null) {
		        statement.close();
		      }

		      if (connect != null) {
		        connect.close();
		      }
		    } catch (Exception e) {
		    	System.out.println(e.toString());
		    }
	}
	
	/**
  	 * print matrix
	 * @throws SQLException 
  	 */
  	private void printMatrix(double[][] M) throws SQLException { 
  		int number_sole = getNumberSOLE();
		for (int r = 0; r < number_sole; r++) {
		    System.out.print( "row " + r + " :");
		    for (int c = 0; c < number_sole; c++) {
		    	System.out.print( M[r][c] + " ");
		    }
		    	
		    System.out.print("\n");
		}
  	}
	/**
	 * getters and setters
	 * @throws SQLException 
	 */
	public int getNumberUser() throws SQLException {
		if (this.number_user == 0) {
			String sql = "select count(*) as number_user from `younet_me`.`engine4_users`";
			resultSet = statement.executeQuery(sql);
			resultSet.next();
			this.number_user = Integer.parseInt(resultSet.getString("number_user") );
		}
		
		return this.number_user;
  	}
  	
	public int getNumberSOLE() throws SQLException {
		if (this.number_sole == 0) {
			String sql = "select count(*) as number_sole from (select owner_id from younet_me.engine4_related_user group by owner_id) as t";
			resultSet = statement.executeQuery(sql);
			resultSet.next();
			this.number_sole = Integer.parseInt(resultSet.getString("number_sole") );
		}
		
		return this.number_sole;
	
	}
	
	/**
	 * end getters and setters
	 */
	
}