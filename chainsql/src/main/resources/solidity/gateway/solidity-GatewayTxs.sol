pragma solidity ^0.4.17;

contract GatewayTxsTest { 

    constructor () public {
    }
	
	function() external payable {  }
	
		
    /*
    *  设置网关相关属性
    * @param uFlag   一般情况下为8，表示asfDefaultRipple，详见https://developers.ripple.com/accountset.html#accountset-flags
    * @param bSet    true，开启uFlag；false 取消uFlag。
    */
    function accountSet(uint32 uFlag,bool bSet) public {
        msg.sender.accountSet(uFlag,bSet);
    }	
	
    /*
    *  设置网关交易费用
    * @param sRate    交易费率。范围为"1.0”- "2.0" 或者"0.0"
    * @param minFee   网关交易最小花费  字符串转成10进制数后， >=0
    * @param maxFee   网关交易最大花费	字符串转成10进制数后,  >=0
	* @ 备注 ,以下规则均在字符串转化为10进制数后进行运算
	
		 1 sRate 为0或者1时，表示取消费率，但是此时的minFee必须等于maxFee。
		 2 minFee 或者 maxFee为0 时，表示取消相应的最小，最大费用。
		 3 minFee等于maxFee时， sRate 必为0或者1。
		 4 除了minFee 或者 maxFee为0 时的情况时，minFee < maxFee。
		   
    */
    function setTransferFee(string sRate,string minFee,string maxFee) public {
        
        msg.sender.setTransferFee(sRate,minFee,maxFee);
    }



    /*
    *   设置信任网关代币以及代币的额度
    * @param value           代币额度
    * @param sCurrency       代币名称
    * @param gateWay         信任网关地址
    */
    function trustSet(string value,string sCurrency,address gateWay) public {

        msg.sender.trustSet(value,sCurrency,gateWay);
    }

    /*
    *   设置信任网关代币以及代币的额度
    * @param contractAddr    合约地址
    * @param value           代币额度
    * @param sCurrency       代币名称
    * @param gateWay         信任网关地址
    */
    function trustSet(address contractAddr,string value,string sCurrency, address gateWay) public {

        contractAddr.trustSet(value,sCurrency,gateWay);
    }
	
    /*
    *   查询网关的信任代币额度
    * @param  sCurrency          代币名称
	* @param  power              查询参数.代币额度为100时，如果该参数为2，函数返回值为10000 = 100*10^2；代币额度为100.5时,如果该参数为1,函数返回值为1005 = 100.5*10^1  				
    * @param  gateWay            网关地址
    * @return -1:不存在网关代币信任关系; >=0 信任网关代币额度
    */
    function trustLimit(string sCurrency,uint64 power,address gateWay)
    public view returns(int256) {

        int256  ret =  (int256)(msg.sender.trustLimit(sCurrency,power,gateWay));
		
		return ret;
    }


    /*
    *   查询网关的信任代币额度
    * @param  contractAddr       合约地址
    * @param  sCurrency          代币名称
	* @param  power              查询参数.代币额度为100时，如果该参数为2，函数返回值为10000 = 100*10^2；代币额度为100.5时,如果该参数为1,函数返回值为1005 = 100.5*10^1  			
    * @param  gateWay            网关地址
    * @return -1:不存在网关代币信任关系; >=0 信任网关代币额度
    */
    function trustLimit(address contractAddr,string sCurrency,uint64 power,address gateWay)
    public view returns(int256) {
        // 合约地址也可查询网关信任代币信息
		
        int256  ret =  (int256)(contractAddr.trustLimit(sCurrency,power,gateWay));
		
		return ret;
    }	
	
    /*
    *   获取网关代币的余额
    * @param  sCurrency       代币名称
	* @param  power           查询参数.代币余额为100时，如果该参数为2，函数返回值为10000 = 100*10^2；代币余额为100.5时,如果该参数为1,函数返回值为1005 = 100.5*10^1  		
    * @param  gateWay         网关地址
    * @return -1:不存在该网关代币; >=0 网关代币的余额
    */
    function gatewayBalance(string sCurrency,uint64 power,address gateWay)   public view returns(int256) {

        int256  ret = (int256)(msg.sender.gatewayBalance(sCurrency,power,gateWay));
		return ret;
    }


    /*
    *   获取网关代币的余额
    * @param  contractAddr    合约地址
    * @param  sCurrency       代币名称
	* @param  power           查询参数.代币余额为100时，如果该参数为2，函数返回值为10000 = 100*10^2；代币余额为100.5时,如果该参数为1,函数返回值为1005 = 100.5*10^1  	
    * @param  gateWay         网关地址
    * @return -1:不存在该网关代币; >=0 网关代币的余额
    */
    function gatewayBalance(address contractAddr,string sCurrency,uint64 power,address gateWay) public view returns(int256)  {
        // 合约地址也可获取网关代币的余额
		
        int256  ret = (int256)(contractAddr.gatewayBalance(sCurrency,power,gateWay));
		return ret;
    }	
	
	
	
  /*
  *   转账代币
  * @param accountTo         转入账户
  * @param value             代币数量
  * @param sendMax           消耗代币的最大值，具体计算规则见http://docs.chainsql.net/interface/javaAPI.html#id84
  * @param sCurrency         代币名称
  * @param sGateway          网关地址
  */
    function pay(address accountTo,string value,string sendMax,
                        string sCurrency,address gateWay) public{


        msg.sender.pay(accountTo,value,sendMax,sCurrency,gateWay);
    }

    /*
    *   转账代币
    * @param contractAddr      合约地址
    * @param accountTo         转入账户
    * @param value             代币数量
    * @param sendMax           消耗代币的最大值，具体计算规则见http://docs.chainsql.net/interface/javaAPI.html#id84	
    * @param sCurrency         代币名称
    * @param gateWay           网关地址
    */
    function gatewayPay(address contractAddr,address accountTo,string value,string sendMax,
                        string sCurrency,address gateWay) public{
       

	   contractAddr.pay(accountTo,value,sendMax,sCurrency,gateWay);
    }		
	
}
