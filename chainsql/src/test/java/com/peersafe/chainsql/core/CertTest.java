package com.peersafe.chainsql.core;

import com.peersafe.chainsql.util.Util;
import junit.framework.TestCase;
import org.json.JSONObject;
import org.junit.Assert;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class CertTest extends TestCase {

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

            c.connect("ws://127.0.0.1:6006");
            c.as(rootAddress,rootSecret);
        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }
    }

    public void tearDown() throws Exception {
        c.disconnect();
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
    public void testCert(){

        try{
            String pemContent = readFile("D:\\ca\\userCert.cert");
            c.useCert(pemContent);
            JSONObject obj =  c.pay(userAddress,"1000").submit(Submit.SyncCond.validate_success);
            System.out.println(obj);

        }catch (Exception e){
            e.printStackTrace();
            Assert.fail();
        }

    }

}