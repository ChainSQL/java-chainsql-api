package com.peersafe.chainsql.core;

import com.peersafe.chainsql.util.Util;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChainsqlTest extends TestCase {

    public static final Chainsql  c    = new Chainsql();

    public static String rootAddress      = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
    public static String rootSecret       = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";
    public static String rootPublicKey    = "cBQG8RQArjx1eTKFEAQXz2gS4utaDiEC9wmi7pfUPTi27VCchwgw";


    public static String userSecret    = "xnnUqirFepEKzVdsoBKkMf577upwT";
    public static String userAddress   = "zpMZ2H58HFPB5QTycMGWSXUeF47eA8jyd4";
    public static String sTableName    = "T1";
    public static String userPublicKey = "cB4pxq1LUfwPxNP9Xyj223mqM8zfeW6t2DqP1Ek3UQWaUVb9ciCZ";


    public static String smRootSecret    = "p97evg5Rht7ZB7DbEpVqmV3yiSBMxR3pRBKJyLcRWt7SL5gEeBb";
    public static String smRootAddress   = "zN7TwUjJ899xcvNXZkNJ8eFFv2VLKdESsj";
    public static String smRootPublicKey = "pYvWhW4azFwanovo5MhL71j5PyTWSJi2NVurPYUrE9UYaSVLp29RhtxxQB7xeGvFmdjbtKRzBQ4g9bCW5hjBQSeb7LePMwFM";

    //pYvWhW4azFwanovo5MhL71j5PyTWSJi2NVurPYUrE9UYaSVLp29RhtxxQB7xeGvFmdjbtKRzBQ4g9bCW5hjBQSeb7LePMwFM


    public static String smUserSecret  = "pw5MLePoMLs1DA8y7CgRZWw6NfHik7ZARg8Wp2pr44vVKrpSeUV";
    public static String smUserAddress = "zKzpkRTZPtsaQ733G8aRRG5x5Z2bTqhGbt";


    public void setUp() throws Exception {
//        try{
//           c.connect("ws://192.168.29.69:16006");
//           c.as(rootAddress, rootSecret);
//        }catch (Exception e){
//            c.disconnect();
//            e.printStackTrace();
//            Assert.fail();
//        }
    }

    public void tearDown() throws Exception {
       // c.disconnect();
    }

    public  void testValidationCreate(){

        try{


            // 生成国密版本的验证key
            JSONObject gmOptions = new JSONObject();
            gmOptions.put("algorithm","softGMAlg");
            gmOptions.put("secret","pno9LgAnCHWZfkqFhF22fFe7p5sViocuJPxhKWL4Ljhtiq17Cgm");

            JSONObject obj =  c.validationCreate(gmOptions);
            System.out.println(obj);

        }catch (Exception e){
           e.printStackTrace();
            Assert.fail();
        }
    }


    public void testGetPeers(){

        try{

            JSONObject obj =  c.getPeers();
            if(obj.has("peers") && obj.get("peers").toString() == "null"){
                System.out.println("getPeers = null. Server has no peers");
            }else{
                System.out.println(obj);
            }

        }catch (Exception e){
//            c.disconnect();
//            e.printStackTrace();
            Assert.fail();
        }
    }

    public void testGenerateAddress(){
        try{

            JSONObject obj =  c.generateAddress();
            System.out.println(" 普通账户: " + obj);
            JSONObject gmOptions = new JSONObject();
            gmOptions.put("algorithm","softGMAlg");
            // gmOptions.put("secret","pno9LgAnCHWZfkqFhF22fFe7p5sViocuJPxhKWL4Ljhtiq17Cgm");

            obj =  c.generateAddress(gmOptions);
            System.out.println(" 国密账户 : " + obj);

        }catch (Exception e){
//            c.disconnect();
//            e.printStackTrace();
            Assert.fail();
        }
    }

    public void testAccountSet(){

        try{

            JSONObject jsonObj = c.accountSet("1.2", "3", "5").submit(Submit.SyncCond.validate_success);
            System.out.println(jsonObj);

            jsonObj = c.accountSet("1.2", "3", "0").submit(Submit.SyncCond.validate_success);
            System.out.println(jsonObj);

            jsonObj = c.accountSet("1.2", "0", "0").submit(Submit.SyncCond.validate_success);
            System.out.println(jsonObj);

            jsonObj = c.accountSet("1.0", "0", "0").submit(Submit.SyncCond.validate_success);
            System.out.println(jsonObj);

        }catch (Exception e){
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
            JSONObject obj =  c.getAccountInfo(userAddress);
            System.out.println(obj);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    /**
     * 测试行级控制表
     */
    public void testRuleTable(){


        try {


            sTableName = "rule_20007";
            List<String> args = Util.array("{'field':'id','type':'int','length':11,'UQ':1,'PK':1}",
                    "{'field':'name','type':'varchar','length':50,'default':null}",
                    "{'field':'age','type':'int'}",
                    "{'field':'account','type':'varchar','length':64}",
                    "{'field':'txid','type':'varchar','length':64}");
            String operationRule = "{" +
                    "'Insert':{" +
                    " 'Condition':{'txid':'$tx_hash','account':'$account'},"+
                    " 'Count':{'AccountField':'account','CountLimit':3}" +
                    "}," +
                    "'Update':{" +
                    "  'Condition':{'id':{'$lt':5}}," +
                    "  'Fields':['name','age','txid']" +
                    "}," +
                    "'Delete':{" +
                    "  'Condition':{'account':'$account'}" +
                    "}," +
                    "'Get':{" +
                    "  'Condition':{'id':{'$ge':5}}" +
                    "}" +

                    "}";
            JSONObject obj;
            obj = c.createTable(sTableName, args, Util.StrToJson(operationRule)).submit(Submit.SyncCond.db_success);
            System.out.println("create result:" + obj);

            List<String> orgs1 = Util.array("{'id':49,'name':'aabbcc'}");
            JSONObject obj2 = c.table(sTableName).insert(orgs1).submit(Submit.SyncCond.db_success);
            System.out.println("insert result:" + obj2);
        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }


    }

    public void testBatchInsert(){

        try{

            // 批量转账
            for( int i=0;i<256;i++){

                JSONObject obj2 = c.pay(userAddress,"1000").submit(Submit.SyncCond.validate_success);
                System.out.println("转账交易结果为 : " + obj2);
            }

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }

    public void testTS(){

        try {

            c.beginTran();
            List<String> item = Util.array("{'id':4,'age': 333,'name':'88.185.0021/210-15508U-014P-05015-200100327'}");
            c.table("T1").insert(item);
            c.table("T1").get(Util.array("{'id':3}")).update("{'age':245}");

            JSONObject obj2 = c.commit(Submit.SyncCond.db_success);
            System.out.println("transaction result:" + obj2);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }


    public void testCreateTable() {

        try {


            // 1、 建表
            List<String> args = Util.array("{'field':'id','type':'int','length':11,'PK':1,'NN':1,'UQ':1}",
                    "{'field':'name','type':'varchar','length':50,'default':null}", "{'field':'age','type':'int'}");

            boolean bEncrypted = false;
            JSONObject obj;
            obj = c.createTable("t14", args, bEncrypted).submit(Submit.SyncCond.db_success);
            System.out.println("create result:" + obj);

            // 2 获取表的nameInDB
            String sTableNameInDB;
            JSONObject nameInDB = c.getTableNameInDB(rootAddress, sTableName);
            sTableNameInDB = nameInDB.getString("nameInDB");

            JSONObject tableProperty = new JSONObject();
            tableProperty.put("nameInDB", sTableNameInDB);
            tableProperty.put("confidential", true);

            // 插入表
            List<String> orgs = Util.array("{'id':23,'age': 333,'name':'88.185.0021/210-15508U-014P-05015-200100327'}");
            obj = c.table("t14").insert(orgs).submit(Submit.SyncCond.db_success);
            System.out.println("insert result:" + obj);
//
            // 3 使用tableSet接口 设置表的属性
            obj = c.table(sTableName).tableSet(tableProperty).get(c.array("{'name':'88.185.0021/210-15508U-014P-05015-200100327'}")).submit();
            System.out.println("get result:" + obj);

            // 4 更新表
            List<String> arr1 = Util.array("{'id': 2}");
            obj = c.table(sTableName).tableSet(tableProperty).get(arr1).update("{'age':200}").submit(Submit.SyncCond.db_success);
            System.out.println("update result:" + obj);

            // 5 删除表数据
            obj = c.table(sTableName).tableSet(tableProperty).get(c.array("{'id': " + 2 + "}")).delete().submit(Submit.SyncCond.db_success);
            System.out.println("delete result:" + obj);

            // 6 授权
            if (bEncrypted) {

                obj = c.grant(sTableName, userAddress, userPublicKey, "{insert:true,update:true}")
                        .submit(Submit.SyncCond.validate_success);
                System.out.println("grant result:" + obj.toString());
            } else {

                obj = c.grant(sTableName, userAddress, "{insert:true,update:true}")
                        .submit(Submit.SyncCond.validate_success);
                System.out.println("grant result:" + obj.toString());
            }


            // 7 授权后使用被授权账户插入数据
            c.as(userAddress, userSecret);
            c.use(rootAddress);
            List<String> orgLst = Util.array("{'id':105,'age': 333,'name':'hello'}", "{'id':106,'age': 444,'name':'sss'}", "{'id':107,'age': 555,'name':'rrr'}");
            obj = c.table(sTableName).tableSet(tableProperty).insert(orgLst).submit(Submit.SyncCond.db_success);
            System.out.println("insert after grant result:" + obj);


            // 8 重命名表
            c.as(rootAddress, rootSecret);
            String sReName = "newTable";
            obj = c.renameTable(sTableName, sReName).submit(Submit.SyncCond.db_success);
            System.out.println("rename result:" + obj);

            //  9 删除表
            obj = c.dropTable("test_batch").submit(Submit.SyncCond.db_success);
            System.out.println("drop result:" + obj);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    public void testPay(){

       // pay
        try{

            c.setSchema("AC69A827983A1DDAC7BD408EB8DC93DCB3F4ABF22C3989BAA5781B1C6154E9A2");

//            JSONObject obj = c.getServerInfo();
//            System.out.println(obj);

            JSONObject obj =  c.pay(userAddress,"1000").submit(Submit.SyncCond.validate_success);
            System.out.println("转账交易结果为: " + obj);

//            obj =  c.pay("zKQwdkkzpUQC9haHFEe2EwUsKHvvwwPHsv","1000").submit(Submit.SyncCond.validate_success);
//            System.out.println("转账交易结果为: " + obj);
//
//            obj =  c.pay("zKQhss2DSrPT3H5p7Ej55gx6F3snWH4KtT","1000").submit(Submit.SyncCond.validate_success);
//            System.out.println("转账交易结果为: " + obj);

            // "zKQwdkkzpUQC9haHFEe2EwUsKHvvwwPHsv"

//            obj = c.getServerInfo();
//            System.out.println(obj);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    public void testCreateSchema() {

        for(int index = 0; index < 5;index ++){

            try{
                JSONObject schemaInfo = new JSONObject();
                schemaInfo.put("SchemaName","hello2");
                schemaInfo.put("WithState",false);
                schemaInfo.put("SchemaAdmin",rootAddress);

                List<String> validators = new ArrayList<String>();
                 validators.add("02BD87A95F549ECF607D6AE3AEC4C95D0BFF0F49309B4E7A9F15B842EB62A8ED1B");
                validators.add("021864E8B97E627356D6FDCE987F7AF3D394E9C4423B1E46C20E0FAB9B72F5655D");
                validators.add("02EF09346F8A33A26AE4E6B3B5A22269BFC4C8E83E868F5B7300DB41C6189A4821");
                validators.add("02F52C24C0B0E26ABC0417DCBC5EEE2CF71577EAE88BEFE7A64BFE3A95A2B3BF58");
                //validators.add("03E498A0792EDE4D2C7FC2174B366BE715F22045E55EBDA6EA8EECE93FA72BFC3D");
                JSONArray validatorsJsonArray = new JSONArray(validators);
                System.out.println(validatorsJsonArray);
                schemaInfo.put("Validators",validatorsJsonArray);

                List<String> peerList = new ArrayList<String>();
                peerList.add("192.168.29.69:15125");
                peerList.add("192.168.29.69:25125");
                peerList.add("192.168.29.69:35125");
                peerList.add("192.168.29.69:45125");
//                    peerList.add("127.0.0.1:5125");
//                    peerList.add("127.0.0.1:15125");
                // peerList.add("192.168.29.69:35125");
                // peerList.add("192.168.29.69:35125");
               // peerList.add("192.168.29.69:55125");
                JSONArray peerListJsonArray = new JSONArray(peerList);

                for(int i=0; i<peerListJsonArray.length(); i++){
                    String tx = (String)peerListJsonArray.get(i);
                    System.out.println(tx);
                }

                System.out.println(peerListJsonArray);
                schemaInfo.put("PeerList",peerListJsonArray);

////                // 不继承状态
//                JSONObject ret = c.createSchema(schemaInfo).submit(Submit.SyncCond.validate_success);
//                System.out.println("创建不继承主链状态的子链: " + ret);

//               //  继承主链的状态
                schemaInfo.put("SchemaName","hello3");
                schemaInfo.put("WithState",true);
                schemaInfo.put("AnchorLedgerHash","EB2459201A80C4515BD518F55AF9E8DE67DE431B4E75CF2ABFFC66BF051F5B01"); // 锚定的主链的账本hash
                JSONObject ret = c.createSchema(schemaInfo).submit(Submit.SyncCond.validate_success);
                System.out.println("继承继承主链的状态的子链: " + ret);

            }catch (Exception e){

                e.printStackTrace();
                Assert.fail();
            }

        }



    }

    public void testModifySchema() {

        try{
            JSONObject schemaInfo = new JSONObject();
            schemaInfo.put("SchemaID","BDDA5198206868E9A341152AB7FDFCAB8F27938909DDB3E5CD376106F65B3EDF");

            List<String> validators = new ArrayList<String>();
            validators.add("02BD87A95F549ECF607D6AE3AEC4C95D0BFF0F49309B4E7A9F15B842EB62A8ED1B");
            validators.add("021864E8B97E627356D6FDCE987F7AF3D394E9C4423B1E46C20E0FAB9B72F5655D");
            JSONArray validatorsJsonArray = new JSONArray(validators);
            System.out.println(validatorsJsonArray);
            schemaInfo.put("Validators",validatorsJsonArray);

            List<String> peerList = new ArrayList<String>();
            peerList.add("192.168.29.69:45125");
            peerList.add("192.168.29.69:15125");
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

    public  void testSignAndVerify(){


        try{

            String hello = "hello world";

            // gm
            byte[] signature = c.sign(hello.getBytes(), smRootSecret);
         //   signature = Util.hexToBytes("EAEC0A8917FF60BB22943EC16CFE95BFFFFD0CEF65EFF6B6B7BDCCBD69C8933A60B235442094671281B788221AD650EA00CB54346EAFB935B70170ABF62AFF21");
            if(c.verify(hello.getBytes(), signature, smRootPublicKey))
            {
                System.out.println("gm verify success");
            }else {
                System.out.println("gm verify failed");
            }

           // System.out.println(Util.bytesToHex(signature));

            // Secp256k1
            signature = c.sign(hello.getBytes(), rootSecret);
            if(c.verify(hello.getBytes(), signature, rootPublicKey))
            {
                System.out.println("Secp256k1 verify success");
            }else {
                System.out.println("Secp256k1 verify failed");
            }


        }catch (Exception e){

            e.printStackTrace();
            Assert.fail("testSignAndVerify failure");
        }



    }



    private static  String readFile(String pemPath){

        String str="";

        File file=new File(pemPath);

        try {

            FileInputStream in=new FileInputStream(file);
            int size=in.available();

            byte[] buffer=new byte[size];

            in.read(buffer);

            in.close();

            str=new String(buffer,"GB2312");

        } catch (IOException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

            return null;

        }

        return str;

    }
}