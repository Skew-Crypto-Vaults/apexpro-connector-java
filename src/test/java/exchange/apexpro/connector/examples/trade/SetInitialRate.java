package exchange.apexpro.connector.examples.trade;

import com.google.common.collect.ImmutableMap;
import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.enums.OrderSide;
import exchange.apexpro.connector.model.enums.OrderType;
import exchange.apexpro.connector.model.enums.TimeInForce;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import exchange.apexpro.connector.model.meta.PerpetualContract;
import exchange.apexpro.connector.model.trade.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
public class SetInitialRate {

        public static void main(String[] args) throws IOException {
            //Initialize the exchange configuration information. This is optional because it will be loaded automatically when you call its internal member variables.
            ExchangeInfo.load();
            BigDecimal initalMargin = new BigDecimal("0.1");
            //Send order to ApeXPro
            ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
            SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
            syncRequestClient.setInitialMarginRate("",initalMargin);
            log.info("Initial Margin Rate Set");
        }
}
