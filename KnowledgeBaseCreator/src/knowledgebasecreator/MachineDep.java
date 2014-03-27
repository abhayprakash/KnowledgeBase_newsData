/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knowledgebasecreator;

/**
 *
 * @author Abhay
 */
public class MachineDep {
    // files
    public static String inputFilePath = "E:/Projects/NewsData/KnowledgeBase/headlines.txt";
    public static String resultFilePath = "E:/Projects/NewsData/KnowledgeBase/entities.txt";
    public static String logFilePath = "E:/Projects/NewsData/KnowledgeBase/log.txt";
    
    //  Database credentials
    public static final String USER = "root";
    public static final String PASS = "";
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    public static final String DB_URL = "jdbc:mysql://localhost/newsData";

    // neo4j related
    public static String DB_PATH = "E:\\Projects\\NewsData\\neo4j-community-2.1.0-M01\\data\\kb.db";
}
