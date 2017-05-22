package java8.test;

import java.util.ArrayList;

import org.json.JSONObject;

import com.peersafe.chainsql.core.Chainsql;
import com.peersafe.chainsql.core.Submit.SyncCond;
import com.peersafe.chainsql.core.Table;
import com.peersafe.chainsql.net.Connection;
import com.peersafe.chainsql.util.Util;



public class Demo1 {
	Chainsql r = null;
	Connection conn =null;
	
	public static void main(String[] args) {
		System.out.println("aaaaaaaaaaaaaaaaa");
		// TODO Auto-generated method stub
//		Demo1 demo =new Demo1("192.9.210.74",6006);
		Demo1 demo =new Demo1("192.168.0.197",6007);
		JSONObject obj = demo.update("hijack","{'age':22}","{'name':'jack'}");
		System.out.println(obj);
		demo.release();//
		//����Ҫ���³�ʼ�����ӣ���ƽ̨���߼�����һ�£�
		demo =new Demo1("192.168.0.197",6007);
		demo.insert();
		demo.release();
		System.out.println("�������");
//		System.exit(0);
	}
	
	private void insert() {
		// TODO Auto-generated method stub
		//�Ȳ���T_BBPS_SESSIONHIS
//		ArrayList<String> raw=new ArrayList<String>();
//		JSONObject row=new JSONObject();
//		row.put("PAYERBANKNO", "100000000001");
//		raw.add(row.toString());
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000002\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		raw.add(" {\"PAYERBANKNO\":\"100000000001\",\"LIQUIDSESSION\":\"11\",\"LIQUIDSTATUS\":\"0\",\"PAYEEBANKNO\":\"100000000003\",\"WORKDATE\":\"20170405\",\"LIQUIDTYPE\":\"0\"}");
//		System.out.println(raw);
//		insert("T_BBPS_SESSIONHIS",raw);
		//�ٲ���T_BBPS_RUNADMIN1
//		raw=new ArrayList<String>();
//		row=new JSONObject();
//		row.put("SYSSTATUS", "01");
//		row.put("WORKDATE", "00000001");
//		row.put("SESSIONID", "1");
//		row.put("PREWORKDATE", "20170402");
//		row.put("PREWORKDATE", "20170402");
//		row.put("LIQUIDTYPE", "1");
//		raw.add(row.toString());
//		insert("T_BBPS_RUNADMIN1",raw);
//
		JSONObject row2=new JSONObject();
		row2.put("name", "jack");
		row2.put("age", "28");
		ArrayList raw=new ArrayList();
		for(int i=0; i<20; i++)
			raw.add(row2.toString());
		insert("hijack2",raw);		
	
	}
	private void createTable_T_BBPS_BANKNO_CHAIN() {
		// TODO Auto-generated method stub
		String tableName="T_BBPS_BANKNO_CHAIN";
		ArrayList<String> raw=new ArrayList<String>();
		JSONObject ID=new JSONObject();
		ID.put("field", "ID");ID.put("type", "int");ID.put("length", 11);ID.put("NN", 1);ID.put("PK",1);ID.put("AI",1);
		raw.add(ID.toString());
		System.out.println("ID:"+ID.toString());
		JSONObject BANKNO=new JSONObject();
		BANKNO.put("field", "BANKNO");BANKNO.put("type", "varchar");BANKNO.put("length", 12);BANKNO.put("NN", 1);
		raw.add(BANKNO.toString());
		System.out.println("BANKNO:"+BANKNO.toString());
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		JSONObject BANKNAME=new JSONObject();
		BANKNAME.put("field", "BANKNAME");BANKNAME.put("type", "varchar");BANKNAME.put("length", 255);BANKNAME.put("default","");
		raw.add(BANKNAME.toString());
		System.out.println("BANKNAME:"+BANKNAME.toString());
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		JSONObject BANKSTATUS=new JSONObject();
		BANKSTATUS.put("field", "BANKSTATUS");BANKSTATUS.put("type", "varchar");BANKSTATUS.put("length", 2);BANKSTATUS.put("default","");
		raw.add(BANKSTATUS.toString());
		System.out.println("BANKSTATUS:"+BANKSTATUS.toString());
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		JSONObject VALIDATEDATE=new JSONObject();
		VALIDATEDATE.put("field", "VALIDATEDATE");VALIDATEDATE.put("type", "varchar");VALIDATEDATE.put("length", 8);VALIDATEDATE.put("default","");
		raw.add(VALIDATEDATE.toString());
		System.out.println("VALIDATEDATE:"+VALIDATEDATE.toString());
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		JSONObject INVALIDATEDATE=new JSONObject();
		INVALIDATEDATE.put("field", "INVALIDATEDATE");INVALIDATEDATE.put("type", "varchar");INVALIDATEDATE.put("length", 8);INVALIDATEDATE.put("default","");
		raw.add(INVALIDATEDATE.toString());
		System.out.println("INVALIDATEDATE:"+INVALIDATEDATE.toString());
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		JSONObject LIQUIDBANKFLAG=new JSONObject();
		LIQUIDBANKFLAG.put("field", "LIQUIDBANKFLAG");LIQUIDBANKFLAG.put("type", "varchar");LIQUIDBANKFLAG.put("length", 1);LIQUIDBANKFLAG.put("default","");
		raw.add(LIQUIDBANKFLAG.toString());
		System.out.println("LIQUIDBANKFLAG:"+LIQUIDBANKFLAG.toString());
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		createTable(tableName, raw);
		System.out.println("�������");
		System.out.println("�������");
	}
	/*************����T_BBPS_CASH************************************************************************************/
	private void createTable_T_BBPS_CASH() {
		// TODO Auto-generated method stub
		/****************************"��ʼ����...T_BBPS_CASH********************/
		String tableName="T_BBPS_CASH";
		ArrayList<String> raw=new ArrayList<String>();
		JSONObject ID=new JSONObject();
		ID.put("field", "ID");ID.put("type", "int");ID.put("length", 11);ID.put("NN", 1);ID.put("PK",1);ID.put("AI",1);
		raw.add(ID.toString());
		System.out.println("ID:"+ID.toString());
		JSONObject BANKNO=new JSONObject();
		BANKNO.put("field", "BANKNO");BANKNO.put("type", "varchar");BANKNO.put("length", 12);BANKNO.put("NN", 1);
		raw.add(BANKNO.toString());
		System.out.println("BANKNO:"+BANKNO.toString());
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		JSONObject BALANCE=new JSONObject();
		BALANCE.put("field", "BALANCE");BALANCE.put("type", "int");BALANCE.put("length", 11);BALANCE.put("default","");
		raw.add(BALANCE.toString());
		System.out.println("BALANCE:"+BALANCE.toString());
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		JSONObject CREDITLINE=new JSONObject();
		CREDITLINE.put("field", "CREDITLINE");CREDITLINE.put("type", "int");CREDITLINE.put("length", 11);CREDITLINE.put("default","");
		raw.add(CREDITLINE.toString());
		System.out.println("CREDITLINE:"+CREDITLINE.toString());
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		createTable(tableName, raw);
		System.out.println("�������");
		System.out.println("�������");
	}
	/*************����T_BBPS_LEDGER_ONCHAIN************************************************************************************/
	private void createTable_T_BBPS_LEDGER_ONCHAIN() {
		// TODO Auto-generated method stub
		/****************************"��ʼ����...T_BBPS_LEDGER_ONCHAIN�������********************/
		String tableName="T_BBPS_LEDGER_ONCHAIN";
		ArrayList<String> raw=new ArrayList<String>();
		JSONObject ID=new JSONObject();
		ID.put("field", "ID");ID.put("type", "int");ID.put("length", 11);ID.put("NN", 1);ID.put("PK",1);ID.put("AI",1);
		raw.add(ID.toString());
		System.out.println("ID:"+ID.toString());
		JSONObject SENDBANKNO=new JSONObject();
		SENDBANKNO.put("field", "SENDBANKNO");SENDBANKNO.put("type", "varchar");SENDBANKNO.put("length", 12);SENDBANKNO.put("NN", 1);
		raw.add(SENDBANKNO.toString());
		System.out.println("SENDBANKNO:"+SENDBANKNO.toString());
		JSONObject AGENTSERIALNO=new JSONObject();
		AGENTSERIALNO.put("field", "AGENTSERIALNO");AGENTSERIALNO.put("type", "varchar");AGENTSERIALNO.put("length", 24);AGENTSERIALNO.put("default","");
		raw.add(AGENTSERIALNO.toString());
		System.out.println("AGENTSERIALNO:"+AGENTSERIALNO.toString());
		JSONObject WORKDATE=new JSONObject();
		WORKDATE.put("field", "WORKDATE");WORKDATE.put("type", "varchar");WORKDATE.put("length", 8);WORKDATE.put("default","");
		raw.add(WORKDATE.toString());
		System.out.println("WORKDATE:"+WORKDATE.toString());
		JSONObject RECVBANKNO=new JSONObject();
		RECVBANKNO.put("field", "RECVBANKNO");RECVBANKNO.put("type", "varchar");RECVBANKNO.put("length", 12);RECVBANKNO.put("default","");
		raw.add(RECVBANKNO.toString());
		System.out.println("RECVBANKNO:"+RECVBANKNO.toString());
		JSONObject PAYERBANKNO=new JSONObject();
		PAYERBANKNO.put("field", "PAYERBANKNO");PAYERBANKNO.put("type", "varchar");PAYERBANKNO.put("length", 12);PAYERBANKNO.put("default","");
		raw.add(PAYERBANKNO.toString());
		System.out.println("PAYERBANKNO:"+PAYERBANKNO.toString());
		JSONObject PAYERACCT=new JSONObject();
		PAYERACCT.put("field", "PAYERACCT");PAYERACCT.put("type", "varchar");PAYERACCT.put("length", 30);PAYERACCT.put("default","");
		raw.add(PAYERACCT.toString());
		System.out.println("PAYERACCT:"+PAYERACCT.toString());
		JSONObject PAYERNAME=new JSONObject();
		PAYERNAME.put("field", "PAYERNAME");PAYERNAME.put("type", "varchar");PAYERNAME.put("length", 255);PAYERNAME.put("default","");
		raw.add(PAYERNAME.toString());
		System.out.println("PAYERNAME:"+PAYERNAME.toString());
		JSONObject PAYEEBANKNO=new JSONObject();
		PAYEEBANKNO.put("field", "PAYEEBANKNO");PAYEEBANKNO.put("type", "varchar");PAYEEBANKNO.put("length", 12);PAYEEBANKNO.put("default","");
		raw.add(PAYEEBANKNO.toString());
		System.out.println("PAYEEBANKNO:"+PAYEEBANKNO.toString());
		JSONObject PAYEEACCT=new JSONObject();
		PAYEEACCT.put("field", "PAYEEACCT");PAYEEACCT.put("type", "varchar");PAYEEACCT.put("length", 30);PAYEEACCT.put("default","");
		raw.add(PAYEEACCT.toString());
		System.out.println("PAYEEACCT:"+PAYEEACCT.toString());
		JSONObject PAYEENAME=new JSONObject();
		PAYEENAME.put("field", "PAYEENAME");PAYEENAME.put("type", "varchar");PAYEENAME.put("length", 255);PAYEENAME.put("default","");
		raw.add(PAYEENAME.toString());
		System.out.println("PAYEENAME:"+PAYEENAME.toString());
		JSONObject CURRTYPE=new JSONObject();
		CURRTYPE.put("field", "CURRTYPE");CURRTYPE.put("type", "varchar");CURRTYPE.put("length", 3);CURRTYPE.put("default","");
		raw.add(CURRTYPE.toString());
		System.out.println("CURRTYPE:"+CURRTYPE.toString());
		JSONObject AMOUNT=new JSONObject();
		AMOUNT.put("field", "AMOUNT");AMOUNT.put("type", "int");AMOUNT.put("length", 20);
		raw.add(AMOUNT.toString());
		System.out.println("AMOUNT:"+AMOUNT.toString());
		JSONObject DEBITCREDITFLAG=new JSONObject();
		DEBITCREDITFLAG.put("field", "DEBITCREDITFLAG");DEBITCREDITFLAG.put("type", "varchar");DEBITCREDITFLAG.put("length", 1);DEBITCREDITFLAG.put("default","");
		raw.add(DEBITCREDITFLAG.toString());
		System.out.println("DEBITCREDITFLAG:"+DEBITCREDITFLAG.toString());
		JSONObject PAYBACKFLAG=new JSONObject();
		PAYBACKFLAG.put("field", "PAYBACKFLAG");PAYBACKFLAG.put("type", "varchar");PAYBACKFLAG.put("length", 2);PAYBACKFLAG.put("default","");
		raw.add(PAYBACKFLAG.toString());
		System.out.println("PAYBACKFLAG"+PAYBACKFLAG.toString());
		JSONObject PAYBACKREASON=new JSONObject();
		PAYBACKREASON.put("field", "PAYBACKREASON");PAYBACKREASON.put("type", "varchar");PAYBACKREASON.put("length", 255);PAYBACKREASON.put("default","");
		raw.add(PAYBACKREASON.toString());
		System.out.println("PAYBACKREASON:"+PAYBACKREASON.toString());
		JSONObject ORISENDBANKNO=new JSONObject();
		ORISENDBANKNO.put("field", "ORISENDBANKNO");ORISENDBANKNO.put("type", "varchar");ORISENDBANKNO.put("length", 12);ORISENDBANKNO.put("default","");
		raw.add(ORISENDBANKNO.toString());
		System.out.println("ORISENDBANKNO:"+ORISENDBANKNO.toString());
		JSONObject ORIAGENTSERIALNO=new JSONObject();
		ORIAGENTSERIALNO.put("field", "ORIAGENTSERIALNO");ORIAGENTSERIALNO.put("type", "varchar");ORIAGENTSERIALNO.put("length", 24);ORIAGENTSERIALNO.put("default","");
		raw.add(ORIAGENTSERIALNO.toString());
		System.out.println("ORIAGENTSERIALNO:"+ORIAGENTSERIALNO.toString());
		JSONObject ORIWORKDATE=new JSONObject();
		ORIWORKDATE.put("field", "ORIWORKDATE");ORIWORKDATE.put("type", "varchar");ORIWORKDATE.put("length", 8);ORIWORKDATE.put("default","");
		raw.add(ORIWORKDATE.toString());
		System.out.println("ORIWORKDATE:"+ORIWORKDATE.toString());
		JSONObject LIQUIDDATE=new JSONObject();
		LIQUIDDATE.put("field", "LIQUIDDATE");LIQUIDDATE.put("type", "varchar");LIQUIDDATE.put("length", 8);LIQUIDDATE.put("default","");
		raw.add(LIQUIDDATE.toString());
		System.out.println("LIQUIDDATE:"+LIQUIDDATE.toString());
		JSONObject SESSIONID=new JSONObject();
		SESSIONID.put("field", "SESSIONID");SESSIONID.put("type", "int");
		raw.add(SESSIONID.toString());
		System.out.println("SESSIONID:"+SESSIONID.toString());
		JSONObject LIQUIDSTATUS=new JSONObject();
		LIQUIDSTATUS.put("field", "LIQUIDSTATUS");LIQUIDSTATUS.put("type", "varchar");LIQUIDSTATUS.put("length", 1);LIQUIDSTATUS.put("default","");
		raw.add(LIQUIDSTATUS.toString());
		System.out.println("LIQUIDSTATUS:"+LIQUIDSTATUS.toString());
		
		createTable(tableName, raw);
		System.out.println("�������");
		System.out.println("�������");
	}
	/*************����T_BBPS_SESSIONHIS************************************************************************************/
	private void createTable_T_BBPS_SESSIONHIS() {
		// TODO Auto-generated method stub
		/****************************����T_BBPS_SESSIONHIS***********************************************************/
		System.out.println("��ʼ����...");
		String  tableName="T_BBPS_SESSIONHIS";
		 ArrayList raw=new ArrayList<String>();
		 JSONObject ID=new JSONObject();
		ID.put("field", "ID");ID.put("type", "int");ID.put("length", 11);ID.put("PK",1);ID.put("NN", 1);ID.put("UQ", 1);ID.put("AI", 1);
		raw.add(ID.toString());
		System.out.println("ID:"+ID.toString());
		JSONObject PAYERBANKNO=new JSONObject();
		PAYERBANKNO.put("field", "PAYERBANKNO");PAYERBANKNO.put("type", "varchar");PAYERBANKNO.put("length", 12);PAYERBANKNO.put("default","");
		raw.add(PAYERBANKNO.toString());
		System.out.println("PAYERBANKNO:"+PAYERBANKNO.toString());
		/////////////////////////
		JSONObject PAYEEBANKNO=new JSONObject();
		PAYEEBANKNO.put("field", "PAYEEBANKNO");PAYEEBANKNO.put("type", "varchar");PAYEEBANKNO.put("length", 12);PAYEEBANKNO.put("default","");
		raw.add(PAYEEBANKNO.toString());
		System.out.println("PAYEEBANKNO:"+PAYEEBANKNO.toString());
		/////////////////////////
		JSONObject WORKDATE=new JSONObject();
		WORKDATE.put("field", "WORKDATE");WORKDATE.put("type", "varchar");WORKDATE.put("length", 8);WORKDATE.put("default","");
		raw.add(WORKDATE.toString());
		System.out.println("WORKDATE:"+WORKDATE.toString());
		/////////////////////////
		JSONObject LIQUIDSESSION=new JSONObject();
		LIQUIDSESSION.put("field", "LIQUIDSESSION");LIQUIDSESSION.put("type", "varchar");LIQUIDSESSION.put("length", 3);LIQUIDSESSION.put("default","");
		raw.add(LIQUIDSESSION.toString());
		System.out.println("LIQUIDSESSION:"+LIQUIDSESSION.toString());
		/////////////////////////
		JSONObject LIQUIDTYPE=new JSONObject();
		LIQUIDTYPE.put("field", "LIQUIDTYPE");LIQUIDTYPE.put("type", "varchar");LIQUIDTYPE.put("length", 1);LIQUIDTYPE.put("default","");
		raw.add(LIQUIDTYPE.toString());
		System.out.println("LIQUIDTYPE:"+LIQUIDTYPE.toString());
		/////////////////////////
		JSONObject LIQUIDSTATUS=new JSONObject();
		LIQUIDSTATUS.put("field", "LIQUIDSTATUS");LIQUIDSTATUS.put("type", "varchar");LIQUIDSTATUS.put("length", 1);LIQUIDSTATUS.put("default","");
		raw.add(LIQUIDSTATUS.toString());
		System.out.println("LIQUIDSESSION:"+LIQUIDSTATUS.toString());
		/////////////////////////
		createTable(tableName, raw);
		System.out.println("�������");
				
	}
	/*************����T_BBPS_LIQUIDDTL************************************************************************************/
	private void createTable_T_BBPS_LIQUIDDTL() {
//		 TODO Auto-generated method stub
		System.out.println("��ʼ����...T_BBPS_LIQUIDDTL");
		String tableName="T_BBPS_LIQUIDDTL";
		ArrayList<String> raw=new ArrayList<String>();
		JSONObject ID=new JSONObject();
		ID.put("field", "ID");ID.put("type", "int");ID.put("length", 11);ID.put("PK",1);ID.put("NN", 1);ID.put("UQ", 1);ID.put("AI", 1);
		raw.add(ID.toString());
		System.out.println("ID:"+ID.toString());
		JSONObject LIQUIDTYPE=new JSONObject();
		LIQUIDTYPE.put("field", "LIQUIDTYPE");LIQUIDTYPE.put("type", "varchar");LIQUIDTYPE.put("length", 1);LIQUIDTYPE.put("default","");
		raw.add(LIQUIDTYPE.toString());
		System.out.println("LIQUIDTYPE:"+LIQUIDTYPE.toString());
		/////////////////////////
		JSONObject LIQUIDDATE=new JSONObject();
		LIQUIDDATE.put("field", "LIQUIDDATE");LIQUIDDATE.put("type", "varchar");LIQUIDDATE.put("length", 8);LIQUIDDATE.put("default","");
		raw.add(LIQUIDDATE.toString());
		System.out.println("PAYEEBANKNO:"+LIQUIDDATE.toString());
		/////////////////////////
		JSONObject LIQUIDSESSION=new JSONObject();
		LIQUIDSESSION.put("field", "LIQUIDSESSION");LIQUIDSESSION.put("type", "int");LIQUIDSESSION.put("length", 3);LIQUIDSESSION.put("default","");
		raw.add(LIQUIDSESSION.toString());
		System.out.println("LIQUIDSESSION:"+LIQUIDSESSION.toString());
		/////////////////////////
		JSONObject AMOUNT=new JSONObject();
		AMOUNT.put("field", "AMOUNT");AMOUNT.put("type", "int");AMOUNT.put("length", 11);AMOUNT.put("default","");
		raw.add(AMOUNT.toString());
		System.out.println("AMOUNT:"+AMOUNT.toString());
		/////////////////////////
		JSONObject PAYERBANKNO=new JSONObject();
		PAYERBANKNO.put("field", "PAYERBANKNO");PAYERBANKNO.put("type", "varchar");PAYERBANKNO.put("length", 12);PAYERBANKNO.put("default","");
		raw.add(PAYERBANKNO.toString());
		System.out.println("PAYERBANKNO:"+PAYERBANKNO.toString());
		/////////////////////////
		JSONObject PAYEEBANKNO=new JSONObject();
		PAYEEBANKNO.put("field", "PAYEEBANKNO");PAYEEBANKNO.put("type", "varchar");PAYEEBANKNO.put("length", 12);PAYEEBANKNO.put("default","");
		raw.add(PAYEEBANKNO.toString());
		System.out.println("PAYEEBANKNO:"+PAYEEBANKNO.toString());
		/////////////////////////
		JSONObject AGENTSERIALNO=new JSONObject();
		AGENTSERIALNO.put("field", "AGENTSERIALNO");AGENTSERIALNO.put("type", "varchar");AGENTSERIALNO.put("length", 24);AGENTSERIALNO.put("default","");
		raw.add(AGENTSERIALNO.toString());
		System.out.println("AGENTSERIALNO:"+AGENTSERIALNO.toString());
		/////////////////////////
		JSONObject WORKDATE=new JSONObject();
		WORKDATE.put("field", "WORKDATE");WORKDATE.put("type", "varchar");WORKDATE.put("length", 8);WORKDATE.put("default","");
		raw.add(WORKDATE.toString());
		System.out.println("WORKDATE:"+WORKDATE.toString());
		/////////////////////////
		createTable(tableName, raw);
		System.out.println("�������");

		
	}
	/*************����T_BBPS_RUNADMIN************************************************************************************/
	private void createTable_T_BBPS_RUNADMIN() {
		// TODO Auto-generated method stub
		System.out.println("��ʼ����...T_BBPS_RUNADMIN");
		String tableName="T_BBPS_RUNADMIN";
		ArrayList<String> raw=new ArrayList<String>();
		JSONObject ID=new JSONObject();
		ID.put("field", "ID");ID.put("type", "int");ID.put("length", 11);ID.put("PK",1);ID.put("NN", 1);ID.put("UQ", 1);ID.put("AI", 1);
		raw.add(ID.toString());
		System.out.println("ID:"+ID.toString());
		JSONObject SYSSTATUS=new JSONObject();
		SYSSTATUS.put("field", "SYSSTATUS");SYSSTATUS.put("type", "varchar");SYSSTATUS.put("length", 2);SYSSTATUS.put("default","");
		raw.add(SYSSTATUS.toString());
		System.out.println("SYSSTATUS:"+SYSSTATUS.toString());
		/////////////////////////
		JSONObject WORKDATE=new JSONObject();
		WORKDATE.put("field", "WORKDATE");WORKDATE.put("type", "varchar");WORKDATE.put("length", 8);WORKDATE.put("default","");
		raw.add(WORKDATE.toString());
		System.out.println("WORKDATE:"+WORKDATE.toString());
		/////////////////////////
		JSONObject SESSIONID=new JSONObject();
		SESSIONID.put("field", "SESSIONID");SESSIONID.put("type", "int");SESSIONID.put("length", 3);SESSIONID.put("default","");
		raw.add(SESSIONID.toString());
		System.out.println("SESSIONID:"+SESSIONID.toString());
		/////////////////////////
		JSONObject PREWORKDATE=new JSONObject();
		PREWORKDATE.put("field", "PREWORKDATE");PREWORKDATE.put("type", "varchar");PREWORKDATE.put("length", 8);PREWORKDATE.put("default","");
		raw.add(PREWORKDATE.toString());
		System.out.println("PREWORKDATE:"+PREWORKDATE.toString());
		/////////////////////////
		JSONObject LIQUIDTYPE=new JSONObject();
		LIQUIDTYPE.put("field", "LIQUIDTYPE");LIQUIDTYPE.put("type", "varchar");LIQUIDTYPE.put("length", 1);LIQUIDTYPE.put("default","");
		raw.add(LIQUIDTYPE.toString());
		System.out.println("LIQUIDTYPE:"+LIQUIDTYPE.toString());
		/////////////////////////
		createTable(tableName, raw);
		ArrayList<String> rows=new ArrayList<String>();
		JSONObject row=new JSONObject();
		row.put("SYSSTATUS", "01");
		row.put("WORKDATE", "20170405");
		row.put("SESSIONID", "1");
		row.put("PREWORKDATE", "20170402");
		row.put("PREWORKDATE", "20170402");
		row.put("LIQUIDTYPE", "1");
		rows.add(row.toString());
		insert("T_BBPS_RUNADMIN",rows);

		System.out.println("�������");
	}
	private void createTable_T_BBPS_RUNADMIN1() {
		// TODO Auto-generated method stub
		System.out.println("��ʼ����...T_BBPS_RUNADMIN1");
		String tableName="T_BBPS_RUNADMIN1";
		ArrayList<String> raw=new ArrayList<String>();
		JSONObject ID=new JSONObject();
		ID.put("field", "ID");ID.put("type", "int");ID.put("length", 11);ID.put("PK",1);ID.put("NN", 1);ID.put("UQ", 1);ID.put("AI", 1);
		raw.add(ID.toString());
		System.out.println("ID:"+ID.toString());
		JSONObject SYSSTATUS=new JSONObject();
		SYSSTATUS.put("field", "SYSSTATUS");SYSSTATUS.put("type", "varchar");SYSSTATUS.put("length", 2);SYSSTATUS.put("default","");
		raw.add(SYSSTATUS.toString());
		System.out.println("SYSSTATUS:"+SYSSTATUS.toString());
		/////////////////////////
		JSONObject WORKDATE=new JSONObject();
		WORKDATE.put("field", "WORKDATE");WORKDATE.put("type", "varchar");WORKDATE.put("length", 8);WORKDATE.put("default","");
		raw.add(WORKDATE.toString());
		System.out.println("WORKDATE:"+WORKDATE.toString());
		/////////////////////////
		JSONObject SESSIONID=new JSONObject();
		SESSIONID.put("field", "SESSIONID");SESSIONID.put("type", "int");SESSIONID.put("length", 3);SESSIONID.put("default","");
		raw.add(SESSIONID.toString());
		System.out.println("SESSIONID:"+SESSIONID.toString());
		/////////////////////////
		JSONObject PREWORKDATE=new JSONObject();
		PREWORKDATE.put("field", "PREWORKDATE");PREWORKDATE.put("type", "varchar");PREWORKDATE.put("length", 8);PREWORKDATE.put("default","");
		raw.add(PREWORKDATE.toString());
		System.out.println("PREWORKDATE:"+PREWORKDATE.toString());
		/////////////////////////
		JSONObject LIQUIDTYPE=new JSONObject();
		LIQUIDTYPE.put("field", "LIQUIDTYPE");LIQUIDTYPE.put("type", "varchar");LIQUIDTYPE.put("length", 1);LIQUIDTYPE.put("default","");
		raw.add(LIQUIDTYPE.toString());
		System.out.println("LIQUIDTYPE:"+LIQUIDTYPE.toString());
		/////////////////////////
//		createTable(tableName, raw);
		ArrayList<String> rows=new ArrayList<String>();
		JSONObject row=new JSONObject();
		row.put("SYSSTATUS", "01");
		row.put("WORKDATE", "20170405");
		row.put("SESSIONID", "1");
		row.put("PREWORKDATE", "20170402");
		row.put("PREWORKDATE", "20170402");
		row.put("LIQUIDTYPE", "1");
		rows.add(row.toString());
		insert("T_BBPS_RUNADMIN1",rows);

		System.out.println("�������");
	}
	public void createTable(String table ,ArrayList<String> raw){
		
		r.createTable(table,raw).submit();
		System.out.println("�����ɹ�");
	}
	public void dropTable(String tableName){
		r.dropTable(tableName).submit(SyncCond.validate_success);
	}
	/**���췽��*/
	public Demo1(String ip,int port){
		r = Chainsql.c;
		conn =r.connect("ws://"+ip+":"+port);
		r.as("rHb9CJAWyB4rj91VRWn96DkukG4bwdtyTh", "snoPBrXtMeMyMHUVTgbuqAfg1SUTb");
		if(conn==null){
			System.out.println("�޷���ȡconnection����");
		}
		
	}
	/**
	 * �ͷŶ���*/
	public void release(){
		this.r.disconnect();
		System.out.println("disconnected!");
	}
	/**
	 * �����������һ���������������¼
	 * @param tableName Ҫ����ı���
	 * @param rows json��ʽ���б�ÿһ�����ݶ�Ӧһ��json����ʽ��{'name':'xxx','age':22}
	 * */
	public void insert(String tableName,ArrayList<String> rows){
		System.out.println("rows:"+rows.toString());
		JSONObject obj= r.table(tableName).insert(rows).submit(SyncCond.db_success);
		System.out.println("������:"+obj.toString());
	}
	/**
	 * ���²���������where����������ָ�����е�ָ��������
	 * @param tableName Ҫ���µı���
	 * @param col Ҫ���µ��м�ֵ��json��ʽ����{'name':'xxx','age':22}
	 * @param whereCond Ҫ���µ��м�ֵ��json��ʽ����{'name':'xxx','age':22}
	 * */
	public JSONObject update(String tableName,String col,String whereCond){
		ArrayList<String> whereList=new ArrayList<String>();
		whereList.add(whereCond);
		return r.table(tableName).get(whereList).update(Util.array(col)).submit(SyncCond.db_success);
	}
	/**
	 * ɾ������������where������ɾ��ָ������
	 * @param tableName Ҫɾ�����ݵı���
	 * @param whereCond Ҫɾ����where������json��ʽ����{'name':'xxx','age':22}
	 * */
	public void delete(String tableName,String whereCond){
		ArrayList<String> whereList=new ArrayList<String>();
		whereList.add(whereCond);
		r.table(tableName).get(whereList).delete().submit(SyncCond.db_success);
	}
	/**
	 * ��ѯ����
	 * @param tableName ����
	 * @param whereCond where������json���󣬸�ʽ��{'id':'123'}*/
	public ArrayList<JSONObject> select(String tableName,String filterWith,String whereCond){
		System.out.println("whereCond:"+whereCond);
		ArrayList<String> cond=new ArrayList<String>();
		cond.add(whereCond);
		System.out.println("cond:"+cond.toString());
//		Table t=r.table(tableName).filterWith(filterWith).get(cond).submit();//��һ����ok��
		Table t=r.table(tableName).withFields(filterWith).get(cond);
		t.submit();
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("��һ�½��...");
		/*JSONArray jsonArray=(JSONArray)t.getData();
		ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();
		System.out.println("�����:");
		for (int j= 0;j<jsonArray.length();j++){  
			System.out.println(jsonArray.getJSONObject(j));
			jsonList.add(jsonArray.getJSONObject(j));  
         
       }  */
//		System.out.println("json:"+json.toString());
		//TODO �ص������д�����
		return null;
	}


}
