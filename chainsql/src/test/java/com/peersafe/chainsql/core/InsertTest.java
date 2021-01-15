package com.peersafe.chainsql.core;

import com.peersafe.chainsql.util.Util;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class InsertTest extends TestCase {

    public static final Chainsql c = new Chainsql();

    public static String rootAddress = "zHb9CJAWyB4zj91VRWn96DkukG4bwdtyTh";
    public static String rootSecret = "xnoPBzXtMeMyMHUVTgbuqAfg1SUTb";


    public void testInsert(){

        c.connect("//139.199.164.113:16001");
        c.as(rootAddress, rootSecret);

        List<String> list = Util.array("{'field':'id','type':'int','length':10,'default':NULL}",
                "{'field':'txt','type':'text','default':NULL}");
        JSONObject obj;
        obj = c.createTable("txt_test", list,false).submit(Submit.SyncCond.db_success);

        if (obj.getString("status").equals("db_success")) {
            System.out.println("创建表成功");
        } else {
            System.out.println("创建表失败");
        }

        JSONObject obj1;
        String str = "a\\'b";
        String test = "[{'id':1,'txt': '"+str+"'}]";
        System.out.println(test);

        JSONArray jsonArray = new JSONArray(test);


        obj1 = c.table("txt_test").insert(jsonArray).submit(Submit.SyncCond.db_success);
        System.out.println("insert result:" + obj1);
    }

    public void testExtraFee() throws Exception {
        c.setExtraZXC(100);
        System.out.println(c.extraDrop);

    }

}
