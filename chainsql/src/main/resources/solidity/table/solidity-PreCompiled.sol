// SPDX-License-Identifier: GPL-3.0
pragma solidity ^0.8.5;

abstract contract TableOperation{
    function createTable(string memory tableName,string memory raw) public virtual;
	
    function createByContract(string memory tableName,string memory raw) public virtual;
	
    function dropTable(string memory tableName) public virtual;
    
    function dropTableByContract(string memory tableName) public virtual;
    
    function grant(address destAddr,string memory tableName,string memory authRaw) public virtual;
    
    function grantByContract(address destAddr,string memory tableName,string memory authRaw) public virtual;
    
    function renameTable(string memory tableName,string memory tableNewName) public virtual;
    
    function renameTableByContract(string memory tableName,string memory tableNewName) public virtual;
    
    function insert(address owner, string memory tableName, string memory raw) public virtual;
    
    function insertWithHash(address owner, string memory tableName, string memory raw,string memory autoFillField) public virtual;
    
    function insertWithHashByContract(address owner, string memory tableName, string memory raw,string memory autoFillField) public virtual;
	
	function insertByContract(address owner, string memory tableName, string memory raw) public virtual;
	
	function update(address owner,string memory tableName,string memory raw,string memory updateRaw) public virtual;
	
	function updateByContract(address owner,string memory tableName,string memory raw,string memory updateRaw) public virtual;
    
    function update(address owner,string memory tableName,string memory raw) public virtual;
	
	function updateByContract(address owner,string memory tableName,string memory raw) public virtual;
    
    function deleteData(address owner,string memory tableName,string memory raw)public virtual;
	
	function deleteByContract(address owner,string memory tableName,string memory raw)public virtual;
	
	function addFields(string memory tableName,string memory raw)public virtual;
	
	function addFieldsByContract(string memory tableName,string memory raw)public virtual;
	
	function deleteFields(string memory tableName,string memory raw)public virtual;
	
	function deleteFieldsByContract(string memory tableName,string memory raw)public virtual;
	
	function modifyFields(string memory tableName,string memory raw)public virtual;
	
	function modifyFieldsByContract(string memory tableName,string memory raw)public virtual;
	
	function createIndex(string memory tableName,string memory raw)public virtual;
	
	function createIndexByContract(string memory tableName,string memory raw)public virtual;
	
	function deleteIndex(string memory tableName,string memory raw)public virtual;
	
	function deleteIndexByContract(string memory tableName,string memory raw)public virtual;
	
	function getDataHandle(address owner,string memory tableName,string memory raw)public view virtual returns(uint256);
	
	function getDataHandleByContract(address owner,string memory tableName,string memory raw)public view virtual returns(uint256);
}
