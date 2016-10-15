package nasir.hadoop.hw4;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.RegionSplitter;
import org.apache.hadoop.hbase.util.RegionSplitter.UniformSplit;



public class CreateHbaseTable {
/*
 * To create hbase table by specifying column families and region splits
 */
public static void createHTable(String tableName) throws IOException{
    Configuration con = HBaseConfiguration.create();
    
    // Instantiating HbaseAdmin class
    HBaseAdmin admin = new HBaseAdmin(con);

    // Instantiating table descriptor class
    HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
  
    // Adding column families to table descriptor
    for(int i=0;i<HbaseMapPopulate_StandA.columnList.size();i++){
    	//if(HbaseMapPopulate_StandA.smallColumnList.contains(HbaseMapPopulate_StandA.columnList.get(i)))
    		tableDescriptor.addFamily(new HColumnDescriptor(HbaseMapPopulate_StandA.columnList.get(i)));
    }
    
    if(admin.tableExists(tableName)){
    	System.out.println("Table exists..deleting");
    	admin.disableTable(tableName);
    	admin.deleteTable(tableName);
    }

    // Execute the table through admin
    // Region splits is applied based on start  and end key.
    admin.createTable(tableDescriptor,Bytes.toBytes("19678#1"),Bytes.toBytes("20436#12"),10);
    System.out.println(" Table created ");
}
}
