package org.inspilab.prototype.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import vn.hus.nlp.tokenizer.VietTokenizer;

public class Main 
{	
	public static String alphaTest(String queryString, QueryConfig config)
	{
		String output = "";
				
		ArrayList<String[]> resultSet = null;
				
		resultSet = getLocation(queryString, config);
				
		if(resultSet.size() != 0)
		{		
			for(int i = 0; (resultSet.size() < 20) ? i < resultSet.size() : i < 20 ; i++)
			{
				output += resultSet.get(i)[0] + "\t" 
							+ resultSet.get(i)[1] + "\t"
							+ resultSet.get(i)[2] + "\t"
							+ resultSet.get(i)[3] + "\t"
							+ resultSet.get(i)[4] + "\n";
			}
		}
		else
		{
			output = "No result found";
		}
	
		return output;
	}
	
	public static ArrayList<String[]> getLocation(String queryString, QueryConfig config)
	{
		ArrayList<String[]> resultSet = new ArrayList<String[]>();
				
		try 
		{	
			String SQL = getQueryStatement(queryString, config);
			
			System.out.println(SQL);
			
			Statement stmt = config.getConnection().createStatement();
			
			ResultSet rs = stmt.executeQuery(SQL);
				
			while(rs.next())
			{				
				String[] newResult = new String[5];
				
				newResult[0] = rs.getString("id");
				newResult[1] = rs.getString("name");
				newResult[2] = rs.getString("address");
				newResult[3] = rs.getString("total_score");
				newResult[4] = rs.getString("importancy");
				
				resultSet.add(newResult);
			}
			
			rs.close();
			stmt.close();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		} 
		
		return resultSet;
	}

	public static ArrayList<String> getQueryAttribute(String queryString, QueryConfig config)
	{		
		ArrayList<String> result = new ArrayList<String>();
		
		String[] districtFeatureFromQuery = districtFeature(queryString, config.getDistrictFeature());
		if(districtFeatureFromQuery[0] != null)
		{
			result.add(districtFeatureFromQuery[2]);
			queryString = queryString.replace(districtFeatureFromQuery[1], "");
		}
		
		String[] wardFeatureFromQuery = wardFeature(queryString, config.getWardFeature());
		if(wardFeatureFromQuery[0] != null)
		{
			result.add(wardFeatureFromQuery[2]);
			queryString = queryString.replace(wardFeatureFromQuery[1], "");
		}
		
		String[] streetFeatureFromQuery = streetFeature(queryString, config.getStreetFeature());
		if(streetFeatureFromQuery[0] != null)
		{
			result.add(streetFeatureFromQuery[0]);
			queryString = queryString.replace(streetFeatureFromQuery[1], "");
		}
		
		String numberFeatureFromQuery = numberFeature(queryString);
		if(numberFeatureFromQuery != null)
		{
			queryString = queryString.replace(numberFeatureFromQuery, "");
		}
		
		ArrayList<String[]> foodFeatureFromQuery = keywordFeature(queryString, config.getKeywordFeature(), config.getVnTokEngine());
		for(int i = 0; i < foodFeatureFromQuery.size(); i++)
		{
			result.add(foodFeatureFromQuery.get(i)[0]);
			
			String replacement = foodFeatureFromQuery.get(i)[1].replace("_", " ");
			queryString = queryString.replace(replacement, "");
		}
		
		ArrayList<String> relatedLocation = getRelatedLocation(foodFeatureFromQuery, config);
		for(int i = 0; i < relatedLocation.size(); i++)
		{
			result.add(relatedLocation.get(i));
		}
		
		queryString = queryString.trim();
		String complementaryPartFromQuery = getComplementaryFeatureID(queryString, config.getComplementaryFeature());
		if(!complementaryPartFromQuery.isEmpty())
		{
			result.add(complementaryPartFromQuery);
		}
	
		return result;
	}
	
