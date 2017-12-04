package com.ripple.core.types.known.tx.txns;

import com.ripple.core.coretypes.AccountID;
import com.ripple.core.coretypes.Amount;
import com.ripple.core.coretypes.Blob;
import com.ripple.core.coretypes.PathSet;
import com.ripple.core.coretypes.STArray;
import com.ripple.core.coretypes.hash.Hash256;
import com.ripple.core.coretypes.uint.UInt16;
import com.ripple.core.coretypes.uint.UInt32;
import com.ripple.core.fields.Field;
import com.ripple.core.serialized.enums.TransactionType;
import com.ripple.core.types.known.tx.Transaction;

public class TableListSet extends Transaction {
	public TableListSet() {
		 super(TransactionType.TableListSet);
	}

    public STArray tables() {return get(STArray.Tables);}
    public AccountID user() {return get(AccountID.User);}
    public UInt32 Flags() {return get(UInt32.Flags);}
    public Blob raw() {return get(Blob.Raw);}
    public UInt16 opType() {return get(UInt16.OpType);}
    public void tables(STArray val) {put(Field.Tables, val);}
    public void user(AccountID val) {put(Field.User, val);}
    public void Flags(UInt32 val) {put(Field.Flags, val);}
    public void raw(Blob val) {put(Field.Raw, val);}
    public void opType(UInt16 val) {put(Field.OpType, val);}
    
  
}
