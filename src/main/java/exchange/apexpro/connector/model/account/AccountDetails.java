package exchange.apexpro.connector.model.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Account
 */
@Builder
@Data
public class AccountDetails {
    private String starkKey;
    private String positionId;
    private String ethereumAddress;
    private String id;
    private List<ExperienceMoney> experienceMoney;
    private List<Account> accounts;
    private List<Wallet> wallets;
    private List<Position> positions;

}
