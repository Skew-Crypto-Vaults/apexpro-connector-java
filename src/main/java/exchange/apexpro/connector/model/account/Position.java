package exchange.apexpro.connector.model.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class Position {
    private String token;
    private String symbol;
    private String status;
    private String side;
    private BigDecimal size;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private long createdAt;
    private long updatedTime;
    private BigDecimal fee;
    private BigDecimal fundingFee;
    private String lightNumbers;
    private BigDecimal customInitialMarginRate;

    private BigDecimal maxSize;
    private BigDecimal sumOpen;
    private BigDecimal sumClose;
    private BigDecimal netFunding;
    private String accountId;
    private Long closedTime;
}
