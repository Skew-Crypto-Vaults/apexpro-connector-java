package exchange.apexpro.connector.model.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class Account {
    private long createdAt;
    private BigDecimal takerFeeRate;
    private BigDecimal makerFeeRate;
    private BigDecimal minInitialMarginRate;
    private String status;
    private String token;
}
