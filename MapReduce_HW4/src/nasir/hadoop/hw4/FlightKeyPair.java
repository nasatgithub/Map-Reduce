package nasir.hadoop.hw4;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;


/*
 * Flight Key Pair Bean Class
 */
public class FlightKeyPair implements WritableComparable{
private String airId;
private int month;
public FlightKeyPair(){
	
}
public FlightKeyPair(String airId,int month){
	this.airId=airId;
	this.month=month;
}

public String getAirId() {
	return airId;
}
public void setAirId(String airId) {
	this.airId = airId;
	
}
public int getMonth() {
	return month;
}
public void setMonth(int month) {
	this.month = month;
}

public int compareTo(Object o) {
	int ret;
	FlightKeyPair fO=(FlightKeyPair)o;
	ret=this.getAirId().compareTo(fO.getAirId());
	if(ret==0){
		if(this.getMonth()>fO.getMonth())
			ret=1;
		else 
			ret=-1;
	}
	return ret;
}

public static int compare(String a,String b){
	return a.compareTo(b);
}

@Override
public void readFields(DataInput in) throws IOException {
	// TODO Auto-generated method stub
	airId=in.readUTF();
	month=in.readInt();
	
}

@Override
public void write(DataOutput out) throws IOException {
	// TODO Auto-generated method stub
	out.writeUTF(airId);
	out.writeInt(month);
	
}
}
