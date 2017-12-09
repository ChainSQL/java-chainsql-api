package com.peersafe.base.config;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.peersafe.base.crypto.sm.SMDevice;
import com.peersafe.base.encodings.B58IdentiferCodecs;
import com.peersafe.base.encodings.base58.B58;

// Somewhat of a global registry, dependency injection ala guice would be nicer, but trying to KISS
public class Config {
    //public  static final String DEFAULT_ALPHABET = "rpshnaf39wBUDNEGHJKLM4PQRST7VWXYZ2bcdeCg65jkm8oFqi1tuvAxyz";
    public  static final String DEFAULT_ALPHABET = "zpxhncf39wBUDNEGHJKLM4PQRST7VWXYZ2badeCg65jkm8oFqi1tuvAsyr";
    
    private static B58IdentiferCodecs b58IdentiferCodecs;
    private static double feeCushion;
    private static B58 b58;
    //set if we use guomi encrypt
    private static boolean useSM_=false;
    private static boolean newKeyPair_ = false;

    /**
     * Set alphabet.
     * @param alphabet alphabet.
     */
    public static void setAlphabet(String alphabet) {
        b58 = new B58(alphabet);
        b58IdentiferCodecs = new B58IdentiferCodecs(b58);
    }	

    /**
     * getB58IdentiferCodecs
     * @return the configured B58IdentiferCodecs object
     */
    public static B58IdentiferCodecs getB58IdentiferCodecs() {
        return b58IdentiferCodecs;
    }

    /**
     * Get Base58.
     * @return the configured B58 object
     */
    public static B58 getB58() {
        return b58;
    }

    /**
     * TODO, this is gross
     */
    static public boolean bouncyInitiated = false;
    static public void initBouncy() {
        if (!bouncyInitiated) {
            Security.addProvider(new BouncyCastleProvider());
            bouncyInitiated = true;
        }
    }
    /***
     * We set up all the defaults here
     */
    static {
        setAlphabet(DEFAULT_ALPHABET);
        setFeeCushion(1.1);
        initBouncy();
    }

    /**
     * GetFeeCushion
     * @return return value.
     */
    public static double getFeeCushion() {
        return feeCushion;
    }

    /**
     * Set fee cushion.
     * @param fee_cushion fee cushion.
     */
    public static void setFeeCushion(double fee_cushion) {
        feeCushion = fee_cushion;
    }

	public static boolean isUseGM() {
		return useSM_;
	}

	public static boolean isNewKeyPair() {
		return newKeyPair_;
	}

	public static void setNewKeyPair(boolean newKeyPair_) {
		Config.newKeyPair_ = newKeyPair_;
	}

	public static boolean setUseGM(boolean useGM,boolean bNewKeyPair,String pin){
		if(useGM){
			newKeyPair_ = bNewKeyPair;
			boolean bRet = SMDevice.openDevice();
			if(!bRet)
				return false;
			bRet = SMDevice.verifyPin(pin);
			if(bRet)
				useSM_ = useGM;
			return bRet;
		}else{
			useSM_ = useGM;
			return true;
		}
	}
}
