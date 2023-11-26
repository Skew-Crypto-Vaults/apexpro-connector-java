package exchange.apexpro.connector.model.account;

import exchange.apexpro.connector.enums.ApexSupportedMarket;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
@Data
@Builder
public class ApexBalance {
    Map<String, Balance> balances;
}
