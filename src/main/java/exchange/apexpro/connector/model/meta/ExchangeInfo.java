package exchange.apexpro.connector.model.meta;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import exchange.apexpro.connector.RequestOptions;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.enums.ApexSupportedMarket;
import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.impl.utils.json.CostBigDecimalAdapter;
import exchange.apexpro.connector.impl.utils.json.CustomLongDecimalAdapter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static exchange.apexpro.connector.exception.ApexProApiException.RUNTIME_ERROR;

@Slf4j
public class ExchangeInfo {

    private static final AtomicReference<Holder> HOLDER_REF = new AtomicReference<>(
            new Holder(ImmutableMap.<ApexSupportedMarket, Global>of(), ImmutableMap.<ApexSupportedMarket, ImmutableMap<String, Currency>>of(),
                    ImmutableMap.<ApexSupportedMarket, ImmutableMap<String, PerpetualContract>>of(),
                    ImmutableMap.<ApexSupportedMarket, ImmutableMap<Integer, PerpetualContract>>of(), ImmutableMap.<ApexSupportedMarket, MultiChain>of()));

    private static boolean isLoaded = false;

    public static void load() {
        if (!isLoaded) {
            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(options);
            String exchangeInfoJson = syncRequestClient.getExchangeInfo();
            loadData(exchangeInfoJson);
            isLoaded = true;
        }
    }

