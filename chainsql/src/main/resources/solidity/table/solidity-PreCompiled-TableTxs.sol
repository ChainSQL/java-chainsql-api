pragma solidity ^0.8.5;
import "./TableOperation.sol";
contract DBTest {
    
    TableOperation op_;
    
     constructor(string memory tableName, string memory raw) payable{
        op_ = TableOperation(address(0x1001));
        op_.createByContract(tableName,raw);
    }
    
    fallback () payable external {}
    receive () payable external {}
    
    /*
    * @param tableName eg: "test1"
    * @param raw eg: "[{\"field\":\"id\", \"type\" : \"int\", \"length\" : 11, \"PK\" : 1, \"NN\" : 1, \"UQ\" : 1}, { \"field\":\"account\", \"type\" : \"varchar\" }, { \"field\":\"age\", \"type\" : \"int\" }]"
    */
    
     function create(string memory tableName, string memory raw) public{
       op_.createTable(tableName,raw);
    }
     function createByContract(string memory tableName, string memory raw) public{
       op_.createByContract(tableName,raw);
    }
    /*
    * @param tableName eg: "test1"
    */
    /*
    * @param owner table's owner'
    * @param tableName eg: "test1"
    * @param raw eg: "[{\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\", \"id\":0}, {\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\",   \"id\":1}, {\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\", \"id\":2}]"
    */
    function insert(address owner, string memory tableName, string memory raw) public{
        //owner.insert(tableName, raw);
        op_.insert(owner,tableName,raw);
    }

    function insertByContract(address owner, string memory tableName, string memory raw) public{
        //owner.insert(tableName, raw);
        op_.insertByContract(owner,tableName,raw);
    }
  
   function insertHash(address owner, string memory tableName, string memory raw,string memory autoFillField) public {
        op_.insertWithHash(owner,tableName,raw,autoFillField);
   }
  
   function insertHashByContract(address owner, string memory tableName, string memory raw,string memory autoFillField) public {
        op_.insertWithHashByContract(owner,tableName,raw,autoFillField);
   }
  /*
    * @param tableName eg: "test1"
    */
    function drop(string memory tableName) public{
        op_.dropTable(tableName);
    }
  
     function dropByContract(string memory tableName) public{
        op_.dropTableByContract(tableName);
    }
    /*
    * @param owner table's owner'
    * @param tableName "test1"
    * @param raw eg: "{\"id\":1}"
    */
    function deletex(address owner, string memory tableName, string memory raw) public {
        op_.deleteData(owner, tableName, raw);
    }

    function deletexByContract(address owner, string memory tableName, string memory raw) public {
         op_.deleteByContract(owner, tableName, raw);
    }

    /*
    * @param owner table's owner'
    * @param tableName eg: "test1"
    * @param rawUpdate eg: "{\"age\":15}"
    * @param rawGet eg: "{\"id\": 2}"
    */
    function update(address owner, string memory tableName, string memory rawUpdate, string memory rawGet) public{
         op_.update(owner, tableName, rawUpdate, rawGet);
    }

    function updateByContract(address owner, string memory tableName, string memory rawUpdate, string memory rawGet) public{
        op_.updateByContract(owner, tableName, rawUpdate, rawGet);
    }

    /*
    * @param owner table's owner'
    * @param tableName eg: "test1"
    * @param raw eg: "[{\"age\":15},{\"id\": 2}]"
    */
    function update(address owner, string memory tableName, string memory raw) public{
         op_.update(owner, tableName, raw);
    }

    function updateByContract(address owner, string memory tableName, string memory raw) public{
        op_.updateByContract(owner, tableName, raw);
    }


    /*
    * @param tableName eg: "test1"
    * @param tableNameNew eg: "testNew1"
    */
    function rename(string memory tableName, string memory tableNameNew) public{
        op_.renameTable(tableName, tableNameNew);
    }
    
    function renameByContract(string memory tableName, string memory tableNameNew) public{
        op_.renameTableByContract(tableName, tableNameNew);
    }

    /*
    * @param toWho ethereum address to be granted. need convert chainsql addr 2 ethereum addr .eg:  "0xzzzzzzzzzzzzzzzzzzzzBZbvji"
    * @param tableName eg: "test1"
    * @param raw eg: "{\"insert\":false, \"delete\":false}"
    */
    function grant(address toWho, string memory tableName, string memory raw) public{
        return op_.grant(toWho, tableName, raw);
    }
    function grantByContract(address toWho, string memory tableName, string memory raw) public{
        return op_.grantByContract(toWho, tableName, raw);
    }

    /* @param tableName eg: "test1"
     * @param raw [{\"field\":\"num\",\"type\":\"int\"}]
     */
    function addFields(string memory tableName, string memory raw) public{
        return op_.addFields(tableName, raw);
    }

   
    function addFieldsByContract(string memory tableName, string memory raw) public{
        return op_.addFieldsByContract(tableName, raw);
    }

    /* @param tableName eg: "test1"
     * @param raw [{\"field\":\"num\"}]
     */
    function deleteFields(string memory tableName, string memory raw) public{
        return op_.deleteFields(tableName, raw);
    }

    function deleteFieldsByContract(string memory tableName, string memory raw) public{
        return op_.deleteFieldsByContract(tableName, raw);
    }
    
    /*@param tableName eg: "test1"
    * @param raw [{\"field\":\"age\",\"type\":\"varchar\",\"length\":10,\"NN\":1}]
    */

    function modifyFields(string memory tableName, string memory raw) public{
        return op_.modifyFields(tableName, raw);
    }

    function modifyFieldsByContract(string memory tableName, string memory raw) public{
        return op_.modifyFieldsByContract(tableName, raw);
    }

    
    /*@param tableName eg: "test1"
    * @param raw [{\"index\":\"AcctLgrIndex\"},{\"field\":\"age\"},{\"field\":\"Account\"}]
    */
    function createIndex(string memory tableName, string memory raw) public{
        return op_.createIndex(tableName, raw);
    }

    function createIndexByContract(string memory tableName, string memory raw) public{
        return op_.createIndexByContract(tableName, raw);
    }

    /*@param tableName eg: "test1"
    * @param raw [{\"index\":\"AcctLgrIndex\"}]
    */
    function deleteIndex(string memory tableName, string memory raw) public{
        return op_.deleteIndex(tableName, raw);
    }

    function deleteIndexByContract(string memory tableName, string memory raw) public{
        return op_.deleteIndexByContract(tableName, raw);
    }


    function sqlTransaction(string memory tableName) public{
        db.beginTrans();
        msg.sender.create(tableName, "[{\"field\":\"id\", \"type\" : \"int\", \"length\" : 11, \"PK\" : 1, \"NN\" : 1, \"UQ\" : 1}, { \"field\":\"account\", \"type\" : \"varchar\" }, { \"field\":\"age\", \"type\" : \"int\" }]");
        msg.sender.insert(tableName, "[{\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\", \"id\":1}, {\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\",   \"id\":2}]");
        msg.sender.deletex(tableName, "{\"id\":1}");
        msg.sender.update(tableName, "{\"account\":\"id==2\"}", "{\"id\": 2}");
        db.commit();
    }

    /*
    * @param owner table's owner'
    * @param tableName eg: "test1"
    * @param raw eg: "[[],{\"$or\":[{\"id\":\"1\"}, {\"id\": \"2\"}]}]"
    */
    
      function get(address owner, string memory tableName, string memory raw) public view returns(string memory) {
        uint256 handle = op_.getDataHandle(owner, tableName, raw);
        require(handle != uint256(0), "Get table data failed,maybe user not authorized!");
        uint row = db.getRowSize(handle);
        uint col = db.getColSize(handle);
        bytes memory xxx = "";
        for(uint i=0; i<row; i++)
        {
            for(uint j=0; j<col; j++)
            {
                string memory y = (db.getValueByIndex(handle, i, j));
                xxx = bytes.concat(xxx,bytes(y));
                if(j != col - 1)
                    xxx = bytes.concat(xxx,", ");
            }
            xxx = bytes.concat(xxx,";\n");
        }
        return string(xxx);
    }
    
 
     function getByContract(address owner, string memory tableName, string memory raw)  public view returns(string memory) {
        uint256 handle = op_.getDataHandleByContract(owner, tableName, raw);
        require(handle != uint256(0), "Get table data failed,maybe user not authorized!");
        uint row = db.getRowSize(handle);
        uint col = db.getColSize(handle);
        bytes memory xxx = "";
        for(uint i=0; i<row; i++)
        {
            for(uint j=0; j<col; j++)
            {
                string memory y = (db.getValueByIndex(handle, i, j));
                xxx = bytes.concat(xxx,bytes(y));
                if(j != col - 1)
                    xxx = bytes.concat(xxx,", ");
            }
            xxx = bytes.concat(xxx,";\n");
        }
        return string(xxx);
    }
        /*
    * @param owner table's owner'
    * @param tableName eg: "test1"
    * @param raw eg: ""
    * @param field eg: "id"
    */

    function get(address owner, string memory tableName, string memory raw, string memory field) public view returns(string memory) {
        uint256 handle = op_.getDataHandle(owner, tableName, raw);
        require(handle != uint256(0), "Get table data failed,maybe user not authorized!");
        uint row = db.getRowSize(handle);
        bytes memory xxx = "";
        for(uint i=0; i<row; i++)
        {
            string memory y = (db.getValueByKey(handle, i, field));
            xxx = bytes.concat(xxx, bytes(y));
            xxx = bytes.concat(xxx, ";");
        }
        return string(xxx);
    }

   function getByContract(address owner, string memory tableName, string memory raw, string memory field) public view returns(string memory) {
        uint256 handle = op_.getDataHandleByContract(owner, tableName, raw);
        require(handle != uint256(0), "Get table data failed,maybe user not authorized!");
        uint row = db.getRowSize(handle);
        bytes memory xxx = "";
        for(uint i=0; i<row; i++)
        {
            string memory y = (db.getValueByKey(handle, i, field));
            xxx = bytes.concat(xxx, bytes(y));
            xxx = bytes.concat(xxx, ";");
        }
        return string(xxx);
    }
}
