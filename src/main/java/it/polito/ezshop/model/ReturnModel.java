package it.polito.ezshop.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReturnModel {
    Integer id;
    SaleTransactionModel sale;
    Integer saleId;
    String status;
    ArrayList<TicketEntryModel> productList;
    HashMap<Integer, String> RfidMap;
    double returnedAmount;
    static Integer currentId = 0;

    public ReturnModel(Integer saleId, SaleTransactionModel sale){
        this.sale = sale;
        this.saleId = saleId;
        id = ++currentId;
        status = "open";
        productList = new ArrayList<>();
        RfidMap = new HashMap<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<TicketEntryModel> getProductList() {
        return productList;
    }

    public void setProductList(ArrayList<TicketEntryModel> productList) {
        this.productList = productList;
    }

    public double setPayment(){
        return -1;
    }

    public double getReturnedAmount() {
        return returnedAmount;
    }

    public void setReturnedAmount(double returnedAmount) {
        this.returnedAmount = returnedAmount;
    }

    public SaleTransactionModel getSale() {
        return sale;
    }

    public void setSale(SaleTransactionModel sale) {
        this.sale = sale;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    public HashMap<Integer, String> getRfidMap() {
        return RfidMap;
    }

    public void setRfidMap(HashMap<Integer, String> rfidMap) {
        RfidMap = rfidMap;
    }

    public void commit(Map<String, ProductTypeModel> productMap){
        this.status = "closed";
        for (TicketEntryModel entry : productList) {
            productMap.get(entry.getBarCode()).updateAvailableQuantity(entry.getAmount());
        }
        for (TicketEntryModel entry : productList) {
            for (TicketEntryModel saleEntry : sale.getTicket().getTicketEntryModelList()) {
                if (saleEntry.getBarCode().equals(entry.getBarCode())) {
                    saleEntry.removeAmount(entry.getAmount());
/*                    if(saleEntry.getAmount() == 0)
                        sale.getTicket().getTicketEntryModelList().remove(saleEntry);*/
                    break;
                }
            }
        }
        returnedAmount = sale.updateAmount();
    }
}
