package com.peersafe.base.crypto.sm;

import java.util.logging.Level;

import com.peersafe.base.client.Client;

import cn.com.sansec.key.SWJAPI;
import cn.com.sansec.key.exception.SDKeyException;

public class SMDevice {
	public static SWJAPI sdkey;
    private static  String mContainerName = "Java-Container-SM";
	public SMDevice(){
	}
	
	public static String getContainerName(){
		return mContainerName;
	}
	
	public static  boolean openDevice(){
		// 获取设备访问环境
		sdkey = SWJAPI.getInstance();
		// 判断是否成功打开设备
		boolean bRet = sdkey.getOpenResult();
		if (!bRet) {
			Client.log(Level.SEVERE, "打开SDKey失败，请确认设备是否存在！");
			sdkey = null;
			return false;
		} else{
			Client.log(Level.INFO, "打开SDKey设备成功！");
			return true;
		}
	}
	public void exit(){
		if (sdkey != null) {
			try {
				sdkey.finalizeBjapi();
			} catch (SDKeyException e) {
				e.printStackTrace();
			} finally {
			}
		}
	}

	public static boolean verifyPin(String pin){
		if (sdkey == null) {
			Client.log(Level.SEVERE, "尚未打开SDKey设备！");
			return false;
		}

		// 获取到口令字符串 Pin
		String Pin = pin.isEmpty() ? "66666666" : pin;
		if ((Pin.length() == 0) || (Pin.length() > 16)) {
			Client.log(Level.SEVERE, "用户口令长度错误，请重新输入！");
			return false;
		}

		// 调用验证方法验证输入的口令
		int returnValue = 0;
		try {
			returnValue = sdkey.login(SWJAPI.PIN_TYPE_USER, Pin);
		} catch (SDKeyException e) {
			return false;
		}

		// 获取函数返回值
		if (returnValue == 1) {
			Client.log(Level.INFO, "口令验证正确，用户已登录！");
			return true;
		} else if (returnValue == 0) {
			Client.log(Level.SEVERE, "用户口令已锁死，请联系管理员解锁！");
			return false;
		} else {
			int nRetry = 0 - returnValue;
			Client.log(Level.SEVERE, "用户口令错误，还可以重试" +Integer.toString(nRetry) + "次!");
			return false;
		}
	}
	
//	public static void unlockPin(){
//		if (sdkey == null) {
//			System.out.println("尚未打开SDKey设备！");
//			return;
//		}
//
//		// 获取到管理员口令字符串 Pin
//		String AdmPin = "88888888";
//		if ((AdmPin.length() == 0) || (AdmPin.length() > 16)) {
//			System.out.println("管理员口令长度错误，请重新输入！");
//			return;
//		}
//		// 获取到口令字符串 Pin
//		String Pin = "66666666";
//		if ((Pin.length() == 0) || (Pin.length() > 16)) {
//			System.out.println("用户口令长度错误，请重新输入！");
//			return;
//		}
//		
//		//尝试管理员登录
//		int returnValue = 0;
//		try {
//			returnValue = sdkey.login(SWJAPI.PIN_TYPE_ADMIN, AdmPin);
//		} catch (SDKeyException e) {
//			return;
//		}
//		// 获取函数返回值
//		if (returnValue == 1) {
//			//showText.setText("口令验证正确，用户已登录！");
//		} else if (returnValue == 0) {
//			System.out.println("管理员口令已锁死，无法再进行解锁。");
//			return;
//		} else {
//			int nRetry = 0 - returnValue;
//			System.out.println("管理员口令错误，还可以重试" + Integer.toString(nRetry) + "次!");
//			return;
//		}
//		//登录成功后设置新的用户口令
//		boolean ret;
//		try {
//			ret = sdkey.unLockUserPass(Pin);
//		} catch (SDKeyException e) {
//			return;
//		}
//		if (ret == true) {
//			System.out.println("解锁用户口令成功，请牢记新口令。");
//		} else {
//			System.out.println("解锁用户口令失败！");
//		}
//	}
}
