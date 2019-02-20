pragma solidity ^0.4.2;

// Modified Greeter contract. Based on example at https://www.ethereum.org/greeter.

contract mortal {
    /* Define variable owner of the type address*/
    address owner;

    /* this function is executed at initialization and sets the owner of the contract */
    constructor() public { owner = msg.sender; }

    /* Function to recover the funds on the contract */
    function kill() public { if (msg.sender == owner) selfdestruct(owner); }
}

contract greeter is mortal {
    /* define variable greeting of the type string */
    string greeting;

    /* this runs when the contract is executed */
    constructor(string _greeting) public payable{
        greeting = _greeting;
    }

    function newGreeting(string _greeting) public {
        emit Modified(greeting, _greeting, greeting, _greeting);
        greeting = _greeting;
    }

    /* main function */
    function greet() public view returns (string) {
        return greeting;
    }

    /* we include indexed events to demonstrate the difference that can be
    captured versus non-indexed */
    event Modified(
            string indexed oldGreetingIdx, string indexed newGreetingIdx,
            string oldGreeting, string newGreeting);
}
