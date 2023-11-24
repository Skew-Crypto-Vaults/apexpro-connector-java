package exchange.apexpro.connector.model.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder(builderMethodName = "newBuilder", builderClassName = "Builder", toBuilder = true, setterPrefix="set")
@AllArgsConstructor
@Data
public class ApexExchangeInfo {
    ExchangeConfig usdtConfig;
    ExchangeConfig usdcConfig;
}
