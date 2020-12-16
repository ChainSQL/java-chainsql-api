package com.peersafe.chainsql.core;

import com.peersafe.chainsql.util.Util;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.Assert;

import java.util.List;

public class ChainsqlTest extends TestCase {

    public static final Chainsql  c    = new Chainsql();
    public static String sTableName    = "T1";
    public static String smRootSecret  = "p97evg5Rht7ZB7DbEpVqmV3yiSBMxR3pRBKJyLcRWt7SL5gEeBb";
    public static String smRootAddress = "zN7TwUjJ899xcvNXZkNJ8eFFv2VLKdESsj";
    public static String smUserSecret  = "pw5MLePoMLs1DA8y7CgRZWw6NfHik7ZARg8Wp2pr44vVKrpSeUV";
    public static String smUserAddress = "zKzpkRTZPtsaQ733G8aRRG5x5Z2bTqhGbt";

    public static String smUserPublicKey =  "pYvKjFb71Qrx26jpfMPAkpN1zfr5WTQoHCpsEtE98ZrBCv2EoxEs4rmWR7DcqTwSwEY81opTgL7pzZ2rZ3948vHi4H23vnY3";


    public static String userSecret = "xnnUqirFepEKzVdsoBKkMf577upwT";
    public static String userAddress = "zpMZ2H58HFPB5QTycMGWSXUeF47eA8jyd4";
    public static String userPublicKey = "cB4pxq1LUfwPxNP9Xyj223mqM8zfeW6t2DqP1Ek3UQWaUVb9ciCZ";
    public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
    public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";

