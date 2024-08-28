package org.web3j.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        new Application().run();
    }

    private void run() throws Exception {
        Web3j web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/YOUR_INFURA_PROJECT_ID"));  // Replace with your Infura Project ID
        log.info("Connected to Ethereum client version: " + web3j.web3ClientVersion().send().getWeb3ClientVersion());

        Credentials credentials = WalletUtils.loadCredentials("<password>", "/path/to/<walletfile>");
        log.info("Credentials loaded");

        log.info("Sending 1 Wei (" + Convert.fromWei("1", Convert.Unit.ETHER).toPlainString() + " Ether)");
        TransactionReceipt transferReceipt = Transfer.sendFunds(web3j, credentials, "0x19e03255f667bdfd50a32722df860b1eeaf4d635", BigDecimal.ONE, Convert.Unit.WEI).send();
        log.info("Transaction complete, view it at https://rinkeby.etherscan.io/tx/" + transferReceipt.getTransactionHash());

        log.info("Deploying smart contract");
        ContractGasProvider contractGasProvider = new DefaultGasProvider();
        Greeter contract = Greeter.deploy(web3j, credentials, contractGasProvider, "test").send();

        String contractAddress = contract.getContractAddress();
        log.info("Smart contract deployed to address " + contractAddress);
        log.info("View contract at https://rinkeby.etherscan.io/address/" + contractAddress);

        log.info("Value stored in remote smart contract: " + contract.greet().send());

        TransactionReceipt transactionReceipt = contract.newGreeting("Well hello again").send();
        log.info("New value stored in remote smart contract: " + contract.greet().send());

        for (Greeter.ModifiedEventResponse event : contract.getModifiedEvents(transactionReceipt)) {
            log.info("Modify event fired, previous value: " + event.oldGreeting + ", new value: " + event.newGreeting);
            log.info("Indexed event previous value: " + Numeric.toHexString(event.oldGreetingIdx) + ", new value: " + Numeric.toHexString(event.newGreetingIdx));
        }
    }
}
