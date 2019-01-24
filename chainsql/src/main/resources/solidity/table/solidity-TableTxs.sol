pragma solidity ^0.4.4;

contract DBTest {
    /*
    * @param tableName eg: "test1"
    * @param raw eg: "[{\"field\":\"id\", \"type\" : \"int\", \"length\" : 11, \"PK\" : 1, \"NN\" : 1, \"UQ\" : 1}, { \"field\":\"account\", \"type\" : \"varchar\" }, { \"field\":\"age\", \"type\" : \"int\" }]"
    */
	function create(string tableName, string raw) public{
		msg.sender.create(tableName, raw);
	}
	/*
	* @param tableName eg: "test1"
	*/
	function drop(string tableName) public{
	    msg.sender.drop(tableName);
	}
	
	/*
	* @param owner table's owner'
	* @param tableName eg: "test1"
	* @param raw eg: "[{\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\", \"id\":0}, {\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\",   \"id\":1}, {\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\", \"id\":2}]"
	*/
	function insert(address owner, string tableName, string raw) public{
	    owner.insert(tableName, raw);
	}
	
	/*
	* @param tableName eg: "test1"
	* @param raw eg: "[{\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\", \"id\":0}, {\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\",   \"id\":1}, {\"account\":\"zU42yDW3fzFjGWosdeVjVasyPsF4YHj224\", \"id\":2}]"
	*/
	function insert(string tableName, string raw) public {
	    msg.sender.insert(tableName, raw);
	}
	
	/*
	* @param owner table's owner'
	* @param tableName "test1"
	* @param raw eg: "{\"id\":1}"
	*/
	function deletex(address owner, string tableName, string raw) public {
	    owner.deletex(tableName, raw);
	}
	
	/*
	* @param tableName "test1"
	* @param raw eg: "{\"id\":1}"
	*/
	function deletex(string tableName, string raw) public {
	    msg.sender.deletex(tableName, raw);
	}
	
	/*
	* @param owner table's owner'
	* @param tableName eg: "test1"
	* @param rawUpdate eg: "{\"age\":15}"
	* @param rawGet eg: "{\"id\": 2}"
	*/
	function update(address owner, string tableName, string rawUpdate, string rawGet) public{
	    owner.update(tableName, rawUpdate, rawGet);
	}
	
	/*
	* @param tableName eg: "test1"
	* @param rawUpdate eg: "{\"age\":15}"
	* @param rawGet eg: "{\"id\": 2}"
	*/
	function update(string tableName, string rawUpdate, string rawGet) public{
	    msg.sender.update(tableName, rawUpdate, rawGet);
	}
	
	/*
	* @param tableName eg: "test1"
	* @param tableNameNew eg: "testNew1"
	*/
	function rename(string tableName, string tableNameNew) public{
	    msg.sender.rename(tableName, tableNameNew);
	}
	
	/*
	* @param toWho ethereum address to be granted. need convert chainsql addr 2 ethereum addr .eg:  "0xzzzzzzzzzzzzzzzzzzzzBZbvji"
	* @param tableName eg: "test1"
	* @param raw eg: "{\"insert\":false, \"delete\":false}"
	*/
	function grant(address toWho, string tableName, string raw) public{
	    return msg.sender.grant(toWho, tableName, raw);
	}
	
	function sqlTransaction(string tableName) public{
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
	* @param raw eg: ""
    */
    function get(address owner, string tableName, string raw) public view returns(string) {
        uint256 handle = owner.get(tableName, raw);
		require(handle != uint256(0), "Get table data failed,maybe user not authorized!");
        uint row = db.getRowSize(handle);
        uint col = db.getColSize(handle);
        string memory xxx;
        for(uint i=0; i<row; i++)
        {
            for(uint j=0; j<col; j++)
            {
                string memory y = (db.getValueByIndex(handle, i, j));
                xxx = concat(xxx, y);
				if(j != col - 1)
                	xxx = concat(xxx, ", ");
            }
            xxx = concat(xxx, ";\n");
        }
        return xxx;
    }
        /*
	* @param owner table's owner'
	* @param tableName eg: "test1"
	* @param raw eg: ""
	* @param field eg: "id"
    */
    function get(address owner, string tableName, string raw, string field) public view returns(string) {
        uint256 handle = owner.get(tableName, raw);
		require(handle != uint256(0), "Get table data failed,maybe user not authorized!");
        uint row = db.getRowSize(handle);
        string memory xxx;
        for(uint i=0; i<row; i++)
        {
            string memory y = (db.getValueByKey(handle, i, field));
            xxx = concat(xxx, y);
            xxx = concat(xxx, ";");
        }
        return xxx;
    }
    
    function concat(string _base, string _value) internal pure returns (string) {
        bytes memory _baseBytes = bytes(_base);
        bytes memory _valueBytes = bytes(_value);

        string memory _tmpValue = new string(_baseBytes.length + _valueBytes.length);
        bytes memory _newValue = bytes(_tmpValue);
        
        uint j = 0;
        for(uint i=0; i<_baseBytes.length; i++) {
            _newValue[j++] = _baseBytes[i];
        }

        for(uint i=0; i<_valueBytes.length; i++) {
            _newValue[j++] = _valueBytes[i];
        }

        return string(_newValue);
    }
}