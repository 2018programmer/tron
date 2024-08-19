package com.dx.common;


import java.math.BigDecimal;

public interface Constant {
    Integer SC_INTERNAL_SERVER_ERROR_500 = 500;
    Integer SC_OK_200 = 0;

    String SUCCESS ="success";

    interface BaseUrl {

        String V1_CHAIN="/v1/chain/";

        String CREATEADDRESS ="/createAddress";


        String CREATEADDRESSBYNUM ="/createAddressBynum";

        String GETNOWBLOCK ="/getnowblock";

        String GETBLOCKBYNUM ="/getblockbynum";

        String ESTIMATEENERGY ="/estimateenergy";

        BigDecimal trxfee =new BigDecimal("0.300");
        String GETTRANSACTIONINFO ="/gettransactioninfo";
        String TRANSFERCONTRACTCOINS ="/transferContractCoins";
        String TRANSFERBASECOINS ="/transferBaseCoins";

        String QUERYBASEBALANCE="/queryBaseBalance";
        String QUERYCONTRACTBALANCE="/queryContractBalance";

        String VERIFYADDRESS="/verifyAddress";
    }
    interface OrderUrl{
        String CREATEORDER ="/merchant/back/recharge/create";

        String GETPRICELIST="/wallet-config/price/list";
    }

    interface TradeUrl{
        String GETCURRENCYLIST="/open/list";
    }

    interface RedisKey{
        String HITCOUNTER = "HitCounter";
    }
}
