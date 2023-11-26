package exchange.apexpro.connector.model.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class ExperienceMoney {
    private BigDecimal availableAmount;
    private BigDecimal totalNumber;
    private BigDecimal totalAmount;
    private BigDecimal recycledAmount;
    private String token;
}
