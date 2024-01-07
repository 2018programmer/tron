package com.dx.common;


import java.math.BigDecimal;

public interface Constant {
    Integer SC_INTERNAL_SERVER_ERROR_500 = 500;
    Integer SC_OK_200 = 0;

    String SUCCESS ="success";

    interface BaseUrl {

        String V1_CHAIN="/v1/chain/";

        String CREATEADDRESS ="/createAddress";

        String GETNOWBLOCK ="/getnowblock";

        String GETBLOCKBYNUM ="/getblockbynum";

        String ESTIMATEENERGY ="/estimateenergy";

        BigDecimal trxfee =new BigDecimal("0.268");
        String GETTRANSACTIONINFO ="/gettransactioninfo";
        String TRANSFERCONTRACTCOINS ="/transferContractCoins";
        String TRANSFERBASECOINS ="/transferBaseCoins";
    }
}
