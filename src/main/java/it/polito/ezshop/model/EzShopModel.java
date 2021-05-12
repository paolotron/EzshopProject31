package it.polito.ezshop.model;

import it.polito.ezshop.data.BalanceOperation;
import it.polito.ezshop.data.User;
import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.data.*;

import javax.management.relation.Role;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class EzShopModel {

    final static String folder = "persistent";
    final static boolean persistent = false;
    List<UserModel> UserList;
    Map<Integer, LoyaltyCardModel> LoyaltyCardMap;
    Map<Integer, CustomerModel> CustomerMap;
    UserModel CurrentlyLoggedUser;
    Map<String, ProductTypeModel> ProductMap;  //K = productCode (barCode), V = ProductType
    Map<Integer, OrderModel> ActiveOrderMap;         //K = OrderId, V = Order
    BalanceModel balance;
    JsonWrite writer;
    JsonRead reader;
    int maxProductId;
    int maxCardId;

    public EzShopModel() {
        UserList = new ArrayList<>();
        CustomerMap = new HashMap<>();
        LoyaltyCardMap = new HashMap<>();
        CurrentlyLoggedUser = null;
        ProductMap = new HashMap<>();
        ActiveOrderMap = new HashMap<>();
        balance = new BalanceModel();
        try {
            writer = new JsonWrite(folder);
            reader = new JsonRead(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(persistent) {
            UserList = reader.parseUsers();
            CustomerMap = reader.parseCustomers().stream().collect(Collectors.toMap(CustomerModel::getId, (c) -> c));
            ProductMap = reader.parseProductType().stream().collect(Collectors.toMap(ProductType::getBarCode, (cust) -> cust));
            balance = reader.parseBalance();
            ActiveOrderMap = reader.parseOrders().stream().collect(Collectors.toMap(OrderModel::getOrderId, (ord) -> ord));
            maxProductId = ProductMap.values().stream().map(ProductTypeModel::getId).max(Integer::compare).orElse(1);
            maxCardId = LoyaltyCardMap.keySet().stream().max(Integer::compare).orElse(1);
        }
    }

    public boolean reset(){
        return writer.reset();
    }

    public User getUserById(Integer Id) throws UnauthorizedException, InvalidUserIdException {
        checkAuthorization(Roles.Administrator);
        if (Id == null || Id == 0)
            throw new InvalidUserIdException("User Not Found");
        return UserList.stream().filter((us) -> us.getId().equals(Id)).findAny().orElse(null);
    }

    public List<User> getUserList() throws UnauthorizedException {
        checkAuthorization(Roles.Administrator);
        return new ArrayList<>(this.UserList);
    }

    public boolean deleteUserById(Integer id) throws UnauthorizedException, InvalidUserIdException {
        int ix = UserList.indexOf((UserModel) getUserById(id));
        if (ix == -1)
            return false;
        UserList.remove(ix);
        writer.writeUsers(this.UserList);
        return true;
    }


    /**
     * Made by PAOLO
     *
     * @param Username username string
     * @param Password password string
     * @return User class or null if user not found or the password was not correct
     * @throws InvalidPasswordException if password is empty or null
     * @throws InvalidUsernameException if username is empty or null
     */
    public User login(String Username, String Password) throws InvalidPasswordException, InvalidUsernameException {
        UserModel newloggedUser;
        if (Username == null || Username.equals(""))
            throw new InvalidUsernameException("Username is null or empty");
        if (Password == null || Password.equals(""))
            throw new InvalidPasswordException("Password is null or empty");
        Optional<UserModel> userfound = this.UserList.stream().filter((user) -> user.getUsername().equals(Username)).findFirst();
        if (!userfound.isPresent())
            return null;
        else
            newloggedUser = userfound.get().checkPassword(Password) ? userfound.get() : null;
        if (newloggedUser != null)
            this.CurrentlyLoggedUser = newloggedUser;
        return newloggedUser;
    }

    public boolean logout() {
        if (this.CurrentlyLoggedUser == null)
            return false;
        this.CurrentlyLoggedUser = null;
        return true;
    }

    /**
     * Made by PAOLO
     *
     * @param username: String for Username, must be unique or null is returned
     * @param password: String for password
     * @param role:     String for role
     * @return UserModel class
     * @throws InvalidRoleException     null or empty string
     * @throws InvalidUsernameException null or empty string
     * @throws InvalidPasswordException null or empty string
     */
    public User createUser(String username, String password, String role) throws InvalidRoleException, InvalidUsernameException, InvalidPasswordException {

        if (role == null || role.equals(""))
            throw new InvalidRoleException("Role is empty or null");

        if (username == null || username.equals(""))
            throw new InvalidUsernameException("Username is empty or null");

        if (password == null || password.equals(""))
            throw new InvalidPasswordException("Password is empty or null");

        if (UserList.stream().anyMatch((user) -> (user.getUsername().equals(username))))
            return null;

        UserModel newUser = new UserModel(username, password, role);
        this.UserList.add(newUser);
        writer.writeUsers(UserList);
        return newUser;

    }

    public BalanceModel getBalance() {
        return this.balance;
    }


    /**
     * Made by OMAR
     *
     * @param productCode:  String , the code of the product that we should order as soon as possible
     * @param quantity:     int, the quantity of product that we should order
     * @param pricePerUnit: double, the price to correspond to the supplier
     * @return Integer, OrderID of the new Order, -1 if the ProductType doesn't exist
     */
    public Integer createOrder(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {

        if (productCode == null || productCode.equals("")) {
            throw new InvalidProductCodeException("Product Code is null or empty");
        }

        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than zero");
        }
        if (pricePerUnit <= 0) {
            throw new InvalidPricePerUnitException("Price per Unit must be greater than zero");
        }

        this.checkAuthorization(Roles.ShopManager, Roles.Administrator);

        if (this.ProductMap.get(productCode) == null) { //ProductType with productCode doesn't exist
            return -1;
        }

        OrderModel newOrder = new OrderModel(productCode, quantity, pricePerUnit);
        newOrder.setStatus("ISSUED");
        this.ActiveOrderMap.put(newOrder.getOrderId(), newOrder);
        writer.writeOrders(ActiveOrderMap);
        return newOrder.getOrderId();
    }

    /**
     * Made by OMAR
     *
     * @param productCode:  String, code of the product to order
     * @param quantity:     int, product's quantity to order
     * @param pricePerUnit: double, single price of each product
     * @return Integer: the id of the order (> 0)
     * -1 if the product does not exists, if the balance is not enough to satisfy the order
     */
    public Integer payOrderFor(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {
        boolean result;
        BalanceModel bal = getBalance();

        if (productCode == null || productCode.equals("")) {
            throw new InvalidProductCodeException("Product Code is null or empty");
        }
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity must be greater than zero");
        }
        if (pricePerUnit <= 0) {
            throw new InvalidPricePerUnitException("Price per Unit must be greater than zero");
        }
        checkAuthorization(Roles.Administrator, Roles.ShopManager);
        if (this.ProductMap.get(productCode) == null) { //ProductType with productCode doesn't exist
            return -1;
        }

        OrderModel newOrder = new OrderModel(productCode, quantity, pricePerUnit);

        result = bal.checkAvailability(newOrder.getTotalPrice());
        if (result) {  //if it's possible to do this Order then...
            result = this.recordBalanceUpdate(newOrder.getTotalPrice());
            if (result) {   //if the balanceUpdate is successfull then...
                newOrder.setStatus("PAYED");
                OrderTransactionModel orderTransactionModel = new OrderTransactionModel(newOrder, newOrder.getDate());
                bal.addBalanceOperation(orderTransactionModel);
                bal.addOrderTransaction(orderTransactionModel);
                this.ActiveOrderMap.put(newOrder.getOrderId(), newOrder);
                result=writer.writeOrders(ActiveOrderMap);
                if(!result) return -1;  //problem with db
                result=writer.writeBalance(bal);
                return newOrder.getOrderId();
            }
        }

        return -1;
    }

    /**
     * Made by OMAR
     *
     * @param orderId: Integer, id of the order to be ORDERED
     * @return boolean: true if success, else false
     */
    public boolean payOrder(Integer orderId) throws InvalidOrderIdException, UnauthorizedException {
        boolean result = false;
        if (orderId == null || orderId <= 0) {
            throw new InvalidOrderIdException("orderId is not valid");
        }

        checkAuthorization(Roles.Administrator, Roles.ShopManager);

        BalanceModel bal = this.getBalance();
        OrderModel ord = this.ActiveOrderMap.get(orderId);
        OrderTransactionModel orderTransactionModel;

        if (ord == null) {        //The order doesn't exist
            return false;
        }
        if (ord.getStatus().equals("PAYED")) { //NO EFFECT
            result = true;
        } else if (ord.getStatus().equals("ISSUED")) {
            result = bal.checkAvailability(ord.getTotalPrice());
            if (result) {   //if it's possible to do this Order then...
                result = this.recordBalanceUpdate(ord.getTotalPrice());
                if (result) { //if the balanceUpdate is successfull then...
                    ord.setStatus("PAYED");
                    orderTransactionModel = new OrderTransactionModel(ord, ord.getDate());
                    bal.addOrderTransaction(orderTransactionModel);
                    bal.addBalanceOperation(orderTransactionModel);
                    result = writer.writeOrders(ActiveOrderMap);
                    result = writer.writeBalance(bal);
                }
            }
        }

        return result;
    }

    /**
     * Made by Omar
     *
     * @param orderId the id of the order that has arrived
     * @return true if the operation was successful
     * false if the order does not exist or if it was not in an ORDERED/COMPLETED state
     */

    public boolean recordOrderArrival(Integer orderId) throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException {
        boolean result = false;
        OrderModel ord;
        ProductTypeModel product;
        int quantity;

        if (orderId == null || orderId <= 0) {
            throw new InvalidOrderIdException("orderId not valid");
        }
        ord = this.ActiveOrderMap.get(orderId);
        if (ord == null) {
            return false;
        }
        product = ProductMap.get(ord.getProductCode());
        if (product.getLocation() == null || product.getLocation().equals("")) {  //the product must have a location registered
            throw new InvalidLocationException("Product with invalid location");
        }
        checkAuthorization(Roles.ShopManager, Roles.Administrator);
        if (ord.getStatus().equals("COMPLETED")) {  //no effect
            return false;
        }
        if (ord.getStatus().equals("PAYED")) {
            ord.setStatus("COMPLETED");
            quantity = ord.getQuantity();
            product.updateAvailableQuantity(quantity);
            result = writer.writeOrders(ActiveOrderMap);
            if(!result) return false;  //problem with db
        }
        return result;
    }

    /**
     * Method for Checking the level of authorization of the user
     *
     * @param rs Role or multiple roles, variable number of arguments is supported
     * @throws UnauthorizedException thrown when CurrentlyLoggedUser is null or his role is not one authorized
     */
    private void checkAuthorization(Roles... rs) throws UnauthorizedException {
        if (this.CurrentlyLoggedUser == null)
            throw new UnauthorizedException("No logged user");
        if (Arrays.stream(rs).anyMatch((r) -> r == this.CurrentlyLoggedUser.getEnumRole()))
            return;
        throw new UnauthorizedException("User does not have right authorization");
    }

    /**
     * Made by Manuel
     *
     * @param toBeAdded the amount of money (positive or negative) to be added to the current balance. If this value
     *                  is >= 0 then it should be considered as a CREDIT, if it is < 0 as a DEBIT
     * @return true if the balance has been successfully updated
     * false if toBeAdded + currentBalance < 0.
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    public boolean recordBalanceUpdate(double toBeAdded) throws UnauthorizedException {
        this.checkAuthorization(Roles.Administrator, Roles.ShopManager);
        double balance = this.computeBalance();
        if (balance + toBeAdded >= 0) {
            String operationType = toBeAdded >= 0 ? "credit" : "debit";
            BalanceOperationModel balanceOP = new BalanceOperationModel(operationType, toBeAdded, LocalDate.now());
            this.balance.addBalanceOperation(balanceOP);
            return true;
        }
        return false;
    }

    /**
     * Made by Manuel
     *
     * @param from the start date : if null it means that there should be no constraint on the start date
     * @param to   the end date : if null it means that there should be no constraint on the end date
     * @return All the operations on the balance whose date is <= to and >= from
     */
    public List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to) throws UnauthorizedException {
        this.checkAuthorization(Roles.Administrator, Roles.ShopManager);
        return this.balance.getCreditsAndDebits(from, to);
    }

    /**
     * Made by Manuel
     *
     * @return the value of the current balance
     * @throws UnauthorizedException if there is no logged user or if it has not the rights to perform the operation
     */
    public double computeBalance() throws UnauthorizedException {
        this.checkAuthorization(Roles.Administrator, Roles.ShopManager);
        return this.balance.computeBalance();
    }

    /**
     * Made by Omar
     *
     * @return the list of all Orders, which any status
     */
    public List<Order> getOrderList() {
        return new ArrayList<>(ActiveOrderMap.values());
    }

    /**
     * Made by Andrea
     *
     * @return a CustomerId
     */

    public int createCustomer(String customerName) throws InvalidCustomerNameException, UnauthorizedException {
        this.checkAuthorization(Roles.Administrator); //check for other roles
        if (customerName.equals("") || !customerName.matches("[a-zA-Z]+"))
            throw new InvalidCustomerNameException();
        CustomerModel c = new CustomerModel(customerName);
        CustomerMap.put(c.getId(), c);
        writer.writeCustomers(new ArrayList<>(CustomerMap.values()));
        return c.getId();
    }

    /**
     * Made by Andrea
     *
     * @return a Customer given its id
     */

    public CustomerModel getCustomerById(int id) throws InvalidCustomerIdException, UnauthorizedException {
        this.checkAuthorization(Roles.Administrator); //check for other roles
        if (!CustomerMap.containsKey(id))
            throw new InvalidCustomerIdException();
        return CustomerMap.get(id);
    }

    /**
     * Made by Andrea
     *
     * @return the result of the operation
     */

    //TODO: add this function to the design model and InvalidCardException handling
    public boolean modifyCustomer(int id, String newCustomerName, String newCustomerCard) throws InvalidCustomerIdException, UnauthorizedException, InvalidCustomerNameException {
        CustomerModel c = this.getCustomerById(id);
        if (newCustomerName.equals("") || !newCustomerName.matches("[a-zA-Z]+"))
            throw new InvalidCustomerNameException();
        c.setCustomerName(newCustomerName);
        c.setCustomerCard(newCustomerCard);
        writer.writeCustomers(new ArrayList<>(CustomerMap.values()));
        return true;
    }

    /**
     * Made by Andrea
     *
     * @return the result of the operation
     */

    public boolean deleteCustomer(int id) throws InvalidCustomerIdException, UnauthorizedException {
        this.checkAuthorization(Roles.Administrator); //check for other roles
        if (!CustomerMap.containsKey(id))
            throw new InvalidCustomerIdException();

        this.CustomerMap.remove(id);
        writer.writeCustomers(new ArrayList<>(CustomerMap.values()));
        return true;
    }

    /**
     * Made by Andrea
     *
     * @return the list of customers
     */

    public List<Customer> getAllCustomer() throws UnauthorizedException {
        checkAuthorization(Roles.Administrator, Roles.ShopManager, Roles.Cashier);
        return new ArrayList<>(CustomerMap.values());
    }

    /**
     * Made by Andrea
     *
     * @return the loyalty card code
     */

    //TODO: add this function to the design model
    public String createCard() throws UnauthorizedException {
        this.checkAuthorization(Roles.Administrator); //check for other roles

        LoyaltyCardModel l = new LoyaltyCardModel((++maxCardId));
        LoyaltyCardMap.put(maxCardId, l);
        return String.valueOf(maxCardId);
    }

    /**
     * Made by Andrea
     *
     * @return the result of the operation
     */

    //TODO: add this function to the design model
    public boolean attachCardToCustomer(String customerCard, Integer userId) throws UnauthorizedException, InvalidCustomerIdException, InvalidCustomerCardException {
        this.checkAuthorization(Roles.Administrator); //check for other roles
        if (!CustomerMap.containsKey(userId))
            throw new InvalidCustomerIdException();
        if (!LoyaltyCardMap.containsKey(customerCard))
            throw new InvalidCustomerCardException();
        CustomerMap.get(userId).setCustomerCard(customerCard);
        return true;
    }

    /**
     * Made by Andrea
     *
     * @return the result of the operation
     */

    //TODO: add this function to the design model
    public boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded) throws InvalidCustomerCardException, UnauthorizedException {
        this.checkAuthorization(Roles.Administrator); //check for other roles
        if (!LoyaltyCardMap.containsKey(customerCard))
            throw new InvalidCustomerCardException();
        LoyaltyCardMap.get(customerCard).addPoints(pointsToBeAdded);
        return true;
    }

    public ProductType createProduct(String description, String productCode, double pricePerUnit, String Note) throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
           if(checkString(description))
               throw new InvalidProductDescriptionException();
           if(checkString(productCode) || ! checkBarCodeWithAlgorithm(productCode))
               throw new InvalidProductCodeException();
           if(checkDouble(pricePerUnit))
               throw new InvalidPricePerUnitException();
           checkAuthorization(Roles.Administrator, Roles.Cashier);
           ProductTypeModel product = new ProductTypeModel(++maxProductId,description, productCode, pricePerUnit, Note);
           this.ProductMap.put(product.getBarCode(), product);
           writer.writeProducts(ProductMap);
           return product;
    }

    /**
     * @param st BarCode
     * @return True if BarCode complies with https://www.gs1.org/services/how-calculate-check-digit-manually
     */
     public static boolean checkBarCodeWithAlgorithm(String st){
        if(st==null || !st.matches("^\\d{12,14}$"))
            return false;
        int tot = 0;
        for (int i = 0; i < st.length()-1; i++)
            tot+=Character.getNumericValue(st.charAt(i))*((st.length()-i)%2 == 0 ? 3:1);
        return Integer.toString(Math.round((float) tot / 10) * 10 - tot).charAt(0) == st.charAt(st.length()-1);
    }

    private static boolean checkString(String st){
        return st == null || st.equals("");
    }
    private static boolean checkDouble(double price){
        return price <= 0;
    }
    /**
     * Made by Omar
     * @param transactionId the number of the transaction that the customer wants to pay
     * @param cash the cash received by the cashier
     */
    public double receiveCashPayment(Integer transactionId, double cash) throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException{
        double change=0;
        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("transactionID not valid");
        }
        checkAuthorization(Roles.Administrator, Roles.ShopManager, Roles.Cashier);
        if(cash <= 0){
            throw new InvalidPaymentException("cash value not valid");
        }
        BalanceModel bal = this.getBalance();
        SaleTransactionModel transaction = bal.getSaleTransactionById(transactionId);
        if(transaction == null ) return -1; //sale doesn't exist
        Ticket ticket = transaction.getTicket();
        if(ticket == null ) return  -1; //ticket doesn't exist
        CashPayment cashPayment = new CashPayment(ticket.getAmount(),false,cash);
        ticket.setPayment(cashPayment);
        ticket.setStatus("PAYED");
        change = cashPayment.computeChange();
        if (change < 0){  //the cash is not enough
            return -1;
        }
        BalanceOperation balanceOperation = new BalanceOperationModel();
        if(!writer.writeBalance(bal)) return -1;  //problem with db
        return change;
    }

    /**
     * Made by Omar
     * @param transactionId the number of the transaction that the customer wants to pay
     * @param creditCard the credit card of the customer
     */
    public boolean receiveCreditCardPayment(Integer transactionId, String creditCard) throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException{
        double change=0;
        boolean outcome=false;
        if(creditCard == null || creditCard.equals("")){
            throw new InvalidCreditCardException("creditCard number empty or null");
        }
        outcome= validateCardWithLuhn(creditCard);
        if(!outcome) return false; //problem with card validity

        checkAuthorization(Roles.Administrator, Roles.ShopManager, Roles.Cashier);
        if(transactionId==null || transactionId <= 0){
            throw new InvalidTransactionIdException("transactionID not valid");
        }
        //TODO if() return false; //card is not registered
        BalanceModel bal = getBalance();
        SaleTransactionModel saleTransaction = bal.getSaleTransactionById(transactionId);
        if(saleTransaction == null ) return false; //sale doesn't exist
        Ticket ticket = saleTransaction.getTicket();
        if(ticket == null ) return  false; //ticket doesn't exist
        CreditCardPayment creditCardPayment = new CreditCardPayment(ticket.getAmount(),false);
        //TODO outcome=sendPaymentRequestThroughAPII();
        if(!outcome) return false;  //problem with payment (not enough money for example)
        ticket.setStatus("PAYED");
        outcome=writer.writeBalance(bal);
        if(!outcome) return false;  //problem with db

        return outcome;

    }

    //TODO method to be implemented
    public boolean validateCardWithLuhn(String cardNumber) throws InvalidCreditCardException{
        return true;
    }
}