	public static String getQueryStatement(String queryString, QueryConfig config)
	{
		String sqlStatement = "";
		
		//Getting query Attribute
		ArrayList<String> queryArr = new ArrayList<String>();
		
		String[] districtFeatureFromQuery = districtFeature(queryString, config.getDistrictFeature());
		if(districtFeatureFromQuery[0] != null)
		{
			queryArr.add(districtFeatureFromQuery[2]);
			queryString = queryString.replace(districtFeatureFromQuery[1], "");
		}
		
		String[] wardFeatureFromQuery = wardFeature(queryString, config.getWardFeature());
		if(wardFeatureFromQuery[0] != null)
		{
			queryArr.add(wardFeatureFromQuery[2]);
			queryString = queryString.replace(wardFeatureFromQuery[1], "");
		}
		
		String[] streetFeatureFromQuery = streetFeature(queryString, config.getStreetFeature());
		if(streetFeatureFromQuery[0] != null)
		{
			queryArr.add(streetFeatureFromQuery[0]);
			queryString = queryString.replace(streetFeatureFromQuery[1], "");
		}
		
		String numberFeatureFromQuery = numberFeature(queryString);
		if(numberFeatureFromQuery != null)
		{
			queryString = queryString.replace(numberFeatureFromQuery, "");
		}
		
		ArrayList<String[]> keywordFeatureFromQuery = keywordFeature(queryString, config.getKeywordFeature(), config.getVnTokEngine());
		for(int i = 0; i < keywordFeatureFromQuery.size(); i++)
		{
			queryArr.add(keywordFeatureFromQuery.get(i)[0]);
			
			String replacement = keywordFeatureFromQuery.get(i)[1].replace("_", " ");
			queryString = queryString.replace(replacement, "");
		}
		
		ArrayList<String> relatedLocation = getRelatedLocation(keywordFeatureFromQuery, config);
		for(int i = 0; i < relatedLocation.size(); i++)
		{
			queryArr.add(relatedLocation.get(i));
		}
		
		queryString = queryString.trim();

		//Old version of get complementary part 3 - 10 - 2011, using like statement instead
		/*String complementaryPartFromQuery = getComplementaryFeatureID(queryString, config.getComplementaryFeature());
		if(!complementaryPartFromQuery.isEmpty())
		{
			queryArr.add(complementaryPartFromQuery);
		}*/
		//Done with getting query attribute
		
		if(queryArr.size() == 0)
		{
			/*	sqlStatement = 
				"select P.id, name, address from place as P, (" +
				getAlternateSQLStatement(queryString) + ") as S where P.id = S.id";*/

			sqlStatement = 	getAlternateSQLStatement(queryString, 0);
		}
		else
		{
			String alternateSLQ = getAlternateSQLStatement(queryString, 1);
			
			String docmtAttributeVector = "(";
			
			for(int i = 0; i < queryArr.size(); i++)
			{
				if(i != queryArr.size() - 1)
				{
					docmtAttributeVector += queryArr.get(i) + ", ";
				}
				else
				{
					docmtAttributeVector += queryArr.get(i);
				}
			}
			
			docmtAttributeVector += ")";
			
			if(!alternateSLQ.isEmpty())
			{
				sqlStatement = 
					"SELECT P.id, P.name, P.address, importancy, keyword_score, comty_score, T.code " +
					"FROM " +
					"( " +
						"( " +
							"SELECT place_id, sum( score ) AS keyword_score, sum( importancy ) AS importancy FROM " +
							"`place_attribute_value` " +
							"WHERE attribute_id " +
							"IN " + docmtAttributeVector + " " +
							"GROUP BY place_id " +
						") AS K " +
						"LEFT JOIN " +
						"(" + alternateSLQ + ") as C on K.place_id = C.id " +
					") " +
					"JOIN place as P ON place_id = P.id " +
					"JOIN place_topic_relationship as PTR ON P.id = PTR.place_id " +
					"JOIN topic as T ON PTR.topic_id = T.id " +
					"group by P.id " +  
					"order by importancy desc, keyword_score desc, comty_score desc";
					
				/*
					"select K.id, K.name, K.address, K.importancy, K.total_score, C.score from " +	
					"(" +
						"select P.id, P.name, P.address, total_score, importancy from place as P,	" +
						"(" +
							"select place_id, sum(score) as total_score, sum(importancy) as importancy " +
							"from `place_attribute_value` where attribute_id in "	+				
							docmtAttributeVector + " group by place_id" +
						") as S where P.id = S.place_id" +
					") as K " +
					"left join " +
					"(" + alternateSLQ + ") as C using (id) " +
					"order by importancy desc, total_score desc, score desc";
				*/
			}
			else
			{
				sqlStatement =
					
					"SELECT P.id, P.name, P.address, importancy, keyword_score, T.code " +
					"FROM " +
					"(" +
						"SELECT place_id, sum( score ) AS keyword_score, sum( importancy ) AS importancy FROM " +
						"`place_attribute_value` " +
						"WHERE attribute_id " +
						"IN " + docmtAttributeVector + " " +
						"GROUP BY place_id " +
					") AS K " + 
					"JOIN place as P ON place_id = P.id " +
					"JOIN place_topic_relationship as PTR ON P.id = PTR.place_id " +
					"JOIN topic as T ON PTR.topic_id = T.id " +
					"group by P.id " +
					"order by importancy desc, keyword_score desc";
				
					/*
					"select P.id, P.name, P.address, importancy, total_score from place as P,	" +
						"(" +
							"select place_id, sum(score) as total_score, sum(importancy) as importancy " +
							"from `place_attribute_value` where attribute_id in "	+				
							docmtAttributeVector + " group by place_id" +
						") as S where P.id = S.place_id " +
					"order by importancy desc, total_score desc";
					*/
			}
		}
		
		
		
		return sqlStatement;
	}
	
	public static String getAlternateSQLStatement(String complementaryPart, int type)
	{
		String result = "";
		
		if(!complementaryPart.isEmpty())
		{
			String[] complementaryComponent = complementaryPart.split(" ");
			
			if(type == 0)
			{
				String likeStatement = "";
				
				if(complementaryComponent[0].contains("d"))
				{
					String replacement = complementaryComponent[0].replace("d", "đ");
					likeStatement = "(name like '%" + complementaryComponent[0] + "%'" + " or name like '%" + replacement + "%')";
				}
				else
				{
					likeStatement = "name like '%" + complementaryComponent[0] + "%'";
				}
				
				
				for(int i = 1; i < complementaryComponent.length; i++)
				{
					if(complementaryComponent[i].contains("d"))
					{
						String replacement = complementaryComponent[i].replace("d", "đ");
						likeStatement += " and (name like '%" + complementaryComponent[i] + "%'" + " or name like '%" + replacement + "%')";
					}
					else
					{
						likeStatement += " and name like '%" + complementaryComponent[i] + "%'";
					}
				}
				
				result = "select id, name, address, 1 as total_score, 1 as importancy from place where " + likeStatement;
			}
			
			if(type == 1)
			{
				String likeStatement = "";
				
				if(complementaryComponent[0].contains("d"))
				{
					String replacement = complementaryComponent[0].replace("d", "đ");
					likeStatement = "(complementary_value like '%" + complementaryComponent[0] + "%'" + " or complementary_value like '%" + replacement + "%')";
				}
				else
				{
					likeStatement = "complementary_value like '%" + complementaryComponent[0] + "%'";
				}
				
				for(int i = 1; i < complementaryComponent.length; i++)
				{
					if(complementaryComponent[i].contains("d"))
					{
						String replacement = complementaryComponent[i].replace("d", "đ");
						likeStatement += " and (complementary_value like '%" + complementaryComponent[i] + "%'" + " or complementary_value like '%" + replacement + "%')";
					}
					else
					{
						likeStatement += " and complementary_value like '%" + complementaryComponent[i] + "%'";
					}
				}
				
				result = 
					"SELECT P.id, P.name, P.address, T.code " +
					"FROM " +
					"(" +
						"SELECT id " +
						"FROM place_complementary " +
						"WHERE " + likeStatement +
					" ) " + 
					"AS C " +
					"JOIN place as P ON C.id = P.id " +
					"JOIN place_topic_relationship as PTR ON P.id = PTR.place_id " +
					"JOIN topic as T ON PTR.topic_id = T.id " +
					"group by P.id";
					/*"select id, 1 as score from place_complementary where " + likeStatement;*/
			}
		}

		return result;
	}
	
