package exchange.apexpro.connector.model.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Wallet
 */
@Data
@Builder
public class Wallet {
private String userId;
    private String accountId;
    private BigDecimal balance;
    private String asset;
    private String token;
    private BigDecimal pendingDepositAmount;
    private BigDecimal pendingWithdrawAmount;
    private BigDecimal pendingTransferOutAmount;
    private BigDecimal pendingTransferInAmount;


}
