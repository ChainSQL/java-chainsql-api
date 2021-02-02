package com.peersafe.chainsql.core;

import com.peersafe.chainsql.util.Util;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class ChainsqlTest extends TestCase {

    public static final Chainsql  c    = new Chainsql();

    public static String rootAddress   = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
    public static String rootSecret    = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";

    public static String userSecret = "xnnUqirFepEKzVdsoBKkMf577upwT";
    public static String userAddress = "zpMZ2H58HFPB5QTycMGWSXUeF47eA8jyd4";

    public void setUp() throws Exception {
        try{

            c.connect("ws://192.168.29.116:7017");
           // c.connect("ws://192.168.29.69:5003");
            c.as(rootAddress, rootSecret);

        }catch (Exception e){
            c.disconnect();
            e.printStackTrace();
            Assert.fail();
        }
    }

    public void testGetServerInfo(){


        try{

            c.setSchema("6BA63B86E5CE48283D03CC21D3BE5F4630CC6572CE7F54982E5AE687C998B7A3");
            JSONObject obj = c.getServerInfo();
            System.out.println(obj);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }


    public void testAccountInfo(){

        try{

            c.setSchema("6BA63B86E5CE48283D03CC21D3BE5F4630CC6572CE7F54982E5AE687C998B7A3");
            //c.setSchema(Chainsql.MAIN_SCHEMA);
            JSONObject obj =  c.getAccountInfo(userAddress);
            System.out.println(obj);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }


    public void testPay(){

       // pay
        try{

            c.setSchema("6BA63B86E5CE48283D03CC21D3BE5F4630CC6572CE7F54982E5AE687C998B7A3");

            JSONObject obj = c.getServerInfo();
            System.out.println(obj);

            obj =  c.pay(userAddress,"1000").submit(Submit.SyncCond.validate_success);
            System.out.println("转账交易结果为: " + obj);

            obj = c.getServerInfo();
            System.out.println(obj);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    public void testCreateSchema() {

        try{
            JSONObject schemaInfo = new JSONObject();
            schemaInfo.put("SchemaName","hello2");
            schemaInfo.put("WithState",false);
            schemaInfo.put("SchemaAdmin",rootAddress);

            List<String> validators = new ArrayList<String>();
            validators.add("02BD87A95F549ECF607D6AE3AEC4C95D0BFF0F49309B4E7A9F15B842EB62A8ED1B");
            JSONArray validatorsJsonArray = new JSONArray(validators);
            System.out.println(validatorsJsonArray);
            schemaInfo.put("Validators",validatorsJsonArray);

            List<String> peerList = new ArrayList<String>();
            peerList.add("192.168.29.116:7016");
            JSONArray peerListJsonArray = new JSONArray(peerList);

            for(int i=0; i<peerListJsonArray.length(); i++){
                String tx = (String)peerListJsonArray.get(i);
                System.out.println(tx);
            }

            System.out.println(peerListJsonArray);
            schemaInfo.put("PeerList",peerListJsonArray);

            // 不继承状态
            JSONObject ret = c.createSchema(schemaInfo).submit(Submit.SyncCond.validate_success);
            System.out.println("创建不继承主链状态的子链: " + ret);

//            // 继承主链的状态
//            schemaInfo.put("SchemaName","hello3");
//            schemaInfo.put("WithState",true);
//            schemaInfo.put("AnchorLedgerHash","2FA25D7E49145E04C25B7B719F4198433EB83FCF13131B0444B879B1DFE6AA55"); // 锚定的主链的账本hash
//            ret = c.createSchema(schemaInfo).submit(Submit.SyncCond.validate_success);
//            System.out.println("继承继承主链的状态的子链: " + ret);

        }catch (Exception e){

            e.printStackTrace();
            Assert.fail();
        }

    }

    public void testModifySchema() {

        try{
            JSONObject schemaInfo = new JSONObject();
            schemaInfo.put("SchemaID","6BA63B86E5CE48283D03CC21D3BE5F4630CC6572CE7F54982E5AE687C998B7A3");

            List<String> validators = new ArrayList<String>();
            validators.add("02BD87A95F549ECF607D6AE3AEC4C95D0BFF0F49309B4E7A9F15B842EB62A8ED1C");
            JSONArray validatorsJsonArray = new JSONArray(validators);
            System.out.println(validatorsJsonArray);
            schemaInfo.put("Validators",validatorsJsonArray);

            List<String> peerList = new ArrayList<String>();
            peerList.add("192.168.29.116:7019");
            JSONArray peerListJsonArray = new JSONArray(peerList);

            for(int i=0; i<peerListJsonArray.length(); i++){
                String tx = (String)peerListJsonArray.get(i);
                System.out.println(tx);
            }

            System.out.println(peerListJsonArray);
            schemaInfo.put("PeerList",peerListJsonArray);

             // SchemaOpType
            JSONObject obj =  c.modifySchema(Submit.SchemaOpType.schema_add,schemaInfo).submit(Submit.SyncCond.validate_success);

            System.out.println(obj);

        }catch (Exception e){

            e.printStackTrace();
            Assert.fail();
        }

    }

    public  void testGetSchemaList(){
        try{

            JSONObject item = new JSONObject();
            item.put("running",false);
            item.put("account",rootAddress);
            JSONObject ret =  c.getSchemaList(item);
            System.out.println(ret);

        }catch (Exception e){

            e.printStackTrace();
            Assert.fail("schema_list failure");
        }
    }

    public  void testGetSchemaInfo(){

        try{

            String schemaID = "6BA63B86E5CE48283D03CC21D3BE5F4630CC6572CE7F54982E5AE687C998B7A3";
            JSONObject ret =  c.getSchemaInfo(schemaID);
            System.out.println(ret);

        }catch (Exception e){

            e.printStackTrace();
            Assert.fail("schema_info failure");
        }

    }

}