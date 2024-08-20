package com.dx.futures.local;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class Test implements Listener{

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager
            .getLogger("tradingEngineServlet");


    public   synchronized void event(final EventType type,
                                          final Object data) {
        switch (type) {
            case SALE: {
                Sale sale = (Sale) data;
                int id = sale.getSalesOrder().getId();
                 if (sale.getSalesOrder().getRemainingQuantity() == 0) {
                    String msg = "COMPLETED sales order";
                    LOGGER.info("\n" + id + ") " + msg + " " + data);
                } else {
                    LOGGER.info("\n" + id + ") PARTIAL sales order " + data);
                }
                break;
            }
            case PURCHASE: {
                Sale sale = (Sale) data;
                int id = sale.getPurchaseOrder().getId();
                 if (sale.getPurchaseOrder().getRemainingQuantity() == 0) {
                    String msg = "COMPLETED purchase order";
                    LOGGER.info("\n" + id + ") " + msg + " " + data);
                } else {
                    LOGGER.info("\n" + id + ") PARTIAL purchase order " + data);
                }
                break;
            }
            case TIMEOUT_SALESORDER: {
                 SalesOrder so = (SalesOrder) data;
                String msg = "TIMEOUT sales order";
                LOGGER.info("\n" + so.getId() + ") " + msg + " " + data);
                break;
            }
            case TIMEOUT_PURCHASEORDER: {
                 PurchaseOrder po = (PurchaseOrder) data;
                String msg = "TIMEOUT purchase order";
                LOGGER.info("\n" + po.getId() + ") " + msg + " " + data);
                break;
            }
            case STATS: {
                synchronized (knownProducts) {
                    Map<String, List<VolumeRecord>> mapOfVolumeRecords = (Map<String, List<VolumeRecord>>) ((Object[]) data)[2];
                    stats.totalSalesPerMinute = knownProducts
                            .stream()
                            .map(productId -> {
                                return VolumeRecord.aggregate(mapOfVolumeRecords
                                        .getOrDefault(productId,
                                                Collections.emptyList())).count;
                            }).reduce(Integer::sum).orElse(0) * 6; // since stats
                    // are
                    // recorded
                    // for the last
                    // 10 secs
                }
                break;
            }
            default:
                break;
        }
    }

    public static void main(String[] args) {

        TradingEngineThread engineThread = new TradingEngineThread(
                DELAY, TIMEOUT, (type, data) -> event(type, data));
        engineThread.start();

        PurchaseOrder po = engine.addPurchaseOrder(who, productId,
                quantity, id);
        SalesOrder so = engine.addSalesOrder(who, productId, quantity,
                price, id);
    }


}
