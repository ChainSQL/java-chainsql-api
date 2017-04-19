import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.Security;

import javax.crypto.Cipher;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.ripple.bouncycastle.jce.interfaces.ECPrivateKey;
import org.ripple.bouncycastle.jce.interfaces.ECPublicKey;
import org.ripple.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ripple.bouncycastle.math.ec.ECCurve;
import org.ripple.bouncycastle.math.ec.ECPoint;
public class TestECC {
 public static void main(String[] args) throws Exception {
  byte[] plainText = "Hello World!".getBytes();
  byte[] cipherText = null;
  
  Security.addProvider(new BouncyCastleProvider());
  showAllMethod();
//  //生成公钥和私钥
  KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECIES");
  KeyPair keyPair = keyPairGenerator.generateKeyPair();  
  ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
  ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
  System.out.println(ecPublicKey.toString());
  System.out.println(ecPrivateKey.toString());
  //打印密钥信息
  ECCurve ecCurve = ecPublicKey.getParameters().getCurve();
  System.out.println("椭圆曲线参数a = " + ecCurve.getA().toBigInteger());
  System.out.println("椭圆曲线参数b = " + ecCurve.getB().toBigInteger());
  System.out.println("椭圆曲线参数q = " + ((ECCurve.Fp) ecCurve).getQ());
  ECPoint basePoint = ecPublicKey.getParameters().getG();
  System.out.println("基点橫坐标              "
    + basePoint.getAffineXCoord().toBigInteger());
  System.out.println("基点纵坐标              "
    + basePoint.getAffineYCoord().toBigInteger());
  System.out.println("公钥横坐标              "
    + ecPublicKey.getQ().getAffineXCoord().toBigInteger());
  System.out.println("公钥纵坐标              "
    + ecPublicKey.getQ().getAffineYCoord().toBigInteger());
  System.out.println("私钥                         " + ecPrivateKey.getD());
  Cipher cipher = Cipher.getInstance("ECIESwithDESede/NONE/PKCS7Padding", "BC");
  // 加密
  cipher.init(Cipher.ENCRYPT_MODE, ecPublicKey);
  cipherText = cipher.doFinal(plainText);
  System.out.println("密文: " + new HexBinaryAdapter().marshal(cipherText));
  // 解密
  cipher.init(Cipher.DECRYPT_MODE, ecPrivateKey);
  plainText = cipher.doFinal(cipherText);
  // 打印解密后的明文
  System.out.println("解密后的明文: " + new String(plainText));
 }
 public static void showAllMethod(){
		System.out.println("-------列出加密服务提供者-----");
		Provider[] pro=Security.getProviders();
		for(Provider p:pro){
			System.out.println("Provider:"+p.getName()+" - version:"+p.getVersion());
			System.out.println(p.getInfo());
		}
		System.out.println("");
		System.out.println("-------列出系统支持的消息摘要算法：");
		for(String s:Security.getAlgorithms("MessageDigest")){
			System.out.println(s);
		}
		System.out.println("-------列出系统支持的生成公钥和私钥对的算法：");
		for(String s:Security.getAlgorithms("KeyPairGenerator")){
			System.out.println(s);
		}
 }
}