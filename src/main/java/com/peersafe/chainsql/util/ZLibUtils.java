package com.peersafe.chainsql.util;

import java.io.ByteArrayOutputStream;  
import java.io.IOException;  
import java.io.InputStream;  
import java.io.OutputStream;  
import java.util.zip.Deflater;  
import java.util.zip.DeflaterOutputStream;  
import java.util.zip.Inflater;  
import java.util.zip.InflaterInputStream;  
  
/** 
 * ZLibѹ������ 
 *  
 * @author <a href="mailto:zlex.dongliang@gmail.com">����</a> 
 * @version 1.0 
 * @since 1.0 
 */  
public abstract class ZLibUtils {  
  
    /** 
     * ѹ�� 
     *  
     * @param data 
     *            ��ѹ������ 
     * @return byte[] ѹ��������� 
     */  
    public static byte[] compress(byte[] data) {  
        byte[] output = new byte[0];  
  
        Deflater compresser = new Deflater();  
  
        compresser.reset();  
        compresser.setInput(data);  
        compresser.finish();  
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);  
        try {  
            byte[] buf = new byte[1024];  
            while (!compresser.finished()) {  
                int i = compresser.deflate(buf);  
                bos.write(buf, 0, i);  
            }  
            output = bos.toByteArray();  
        } catch (Exception e) {  
            output = data;  
            e.printStackTrace();  
        } finally {  
            try {  
                bos.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        compresser.end();  
        return output;  
    }  
  
    /** 
     * ѹ�� 
     *  
     * @param data 
     *            ��ѹ������ 
     *  
     * @param os 
     *            ����� 
     */  
    public static void compress(byte[] data, OutputStream os) {  
        DeflaterOutputStream dos = new DeflaterOutputStream(os);  
  
        try {  
            dos.write(data, 0, data.length);  
  
            dos.finish();  
  
            dos.flush();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
  
    /** 
     * ��ѹ�� 
     *  
     * @param data 
     *            ��ѹ�������� 
     * @return byte[] ��ѹ��������� 
     */  
    public static byte[] decompress(byte[] data) {  
        byte[] output = new byte[0];  
  
        Inflater decompresser = new Inflater();  
        decompresser.reset();  
        decompresser.setInput(data);  
  
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);  
        try {  
            byte[] buf = new byte[1024];  
            while (!decompresser.finished()) {  
                int i = decompresser.inflate(buf);  
                o.write(buf, 0, i);  
            }
            output = o.toByteArray();  
        } catch (Exception e) {  
            output = data;  
            e.printStackTrace();  
        } finally {  
            try {  
                o.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
  
        decompresser.end();  
        return output;  
    }  
  
    /** 
     * ��ѹ�� 
     *  
     * @param is 
     *            ������ 
     * @return byte[] ��ѹ��������� 
     */  
    public static byte[] decompress(InputStream is) {  
        InflaterInputStream iis = new InflaterInputStream(is);  
        ByteArrayOutputStream o = new ByteArrayOutputStream(1024);  
        try {  
            int i = 1024;  
            byte[] buf = new byte[i];  
  
            while ((i = iis.read(buf, 0, i)) > 0) {  
                o.write(buf, 0, i);  
            }  
  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return o.toByteArray();  
    }  
}  