package com.dx.futures.local;

import java.util.List;

public interface OnSales {
      void persistSale(List<Sale> sales) throws Exception;
}
