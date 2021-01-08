package com.peersafe.base.client.subscriptions;

import com.peersafe.base.config.Config;
import com.peersafe.base.core.coretypes.Amount;
import com.peersafe.base.core.types.known.tx.Transaction;
import org.json.JSONObject;

import java.util.Date;

import static com.peersafe.base.core.coretypes.RippleDate.fromSecondsSinceRippleEpoch;

// TODO, really want to split this into a few classes
// ServerStatus / LedgerClosed events.
public class ServerInfo {
    public boolean updated = false;

    public int fee_base;
    public int fee_ref;
    public int drops_per_byte = (int) Math.ceil(1000000.0 / 1024);
    public int reserve_base;
    public int reserve_inc;
    public int load_base;
    public int load_factor;
    public long ledger_time;
    public long ledger_index;

    public int txn_success = 0;
    public int txn_failure = 0;

    public int txn_count;

    public String ledger_hash;
    public String random;
    public String server_status;
    public String validated_ledgers;

    /**
     * computeFee
     *
     * @param units units.
     * @return return amount.
     */
    public Amount computeFee(int units) {
        if (!updated) {
            throw new IllegalStateException("No information from the server yet");
        }
        //in case of divide by zero
        {
            if (fee_ref == 0)
                fee_ref = 10;
            if (load_base == 0)
                load_base = 256;
            if (fee_base == 0)
                fee_base = 10;
        }

        double fee_unit = (double) fee_base / fee_ref, fee;
        fee_unit *= load_factor / load_base;
        fee = units * fee_unit * Config.getFeeCushion();
        String s = String.valueOf((long) Math.ceil(fee));
        return Amount.fromString(s);
    }

    /**
     * transactionFee
     *
     * @param transaction transaction.
     * @return return.
     */
    public Amount transactionFee(Transaction transaction) {
        return computeFee(fee_base);
    }

    /**
     * update.
     *
     * @param json json.
     */
    public void update(JSONObject json) {
        // TODO, this might asking for trouble, just assuming certain fields, it should BLOW UP

    	if(json.optLong("ledger_index", ledger_index) < ledger_index) {
//    		System.out.println("Server_info update:"+json);
    		return;
    	}
        fee_base = json.optInt("fee_base", fee_base);
        drops_per_byte = json.optInt("drops_per_byte", drops_per_byte);
        txn_count = json.optInt("txn_count", txn_count);
        fee_ref = json.optInt("fee_ref", fee_ref);
        reserve_base = json.optInt("reserve_base", reserve_base);
        reserve_inc = json.optInt("reserve_inc", reserve_inc);
        load_base = json.optInt("load_base", load_base);
        load_factor = json.optInt("load_factor", load_factor);
        ledger_time = json.optLong("ledger_time", ledger_time);
        ledger_index = json.optLong("ledger_index", ledger_index);
        ledger_hash = json.optString("ledger_hash", ledger_hash);
        validated_ledgers = json.optString("validated_ledgers", validated_ledgers);

        random = json.optString("random", random);
        server_status = json.optString("server_status", server_status);

        txn_success = json.optInt("txn_success", txn_success);
        txn_failure = json.optInt("txn_failure", txn_failure);

        updated = true;
    }

    /**
     * Date.
     *
     * @return return value.
     */
    public Date date() {
        return fromSecondsSinceRippleEpoch(ledger_time);
    }

    /**
     * primed.
     *
     * @return return value.
     */
    public boolean primed() {
        return updated;
    }
}

