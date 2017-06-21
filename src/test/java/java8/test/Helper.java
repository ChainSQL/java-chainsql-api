package java8.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
	public static String readFile(String fileName){
		String content = "";
		try {
	        // 一次读一个字节
	        FileInputStream in = new FileInputStream(fileName);
	        //创建一个长度为1024的竹筒  
	        byte[] bbuf = new byte[1024];  
	        //用于保存实际读取的字节数  
	        int hasRead = 0;  
	        //使用循环来重复“取水”的过程  
	        while((hasRead = in.read(bbuf))>0)  
	        {  
	            //取出"竹筒"中(字节),将字节数组转成字符串输入  
	            content += new String(bbuf,0,hasRead);  
	        }  
	        
	        in.close();
	     } catch (IOException e) {
	        e.printStackTrace();
	     }
		 return content;
	}
    public static boolean isIpv4(String ipAddress) {
        String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        Pattern pattern = Pattern.compile(ip);
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }
    
	public static void wait(int milliseconds){
		try {
			//System.out.println("等待" + milliseconds/1000 + "秒");
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
