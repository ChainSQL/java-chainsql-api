package com.peersafe.example.contract;

import com.peersafe.abi.TypeReference;
import com.peersafe.abi.datatypes.Function;
import com.peersafe.abi.datatypes.Type;
import com.peersafe.abi.datatypes.generated.Int256;
import com.peersafe.base.client.pubsub.Publisher;
import com.peersafe.chainsql.contract.Contract;
import com.peersafe.chainsql.contract.exception.ContractCallException;
import com.peersafe.chainsql.contract.exception.TransactionException;
import com.peersafe.chainsql.core.Chainsql;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the com.peersafe.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.4.0.
 */
public class GatewayTest extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b50611239806100206000396000f3006080604052600436106100a4576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806334bcb956146100a6578063399d32c0146101d65780634262b061146103555780636e7146be1461058b578063bd1e7722146106bb578063e66256b014610911578063e7092c191461095e578063f5b960ab14610b54578063f812ecd314610c64578063f824e6f814610e03575b005b3480156100b257600080fd5b506101c0600480360360808110156100c957600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019064010000000081111561010657600080fd5b82018360208201111561011857600080fd5b8035906020019184600183028401116401000000008311171561013a57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803567ffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610f13565b6040518082815260200191505060405180910390f35b3480156101e257600080fd5b50610353600480360360608110156101f957600080fd5b810190808035906020019064010000000081111561021657600080fd5b82018360208201111561022857600080fd5b8035906020019184600183028401116401000000008311171561024a57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803590602001906401000000008111156102ad57600080fd5b8201836020820111156102bf57600080fd5b803590602001918460018302840111640100000000831117156102e157600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610f5a565b005b34801561036157600080fd5b50610589600480360360a081101561037857600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001906401000000008111156103b557600080fd5b8201836020820111156103c757600080fd5b803590602001918460018302840111640100000000831117156103e957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019064010000000081111561044c57600080fd5b82018360208201111561045e57600080fd5b8035906020019184600183028401116401000000008311171561048057600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803590602001906401000000008111156104e357600080fd5b8201836020820111156104f557600080fd5b8035906020019184600183028401116401000000008311171561051757600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610fa5565b005b34801561059757600080fd5b506106a5600480360360808110156105ae57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001906401000000008111156105eb57600080fd5b8201836020820111156105fd57600080fd5b8035906020019184600183028401116401000000008311171561061f57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803567ffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050611000565b6040518082815260200191505060405180910390f35b3480156106c757600080fd5b5061090f600480360360c08110156106de57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019064010000000081111561073b57600080fd5b82018360208201111561074d57600080fd5b8035906020019184600183028401116401000000008311171561076f57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803590602001906401000000008111156107d257600080fd5b8201836020820111156107e457600080fd5b8035906020019184600183028401116401000000008311171561080657600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192908035906020019064010000000081111561086957600080fd5b82018360208201111561087b57600080fd5b8035906020019184600183028401116401000000008311171561089d57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050611047565b005b34801561091d57600080fd5b5061095c6004803603604081101561093457600080fd5b81019080803563ffffffff1690602001909291908035151590602001909291905050506110a3565b005b34801561096a57600080fd5b50610b526004803603606081101561098157600080fd5b810190808035906020019064010000000081111561099e57600080fd5b8201836020820111156109b057600080fd5b803590602001918460018302840111640100000000831117156109d257600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190640100000000811115610a3557600080fd5b820183602082011115610a4757600080fd5b80359060200191846001830284011164010000000083111715610a6957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190640100000000811115610acc57600080fd5b820183602082011115610ade57600080fd5b80359060200191846001830284011164010000000083111715610b0057600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f8201169050808301925050505050505091929192905050506110e2565b005b348015610b6057600080fd5b50610c4e60048036036060811015610b7757600080fd5b8101908080359060200190640100000000811115610b9457600080fd5b820183602082011115610ba657600080fd5b80359060200191846001830284011164010000000083111715610bc857600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803567ffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050611135565b6040518082815260200191505060405180910390f35b348015610c7057600080fd5b50610e0160048036036080811015610c8757600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190640100000000811115610cc457600080fd5b820183602082011115610cd657600080fd5b80359060200191846001830284011164010000000083111715610cf857600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f82011690508083019250505050505050919291929080359060200190640100000000811115610d5b57600080fd5b820183602082011115610d6d57600080fd5b80359060200191846001830284011164010000000083111715610d8f57600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505061117b565b005b348015610e0f57600080fd5b50610efd60048036036060811015610e2657600080fd5b8101908080359060200190640100000000811115610e4357600080fd5b820183602082011115610e5557600080fd5b80359060200191846001830284011164010000000083111715610e7757600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803567ffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506111c7565b6040518082815260200191505060405180910390f35b6000808573ffffffffffffffffffffffffffffffffffffffff16858051906020018667ffffffffffffffff16868082858588d5945050505050905080915050949350505050565b3373ffffffffffffffffffffffffffffffffffffffff1683805190602001848051906020018580838387878ad495505050505050158015610f9f573d6000803e3d6000d15b50505050565b3373ffffffffffffffffffffffffffffffffffffffff168585805190602001868051906020018780519060200188888888888888888888d798505050505050505050158015610ff8573d6000803e3d6000d15b505050505050565b6000808573ffffffffffffffffffffffffffffffffffffffff16858051906020018667ffffffffffffffff16868082858588d6945050505050905080915050949350505050565b8573ffffffffffffffffffffffffffffffffffffffff168585805190602001868051906020018780519060200188888888888888888888d79850505050505050505015801561109a573d6000803e3d6000d15b50505050505050565b3373ffffffffffffffffffffffffffffffffffffffff168263ffffffff16821515808284d2925050501580156110dd573d6000803e3d6000d15b505050565b3373ffffffffffffffffffffffffffffffffffffffff168380519060200184805190602001858051906020018181858589898cd3965050505050505015801561112f573d6000803e3d6000d15b50505050565b6000803373ffffffffffffffffffffffffffffffffffffffff16858051906020018667ffffffffffffffff16868082858588d59450505050509050809150509392505050565b8373ffffffffffffffffffffffffffffffffffffffff1683805190602001848051906020018580838387878ad4955050505050501580156111c0573d6000803e3d6000d15b5050505050565b6000803373ffffffffffffffffffffffffffffffffffffffff16858051906020018667ffffffffffffffff16868082858588d694505050505090508091505093925050505600a165627a7a72305820848cf11c8637fec291aeb62dda86fb14da1838f77e3c5c2820fc4d727a17362a0029";

    public static final String FUNC_TRUSTLIMIT = "trustLimit";

    public static final String FUNC_TRUSTSET = "trustSet";

    public static final String FUNC_PAY = "pay";

    public static final String FUNC_GATEWAYBALANCE = "gatewayBalance";

    public static final String FUNC_GATEWAYPAY = "gatewayPay";

    public static final String FUNC_ACCOUNTSET = "accountSet";

    public static final String FUNC_SETTRANSFERFEE = "setTransferFee";

    protected GatewayTest(Chainsql chainsql, String contractAddress, BigInteger gasLimit) {
        super(chainsql,BINARY, contractAddress, gasLimit);
    }

    public BigInteger trustLimit(String contractAddr, String sCurrency, BigInteger power, String gateWay) throws ContractCallException {
        final Function function = new Function(FUNC_TRUSTLIMIT,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(contractAddr),
                        new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.generated.Uint64(power),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public void trustLimit(String contractAddr, String sCurrency, BigInteger power, String gateWay, Publisher.Callback<BigInteger> cb) throws ContractCallException {
        final Function function = new Function(FUNC_TRUSTLIMIT,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(contractAddr),
                        new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.generated.Uint64(power),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        executeCallSingleValueReturn(function, BigInteger.class,new Publisher.Callback<BigInteger>() {
            @Override
            public void called(BigInteger args) {
                cb.called(args);
            }
        });
    }

    public Contract trustSet(String value, String sCurrency, String gateWay) {
        final Function function = new Function(
                FUNC_TRUSTSET,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(value),
                        new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public Contract pay(String accountTo, String value, String sendMax, String sCurrency, String gateWay) {
        final Function function = new Function(
                FUNC_PAY,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(accountTo),
                        new com.peersafe.abi.datatypes.Utf8String(value),
                        new com.peersafe.abi.datatypes.Utf8String(sendMax),
                        new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public BigInteger gatewayBalance(String contractAddr, String sCurrency, BigInteger power, String gateWay) throws ContractCallException {
        final Function function = new Function(FUNC_GATEWAYBALANCE,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(contractAddr),
                        new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.generated.Uint64(power),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public void gatewayBalance(String contractAddr, String sCurrency, BigInteger power, String gateWay, Publisher.Callback<BigInteger> cb) throws ContractCallException {
        final Function function = new Function(FUNC_GATEWAYBALANCE,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(contractAddr),
                        new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.generated.Uint64(power),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        executeCallSingleValueReturn(function, BigInteger.class,new Publisher.Callback<BigInteger>() {
            @Override
            public void called(BigInteger args) {
                cb.called(args);
            }
        });
    }

    public Contract gatewayPay(String contractAddr, String accountTo, String value, String sendMax, String sCurrency, String gateWay) {
        final Function function = new Function(
                FUNC_GATEWAYPAY,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(contractAddr),
                        new com.peersafe.abi.datatypes.Address(accountTo),
                        new com.peersafe.abi.datatypes.Utf8String(value),
                        new com.peersafe.abi.datatypes.Utf8String(sendMax),
                        new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public Contract accountSet(BigInteger uFlag, Boolean bSet) {
        final Function function = new Function(
                FUNC_ACCOUNTSET,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.generated.Uint32(uFlag),
                        new com.peersafe.abi.datatypes.Bool(bSet)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public Contract setTransferFee(String sRate, String minFee, String maxFee) {
        final Function function = new Function(
                FUNC_SETTRANSFERFEE,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(sRate),
                        new com.peersafe.abi.datatypes.Utf8String(minFee),
                        new com.peersafe.abi.datatypes.Utf8String(maxFee)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public BigInteger trustLimit(String sCurrency, BigInteger power, String gateWay) throws ContractCallException {
        final Function function = new Function(FUNC_TRUSTLIMIT,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.generated.Uint64(power),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public void trustLimit(String sCurrency, BigInteger power, String gateWay, Publisher.Callback<BigInteger> cb) throws ContractCallException {
        final Function function = new Function(FUNC_TRUSTLIMIT,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.generated.Uint64(power),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        executeCallSingleValueReturn(function, BigInteger.class,new Publisher.Callback<BigInteger>() {
            @Override
            public void called(BigInteger args) {
                cb.called(args);
            }
        });
    }

    public Contract trustSet(String contractAddr, String value, String sCurrency, String gateWay) {
        final Function function = new Function(
                FUNC_TRUSTSET,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Address(contractAddr),
                        new com.peersafe.abi.datatypes.Utf8String(value),
                        new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public BigInteger gatewayBalance(String sCurrency, BigInteger power, String gateWay) throws ContractCallException {
        final Function function = new Function(FUNC_GATEWAYBALANCE,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.generated.Uint64(power),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public void gatewayBalance(String sCurrency, BigInteger power, String gateWay, Publisher.Callback<BigInteger> cb) throws ContractCallException {
        final Function function = new Function(FUNC_GATEWAYBALANCE,
                Arrays.<Type>asList(new com.peersafe.abi.datatypes.Utf8String(sCurrency),
                        new com.peersafe.abi.datatypes.generated.Uint64(power),
                        new com.peersafe.abi.datatypes.Address(gateWay)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        executeCallSingleValueReturn(function, BigInteger.class,new Publisher.Callback<BigInteger>() {
            @Override
            public void called(BigInteger args) {
                cb.called(args);
            }
        });
    }

    public static GatewayTest deploy(Chainsql chainsql, BigInteger gasLimit) throws TransactionException {
        return deployRemoteCall(GatewayTest.class, chainsql, gasLimit, BINARY, "");
    }

    public static void deploy(Chainsql chainsql, BigInteger gasLimit, Publisher.Callback<GatewayTest> cb) throws TransactionException {
        deployRemoteCall(GatewayTest.class, chainsql, gasLimit, BINARY, "", cb);
    }

    public static GatewayTest load(Chainsql chainsql, String contractAddress, BigInteger gasLimit) {
        return new GatewayTest(chainsql,contractAddress, gasLimit);
    }
}
