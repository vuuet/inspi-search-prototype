package org.inspilab.prototype.search;

//Active Record Pattern alpha implementation (prototype)

public class DataContainer {
	
	private String containerName;
	private String[] attributeNames;
	private String[] attributeValues;
	
	public DataContainer()
	{
		setContainerName("");
		setAttributeNames(null);
		setAttributeValues(null);
	}
	
	public String toInsertStatement()
	{
		String output = "INSERT INTO " + containerName;
		
		String attributeNamesList = "(";
		for(int i = 0; i < attributeNames.length; i++)
		{
			if (i != attributeNames.length - 1)
			{
				attributeNamesList += attributeNames[i] + ", ";
			}
			else
			{
				attributeNamesList += attributeNames[i] + ")";
			}
		}
		
		String attributeValuesList = " VALUES (";
		for(int i = 0; i < attributeValues.length; i++)
		{
			if(i != attributeValues.length - 1)
			{
				attributeValuesList += "\"" + attributeValues[i] + "\"" + ", ";	
			}
			else
			{
				attributeValuesList += "\"" + attributeValues[i] + "\"" + ")";
			}
		}
		
		output += attributeNamesList + attributeValuesList + ";"; 
		
		return output;
	}
	
	/*public void save()
	{
	}*/

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	public String getContainerName() {
		return containerName;
	}

	public void setAttributeNames(String[] attributeNames) {
		this.attributeNames = attributeNames;
	}

	public String[] getAttributeNames() {
		return attributeNames;
	}

	public void setAttributeValues(String[] attributeValues) {
		this.attributeValues = attributeValues;
	}

	public String[] getAttributeValues() {
		return attributeValues;
	}
}
