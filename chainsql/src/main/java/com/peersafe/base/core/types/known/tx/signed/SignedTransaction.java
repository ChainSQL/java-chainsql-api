package com.peersafe.base.core.types.known.tx.signed;

import java.util.Arrays;

import com.peersafe.base.config.Config;
import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.coretypes.Blob;
import com.peersafe.base.core.coretypes.STObject;
import com.peersafe.base.core.coretypes.hash.HalfSha512;
import com.peersafe.base.core.coretypes.hash.Hash256;
import com.peersafe.base.core.coretypes.hash.prefixes.HashPrefix;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.serialized.BytesList;
import com.peersafe.base.core.serialized.MultiSink;
import com.peersafe.base.core.serialized.enums.TransactionType;
import com.peersafe.base.core.types.known.tx.Transaction;
import com.peersafe.base.crypto.ecdsa.IKeyPair;
import com.peersafe.base.crypto.ecdsa.Seed;
import com.peersafe.chainsql.util.Util;

public class SignedTransaction {
    private SignedTransaction(Transaction of) {
        // TODO: is this just over kill ?
        txn = (Transaction) STObject.translate.fromBytes(of.toBytes());
    }

    public SignedTransaction(SignedTransaction st){
    	this.txn = st.txn;
    	this.hash = st.hash;
    	this.signingData = st.signingData;
    	this.previousSigningData = st.previousSigningData;
    	this.tx_blob = st.tx_blob;
    	this.ca_pem  = st.ca_pem;
    }
    // This will eventually be private
    @Deprecated
    public SignedTransaction() {}

    public Transaction txn;
    public Hash256 hash;

    public byte[] signingData;
    public byte[] previousSigningData;
    public String tx_blob;

    public String ca_pem;// CA

    public void multiSign(String base58Secret){
    	multiSign(Seed.fromBase58(base58Secret).keyPair());
    }
    public void multiSign(IKeyPair keyPair){
    	multiSignPrepare(keyPair,null,null,null);
    }
    public void multiSignPrepare(IKeyPair keyPair,
            Amount fee,
            UInt32 Sequence,
            UInt32 lastLedgerSequence){

        // This won't always be specified
        if (lastLedgerSequence != null) {
            txn.put(UInt32.LastLedgerSequence, lastLedgerSequence);
        }
        if (Sequence != null) {
            txn.put(UInt32.Sequence, Sequence);
        }
        if (fee != null) {
            txn.put(Amount.Fee, fee);
        }

        byte[] pub = new byte[0];
        txn.signingPubKey(new Blob(pub));

//        if (Transaction.CANONICAL_FLAG_DEPLOYED) {
//            txn.setCanonicalSignatureFlag();
//        }

        AccountID account = AccountID.fromKeyPair(keyPair);
        txn.checkFormat();
        signingData = txn.multiSigningData(account);
        
        try {
            txn.txnSignature(new Blob(keyPair.signMessage(signingData)));

            BytesList blob = new BytesList();
            HalfSha512 id = HalfSha512.prefixed256(HashPrefix.transactionID);


            txn.toBytesSink(new MultiSink(blob, id));
            tx_blob = blob.bytesHex();
            hash = Hash256.prefixedHalfSha512(HashPrefix.transactionID, blob.bytes());
            //hash = id.finish();
        } catch (Exception e) {
            // electric paranoia
            previousSigningData = null;
            throw new RuntimeException(e);
        } 
    }
    
    public void sign(String base58Secret) {
        sign(Seed.fromBase58(base58Secret).keyPair());
    }

    public static SignedTransaction fromTx(Transaction tx) {
        return new SignedTransaction(tx);
    }

    public void sign(IKeyPair keyPair) {
        prepare(keyPair, null, null, null);
    }

    public void prepare(IKeyPair keyPair,
                        Amount fee,
                        UInt32 Sequence,
                        UInt32 lastLedgerSequence) {

        Blob pubKey = new Blob(keyPair.canonicalPubBytes());

        // This won't always be specified
        if (lastLedgerSequence != null) {
            txn.put(UInt32.LastLedgerSequence, lastLedgerSequence);
        }
        if (Sequence != null) {
            txn.put(UInt32.Sequence, Sequence);
        }
        if (fee != null) {
            txn.put(Amount.Fee, fee);
        }

        txn.signingPubKey(pubKey);



        System.out.println("public key "+ Util.bytesToHex(pubKey.toBytes()));

        if (Transaction.CANONICAL_FLAG_DEPLOYED) {
            txn.setCanonicalSignatureFlag();
        }

        txn.checkFormat();
        signingData = txn.signingData();
        if (previousSigningData != null && Arrays.equals(signingData, previousSigningData)) {
            return;
        }
        try {
            txn.txnSignature(new Blob(keyPair.signMessage(signingData)));

            BytesList blob = new BytesList();
            HalfSha512 id = HalfSha512.prefixed256(HashPrefix.transactionID);

//             System.out.println("Transaction:" + txn.prettyJSON());
//            for (Field field : txn) {
//            	if(field.isSerialized()){
//            		System.out.println(field.toString() + "1");
//            	}else{
//            		System.out.println(field.toString() + "0");
//            	}
//            }
            //signingData = txn.signingData();
            txn.toBytesSink(new MultiSink(blob, id));
            tx_blob = blob.bytesHex();
            if(Config.isUseGM()){
//            	hash = SMDigest.getTransactionHash(HashPrefix.transactionID, blob.bytes());
            }else{
            	hash = Hash256.prefixedHalfSha512(HashPrefix.transactionID, blob.bytes());	
            }
            
            //hash = id.finish();
        } catch (Exception e) {
            // electric paranoia
            previousSigningData = null;
            throw new RuntimeException(e);
        } /*else {*/
        previousSigningData = signingData;
        // }
    }

    public TransactionType transactionType() {
        return txn.transactionType();
    }
}