	public static ArrayList<String> alphaTest_addLocation(String[] placeInfo, QueryConfig config)
	{
		ArrayList<String> resultSet = null;
		
		resultSet = runInsertStatement(placeInfo, config);
		
		return resultSet;
	}
	
	public static ArrayList<String> runInsertStatement(String[] placeInfo, QueryConfig config)
	{
		ArrayList<String> resultSet = new ArrayList<String>();
		
		ArrayList<String> queryArr = getInsertStatement(placeInfo, config);

		try 
		{	
			Statement stmt = config.getConnection().createStatement();
			
			for(int i = 0; i < queryArr.size(); i++)
			{
				if(stmt.executeUpdate(queryArr.get(i)) == 0)
				{
					resultSet.add(queryArr.get(i));
				}
			}
			
			stmt.close();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		} 
		
		return resultSet;
	}
	
	public static void insertLocation(QueryConfig config)
	{	
		try 
		{
			
			Calendar currentDate = Calendar.getInstance();
			
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss");
			SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
			
			String dateNow = formatter.format(currentDate.getTime());
			String dateNow2 = formatter2.format(currentDate.getTime());
			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("InsertLog_" + dateNow2), "UTF-8"));
			
			String sql = "select id, name, address from place where id in " +
						"(select id from place where id not in (select distinct place_id from place_attribute_value));";
			
			Statement stmt = config.getConnection().createStatement();
			
			ResultSet rs = stmt.executeQuery(sql);
			
			writer.write("Running Batch Place Insert at " + dateNow + "\n");
			
			while(rs.next())
			{
				String[] placeInfo = new String[3];
				placeInfo[0] = rs.getString("id");
				placeInfo[1] = rs.getString("name");
				placeInfo[2] = rs.getString("address");
				
				insertLocationWithBatchStatement(placeInfo, config, writer);
			}
			
