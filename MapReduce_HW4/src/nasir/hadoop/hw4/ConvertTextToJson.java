package nasir.hadoop.hw4;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ConvertTextToJson {
public static void main(String args[]) throws FileNotFoundException, JSONException{
	String s1="1/e:b/b";
	String s2="2/e:c";
	JSONObject json=new JSONObject();
	String[] splits1=s1.split(":");
	String[] splits2=s2.split(":");
	
	json.put(splits1[0], splits1[1]);
	json.put(splits2[0], splits2[1]);
	
	System.out.println(json.get("1/e"));
	System.out.println(json.toString());
}
}