    private static void loadData(String exchangeInfoJsonStr) {
        log.debug("[loadMetaData] content=", exchangeInfoJsonStr);
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(BigDecimal.class, new CostBigDecimalAdapter());
            Gson gson = builder.create();

            ApexExchangeInfo apexInfo = gson.fromJson(exchangeInfoJsonStr, ApexExchangeInfo.class);
            ImmutableMap<String, Currency> currencyMapUsdc = parseCurrency(apexInfo.getUsdcConfig().getCurrency());
            ImmutableMap<String, Currency> currencyMapUsdt = parseCurrency(apexInfo.getUsdtConfig().getCurrency());
            ImmutableMap<ApexSupportedMarket, ImmutableMap<String, Currency>> currencyMap =
                    ImmutableMap.<ApexSupportedMarket, ImmutableMap<String, Currency>>builder().put(ApexSupportedMarket.BSC_USDC, currencyMapUsdc)
                                .put(ApexSupportedMarket.BTC_USDT, currencyMapUsdt).build();

            ImmutableMap<String, PerpetualContract> perpetualContractMapUsdc =
                    parsePerpetualContract(apexInfo.getUsdcConfig().getPerpetualContract(), currencyMapUsdc);
            ImmutableMap<String, PerpetualContract> perpetualContractMapUsdt =
                    parsePerpetualContract(apexInfo.getUsdtConfig().getPerpetualContract(), currencyMapUsdt);
            ImmutableMap<ApexSupportedMarket, ImmutableMap<String, PerpetualContract>> perpetualMap =
                    ImmutableMap.<ApexSupportedMarket, ImmutableMap<String, PerpetualContract>>builder()
                                .put(ApexSupportedMarket.BSC_USDC, perpetualContractMapUsdc)
                                .put(ApexSupportedMarket.BTC_USDT, perpetualContractMapUsdt).build();

            ImmutableMap<ApexSupportedMarket, MultiChain> multiChainMap = ImmutableMap.<ApexSupportedMarket, MultiChain>builder().put(ApexSupportedMarket.BSC_USDC,
                                                                                              apexInfo.getUsdcConfig().getMultiChain()).put(ApexSupportedMarket.BTC_USDT, apexInfo.getUsdtConfig().getMultiChain())
                                                                                      .build();


            ImmutableMap<ApexSupportedMarket, Global> globalMap =
                    ImmutableMap.<ApexSupportedMarket, Global>builder().put(ApexSupportedMarket.BSC_USDC, apexInfo.getUsdcConfig().getGlobal())
                                .put(ApexSupportedMarket.BTC_USDT, apexInfo.getUsdtConfig().getGlobal()).build();

            ImmutableMap<Integer, PerpetualContract> crossSymbolIdToPerpetualContractMapUsdc = perpetualContractMapUsdc.values().stream()
                                                                                                                       .collect(
                                                                                                                               ImmutableMap.toImmutableMap(
                                                                                                                                       PerpetualContract::getCrossSymbolId,
                                                                                                                                       Function.identity(),
                                                                                                                                       (a, b) -> a));
            ImmutableMap<Integer, PerpetualContract> crossSymbolIdToPerpetualContractMapUsdt = perpetualContractMapUsdt.values().stream()
                                                                                                                       .collect(
                                                                                                                               ImmutableMap.toImmutableMap(
                                                                                                                                       PerpetualContract::getCrossSymbolId,
                                                                                                                                       Function.identity(),
                                                                                                                                       (a, b) -> a));

            ImmutableMap<ApexSupportedMarket, ImmutableMap<Integer, PerpetualContract>> crossSymbolIdToPerpetualContractMap =
                    ImmutableMap.<ApexSupportedMarket, ImmutableMap<Integer, PerpetualContract>>builder()
                                .put(ApexSupportedMarket.BSC_USDC, crossSymbolIdToPerpetualContractMapUsdc)
                                .put(ApexSupportedMarket.BTC_USDT, crossSymbolIdToPerpetualContractMapUsdt).build();
            ;
            HOLDER_REF.set(new Holder(globalMap, currencyMap, perpetualMap, crossSymbolIdToPerpetualContractMap, multiChainMap));
            log.debug("[loadMetaData] loaded. global={}", globalMap.toString());
            currencyMap.values().forEach(currency -> log.debug("[loadMetaData] finish. currencyMap={}", currency.toString()));
            perpetualMap.values().forEach(
                    perpetualContract -> log.debug("[loadMetaData] finish. perpetualContractMap={}", perpetualContract.toString()));
        }
        catch (Throwable throwable) {
            log.error("[loadMetaData] error. exchangeInfoJsonStr={}", exchangeInfoJsonStr, throwable);
            throw new Error(throwable);
        }
    }


    private static ImmutableMap<String, Currency> parseCurrency(List<Currency> list) {
        return list.stream().map(pb -> {
            BigDecimal stepSize = pb.getStepSize();
            return Currency.newBuilder().setId(pb.getId()).setStepSize(stepSize)
                           .setStepSizeZero(BigDecimal.ZERO.setScale(stepSize.scale(), RoundingMode.UNNECESSARY))
                           .setIconUrl(pb.getIconUrl()).setStarkExAssetId(pb.getStarkExAssetId())
                           .setStarkExResolution(pb.getStarkExResolution() == null ? BigDecimal.ONE : pb.getStarkExResolution()).build();
        }).collect(ImmutableMap.toImmutableMap(Currency::getId, Function.identity()));
    }

    private static ImmutableMap<String, PerpetualContract> parsePerpetualContract(List<PerpetualContract> list,
                                                                                  ImmutableMap<String, Currency> currencyMap)
    {
        return list.stream().map(pb -> {
            Currency settleCurrency = currencyMap.get(pb.getSettleCurrencyId());
            if (settleCurrency == null) {
                throw new RuntimeException("invalid settleCurrencyId: " + pb.getSettleCurrencyId());
            }
            Currency underlyingCurrency = currencyMap.get(pb.getUnderlyingCurrencyId());
            if (underlyingCurrency == null) {
                throw new RuntimeException("invalid underlyingCurrencyId: " + pb.getUnderlyingCurrencyId());
            }

            BigDecimal tickSize = pb.getTickSize();
            BigDecimal stepSize = pb.getStepSize();
            BigDecimal valueSize = tickSize.multiply(stepSize);
            return PerpetualContract.newBuilder().setSymbol(pb.getSymbol()).setSymbolDisplayName(pb.getSymbolDisplayName())
                                    .setSettleCurrencyId(pb.getSettleCurrencyId()).setSettleCurrency(settleCurrency)
                                    .setUnderlyingCurrencyId(pb.getUnderlyingCurrencyId()).setUnderlyingCurrency(underlyingCurrency)
                                    .setTickSize(tickSize)
                                    .setTickSizeZero(BigDecimal.ZERO.setScale(tickSize.scale(), RoundingMode.UNNECESSARY))
                                    .setStepSize(stepSize)
                                    .setStepSizeZero(BigDecimal.ZERO.setScale(stepSize.scale(), RoundingMode.UNNECESSARY))
                                    .setValueSize(valueSize)
                                    .setValueSizeZero(BigDecimal.ZERO.setScale(valueSize.scale(), RoundingMode.UNNECESSARY))
                                    .setMinOrderSize(pb.getMinOrderSize()).setMaxOrderSize(pb.getMaxOrderSize())
                                    .setMaxPositionSize(pb.getMaxPositionSize()).setMaxMarketPriceRange(pb.getMaxMarketPriceRange())
                                    .setInitialMarginRate(pb.getInitialMarginRate()).setMaintenanceMarginRate(pb.getMaintenanceMarginRate())
                                    .setBaselinePositionValue(pb.getBaselinePositionValue())
                                    .setIncrementalPositionValue(pb.getIncrementalPositionValue())
                                    .setIncrementalInitialMarginRate(pb.getIncrementalInitialMarginRate())
                                    .setIncrementalMaintenanceMarginRate(pb.getIncrementalMaintenanceMarginRate())
                                    .setMaxMaintenanceMarginRate(pb.getMaxMaintenanceMarginRate())
                                    .setMaxPositionValue(pb.getMaxPositionValue()).setEnableTrade(pb.isEnableTrade())
                                    .setEnableDisplay(pb.isEnableDisplay()).setEnableOpenPosition(pb.isEnableOpenPosition())
                                    .setEnableFundingSettlement(pb.isEnableFundingSettlement()).setDigitMerge(pb.getDigitMerge())
                                    .setCrossId(pb.getCrossId()).setCrossSymbolId(pb.getCrossSymbolId())
                                    .setCrossSymbolName(pb.getCrossSymbolName()).setFundingInterestRate(pb.getFundingInterestRate())
                                    .setFundingImpactMarginNotional(pb.getFundingImpactMarginNotional())
                                    .setDisplayMaxLeverage(pb.getDisplayMaxLeverage()).setDisplayMinLeverage(pb.getDisplayMinLeverage())
                                    .setIndexPriceDecimals(pb.getIndexPriceDecimals()).setIndexPriceVarRate(pb.getIndexPriceVarRate())
                                    .setOpenPositionOiLimitRate(pb.getOpenPositionOiLimitRate())
                                    .setOpenPositionOiLimitMin(pb.getOpenPositionOiLimitMin())
                                    .setStarkExSyntheticAssetId(pb.getStarkExSyntheticAssetId())
                                    .setStarkExResolution(pb.getStarkExResolution()).setStarkExRiskFactor(pb.getStarkExRiskFactor())
                                    .setStarkExRiskRate(new BigDecimal(pb.getStarkExRiskFactor()).divide(new BigDecimal(1L << 32))).build();
        }).collect(ImmutableMap.toImmutableMap(PerpetualContract::getSymbol, Function.identity()));
    }

    public static Global global(ApexSupportedMarket market) {
        if (!isLoaded) {
            load();
        }
        return HOLDER_REF.get().global.get(market);
    }

    public static MultiChain multiChain(ApexSupportedMarket market) {
        if (!isLoaded) {
            load();
        }

        return HOLDER_REF.get().multiChain.get(market);
    }

    public static Currency currency(ApexSupportedMarket market, String currencyId) {
        if (!isLoaded) {
            load();
        }

        Currency currency = HOLDER_REF.get().currencyMap.get(market).get(currencyId);
        if (currency == null) {
            throw new ApexProApiException(RUNTIME_ERROR, "invalid currencyId: " + currencyId);
        }
        return currency;
    }

    public static ImmutableMap<String, Currency> currencyMap(ApexSupportedMarket market) {
        if (!isLoaded) {
            load();
        }

        return HOLDER_REF.get().currencyMap.get(market);
    }

    public static PerpetualContract perpetualContract(ApexSupportedMarket market, String symbol) {
        if (!isLoaded) {
            load();
        }

        PerpetualContract perpetualContract = HOLDER_REF.get().perpetualContractMap.get(market).get(symbol);
        if (perpetualContract == null) {
            throw new ApexProApiException(RUNTIME_ERROR, "invalid symbol: " + symbol);
        }
        return perpetualContract;
    }

    public static ImmutableMap<String, PerpetualContract> perpetualContractMap(ApexSupportedMarket market) {
        if (!isLoaded) {
            load();
        }

        return HOLDER_REF.get().perpetualContractMap.get(market);
    }

    public static ImmutableMap<Integer, PerpetualContract> crossSymbolIdToPerpetualContractMap(ApexSupportedMarket market) {
        if (!isLoaded) {
            load();
        }

        return HOLDER_REF.get().crossSymbolIdToPerpetualContractMap.get(market);
    }

    private static class Holder {
        private final ImmutableMap<ApexSupportedMarket, Global> global;
        private final ImmutableMap<ApexSupportedMarket, ImmutableMap<String, Currency>> currencyMap;
        private final ImmutableMap<ApexSupportedMarket, ImmutableMap<String, PerpetualContract>> perpetualContractMap;
        private final ImmutableMap<ApexSupportedMarket, ImmutableMap<Integer, PerpetualContract>> crossSymbolIdToPerpetualContractMap;
        private final ImmutableMap<ApexSupportedMarket, MultiChain> multiChain;

        Holder(ImmutableMap<ApexSupportedMarket, Global> global, ImmutableMap<ApexSupportedMarket, ImmutableMap<String, Currency>> currencyMap,
               ImmutableMap<ApexSupportedMarket, ImmutableMap<String, PerpetualContract>> perpetualContractMap,
               ImmutableMap<ApexSupportedMarket, ImmutableMap<Integer, PerpetualContract>> crossSymbolIdToPerpetualContractMap,
               ImmutableMap<ApexSupportedMarket, MultiChain> multiChain)
        {
            this.global = global;
            this.currencyMap = currencyMap;
            this.perpetualContractMap = perpetualContractMap;
            this.crossSymbolIdToPerpetualContractMap = crossSymbolIdToPerpetualContractMap;
            this.multiChain = multiChain;
        }
    }

    private static BigDecimal parseDecimal(String str, BigDecimal defaultValue) {
        return (str == null || str.equals("")) ? defaultValue : new BigDecimal(str);
    }
}
