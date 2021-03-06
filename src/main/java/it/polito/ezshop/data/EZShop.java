package it.polito.ezshop.data;

import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


public class EZShop implements EZShopInterface {

    public EzShopModel model;

    public EZShop(){
        model = new EzShopModel();
        model.loadEZShop();
    }

    @Override
    public void reset(){
        model.reset();
        model = new EzShopModel();
    }

    @Override
    public Integer createUser(String username, String password, String role) throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException {
        User user = model.createUser(username, password, role);
        return user == null? -1:user.getId();
    }

    @Override
    public boolean deleteUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        return this.model.deleteUserById(id);
    }

    @Override
    public List<User> getAllUsers() throws UnauthorizedException {
        return this.model.getUserList();
    }

    @Override
    public User getUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        return this.model.getUserById(id);
    }

    @Override
    public boolean updateUserRights(Integer id, String role) throws InvalidUserIdException, InvalidRoleException, UnauthorizedException {
        return model.updateUserRights(id, role);
    }

    @Override
    public User login(String username, String password) throws InvalidUsernameException, InvalidPasswordException {
        return this.model.login(username, password);
    }

    @Override
    public boolean logout() {
        return this.model.logout();
    }

    @Override
    public Integer createProductType(String description, String productCode, double pricePerUnit, String note) throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        ProductType p = model.createProduct(description,productCode,pricePerUnit,note);
        if(p==null)
            return -1;
        return p.getId();
    }

    @Override
    public boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote) throws InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        if(id == null || id <= 0)
            throw new InvalidProductIdException();
        if(newDescription == null || newDescription.equals(""))
            throw new InvalidProductDescriptionException();
        if(newCode == null || newCode.equals(""))
            throw new InvalidProductCodeException();
        if(newPrice <= 0)
            throw new InvalidPricePerUnitException();

        return model.updateProduct(id, newDescription, newCode, newPrice, newNote);
    }

    @Override
    public boolean deleteProductType(Integer id) throws InvalidProductIdException, UnauthorizedException {
        if(id == null || id <= 0)
            throw new InvalidProductIdException();
        return model.deleteProduct(id);
    }

    @Override
    public List<ProductType> getAllProductTypes() throws UnauthorizedException {
        return model.getAllProducts().stream().map((prod)->(ProductType)prod).collect(Collectors.toList());
    }

    @Override
    public ProductType getProductTypeByBarCode(String barCode) throws InvalidProductCodeException, UnauthorizedException {
        model.checkAuthorization(Roles.Administrator, Roles.ShopManager);
        return model.getProductByBarCode(barCode);
    }

    @Override
    public List<ProductType> getProductTypesByDescription(String description) throws UnauthorizedException {
        model.checkAuthorization(Roles.Administrator, Roles.ShopManager);
        if(description == null)
            description = "";
        String finalDescription = description;
        return model.getAllProducts().stream().filter((product)->product.getProductDescription().contains(finalDescription)).collect(Collectors.toList());
    }

    @Override
    public boolean updateQuantity(Integer productId, int toBeAdded) throws InvalidProductIdException, UnauthorizedException {
        model.checkAuthorization(Roles.Administrator, Roles.ShopManager);
        ProductTypeModel  productType = model.getProductById(productId);
        if(productType == null)
            return false;
        return productType.updateAvailableQuantity(toBeAdded);
    }

    @Override
    public boolean updatePosition(Integer productId, String newPos) throws InvalidProductIdException, InvalidLocationException, UnauthorizedException {
        return model.updateProductPosition(productId, newPos);
    }

    @Override
    public Integer issueOrder(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        return model.createOrder(productCode, quantity, pricePerUnit);
    }

    @Override
    public Integer payOrderFor(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        return model.payOrderFor(productCode,quantity,pricePerUnit);
    }

    @Override
    public boolean payOrder(Integer orderId) throws InvalidOrderIdException, UnauthorizedException {
        return model.payOrder(orderId);
    }

    @Override
    public boolean recordOrderArrival(Integer orderId) throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException {
        model.checkAuthorization(Roles.Administrator, Roles.ShopManager);
        return model.recordOrderArrival(orderId);
    }

    @Override
    public boolean recordOrderArrivalRFID(Integer orderId, String RFIDfrom) throws InvalidOrderIdException, UnauthorizedException, 
    InvalidLocationException, InvalidRFIDException {
        return model.recordOrderArrivalRFID(orderId, RFIDfrom);
    }
    @Override
    public List<Order> getAllOrders() throws UnauthorizedException {
        return this.model.getOrderList();
    }

    @Override
    public Integer defineCustomer(String customerName) throws InvalidCustomerNameException, UnauthorizedException {
        return this.model.createCustomer(customerName);
    }

    @Override
    public boolean modifyCustomer(Integer id, String newCustomerName, String newCustomerCard) throws InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException, UnauthorizedException {
        return this.model.modifyCustomer(id, newCustomerName, newCustomerCard);
    }

    @Override
    public boolean deleteCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        return this.model.deleteCustomer(id);
    }

    @Override
    public Customer getCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        return this.model.getCustomerById(id);
    }

    @Override
    public List<Customer> getAllCustomers() throws UnauthorizedException {
        return this.model.getAllCustomer();
    }

    @Override
    public String createCard() throws UnauthorizedException {
        return this.model.createCard();
    }

    @Override
    public boolean attachCardToCustomer(String customerCard, Integer customerId) throws InvalidCustomerIdException, InvalidCustomerCardException, UnauthorizedException {
        return this.model.attachCardToCustomer(customerCard, customerId);
    }

    @Override
    public boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded) throws InvalidCustomerCardException, UnauthorizedException {
        return this.model.modifyPointsOnCard(customerCard, pointsToBeAdded);
    }

    @Override
    public Integer startSaleTransaction() throws UnauthorizedException {
        model.checkAuthorization(Roles.ShopManager, Roles.Administrator, Roles.Cashier);
        return model.startSaleTransaction();
    }

    @Override
    public boolean addProductToSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        model.checkAuthorization(Roles.ShopManager, Roles.Administrator, Roles.Cashier);
        return model.addProductToSale(transactionId, productCode, amount);
    }

    @Override
    public boolean addProductToSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException, UnauthorizedException{
        model.checkAuthorization(Roles.ShopManager, Roles.Administrator, Roles.Cashier);
        return model.addProductToSaleRFID(transactionId, RFID);
    }
    
    @Override
    public boolean deleteProductFromSale(Integer transactionId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        model.checkAuthorization(Roles.ShopManager, Roles.Administrator, Roles.Cashier);
        return model.deleteProductFromSale(transactionId, productCode, amount);
    }

    @Override
    public boolean deleteProductFromSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, InvalidQuantityException, UnauthorizedException{
        return model.deleteProductFromSaleRFID(transactionId, RFID);
    }

    @Override
    public boolean applyDiscountRateToProduct(Integer transactionId, String productCode, double discountRate) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, UnauthorizedException {
        model.checkAuthorization(Roles.ShopManager, Roles.Administrator, Roles.Cashier);
        return model.applyDiscountRateToProduct(transactionId, productCode, discountRate);
    }

    @Override
    public boolean applyDiscountRateToSale(Integer transactionId, double discountRate) throws InvalidTransactionIdException, InvalidDiscountRateException, UnauthorizedException {
        model.checkAuthorization(Roles.ShopManager, Roles.Administrator, Roles.Cashier);
        return model.applyDiscountRateToSale(transactionId, discountRate);
    }

    @Override
    public int computePointsForSale(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        model.checkAuthorization(Roles.ShopManager, Roles.Administrator, Roles.Cashier);
        return model.computePointsForSale(transactionId);
    }

    @Override
    public boolean endSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        model.checkAuthorization(Roles.Cashier, Roles.Administrator, Roles.ShopManager);
        return model.endSaleTransaction(transactionId);
    }

    @Override
    public boolean deleteSaleTransaction(Integer saleNumber) throws InvalidTransactionIdException, UnauthorizedException {
        model.checkAuthorization(Roles.Cashier, Roles.Administrator, Roles.ShopManager);
        return model.deleteSaleTransaction(saleNumber);
    }

    @Override
    public SaleTransaction getSaleTransaction(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {
        model.checkAuthorization(Roles.Administrator, Roles.Cashier, Roles.ShopManager);
        if(transactionId == null || transactionId<=0)
            throw new InvalidTransactionIdException();
        return model.getBalance().getSaleTransactionById(transactionId);
    }

    @Override
    public Integer startReturnTransaction(Integer saleNumber) throws /*InvalidTicketNumberException,*/InvalidTransactionIdException, UnauthorizedException {
        model.checkAuthorization(Roles.ShopManager, Roles.Administrator, Roles.Cashier);
        return model.startReturnTransaction(saleNumber);
    }

    @Override
    public boolean returnProduct(Integer returnId, String productCode, int amount) throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        model.checkAuthorization(Roles.Cashier, Roles.Administrator, Roles.ShopManager);
        return model.returnProduct(returnId, productCode, amount);
    }

    @Override
    public boolean returnProductRFID(Integer returnId, String RFID) throws InvalidTransactionIdException, InvalidRFIDException, UnauthorizedException 
    {
        model.checkAuthorization(Roles.Cashier, Roles.Administrator, Roles.ShopManager);
        return model.returnProductRFID(returnId, RFID);
    }


    @Override
    public boolean endReturnTransaction(Integer returnId, boolean commit) throws InvalidTransactionIdException, UnauthorizedException {
        model.checkAuthorization(Roles.Cashier, Roles.Administrator, Roles.ShopManager);
        return model.endReturnTransaction(returnId, commit);
    }

    @Override
    public boolean deleteReturnTransaction(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
        model.checkAuthorization(Roles.Cashier, Roles.Administrator, Roles.ShopManager);
        return model.deleteReturnTransaction(returnId);
    }

    @Override
    public double receiveCashPayment(Integer ticketNumber, double cash) throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException {
        return model.receiveCashPayment(ticketNumber, cash);
    }

    @Override
    public boolean receiveCreditCardPayment(Integer ticketNumber, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
        return model.receiveCreditCardPayment(ticketNumber, creditCard);
    }

    @Override
    public double returnCashPayment(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {
        return model.returnCashPayment(returnId);
    }

    @Override
    public double returnCreditCardPayment(Integer returnId, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
        return model.returnCreditCardPayment(returnId, creditCard);
    }

    @Override
    public boolean recordBalanceUpdate(double toBeAdded) throws UnauthorizedException {
        return model.recordBalanceUpdate(toBeAdded);
    }

    @Override
    public List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to) throws UnauthorizedException {
        return model.getCreditsAndDebits(from, to);
    }

    @Override
    public double computeBalance() throws UnauthorizedException {
        return model.computeBalance();
    }

}
