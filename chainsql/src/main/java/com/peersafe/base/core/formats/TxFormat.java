package com.peersafe.base.core.formats;

import com.peersafe.base.core.fields.Field;
import com.peersafe.base.core.formats.Format.Requirement;
import com.peersafe.base.core.serialized.enums.TransactionType;

import java.util.EnumMap;

public class TxFormat extends Format {
    static public EnumMap<TransactionType, TxFormat> formats = new EnumMap<TransactionType, TxFormat>(TransactionType.class);
    public final TransactionType transactionType;

    static public TxFormat fromString(String name) {
        return getTxFormat(TransactionType.valueOf(name));
    }

    static public TxFormat fromNumber(Number ord) {
        return getTxFormat(TransactionType.fromNumber(ord));
    }

    static public TxFormat fromValue(Object o) {
        if (o instanceof Number) {
            return fromNumber(((Number) o).intValue());
        } else if (o instanceof String){
            return fromString((String) o);
        }
        else {
            return null;
        }
    }

    private static TxFormat getTxFormat(TransactionType key) {
        if (key == null) return null;
        return formats.get(key);
    }

    public TxFormat(TransactionType type, Object... args) {
        super(args);
        transactionType = type;
        addCommonFields();
        formats.put(transactionType, this);
    }

    @Override
    public void addCommonFields() {
        put(Field.TransactionType,     Requirement.REQUIRED);
        put(Field.Account,             Requirement.REQUIRED);
        put(Field.Sequence,            Requirement.REQUIRED);
        put(Field.Fee,                 Requirement.REQUIRED);
        put(Field.SigningPubKey,       Requirement.REQUIRED);
        put(Field.Certificate,         Requirement.OPTIONAL);

        put(Field.Flags,               Requirement.OPTIONAL);
        put(Field.SourceTag,           Requirement.OPTIONAL);
        put(Field.PreviousTxnID,       Requirement.OPTIONAL);
        put(Field.OperationLimit,      Requirement.OPTIONAL);
        put(Field.TxnSignature,        Requirement.OPTIONAL);
        put(Field.AccountTxnID,        Requirement.OPTIONAL);
        put(Field.LastLedgerSequence,  Requirement.OPTIONAL);
        put(Field.Memos,               Requirement.OPTIONAL);
        put(Field.Signers,			   Requirement.OPTIONAL);
    }

    @Override
    public String name() {
        return transactionType.toString();
    }

    static public TxFormat AccountSet = new TxFormat(
            TransactionType.AccountSet,
            Field.EmailHash,       Requirement.OPTIONAL,
            Field.WalletLocator,   Requirement.OPTIONAL,
            Field.WalletSize,      Requirement.OPTIONAL,
            Field.MessageKey,      Requirement.OPTIONAL,
            Field.Domain,          Requirement.OPTIONAL,
            Field.TransferRate,    Requirement.OPTIONAL,
            Field.TransferFeeMin,  Requirement.OPTIONAL,
            Field.TransferFeeMax,  Requirement.OPTIONAL,
            Field.SetFlag,         Requirement.OPTIONAL,
            Field.ClearFlag,       Requirement.OPTIONAL,
    		Field.WhiteLists,      Requirement.OPTIONAL);

    static public TxFormat TrustSet = new TxFormat(
            TransactionType.TrustSet,
            Field.LimitAmount,     Requirement.OPTIONAL,
            Field.QualityIn,       Requirement.OPTIONAL,
            Field.QualityOut,      Requirement.OPTIONAL);

    static public TxFormat OfferCreate = new TxFormat(
            TransactionType.OfferCreate,
            Field.TakerPays,       Requirement.REQUIRED,
            Field.TakerGets,       Requirement.REQUIRED,
            Field.Expiration,      Requirement.OPTIONAL,
            Field.OfferSequence,   Requirement.OPTIONAL);

    static public TxFormat OfferCancel = new TxFormat(
            TransactionType.OfferCancel,
            Field.OfferSequence,   Requirement.REQUIRED);

    static public TxFormat TicketCreate = new TxFormat(
            TransactionType.TicketCreate,
            Field.Target,     Requirement.OPTIONAL,
            Field.Expiration, Requirement.OPTIONAL);

    static public TxFormat TicketCancel = new TxFormat(
            TransactionType.TicketCancel,
            Field.TicketID,   Requirement.REQUIRED);
    
    static public TxFormat SignerListSet = new TxFormat(
            TransactionType.SignerListSet,
            Field.SignerQuorum,   Requirement.REQUIRED,
            Field.SignerEntries,  Requirement.OPTIONAL);

    static public TxFormat SetRegularKey = new TxFormat(
            TransactionType.SetRegularKey,
            Field.RegularKey,  Requirement.OPTIONAL);

    static public TxFormat Payment = new TxFormat(
            TransactionType.Payment,
            Field.Destination,     Requirement.REQUIRED,
            Field.Amount,          Requirement.REQUIRED,
            Field.SendMax,         Requirement.OPTIONAL,
            Field.Paths,           Requirement.DEFAULT,
            Field.InvoiceID,       Requirement.OPTIONAL,
            Field.DestinationTag,  Requirement.OPTIONAL);
    
    
    static public TxFormat EscrowCreate = new TxFormat(
    		TransactionType.EscrowCreate,
    		Field.Destination, 		Requirement.REQUIRED,
    		Field.Amount,			Requirement.REQUIRED,
    		Field.Condition,		Requirement.OPTIONAL,
    		Field.CancelAfter,		Requirement.OPTIONAL,
    		Field.FinishAfter,		Requirement.OPTIONAL,
    		Field.DestinationTag,	Requirement.OPTIONAL);
    