			writer.close();
			rs.close();
			stmt.close();
								
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
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void checkUpdateStatus(int[] updateCounts, ArrayList<String> insertStatement, BufferedWriter writer) 
	{
		try
		{
			for (int i = 0; i < updateCounts.length; i++) 
			{
				if (updateCounts[i] == Statement.EXECUTE_FAILED) 
				{	
					writer.write(insertStatement.get(i) + "\n");
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void insertLocationWithBatchStatement(String[] placeInfo, QueryConfig config, BufferedWriter writer)
	{
		Connection conn = config.getConnection();
		Statement stmt = null;	
		
		ArrayList<String> insertStatements = getInsertStatement(placeInfo, config);
		
		try 
		{
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			for(int i = 0; i < insertStatements.size(); i++)
			{
				stmt.addBatch(insertStatements.get(i));
			}
			
			stmt.executeBatch();
			conn.commit();
			
		} 
		catch (BatchUpdateException e)
		{
			try
			{
				writer.write("PlaceID: " + placeInfo[0] + " - Name: " + placeInfo[1] + " - Address: " + placeInfo[2] + "\n");
				writer.write("Error insert statement at: ");
				
				int[] updateCounts = e.getUpdateCounts();
				checkUpdateStatus(updateCounts, insertStatements, writer);
				
				conn.rollback();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
			
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				stmt.close();
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public static ArrayList<String> getInsertStatement(String[] placeInfo, QueryConfig config)
	{
		ArrayList<String> result = new ArrayList<String>();
		
		DataContainer insertStatement = new DataContainer();
		insertStatement.setContainerName("place_attribute_value");
		insertStatement.setAttributeNames(new String[]{"place_id", "attribute_id", "frequency", "score", "importancy"});
		String[] values = new String[5];
		
		//component[0] = place_id, component[1] = place_name, component[2] = place_address;
		String[] component = placeInfo;
		component[1] = component[1].toLowerCase();
		component[2] = component[2].toLowerCase();
		
		if(component.length < 3)
		{
			return result;
		}
		
		//id - keyword - frequency
		ArrayList<String[]> keywordFeatureFromPlace = keywordFeature(component[1], config.getKeywordFeature(), config.getVnTokEngine());
		
		for(int i = 0; i < keywordFeatureFromPlace.size(); i++)
		{
			values = new String[5];
					
			values[0] = component[0];
			values[1] = keywordFeatureFromPlace.get(i)[0];
			values[2] = "1";
			values[3] = "5";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
			
			String replacement = keywordFeatureFromPlace.get(i)[1].replace("_", " ");
			component[1] = component[1].replace(replacement, "");
		}
		
		component[1] = component[1].trim();
		
		//Complementary Part
		if(!component[1].isEmpty())
		{
			String complementaryInsertStatement = getComplementaryInsertStatement(component[1], component[0]);
	
			String[] complementaryFromPlace = complementaryFeature(component[1], config.getComplementaryFeature());
			
			if(complementaryFromPlace[2] != null && complementaryFromPlace[2].equals("1"))
			{
				QueryConfig.updateComplementaryFeature(complementaryFromPlace[1]);
			}

			values[0] = component[0];
			values[1] = complementaryFromPlace[0];
			values[2] = "1";
			values[3] = "3";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
			result.add(complementaryInsertStatement);
		}
		
		//Get District
		String[] districtFeatureFromPlace = districtFeature(component[2], config.getDistrictFeature());
		if(districtFeatureFromPlace[0] != null)
		{
			if(districtFeatureFromPlace[3] != null && districtFeatureFromPlace[3].equals("1"))
			{
				QueryConfig.updateDistrictFeature(districtFeatureFromPlace[0]);
			}
			
			values[0] = component[0];
			values[1] = districtFeatureFromPlace[2];
			values[2] = "1";
			values[3] = "1";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
			
			component[2] = component[2].replace(districtFeatureFromPlace[1], "");
		}
		
		//Get Ward
		String[] wardFeatureFromPlace = wardFeature(component[2], config.getWardFeature());
		if(wardFeatureFromPlace[0] != null)
		{	
			if(wardFeatureFromPlace[3] != null && wardFeatureFromPlace[3].equals("1"))
			{
				QueryConfig.updateWardFeature(wardFeatureFromPlace[0]);
			}
			
			values[0] = component[0];
			values[1] = wardFeatureFromPlace[2];
			values[2] = "1";
			values[3] = "1";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
			
			component[2] = component[2].replace(wardFeatureFromPlace[1], "");
		}
		
		//Get Street
		String[] streetFeatureFromPlace = streetFeature(component[2], config.getStreetFeature());
		if(streetFeatureFromPlace[0] != null)
		{
			values[0] = component[0];
			values[1] = streetFeatureFromPlace[0];
			values[2] = "1";
			values[3] = "1";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
		}
		return result;
	}
 	
	public static ArrayList<String> getInsertStatement_Outputtest(String locationInfo, QueryConfig config)
	{
		ArrayList<String> result = new ArrayList<String>();
		//ArrayList<String> newKeyword = new ArrayList<String>();
		
		DataContainer insertStatement = new DataContainer();
		insertStatement.setContainerName("place_attribute_value");
		insertStatement.setAttributeNames(new String[]{"place_id", "attribute_id", "frequency", "score", "importancy"});
		String[] values = new String[5];
		
		//component[0] = place_id, component[1] = place_name, component[2] = place_address;
		String[] component = locationInfo.split(" - ");
		
		if(component.length < 3)
		{
			return result;
		}
		
		//id - keyword - frequency
		ArrayList<String[]> foodFeatureFromPlace = keywordFeature(component[1], config.getKeywordFeature(), config.getVnTokEngine());
		
		String keywordInfo;
		
		result.add("Food keyword");
		
		for(int i = 0; i < foodFeatureFromPlace.size(); i++)
		{
			keywordInfo = "";
			
			keywordInfo += foodFeatureFromPlace.get(i)[1] + ", ";
			
			result.add(keywordInfo);
			
			values = new String[5];
					
			values[0] = component[0];
			values[1] = foodFeatureFromPlace.get(i)[0];
			values[2] = "1";
			values[3] = "2";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
			
			String replacement = foodFeatureFromPlace.get(i)[1].replace("_", " ");
			component[1] = component[1].replace(replacement, "");
		}
		
		component[1] = component[1].trim();
		
		result.add("Complementary keyword");
		
		if(!component[1].isEmpty())
		{
			String[] complementaryFromPlace = complementaryFeature(component[1], config.getComplementaryFeature());
			
			keywordInfo = "";
			
			if(complementaryFromPlace[2] != null && complementaryFromPlace[2].equals("1"))
			{
				keywordInfo = "new: " + complementaryFromPlace[1];
				QueryConfig.updateComplementaryFeature(complementaryFromPlace[1]);
			}
			else
			{
				keywordInfo = complementaryFromPlace[1];
			}
			
			result.add(keywordInfo);
			
			values[0] = component[0];
			values[1] = complementaryFromPlace[0];
			values[2] = "1";
			values[3] = "2";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
		}
		
		result.add("District keyword");
		String[] districtFeatureFromPlace = districtFeature(component[2], config.getDistrictFeature());
		if(districtFeatureFromPlace[0] != null)
		{
			keywordInfo = "";
			if(districtFeatureFromPlace[3] != null && districtFeatureFromPlace[3].equals("1"))
			{
				keywordInfo = "new: " + districtFeatureFromPlace[0];
				QueryConfig.updateDistrictFeature(districtFeatureFromPlace[0]);
			}
			else
			{
				keywordInfo = districtFeatureFromPlace[0];
			}
			
			result.add(keywordInfo);
			
			values[0] = component[0];
			values[1] = districtFeatureFromPlace[2];
			values[2] = "1";
			values[3] = "1";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
			
			component[2] = component[2].replace(districtFeatureFromPlace[1], "");
		}
		
		result.add("Ward keyword");
		String[] wardFeatureFromPlace = wardFeature(component[2], config.getWardFeature());
		if(wardFeatureFromPlace[0] != null)
		{
			keywordInfo = "";
			
			if(wardFeatureFromPlace[3] != null && wardFeatureFromPlace[3].equals("1"))
			{
				keywordInfo = "new: " + wardFeatureFromPlace[0];
				QueryConfig.updateWardFeature(wardFeatureFromPlace[0]);
			}
			else
			{
				keywordInfo = wardFeatureFromPlace[0];
			}
			
			result.add(keywordInfo);
			
			values[0] = component[0];
			values[1] = wardFeatureFromPlace[2];
			values[2] = "1";
			values[3] = "1";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
			
			component[2] = component[2].replace(wardFeatureFromPlace[1], "");
		}
		
		result.add("Street keyword");
		String[] streetFeatureFromPlace = streetFeature(component[2], config.getStreetFeature());
		if(streetFeatureFromPlace[0] != null)
		{
			keywordInfo = streetFeatureFromPlace[1];
			
			result.add(keywordInfo);
			
			values[0] = component[0];
			values[1] = streetFeatureFromPlace[0];
			values[2] = "1";
			values[3] = "1";
			values[4] = "1";
			
			insertStatement.setAttributeValues(values);
			
			result.add(insertStatement.toInsertStatement());
		}
		return result;
	}
	
	public static String[] wardFeature(String line, ArrayList<String> wardList)
	{	
		String[] output = new String[4];
		
		Pattern disReg = Pattern.compile("(phường|phuong|p)+ ?\\.? ?[0-9]{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = disReg.matcher("");
		
		Pattern number = Pattern.compile("[0-9]{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher numMatch = number.matcher("");
		
		int indexWard = 1;
		int indexPrefixToWard = 0;
		
		
		String prefix = null;
		
		line = line.toLowerCase();
		
		matcher.reset(line.toLowerCase());
			
		if(matcher.find())
		{
			int flag = 1;
			
			numMatch.reset(matcher.group());
		
			output[1] = matcher.group();
			
			if(numMatch.find())
			{
				output[0] = numMatch.group();
			}
			
			if(output[0] != null && !output[0].contains("phường"))
				output[0] = "phường " + output[0];
			
			for(int i = 0; i < wardList.size(); i++)
			{
				if(output[0].equals(wardList.get(i)))
				{
					output[2] = "3" + (i + 1);
					flag = 0;
					output[3] = "0";
					break;
				}
			}
			
			if(flag == 1)
			{
				output[2] = "3" + (wardList.size() + 1);
				output[3] = "1";
			}
		}
		else
		{
			if((indexWard = line.lastIndexOf("phường ")) != -1)
			{
				prefix = "phường ";
				indexPrefixToWard = 7;
			}
			else if((indexWard = line.lastIndexOf("p. ")) != -1)
			{
				prefix = "p. ";
				indexPrefixToWard = 3;
			}
			else if((indexWard = line.lastIndexOf("p.")) != -1)
			{
				prefix = "p.";
				indexPrefixToWard = 2;
			}
			else if ((indexWard = line.lastIndexOf("p ")) != -1)
			{
				prefix = "p ";
				indexPrefixToWard = 2;
			}

			if(indexWard != -1)
			{
				for(int i = 0; i < wardList.size(); i++)
				{
					if(line.contains(wardList.get(i)) && (line.indexOf(wardList.get(i)) - indexPrefixToWard >= indexWard))
					{
						output[0] = wardList.get(i);
						output[1] = prefix + output[0];
						output[2] = "3" + (i + 1);
						break;
					}
				}
			}
		}
		
		return output;
	}
	
	public static String[] districtFeature(String line, ArrayList<String> districtList)
	{
		String[] output = new String[4];
		
		//output[0] - standard output
		//output[1] - original output
		//output[2] - id - if this is a keyword
		//output[3] - new keyword indicator
		
		Pattern disReg = Pattern.compile("(quận|quan|q)+ ?\\.? ?[0-9]{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = disReg.matcher("");
		
		Pattern number = Pattern.compile("[0-9]{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher numMatch = number.matcher("");

		int indexDistrict = 0;
		int indexPrefixToDistrict = 0;
		
		String prefix = "";
		
		line = line.toLowerCase();
		
		matcher.reset(line.toLowerCase());
			
		if(matcher.find())
		{	
			int flag = 1;
			
			numMatch.reset(matcher.group());
			
			output[1] = matcher.group(); //original output, for replacement
			
			if(numMatch.find())
			{
				output[0] = numMatch.group();//standard output
			}
			
			if(output[0] != null && !output[0].contains("quận"))
				output[0] = "quận " + output[0];
			
			for(int i = 0; i < districtList.size(); i++)
			{
				if(output[0].equals(districtList.get(i)))
				{
					output[2] = "2" + (i + 1);
					flag = 0;
					output[3] = "0";
					break;
				}
			}
			
			if(flag == 1)
			{
				output[2] = "2" + (districtList.size() + 1);
				output[3] = "1";
			}
		}
		else
		{	
			if((indexDistrict = line.indexOf("quận ")) != -1)
			{
				prefix = "quận ";
				indexPrefixToDistrict = 5;
			}
			else if((indexDistrict = line.indexOf("q ")) != -1)
			{
				prefix = "q ";
				indexPrefixToDistrict = 2;
			}
			else if((indexDistrict = line.indexOf("q. ")) != -1)
			{
				prefix = "q. ";
				indexPrefixToDistrict = 3;
			}
			else if ((indexDistrict = line.indexOf("q.")) != -1)
			{
				prefix = "q.";
				indexPrefixToDistrict = 2;
			}
	
			for(int i = 0; i < districtList.size(); i++)
			{
				if(line.contains(districtList.get(i)) && line.indexOf(districtList.get(i)) - indexPrefixToDistrict  >= indexDistrict)
				{
					output[0] = districtList.get(i); //standard output
					output[1] = prefix + output[0]; //original output, for replacement
					output[2] = "2" + (i + 1);
					break;
				}
			}
		}
		
		return output;
	}
	
	public static String[] streetFeature(String line, ArrayList<String> streetList)
	{
		String[] output = new String[2];
		
		for(int i = 0; i < streetList.size(); i++)
		{
			if(line.contains(streetList.get(i)))
			{
				output[0] = "5" + (i + 1);
				output[1] = streetList.get(i);
				break;
			}
		}
		
		return output;
	}
	
	public static String numberFeature(String line)
	{
		String output = null;
		
		Pattern disReg = Pattern.compile("([a-zA-Z]?[0-9]{1,5}(bis)?[a-zA-Z]?/?)+( ?- ?([a-zA-Z]?[0-9]{1,5}(bis)?[a-zA-Z]?/?)*)*", Pattern.CASE_INSENSITIVE);
		Matcher matcher = disReg.matcher("");
		
		matcher.reset(line);
		
		if(matcher.find())
		{
			output = matcher.group();
		}
			
		return output;
	}

	public static ArrayList<String[]> keywordFeature(String line, ArrayList<String> keywordList, VietTokenizer vnTokEngine)
	{
		ArrayList<String[]> output = new ArrayList<String[]>();
		
		String[] token = null;
		String[] wordArr = null;
		
		token = vnTokEngine.tokenize(line);
		
		wordArr = token[0].split(" ");
		
		for(int i = 0; i < wordArr.length; i++)
		{
			int flag = 0;
			for(int k = 0; k < output.size(); k++)
			{
				if(wordArr[i].equals(output.get(k)[1]))
				{
					output.get(k)[2] = Integer.toString((Integer.parseInt(output.get(k)[2]) + 1));
					flag = 1;
					break;
				}
			}
			
			if(flag == 0)
			{
				for(int j = 0; j < keywordList.size(); j++)
				{
					if(wordArr[i].equals(keywordList.get(j)))
					{
						String[] result = new String[3];
						
						result[0] = "1" + (j + 1);
						result[1] = keywordList.get(j);
						result[2] = "1";

						output.add(result);
						break;
					}
				}
			}
		}

		return output;
	}
	
	public static String[] complementaryFeature(String line, ArrayList<String> complementaryList)
	{
		String[] output = new String[3];
		
		int flag = 1;
		
		for(int i = 0; i < complementaryList.size(); i++)
		{
			if(line.equals(complementaryList.get(i)))
			{
				output[0] = "4" + (i + 1);
				output[1] = complementaryList.get(i);
				output[2] = "0";
				flag = 0;
				break;
			}
		}
		
		if(flag == 1)
		{
			output[0] = "4" + (complementaryList.size() + 1);
			output[1] = line;
			output[2] = "1";
		}
		
		return output;
	}
	
	public static String getComplementaryFeatureID(String line, ArrayList<String> complementaryList)
	{	
		String result = "";
		int index = -1;
		double score = 0.5;
		
		QGramsDistance measurement = new QGramsDistance();
		
		for(int i = 0; i < complementaryList.size(); i++)
		{
			double tempScore = measurement.getSimilarity(line, complementaryList.get(i));
			if(tempScore > score)
			{
				score = tempScore;
				index = i;
			}
		}
		
		if(index != -1)
		{
			result = "4" + (index + 1);
		}
		
		return result;
	}
	
	public static ArrayList<String> getRelatedLocation (ArrayList<String[]> foodFeatureFromQuery, QueryConfig config)
	{
		ArrayList<String> relatedPlace = new ArrayList<String>();
		
		for(int i = 0; i < foodFeatureFromQuery.size(); i++)
		{
			for(int j = 0; j < config.getKeywordMatrixFeature().size(); j++)
			{
				if(foodFeatureFromQuery.get(i)[0].equals(config.getKeywordMatrixFeature().get(j)[0]))
				{
					for(int k = 0; k < config.getKeywordMatrixFeature().get(j).length; k++)
					{
						relatedPlace.add(config.getKeywordMatrixFeature().get(j)[k]);
					}
				}
			}
			
		}
		
		return relatedPlace;
	}
	
	public static String getComplementaryInsertStatement(String complemtaryPart, String placeID)
	{
		DataContainer insertStatement = new DataContainer();
		
		insertStatement.setContainerName("place_complementary");
		insertStatement.setAttributeNames(new String[]{"id", "complementary_value"});
		
		String[] values = new String[2];
		
		values[0] = placeID;
		values[1] = complemtaryPart;
		
		insertStatement.setAttributeValues(values);
		
		return insertStatement.toInsertStatement();
	}
	
	public static String preProcessing(String queryString)
	{
		String result;
		
		result = queryString.replace("@", "");
		result = queryString.replace("!", "");
		result = queryString.replace("#", "");
		result = queryString.replace("$", "");
		result = queryString.replace("%", "");
		result = queryString.replace("^", "");
		result = queryString.replace("&", "");
		result = queryString.replace("*", "");
		result = queryString.replace("(", "");
		result = queryString.replace(")", "");
		result = queryString.replace("_", "");
		result = queryString.replace("-", "");
		result = queryString.replace("+", "");
		result = queryString.replace("=", "");
		result = queryString.replace("`", "");
		result = queryString.replace("~", "");
		result = queryString.replace("'", "");
		
		return result;
	}
	
	public static void main(String[] args)
	{ 
		QueryConfig config = new QueryConfig(0);
		
		insertLocation(config);
	}
	
	/*
	public static void main(String[] args)
	{ 	
		QueryConfig config = new QueryConfig(4);
		
		int port = 21010;
		
		ServerSocket listenSock = null;
		Socket sock = null;
		
		try 
		{
			listenSock = new ServerSocket(port);
			
			while(true)
			{
				sock = listenSock.accept();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"));
				
				String line = null;
				String queryResult = null;
				ArrayList<String> locationResult = null;
				
				while((line = reader.readLine()) != null)
				{	
					int br = 0;
					
					if(line.endsWith("/end"))
					{
						line = line.replace("/end", "");
						br = 1;
					}
					
					if(line.startsWith("/q/"))
					{
						queryResult = getQueryStatement(line.replace("/q/", ""), config);
						
						writer.write(queryResult + "\n");
						writer.flush();
					}
					
					if(line.startsWith("/i/"))
					{
						locationResult = alphaTest_addLocation(line.replace("/i/", ""), config);
						
						if(locationResult.isEmpty())
						{
							writer.write("0" + "\n");
							writer.flush();
						}
						else
						{
							String result = new String();
							for(int i = 0; i < locationResult.size(); i++)
							{
								if(i == 0)
								{
									result += locationResult.get(i);
								}
								else
								{
									result += "/qi/" + locationResult.get(i);
								}
							}
							
							result += "\n";
							
							writer.write(result);
							writer.flush();
						}
					}

					if(br == 1)
					{
						break;
					}
				}
				
				reader.close();
				writer.close();
				sock.close();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	*/

	/*public static void main(String[] args)
	{		
		QueryConfig config = new QueryConfig(0);
		
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try 
		{
			fis = new FileInputStream(args[0]);
			fos = new FileOutputStream(args[1]);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		InputStreamReader inputStream = null;
		BufferedReader reader = null;
		OutputStreamWriter outputStream = null;
		BufferedWriter writer = null;
		
		//
		try
		{
			inputStream = new InputStreamReader(fis, "UTF-8");
			reader = new BufferedReader(inputStream);
			outputStream = new OutputStreamWriter(fos, "UTF-8");
			writer = new BufferedWriter(outputStream);
			
			String line = null;
							
			try
			{
				reader.readLine();
				
				while((line = reader.readLine()) != null)
				{	
					line = line.toLowerCase();
					writer.write("Test case: " + line + "\n");
					writer.write(alphaTest(line, config) + "\n");
				}		
			}
			finally
			{
				reader.close();//Close reading file
				writer.close();//Close writing file
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();	
		}
	
	}*/
	
	/*
	public static void main(String[] args)
	{ 	
		int port = 21010;
		
		ServerSocket listenSock = null;
		Socket sock = null;
		
		try 
		{
			listenSock = new ServerSocket(port);
			VietTokenizer vnTokEngine = new VietTokenizer();
			
			while(true)
			{
				sock = listenSock.accept();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream(), "UTF-8"));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"));
				
				String line = null;
				String queryResult = null;
				ArrayList<String> locationResult = null;
				
				while((line = reader.readLine()) != null)
				{	
					int br = 0;
					
					if(line.endsWith("/end"))
					{
						line = line.replace("/end", "");
						br = 1;
					}
					
					if(line.startsWith("/q"))
					{
						queryResult = alphaTest(line.replace("/q", ""), vnTokEngine);
						
						writer.write(queryResult + "\n");
						writer.flush();
					}
					
					if(line.startsWith("/d"))
					{
						locationResult = alphaTest_addLocation(line.replace("/d", ""), vnTokEngine);
						
						if(locationResult.isEmpty())
						{
							writer.write("0" + "\n");
							writer.flush();
						}
						else
						{
							String result = new String();
							for(int i = 0; i < locationResult.size(); i++)
							{
								if(i == 0)
								{
									result += locationResult.get(i);
								}
								else
								{
									result += "/qi/" + locationResult.get(i);
								}
							}
							
							result += "\n";
							
							writer.write(result);
							writer.flush();
						}
					}

					if(br == 1)
					{
						break;
					}
					
				}
				
				reader.close();
				writer.close();
				sock.close();
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	*/
	

	/*public static ArrayList<String> getInsertStatement(String locationInfo, QueryConfig config)
	{
		ArrayList<String> result = new ArrayList<String>();
		
		ArrayList<String> output1 = null;
		String[] output2 = null;
		String[] output3 = null;
		String output4 = null;
		String output5 = null;
		String insertStatement = null;
		
		output2 = districtFeature(locationInfo, config.getDistrictFeature());	
		output3 = wardFeature(locationInfo, config.getWardFeature());
		output4 = streetFeature(locationInfo, config.getStreetFeature());
		output5 = numberFeature(locationInfo);
		
		String[] component = locationInfo.split("/pid/");
		
		int placeID = Integer.parseInt(component[0]);
		
		locationInfo = component[1];
		
		int attributeID;
		
		locationInfo = locationInfo.toLowerCase();
		
		if(output2[0] != null)
		{
			attributeID = 0;
			for(int i = 0; i < config.getKeywordVector().size(); i++)
			{
				if(config.getKeywordVector().get(i).equals(output2[0]))
				{
					attributeID = i + 1;
					break;
				}
			}
			
			if(attributeID != 0)
			{
				insertStatement = "INSERT INTO x1_place_feature_value " +
					"(place_id, attribute_id, frequency, score, importancy) " +
					"VALUES ('"
					+ placeID + "', '"
					+ attributeID + "', '"
					+ "1" + "', '"
					+ "3" + "', '"
					+ "0" + "');";
		
				result.add(insertStatement);
			}
			locationInfo = locationInfo.replace(output2[1], "");
		}

		if(output3[0] != null)
		{
			attributeID = 0;
			for(int i = 0; i < config.getKeywordVector().size(); i++)
			{
				if(config.getKeywordVector().get(i).equals(output3[0]))
				{
					attributeID = i + 1;
					break;
				}
			}
			
			if(attributeID != 0)
			{
				insertStatement = "INSERT INTO x1_place_feature_value " +
						"(place_id, attribute_id, frequency, score, importancy) " +
						"VALUES ('"
						+ placeID + "', '"
						+ attributeID + "', '"
						+ "1" + "', '"
						+ "2" + "', '"
						+ "0" + "');";
				
				result.add(insertStatement);
			}
			locationInfo = locationInfo.replace(output3[1], "");
		}

		if(output4 != null)
		{
			attributeID = 0;
			for(int i = 0; i < config.getKeywordVector().size(); i++)
			{
				if(config.getKeywordVector().get(i).equals(output4))
				{
					attributeID = i + 1;
					break;
				}
			}
			
			if(attributeID != 0)
			{
				insertStatement = "INSERT INTO x1_place_feature_value " +
						"(place_id, attribute_id, frequency, score, importancy) " +
						"VALUES ('"
						+ placeID + "', '"
						+ attributeID + "', '"
						+ "1" + "', '"
						+ "1" + "', '"
						+ "0" + "');";
		
				result.add(insertStatement);
			}
			locationInfo = locationInfo.replace(output4, "");
		}
		if(output5 != null)
		{
			locationInfo = locationInfo.replace(output5, "");
		}
		
		locationInfo = locationInfo.replace("(", "");
		locationInfo = locationInfo.replace(")", "");
		locationInfo = locationInfo.replace("-", "");
		
		output1 = foodFeature(locationInfo, config.getFoodFeature(), config.getVnTokEngine());
		
		for(int i = 0; i < output1.size(); i++)
		{
			for(int j = 0; j < config.getKeywordVector().size(); j++)
			{
				if(config.getKeywordVector().get(j).equals(output1.get(i)))
				{								
					insertStatement = "INSERT INTO x1_place_feature_value (place_id, attribute_id, frequency, score, importancy) VALUES ('"
							+ placeID + "', '"
							+ (j + 1) + "', '"
							+ "1" + "', '"
							+ "4" + "', '"
							+ "1" + "');";
					
					break;
				}
			}
			result.add(insertStatement);
		}
		
		return result;
	}*/
	
	/*
	public static String wardFeature(String line, ArrayList<String> wardList)
	{	
		String output = null;
		
		Pattern disReg = Pattern.compile("(phường|phuong|p)+ ?.? ?[0-9]{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = disReg.matcher("");
		
		Pattern number = Pattern.compile("[0-9]{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher numMatch = number.matcher("");
		
		int flag;
		int indexWard = 1;
		
		flag = 0;
		
		line = line.toLowerCase();
		
		if((indexWard = line.indexOf("phường ")) == -1)
			if((indexWard = line.indexOf("p ")) == -1)
				if((indexWard = line.indexOf("p. ")) == -1)
					indexWard = line.indexOf("p.");
		
		if(indexWard != -1)
		{
			for(int i = 0; i < wardList.size(); i++)
			{
				if(line.contains(wardList.get(i)) && line.indexOf(wardList.get(i)) > indexWard)
				{
					output = wardList.get(i);
					flag = 1;
					break;
				}
			}
		}

		if(flag == 0)
		{
			matcher.reset(line.toLowerCase());
			
			if(matcher.find())
			{
				numMatch.reset(matcher.group());
				
				if(numMatch.find())
				{
					output = numMatch.group();
				}
			}
		}
		
		if(output != null)
			output = "phường " + output;
		
		return output;
	}
	*/
		
	/*
	public static String districtFeature(String line, ArrayList<String> districtList)
	{
		String output = null;
		
		Pattern disReg = Pattern.compile("(quận|quan|q)+ ?.? ?[0-9]{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = disReg.matcher("");
		
		Pattern number = Pattern.compile("[0-9]{1,2}", Pattern.CASE_INSENSITIVE);
		Matcher numMatch = number.matcher("");
		
		int flag = 0;
		for(int i = 0; i < districtList.size(); i++)
		{
			if(line.toLowerCase().contains(districtList.get(i)))
			{
				output = districtList.get(i);
				flag = 1;
				break;
			}
		}
		
		if(flag == 0)
		{
			matcher.reset(line.toLowerCase());
			
			if(matcher.find())
			{
				numMatch.reset(matcher.group());
				
				if(numMatch.find())
				{
					output = numMatch.group();
				}
			}
		}
		
		if(output != null && !output.contains("quận"))
			output = "quận " + output;
		
		return output;
	}
	*/
	
	
	/*
	public static String alphaTest(String queryString, QueryConfig config)
	{
		String output = null;
		
		JSONArray jArray = new JSONArray();
		
		ArrayList<String[]> resultSet = null;
		
				
		resultSet = getLocation(queryString, config);
				
		if(resultSet.size() != 0)
		{		
			for(int i = 0; (resultSet.size() < 20) ? i < resultSet.size() : i < 20 ; i++)
			{
				jArray.add(resultSet.get(i)[0]);
				jArray.add(resultSet.get(i)[1]);
				jArray.add(resultSet.get(i)[2]);
				jArray.add(resultSet.get(i)[3]);
			}
					
			output = jArray.toString();
		}
		else
		{
			output = jArray.toString();
		}
	
		return output;
	}
	*/
}
