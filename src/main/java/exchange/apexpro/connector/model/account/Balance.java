package exchange.apexpro.connector.model.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Balance
 */
@Data
public class Balance {

    private BigDecimal totalEquityValue;
    private BigDecimal availableBalance; //Available to withdraw or as margin to open new position.
    private BigDecimal initialMargin;
    private BigDecimal maintenanceMargin;

    public long updatedTime;

}
