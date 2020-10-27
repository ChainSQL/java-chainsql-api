package com.peersafe.chainsql.core;

import com.peersafe.chainsql.util.Util;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.Assert;

import java.util.List;

public class ChainsqlTest extends TestCase {

    public static final Chainsql  c    = new Chainsql();
    public static String sTableName    = "B14";
    public static String smRootSecret  = "p97evg5Rht7ZB7DbEpVqmV3yiSBMxR3pRBKJyLcRWt7SL5gEeBb";
    public static String smRootAddress = "zN7TwUjJ899xcvNXZkNJ8eFFv2VLKdESsj";
    public static String smUserSecret  = "pw5MLePoMLs1DA8y7CgRZWw6NfHik7ZARg8Wp2pr44vVKrpSeUV";
    public static String smUserAddress = "zKzpkRTZPtsaQ733G8aRRG5x5Z2bTqhGbt";

    public static String smUserPublicKey =  "pYvKjFb71Qrx26jpfMPAkpN1zfr5WTQoHCpsEtE98ZrBCv2EoxEs4rmWR7DcqTwSwEY81opTgL7pzZ2rZ3948vHi4H23vnY3";

    public void setUp() throws Exception {
        try{

//            c.connect("ws://192.168.29.69:5003");
//            c.as(smRootAddress,smRootSecret);
//            super.setUp();

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    public void tearDown() throws Exception {
       // c.disconnect();
    }

    public  void testValidationCreate(){

        try{

            for(int i=0;i<10000;i++){
                JSONObject gmOptions = new JSONObject();
                gmOptions.put("algorithm","softGMAlg");
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

         c.pay(smUserAddress,"1000").submit(Submit.SyncCond.validate_success);

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

            boolean bEncrypted = false;

            JSONObject obj;
            obj = c.createTable(sTableName,args,bEncrypted).submit(Submit.SyncCond.db_success);
            System.out.println("create result:" + obj);

            // 插入表
            List<String> orgs = Util.array("{'id':2,'age': 333,'name':'hello'}");
            obj = c.table(sTableName).insert(orgs).submit(Submit.SyncCond.db_success);
            System.out.println("insert result:" + obj);

            // 更新表
            List<String> arr1 = Util.array("{'id': 2}");
            obj = c.table(sTableName).get(arr1).update("{'age':200}").submit(Submit.SyncCond.db_success);
            System.out.println("update result:" + obj);

            // 删除表数据
            obj = c.table(sTableName).get(c.array("{'id': " + 2 + "}")).delete().submit(Submit.SyncCond.db_success);
            System.out.println("delete result:" + obj);

            // 授权
            if(bEncrypted){

                obj = c.grant(sTableName, smUserAddress,smUserPublicKey,"{insert:true,update:true}")
                        .submit(Submit.SyncCond.validate_success);
                System.out.println("grant result:" + obj.toString());
            }else{

                obj = c.grant(sTableName, smUserAddress,"{insert:true,update:true}")
                        .submit(Submit.SyncCond.validate_success);
                System.out.println("grant result:" + obj.toString());
            }


            // 授权后使用被授权账户插入数据
            c.as(smUserAddress, smUserSecret);
            c.use(smRootAddress);
            List<String> orgLst = Util.array("{'id':105,'age': 333,'name':'hello'}","{'id':106,'age': 444,'name':'sss'}","{'id':107,'age': 555,'name':'rrr'}");
            obj = c.table(sTableName).insert(orgLst).submit(Submit.SyncCond.db_success);
            System.out.println("insert after grant result:" + obj);


            // 重命名表
            c.as(smRootAddress, smRootSecret);
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
}