    static public TxFormat EscrowFinish = new TxFormat(
    		TransactionType.EscrowFinish,
    		Field.Owner, 			Requirement.REQUIRED,
    		Field.OfferSequence,	Requirement.REQUIRED,
    		Field.Fulfillment,		Requirement.OPTIONAL,
    		Field.Condition,		Requirement.OPTIONAL);
    
    static public TxFormat EscrowCancel = new TxFormat(
    		TransactionType.EscrowCancel,
    		Field.Owner, 			Requirement.REQUIRED,
    		Field.OfferSequence,	Requirement.REQUIRED);

    static public TxFormat TableListSet = new TxFormat(
            TransactionType.TableListSet,
            Field.Tables,        Requirement.REQUIRED,
            Field.TableNewName,	 Requirement.OPTIONAL,
            Field.User,          Requirement.OPTIONAL,
            Field.Raw,           Requirement.OPTIONAL,
            Field.Token,		 Requirement.OPTIONAL,
            Field.OpType,  		 Requirement.REQUIRED,
            Field.TxnLgrSeq,	 Requirement.OPTIONAL,
            Field.OriginalAddress,Requirement.OPTIONAL,
            Field.CurTxHash,	 Requirement.OPTIONAL,
            Field.FutureTxHash,	 Requirement.OPTIONAL,
            Field.OperationRule, Requirement.OPTIONAL);
    
    static public TxFormat SQLStatement = new TxFormat(
            TransactionType.SQLStatement,
            Field.Owner,         Requirement.REQUIRED,
            Field.Tables,        Requirement.REQUIRED,
            Field.Raw,       	 Requirement.REQUIRED,
            Field.AutoFillField, Requirement.OPTIONAL,
            Field.TxCheckHash,	 Requirement.OPTIONAL,
            Field.OpType, 		 Requirement.REQUIRED,
            Field.TxnLgrSeq,	 Requirement.OPTIONAL,
            Field.OriginalAddress,Requirement.OPTIONAL,
            Field.CurTxHash,	 Requirement.OPTIONAL,
            Field.FutureTxHash,	 Requirement.OPTIONAL,
            Field.TxsHashFillField, Requirement.OPTIONAL,
            Field.LedgerSeqField,Requirement.OPTIONAL,
            Field.LedgerTimeField,Requirement.OPTIONAL);
    
    static public TxFormat SQLTransaction = new TxFormat(
            TransactionType.SQLTransaction,
            Field.Statements,    		Requirement.REQUIRED,
            Field.NeedVerify, 		 	Requirement.REQUIRED,
            Field.TxnLgrSeq,	 		Requirement.OPTIONAL,
            Field.OriginalAddress,		Requirement.OPTIONAL,
            Field.CurTxHash,	 		Requirement.OPTIONAL,
            Field.FutureTxHash,	 		Requirement.OPTIONAL);


    static public TxFormat Contract = new TxFormat(
    		TransactionType.Contract,
    		Field.ContractOpType,		Requirement.REQUIRED,
    		Field.ContractData,			Requirement.REQUIRED,
    		Field.Gas,					Requirement.REQUIRED,
    		Field.ContractAddress,		Requirement.OPTIONAL,
    		Field.ContractValue,		Requirement.OPTIONAL); 

    static public TxFormat EnableAmendment = new TxFormat(
            TransactionType.EnableAmendment,
            Field.Amendment,      Requirement.REQUIRED);

    static public TxFormat SetFee = new TxFormat(
            TransactionType.SetFee,
            Field.BaseFee,              Requirement.REQUIRED,
            Field.ReferenceFeeUnits,    Requirement.REQUIRED,
            Field.ReserveBase,          Requirement.REQUIRED,
            Field.ReserveIncrement,     Requirement.REQUIRED);


    static public TxFormat SchemaCreate = new TxFormat(
            TransactionType.SchemaCreate,
            Field.SchemaName,           Requirement.REQUIRED,
            Field.SchemaStrategy,       Requirement.REQUIRED,
            Field.SchemaAdmin,          Requirement.OPTIONAL,
            Field.AnchorLedgerHash,     Requirement.OPTIONAL,
            Field.Validators,           Requirement.REQUIRED,
            Field.PeerList,             Requirement.REQUIRED);

    static public TxFormat SchemaModify = new TxFormat(
            TransactionType.SchemaModify,
            Field.OpType,              Requirement.REQUIRED,
            Field.Validators,          Requirement.REQUIRED,
            Field.PeerList,            Requirement.REQUIRED,
            Field.SchemaID,            Requirement.REQUIRED);
    
    static public TxFormat Authorize = new TxFormat(
            TransactionType.Authorize,
            Field.Account,              Requirement.REQUIRED,
            Field.SetFlag,          	Requirement.OPTIONAL,
            Field.ClearFlag,            Requirement.OPTIONAL,
            Field.Destination,          Requirement.REQUIRED);

}
