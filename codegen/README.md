#  codegen
用来生成调用智能合约需要的java类，调用方式：
```
java -jar codegen_chainsql.jar <input binary file>.bin <input abi file>.abi -p|--package <base package name> -o|--output <destination base directory>
```
打包 jar包需要导出的相关jar包包括：
- chainsql
- abi
- utils
- core
- javapoet  
- slf4j-api
- rxjava
- jackson-databind
- jackson-annotation
- jackson-core