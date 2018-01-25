package de.vogella.jpa.simple;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import de.vogella.jpa.simple.model.Todo;

public class Main {
    private static final String PERSISTENCE_UNIT_NAME = "todos";
    private static EntityManagerFactory factory;

    public static void main(String[] args) {
    	Connection conn = getPostgresDatabaseConnection();
    	
    	/*
    	//basicTest();
    	int count = 1000;
    	batchTest(getDerbyDatabaseConnection(), count);
    	int[] successes = preparedBatchTest(getDerbyDatabaseConnection(), count);
    	String successes_string = "";
    	for (int i = 0; i < successes.length; i++) {
    		successes_string += successes[i] + " ";
    	}
    	System.out.println("Success? "+successes_string);
    	 */
    }
    
    private static int[] preparedBatchTest(Connection connection, int records) {
    	//
    	// From post on
    	//
    	//    https://stackoverflow.com/questions/3784197/efficient-way-to-do-batch-inserts-with-jdbc
    	//    (specifically this response: https://stackoverflow.com/a/42756134)
    	//
    	//*DTA NOTE MUST DO THIS WITH POSTGRESQL INSTEAD OF DERBY!!!
  	    PreparedStatement preparedStatement;

   	    try {
   	        connection.setAutoCommit(true);

   	        String compiledQuery = "INSERT INTO todo(summary, description)" +
   	                " VALUES" + "(?, ?)";
   	        preparedStatement = connection.prepareStatement(compiledQuery);

   	        for(int index = 1; index <= records; index++) {
   	            preparedStatement.setString(1, "summary-"+index);
   	            preparedStatement.setString(2, "description-"+index);
   	            preparedStatement.addBatch();
   	        }

   	        long start = System.currentTimeMillis();
   	        int[] inserted = preparedStatement.executeBatch();
   	        long end = System.currentTimeMillis();

   	        System.out.println("total time taken to insert the prepared batch of " + records + " = " + (end - start) + " ms");
   	        System.out.println("total time taken = " + (end - start)/records + " s");

   	        preparedStatement.close();
   	        connection.close();

   	        return inserted;

   	    } catch (SQLException ex) {
   	        System.err.println("SQLException information");
   	        while (ex != null) {
   	            System.err.println("Error msg: " + ex.getMessage());
   	            ex = ex.getNextException();
   	        }
   	        throw new RuntimeException("Error");
   	    }
    }

    private static void batchTest(Connection connection, int records) {
        PreparedStatement statement;

        try {
            connection.setAutoCommit(true);

   	        String compiledQuery = "INSERT INTO todo(summary, description)" +
   	                " VALUES" + "(?, ?)";
            statement = connection.prepareStatement(compiledQuery);

            long start = System.currentTimeMillis();

            for(int index = 1; index < records; index++) {
            	statement.setString(1, "summary-"+index);
            	statement.setString(2, "description-"+index);

                long startInternal = System.currentTimeMillis();
                statement.executeUpdate();
                //System.out.println("each transaction time taken = " + (System.currentTimeMillis() - startInternal) + " ms");
            }

            long end = System.currentTimeMillis();
            System.out.println("total time taken for non-prepared batch of " + records + " = " + (end - start) + " ms");
            System.out.println("avg total time taken = " + (end - start)/ records + " ms");

            statement.close();
            connection.close();

        } catch (SQLException ex) {
            System.err.println("SQLException information");
            while (ex != null) {
                System.err.println("Error msg: " + ex.getMessage());
                ex = ex.getNextException();
            }
        }
        
        try {
        	connection.setAutoCommit(false); //*DTA probably need to do this too - not sure what rammifications are yet. Might be good to have a separate "batch" connection for this in actual app?
        } catch (SQLException ex) {
            System.err.println("SQLException information");
            while (ex != null) {
                System.err.println("Error msg: " + ex.getMessage());
                ex = ex.getNextException();
            }
        }
    }
    
    private static Connection getPostgresDatabaseConnection() {
        //"jdbc:postgresql://localhost:5432/bnr_datamart_dev",
        //"danny",
        //"root");
    	String url = "jdbc:postgresql://localhost:5432/batch_test?user=danny&password=root";
    	try
        {
    		Connection conn = DriverManager.getConnection(url);
    		return conn;
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
    	return null;
    }
    
    private static Connection getDerbyDatabaseConnection() {
        String dbURL = "jdbc:derby:/home/dave/databases/simpleDb;create=true;user=test;password=test";
        try
        {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            //Get a connection
            Connection conn = DriverManager.getConnection(dbURL);
            return conn;
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
        return null;
    }
    	
    private static void basicTest() {
        factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        EntityManager em = factory.createEntityManager();
        // read the existing entries and write to console
        Query q = em.createQuery("select t from Todo t");
        List<Todo> todoList = q.getResultList();
        for (Todo todo : todoList) {
            System.out.println(todo);
        }
        System.out.println("Size: " + todoList.size());

        // create new todo
        em.getTransaction().begin();
        Todo todo = new Todo();
        todo.setSummary("This is a test");
        todo.setDescription("This is a test");
        em.persist(todo);
        em.getTransaction().commit();

        em.close();
    }
}