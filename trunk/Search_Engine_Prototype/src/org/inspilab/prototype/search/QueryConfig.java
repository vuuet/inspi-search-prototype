package org.inspilab.prototype.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import vn.hus.nlp.tokenizer.VietTokenizer;

public class QueryConfig 
{
	private String dbName;
	private String dbUsername;
	private String dbPassword;
	private static String foodFilePath;
	private static String districtFilePath;
	private static String wardFilePath;
	private static String streetFilePath;
	private static String complementaryFilePath;
	private static String keywordMatrixFilePath;
	private Connection con;
	private static VietTokenizer vnTokEngine;	
	private static ArrayList<String> foodFeature;
	private static ArrayList<String> districtFeature;
	private static ArrayList<String> wardFeature;
	private static ArrayList<String> streetFeature;
	private static ArrayList<String> complementaryFeature;
	private static ArrayList<String[]> keywordMatrixFeature;
	
	public QueryConfig(int type)
	{
		vnTokEngine = new VietTokenizer();
		foodFilePath = "." + File.separatorChar + "feature" + File.separatorChar + "foodList.txt";
		districtFilePath = "." + File.separatorChar + "feature" + File.separatorChar + "districtList.txt";
		wardFilePath = "." + File.separatorChar + "feature" + File.separatorChar + "wardList.txt";
		streetFilePath = "." + File.separatorChar + "feature" + File.separatorChar + "streetList.txt";
		complementaryFilePath = "." + File.separatorChar + "feature" + File.separatorChar + "complementaryList.txt";
		keywordMatrixFilePath = "." + File.separatorChar + "feature" + File.separatorChar + "keywordMatrix.txt";
		
		foodFeature = readFileToArrayList(foodFilePath);
		districtFeature = readFileToArrayList(districtFilePath);
		wardFeature = readFileToArrayList(wardFilePath);
		streetFeature = readFileToArrayList(streetFilePath);
		complementaryFeature = readFileToArrayList(complementaryFilePath);
		keywordMatrixFeature = readFiletoArrayList_2(keywordMatrixFilePath);
		
		if(type == 0)
		{
			dbName = "jdbc:mysql://localhost:3306/inspilab_local?useUnicode=yes&characterEncoding=UTF-8";
			dbUsername = "root";
			dbPassword = "root";
			
			con = databaseConnect(dbName, dbUsername, dbPassword);
		}
		
		if(type == 1)
		{
			dbName = "jdbc:mysql://localhost:3306/inspilab_alpha_v2";
			dbUsername = "root";
			dbPassword = "";
			
			con = databaseConnect(dbName, dbUsername, dbPassword);
		}
		
		if(type == 2)
		{
			dbName = "jdbc:mysql://sarabi.inspilab.com:3306/alpha_web_dev";
			dbUsername = "alphaweb";
			dbPassword = "1234569870";
				
			con = databaseConnect(dbName, dbUsername, dbPassword);
		}
		
		if(type == 3)
		{
			dbName = "jdbc:mysql://localhost:3306/alpha_web_dev";
			dbUsername = "alphaweb";
			dbPassword = "1234569870";
				
			con = databaseConnect(dbName, dbUsername, dbPassword);
		}
	}
	
	private Connection databaseConnect(String dbName, String dbUsername, String dbPassword)
	{
		Connection con = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			try
			{
				con = DriverManager.getConnection(dbName, dbUsername, dbPassword);
			}
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
			
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return con;
	}
	
	private ArrayList<String> readFileToArrayList(String filePath)
	{
		ArrayList<String> arrList = new ArrayList<String>();
		
		try 
		{	
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
			
			String line = null;
			
			reader.readLine();
			
			while((line = reader.readLine()) != null)
			{
				arrList.add(line);
			}
			
			reader.close();
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return arrList;
	}
	
	private ArrayList<String[]> readFiletoArrayList_2(String filePath)
	{
		ArrayList<String[]> result = new ArrayList<String[]>();
		
		try 
		{	
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"));
			
			String line = null;
			
			reader.readLine();
			
			while((line = reader.readLine()) != null)
			{
				String[] feature = line.split("\t");
				
				result.add(feature);
			}
			
			reader.close();
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static void updateDistrictFeature(String keyword)
	{
		districtFeature.add(keyword);
		
		updateFile(districtFilePath, keyword);
	}
	
	public static void updateWardFeature(String keyword)
	{
		wardFeature.add(keyword);
		
		updateFile(wardFilePath, keyword);
	}
	
	public static void updateComplementaryFeature(String keyword)
	{
		complementaryFeature.add(keyword);
		
		updateFile(complementaryFilePath, keyword);
	}
	
	public static void updateFile(String filePath, String keyword)
	{
		try 
		{
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8"));
			
			writer.write(keyword + "\n");
			
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public VietTokenizer getVnTokEngine() {
		return vnTokEngine;
	}
	
	public ArrayList<String> getFoodFeature() {
		return foodFeature;
	}
		
	public ArrayList<String> getDistrictFeature() {
		return districtFeature;
	}
	
	public ArrayList<String> getWardFeature() {
		return wardFeature;
	}
	
	public ArrayList<String> getStreetFeature() {
		return streetFeature;
	}

	public ArrayList<String> getComplementaryFeature() {
		return complementaryFeature;
	}
	
	public Connection getConnection(){
		return con;
	}
	
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbPassWord(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbPassWord() {
		return dbPassword;
	}

	public void setDbUserName(String dbUsername) {
		this.dbUsername = dbUsername;
	}

	public String getDbUserName() {
		return dbUsername;
	}

	public void setFoodFilePath(String inputFoodFilePath) {
		foodFilePath = inputFoodFilePath;
		
		foodFeature = readFileToArrayList(foodFilePath);
	}

	public String getFoodFilePath() {
		return foodFilePath;
	}

	public void setWardFilePath(String inputWardFilePath) {
		wardFilePath = inputWardFilePath;
		
		wardFeature = readFileToArrayList(wardFilePath);
	}

	public String getWardFilePath() {
		return wardFilePath;
	}

	public void setDistrictFilePath(String inputDistrictFilePath) {
		districtFilePath = inputDistrictFilePath;
		
		districtFeature = readFileToArrayList(districtFilePath);
	}

	public String getDistrictFilePath() {
		return districtFilePath;
	}

	public void setStreetFilePath(String inputStreetFilePath) {
		streetFilePath = inputStreetFilePath;
		
		streetFeature = readFileToArrayList(streetFilePath);
	}

	public void setComplementaryFilePath(String inputComplementaryFilePath)
	{
		complementaryFilePath = inputComplementaryFilePath;
		
		complementaryFeature = readFileToArrayList(complementaryFilePath);
	}
	
	public String getStreetFilePath() {
		return streetFilePath;
	}
	
	public void setKeywordMatrixFilePath(String inputKeywordMatrixFilePath) {
		keywordMatrixFilePath = inputKeywordMatrixFilePath;
		
		readFiletoArrayList_2(keywordMatrixFilePath);
	}
	
	public String getKeywordMatrixFilePath() {
		return keywordMatrixFilePath;
	}

	public ArrayList<String[]> getKeywordMatrixFeature() {
		return keywordMatrixFeature;
	}
}
