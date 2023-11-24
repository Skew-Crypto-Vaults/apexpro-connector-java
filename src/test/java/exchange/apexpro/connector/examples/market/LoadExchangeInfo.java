package exchange.apexpro.connector.examples.market;

import exchange.apexpro.connector.enums.ApexSupportedMarket;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadExchangeInfo {


    public static void main(String[] args) {
        ExchangeInfo.load();
        log.info("ExchangeInfo.global:{} ",ExchangeInfo.global(ApexSupportedMarket.BTC_USDT));
        log.info("ExchangeInfo.currencies: {}",ExchangeInfo.currencyMap(ApexSupportedMarket.BTC_USDT));
        log.info("ExchangeInfo.perpetualContracts: {}", ExchangeInfo.perpetualContractMap(ApexSupportedMarket.BSC_USDC));
    }
}
