package com.dx.common;


public interface Constant {
    Integer SC_INTERNAL_SERVER_ERROR_500 = 500;
    Integer SC_OK_200 = 200;

    String SUCCESS ="success";

    interface BaseUrl {

        String V1_CHAIN="/v1/chain/";

        String CREATEADDRESS ="/createAddress";
    }
}
