package exchange.apexpro.connector.model.meta;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExchangeConfig {
    List<PerpetualContract> perpetualContract;
    List<Currency> currency;
    Global global;
    MultiChain multiChain;

}