    public void setUp() throws Exception {
        try{

            c.connect("ws://192.168.29.108:7017");
            c.as(rootAddress,rootSecret);
        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    public void tearDown() throws Exception {
        c.disconnect();
    }

    public  void testValidationCreate(){

        try{

            for(int i=0;i<100;i++){
                JSONObject gmOptions = new JSONObject();
                gmOptions.put("algorithm","softGMAlg");
                gmOptions.put("secret","pc9rUimGMRkKFAkvprzPCuAFbMbJNTP7K6nfoeSZF6WW5Ltqgh7");
                JSONObject validateCreate = c.validationCreate(gmOptions);
                System.out.println(validateCreate);
            }

        }catch (Exception e){

            e.printStackTrace();
            Assert.fail();
        }

    }

    public void testGenerateAddress(){

        try{

            for( int i= 0 ; i< 10; i++) {

                JSONObject options = new JSONObject();
                options.put("algorithm","softGMAlg");
                JSONObject ret = c.generateAddress(options);
                System.out.println(ret);

                // 指定软国密算法的secret
                options.put( "secret",smUserSecret);
                ret = c.generateAddress(options);
                System.out.println(ret);

                // 默认使用spec256k1
                ret = c.generateAddress();
                System.out.println(ret);

                // 指定secret 生成 账户信息
                ret = c.generateAddress(smUserSecret);
                System.out.println(ret);

            }

        }catch (Exception e){

            e.printStackTrace();
            Assert.fail();
        }

    }

    public  void testPay(){

        try{

         c.pay(userAddress,"1000").submit(Submit.SyncCond.validate_success);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }

    public void testCreateTable() {

        try{
            // 建表
            List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1}",
                    "{'field':'name','type':'varchar','length':50,'default':null}", "{'field':'age','type':'int'}");

            boolean bEncrypted = true;
            JSONObject obj;
            obj = c.createTable(sTableName,args,bEncrypted).submit(Submit.SyncCond.db_success);
            System.out.println("create result:" + obj);

            String sTableNameInDB;
            JSONObject nameInDB = c.getTableNameInDB(rootAddress,sTableName);
            sTableNameInDB = nameInDB.getString("nameInDB");

            JSONObject tableProperty = new JSONObject();
            tableProperty.put("nameInDB",sTableNameInDB);
            tableProperty.put("confidential",true);

            // 插入表
            List<String> orgs = Util.array("{'id':2,'age': 333,'name':'88.185.0021/210-15508U-014P-05015-200100327'}");
            obj = c.table(sTableName).insert(orgs).submit(Submit.SyncCond.db_success);
            System.out.println("insert result:" + obj);

            obj = c.table(sTableName).tableSet(tableProperty).get(c.array("{'name':'88.185.0021/210-15508U-014P-05015-200100327'}")).submit();
            System.out.println("get result:" + obj);

            // 更新表
            List<String> arr1 = Util.array("{'id': 2}");
            obj = c.table(sTableName).tableSet(tableProperty).get(arr1).update("{'age':200}").submit(Submit.SyncCond.db_success);
            System.out.println("update result:" + obj);

            // 删除表数据
            obj = c.table(sTableName).tableSet(tableProperty).get(c.array("{'id': " + 2 + "}")).delete().submit(Submit.SyncCond.db_success);
            System.out.println("delete result:" + obj);

            // 授权
            if(bEncrypted){

                obj = c.grant(sTableName, userAddress,userPublicKey,"{insert:true,update:true}")
                        .submit(Submit.SyncCond.validate_success);
                System.out.println("grant result:" + obj.toString());
            }else{

                obj = c.grant(sTableName, userAddress,"{insert:true,update:true}")
                        .submit(Submit.SyncCond.validate_success);
                System.out.println("grant result:" + obj.toString());
            }


            // 授权后使用被授权账户插入数据
            c.as(userAddress, userSecret);
            c.use(rootAddress);
            List<String> orgLst = Util.array("{'id':105,'age': 333,'name':'hello'}","{'id':106,'age': 444,'name':'sss'}","{'id':107,'age': 555,'name':'rrr'}");
            obj = c.table(sTableName).tableSet(tableProperty).insert(orgLst).submit(Submit.SyncCond.db_success);
            System.out.println("insert after grant result:" + obj);


            // 重命名表
            c.as(rootAddress, rootSecret);
            String sReName = "newTable";
            obj = c.renameTable(sTableName, sReName).submit(Submit.SyncCond.db_success);
            System.out.println("rename result:" + obj);

            // 删除表
            obj = c.dropTable(sReName).submit(Submit.SyncCond.db_success);
            System.out.println("drop result:" + obj);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }

    public void testTableSet(){
        try{

            String table_set_name = "tableSet";
            // 建表
            List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1}",
                    "{'field':'name','type':'varchar','length':50,'default':null}", "{'field':'age','type':'int'}");

            boolean bEncrypted = false;
            JSONObject obj;
            obj = c.createTable(table_set_name,args,bEncrypted).submit(Submit.SyncCond.db_success);
            System.out.println("create result:" + obj);

            String sTableNameInDB;
            JSONObject nameInDB = c.getTableNameInDB(rootAddress,table_set_name);
            sTableNameInDB = nameInDB.getString("nameInDB");

            JSONObject tableProperty = new JSONObject();
            tableProperty.put("nameInDB",sTableNameInDB);
            tableProperty.put("confidential",false);

            // 插入表
            List<String> orgs = Util.array("{'id':2,'age': 333,'name':'88'}");
            obj = c.table(table_set_name).insert(orgs).submit(Submit.SyncCond.db_success);
            System.out.println("insert result:" + obj);

            obj = c.table(table_set_name).tableSet(tableProperty).get(c.array("{'name':'88'}")).submit();
            System.out.println("get result:" + obj);

            // 更新表
            List<String> arr1 = Util.array("{'id': 2}");
            obj = c.table(table_set_name).tableSet(tableProperty).get(arr1).update("{'age':200}").submit(Submit.SyncCond.db_success);
            System.out.println("update result:" + obj);

            // 删除表数据
            obj = c.table(table_set_name).tableSet(tableProperty).get(c.array("{'id': " + 2 + "}")).delete().submit(Submit.SyncCond.db_success);
            System.out.println("delete result:" + obj);


            // 删除表
            obj = c.dropTable(table_set_name).submit(Submit.SyncCond.db_success);
            System.out.println("drop result:" + obj);


        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }


    public void testTransaction(){

        try{

            JSONObject obj = new JSONObject();
            obj.put("hash","AFAD615877384B9CACB099BAFEB4F5AB7F9577062BF313ADAD50253B6D6D108F");
            obj.put("meta",true);
            obj.put("meta_chain",true);

            JSONObject txInfo =  c.getTransaction(obj);
            System.out.println(txInfo);

            txInfo =  c.getTransaction("AFAD615877384B9CACB099BAFEB4F5AB7F9577062BF313ADAD50253B6D6D108F");
            System.out.println(txInfo);


        }catch (Exception e){

            e.printStackTrace();
            Assert.fail();
        }

    }

    public void testTable() {

        try{

            //  表数据的获取
            //  表中字段含有'.' 等特殊字符
            JSONObject obj = c.table(sTableName).get(c.array("{'name':'88.185.0021/210-15508U-014P-05015-200100327'}")).submit();
            System.out.println("get result:" + obj);
            obj = c.dropTable("N8").submit(Submit.SyncCond.db_success);
            System.out.println("get result:" + obj);


        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }


    }

}