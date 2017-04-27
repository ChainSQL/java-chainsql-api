import static com.ripple.config.Config.getB58IdentiferCodecs;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.peersafe.chainsql.crypto.Aes;
import com.peersafe.chainsql.crypto.Ecies;
import com.peersafe.chainsql.util.Util;
import com.ripple.encodings.B58IdentiferCodecs;  

public class ECTest {  
	public static final String secret = "ssnqAfDUjc6Bkevd1Xmz5dJS5yHdz";
	public static final String address = "rBuLBiHmssAMHWQMnEN7nXQXaVj7vhAv6Q";
	public static final String publicKey = "aBP8JEiNXr3a9nnBFDNKKzAoGNezoXzsa1N8kQAoLU5F5HrQbFvs";
	
//	 Seed seed = Seed.randomSeed();
//	 IKeyPair pair = seed.keyPair();
//
//	 System.out.println("Seed：\t\t" + getB58IdentiferCodecs().encodeFamilySeed(seed.bytes()));
//	 System.out.println("PublicKey：\t" + getB58IdentiferCodecs().encode(pair.canonicalPubBytes(), B58IdentiferCodecs.VER_ACCOUNT_PUBLIC));
//	 System.out.println("Address：\t" + getB58IdentiferCodecs().encode(pair.pub160Hash(), 0));
	
    static {  
        Security.addProvider(new BouncyCastleProvider());  
    }
  
    public static void main(String[] argu) throws Exception {  
//    	String str = "test";
//    	String hex = Util.bytesToHex(str.getBytes());
//    	System.out.println(hex);
    	String hex = "3b2a3563a37cdf77";
    	byte[] bytes = Util.hexToBytes(hex);
    	System.out.println(bytes.length);
    	System.out.println(new String(bytes).getBytes().length);
//    	System.out.println(Util.bytesToHex(bytes));
//    	String hex2 = Util.toHexString(str);
//    	String str2 = Util.fromHexString(hex2);
//    	System.out.println(hex2);
//    	System.out.println(str2);
//    	byte[] pub = Util.hexToBytes("02F039E54B3A0D209D348F1B2C93BE3689F2A7595DDBFB1530499D03264B87A61F");
//    	System.out.println("length:" + pub.length);
//    	for(byte b : pub){
//    		System.out.print(b + " ");
//    	}
//    	System.out.println("");
//    	
//    	byte [] dataPubB = getB58IdentiferCodecs().decode(publicKey, B58IdentiferCodecs.VER_ACCOUNT_PUBLIC);
//    	System.out.println("length:" + dataPubB.length);
//    	for(byte b : dataPubB){
//    		System.out.print(b + " ");
//    	}
//    	System.out.println("");
    	
//    	String res = Ecies.eciesEncrypt("test",publicKey);
//    	//System.out.println(res);
//    	byte[] dec = Ecies.eciesDecrypt(res, secret);
//    	System.out.println(new String(dec));
//    	
//    	String aesEnc = Aes.aesEncrypt("hello12345123456", "test");
//    	byte[] aesDec = Aes.aesDecrypt("hello12345123456", aesEnc);
//    	System.out.println(new String(aesDec));
    }
}  

