package com.peersafe.base.core.coretypes.hash;

import com.peersafe.base.core.coretypes.AccountID;
import com.peersafe.base.core.coretypes.Currency;
import com.peersafe.base.core.coretypes.Issue;
import com.peersafe.base.core.coretypes.hash.prefixes.HashPrefix;
import com.peersafe.base.core.coretypes.hash.prefixes.LedgerSpace;
import com.peersafe.base.core.coretypes.uint.UInt32;
import com.peersafe.base.core.coretypes.uint.UInt64;
import com.peersafe.base.core.serialized.SerializedType;

import java.util.Arrays;
import java.util.List;

import static com.peersafe.base.core.coretypes.hash.HalfSha512.prefixed256;
import static java.util.Collections.sort;

public class Index {
    private static Hash256 createBookBase(Issue pays, Issue gets) {
        return prefixed256(LedgerSpace.bookDir)
                .add(pays.currency())
                .add(gets.currency())
                .add(pays.issuer())
                .add(gets.issuer())
                .finish();
    }

    /**
     * Quality.
     * @param index Hash256 index.
     * @param quality quality.
     * @return a copy of index, with quality overlaid in lowest 8 bytes
     */
    public static Hash256 quality(Hash256 index, UInt64 quality) {
        byte[] qi = new byte[32];
        System.arraycopy(index.bytes(), 0, qi, 0, 24);
        if (quality != null) System.arraycopy(quality.toBytes(), 0, qi, 24, 8);
        return new Hash256(qi);
    }

    /**
     * @return A copy of index, with the lowest 8 bytes all zeroed.
     */
    private static Hash256 zeroQuality(Hash256 fullIndex) {
        return quality(fullIndex, null);
    }

    /**
     * Get rippleState.
     * @param a1 Account address1.
     * @param a2 Account address2.
     * @param currency Currency.
     * @return RippleAddress value.
     */
    public static Hash256 rippleState(AccountID a1, AccountID a2, Currency currency) {
        List<AccountID> accounts = Arrays.asList(a1, a2);
        sort(accounts);
        return rippleState(accounts, currency);
    }

    /**
     * rippleState
     * @param accounts List of account address.
     * @param currency Currency value.
     * @return return value.
     */
    public static Hash256 rippleState(List<AccountID> accounts, Currency currency) {
        HalfSha512 hasher = prefixed256(LedgerSpace.ripple);
        // Low then High
        for (AccountID account : accounts) account.toBytesSink(hasher);
        // Currency
        currency.toBytesSink(hasher);

        return hasher.finish();
    }

    /**
     *
     * @param rootIndex The RootIndex index for the directory node
     * @param nodeIndex nullable LowNode, HighNode, OwnerNode, BookNode etc
     *                  defining a `page` number.
     *
     * @return  A hash of rootIndex and nodeIndex when nodeIndex is non default
     *          else the rootIndex. This hash is used as an index for the next
     *          DirectoryNode page.
     */
    public static Hash256 directoryNode(Hash256 rootIndex, UInt64 nodeIndex) {
        if (nodeIndex == null || nodeIndex.isZero()) {
            return rootIndex;
        }

        return prefixed256(LedgerSpace.dirNode)
                .add(rootIndex)
                .add(nodeIndex)
                .finish();
    }

    /**
     * Create accountRoot from accountId.
     * @param accountID Account address.
     * @return return value.
     */
    public static Hash256 accountRoot(AccountID accountID) {
        return prefixed256(LedgerSpace.account).add(accountID).finish();
    }

    /**
     * Owner Directory
     * @param account Account address.
     * @return Owner directory.
     */
    public static Hash256 ownerDirectory(AccountID account) {
        return Hash256.prefixedHalfSha512(LedgerSpace.ownerDir, account.bytes());
    }

    /**
     * Get transaction Id from blob.
     * @param blob tx blob.
     * @return Transaction Id.
     */
    public static Hash256 transactionID(byte[] blob) {
        return Hash256.prefixedHalfSha512(HashPrefix.transactionID, blob);
    }

    /**
     * Book start.
     * @param pays Pays.
     * @param gets Gets.
     * @return return value.
     */
    public static Hash256 bookStart(Issue pays, Issue gets) {
        return zeroQuality(createBookBase(pays, gets));
    }

    /**
     * Book start.
     * @param indexFromBookRange indexFromBookRange
     * @return return value.
     */
    public static Hash256 bookStart(Hash256 indexFromBookRange) {
        return zeroQuality(indexFromBookRange);
    }

    /**
     * Book end.
     * @param base hash256 base.
     * @return Hash256.
     */
    public static Hash256 bookEnd(Hash256 base) {
        byte[] end = base.bigInteger().add(Hash256.bookBaseSize).toByteArray();
        if (end.length > 32) {
            byte[] source = end;
            end = new byte[32];
            System.arraycopy(source, source.length - 32, end, 0, 32);
        }
        return new Hash256(end);
    }

    /**
     * Get ledger-hash.
     * @param prev Prev.
     * @return Ledger-hash.
     */
    public static Hash256 ledgerHashes(long prev) {
        return prefixed256(LedgerSpace.skipList)
                    .add(new UInt32(prev >> 16))
                    .finish();
    }
    
    /**
     * Get Ledger-hash.
     * @return Ledger-hash.
     */
    public static Hash256 ledgerHashes() {
        return prefixed256(LedgerSpace.skipList).finish();
    }
}
