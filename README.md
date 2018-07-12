# ChainSQL API for JAVA
## 使用说明 
chainsql-java-api 是一个 maven 工程，包含以下子工程：<br>
- **chainsql**  api 工程
- **codegen** 生成智能合约类代码工程


##  chainsql
引入方式：
```
<!-- https://mvnrepository.com/artifact/com.peersafe/chainsql -->
<dependency>
    <groupId>com.peersafe</groupId>
    <artifactId>chainsql</artifactId>
    <version>1.4.4</version>
</dependency>

```
具体使用方法请参考：[Chainsql 官网](http://chainsql.net/api_java.html)

##  codegen
用来生成调用智能合约需要的java类，调用方式：
```
java -jar codegen_chainsql.jar <input binary file>.bin <input abi file>.abi -p|--package <base package name> -o|--output <destination base directory>
```
