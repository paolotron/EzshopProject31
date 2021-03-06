# Unit Testing Documentation

Authors: Paolo Rabino, Manuel Messina, Andrea Sindoni, Omar Gai

Date: 19/05/2021

Version: 1.0

# Contents

- [Black Box Unit Tests](#black-box-unit-tests)
- [White Box Unit Tests](#white-box-unit-tests)

### Notes

Unit tests about getters and setters where not done as no logic is behind these methods.
All validation checks are done on the callers of the getters and setters and not on the methods themselves.

# Black Box Unit Tests


 ### **Class *UserModel* - method *Constructor***



**Criteria for method *Constructor*:**
 - Role is valid





**Predicates for method *Constructor*:**

| Criteria | Predicate |
| -------- | --------- |
|     Role  is valid   |  "Shop Manager"         |
|          |       "Administrator"    |
|          |       "Cashier"    |
|          |       Any other string    |




**Combination of predicates**:


| Role is valid | Valid / Invalid | Description of the test case | JUnit test case |
|-------|-------|-------|-------|
|"Cashier"|valid|Constructor("Paolo", "Password", "Cashier")|UserModelTest/TestRole|
|"Administrator"|valid|Constructor("Paolo", "Password", "Administrator")|UserModelTest/TestRole|
|"Shop Manager"|valid|Constructor("Paolo", "Password", "ShopManager")|UserModelTest/TestRole|
|"sopManag"|invalid|Constructor("Paolo", "Password", "SoopManag")|UserModelTest/TestRole|



### **Class *UserModel* - method *checkPassword***



**Criteria for method *checkPassword*:**


- Password 





**Predicates for method *checkPassword*:**

| Criteria | Predicate |
| -------- | --------- |
|     Password is empty     |   null        |
|          |      ""     |
|    |     "Previously set Password"      |
|          |     anything else      |





**Combination of predicates**:


| Password is the one set | Valid / Invalid | Description of the test case | JUnit test case |
|-------|-------|-------|-------|
|""|Invalid|checkPassword("")|UserModelTest/testInvalidPassword|
|null|Invalid|checkPassword(null)|UserModelTest/testInvalidPassword|
|"Previously set password"|Valid|User.setPassword("Password")<br />checkPassword("Password")->True|UserModelTest/testValidPassword|
|"Anything else"|Valid|User.setPassword("Password")<br />checkPassword("else")->False|UserModelTest/testValidPassword|





### **Class *CreditCardPayment* - method *validateCardWithLuhn***



**Criteria for method *validateCardWithLuhn*:**

- CreditCardNumber

**Predicates for method *validateCardWithLuhn*:**

| Criteria | Predicate |
| -------- | --------- |
|     CreditCardNumber     |      Luhn digit is valid     |
|          |       Luhn digist is not valid    |
|         |     Number is empty

**Combination of predicates**:


| CreditCardNumber | Valid / Invalid / Empty | Description of the test case | JUnit test case |
|-------|-------|-------|-------|
|Luhn digit is valid| valid |(5265807692)->True|CreditCardPaymenTest/testCorrectLuhn|
|Luhn digit is valid| valid |(6214838176)->True|CreditCardPaymenTest/testCorrectLuhn|
|Luhn digit is not valid| valid|(6234838176)->False|CreditCardPaymenTest/testWrongLuhn|
|Luhn digit is not valid| valid|(51658026)->False|CreditCardPaymenTest/testWrongLuhn|
|Luhn digit is not valid| valid|(ABC)->False|CreditCardPaymenTest/testWrongLuhn|

### **Class *CreditCardPayment* - method *sendPaymentRequestThroughAPI***
**Criteria for method *sendPaymentRequestThroughAPI*:**
- CardNumber
- Amount
- Balance

**Predicates for method *sendPaymentRequestThroughAPI*:**

| Criteria | Predicate |
| -------- | --------- |
|     CardNumber     |     CardNumber is invalid      |
|          |      CardNumber is registered      |
|    Amount      |     Amount > Credit card balance      |
|          |      Amount <= Credit card balance     |





**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
|     Amount     |        -999, credit card balance         |
|          |          credit card balance, 999       |



**Combination of predicates**:


| CardNumber | Amount | CreditCardBalance | Valid / Invalid | Description of the test case | JUnit test case |
|-------|-------|-------|-------|-------|-------|
|""|*|*|invalid|("")->InvalidCreditCardException|CreditCardPaymentTest/testInvalidPaymentWithAPI|
|null|*|*|invalid|(null)->InvalidCreditCardException|CreditCardPaymentTest/testInvalidPaymentWithAPI|
|invalid|*|*|invalid|(1234)->InvalidCreditCardException|CreditCardPaymentTest/testInvalidPaymentWithAPI|
|5265807692|20|30|valid|setAmount(20)</br>writeToFile(30)</br>(5265807692)->true|CreditCardPaymentTest/testCorrectPaymentWithAPI|
|5265807692|20|10|valid|setAmount(20)</br>writeToFile(10)</br>(5265807692)->false|CreditCardPaymentTest/testFailPaymentWithAPI|
|6214838176|20|/|valid|setAmount(20)</br>(6214838176)->false|CreditCardPaymentTest/testFailPaymentWithAPI|

### **Class *TicketEntryModel* - method *ComputeCost***
**Criteria for method *ComputeCost*:**

- Amount
- DiscountRate
- CostPerUnit

**Predicates for method *ComputeCost*:**

| Criteria | Predicate |
| -------- | --------- |
|    SignOfAmount      |    [0, inf)       |
|          |       (-inf, 0)    |
|   DiscountRate       |     \[0, 1\]      |
|    SignOfConstPerUnit      |     [0, inf)      |
|          |     (-inf, 0)      |







**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
|     SignOfAmount     |       -1, 0, 1          |
|   DiscountRate       |       0, 1          |
|   SignOfCostPerUnit       |       0, 1          |



**Combination of predicates**:


| SignOfAmount | DiscountRate | SignOfCostPerUnit | Valid / Invalid | Description of the test case | JUnit test case |
|-------|-------|-------|-------|-------|-------|
|(-minint,0)|*|*|valid|(-10, 0, 2)->0|TicketModelTest/testEntryModelTotalZero|
|*|\[0, 1\]|(-minint, 0)|valid|(10, 0, -2)->0|TicketModelTest/testEntryModelTotalZero||
|[0,maxint)|\[0,1\]|[0, maxint)|valid|(10, 0, 2)->20|TicketModelTest/testEntryModelTotal|
|[0,maxint)|\[0,1\]|[0, maxint)|valid|(10, 0.5, 2)->10|TicketModelTest/testEntryModelTotalDiscount|

### **Class *TicketEntry* - method *AddQuantity***

**Criteria for method *addQuantity*:**

- Sign of QuantityToAdd
- Amount

**Predicates for method *name*:**

| Criteria | Predicate |
| -------- | --------- |
|     Sign Of QuantityToAdd     |     (-inf, 0)      |
|          |      [0, inf)     |
| Amount | (-inf, inf)|
**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
|  Sign Of QuantityToAdd        |      -1, 0 ,1           |



**Combination of predicates**:


| Sign Of QuantityToAdd | Amount | Valid / Invalid | Description of the test case | JUnit test case |
|-------|-------|-------|-------|------|
|-1|*|valid|(-10,100)->false|testEntryModelAddQuantity|
|1|*|valid|(2,10)->true</br>getAmount()->12|testEntryModelAddQuantity|

### **Class *TicketEntry* - method *RemoveQuantity***

**Criteria for method *RemoveQuantity*:**

- Sign of QuantityToAdd
- Amount
- Sign of TotalQuantity (Amount-QuantityToAdd)

**Predicates for method *name*:**

| Criteria | Predicate |
| -------- | --------- |
|     Sign Of QuantityToAdd     |     (-inf, 0)      |
|          |      [0, inf)     |
| Amount | (-inf, inf)|
| Sign Of TotalAmount |     (-inf, 0)      |
|          |      [0, inf)     |
**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
|  Sign Of QuantityToAdd        |      -1, 0 ,1           |
|  Sign Of TotalAmount        |      -1, 0 ,1           |


**Combination of predicates**:


| Sign Of QuantityToRemove | Amount | Sign of Total Amount | Valid / Invalid | Description of the test case | JUnit test case |
|-------|-------|-------|-------|------|----|
|-10|*|*|valid|(-10,100)->false|testEntryModelRemoveQuantity|
|2|8|10|valid|(2,10)->true</br>getAmount()->8|testEntryModelRemoveQuantity|
|10|8|-2|valid|(8,10)->false|testEntryModelRemoveQuantity|

### **Class *LoyaltyCardModel* - method *updatePoints***



**Criteria for method *name*:updatePoints**


- total points (StoredPoints + PointsUpdate)


**Predicates for method *name*:updatePoints**

| Criteria | Predicate |
| -------- | --------- |
|    total points               |  (minint, 0)  |
|                               |  \[0, maxint] |



**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
|     total points       |   minint, 0, maxint |


**Combination of predicates**:


 total points  | Valid / Invalid | Description of the test case | JUnit test case |
|-------|-------|-------|-------|
|  (minint 0) | valid | T1(40, -100) -> false<br/> Tb1(maxint, maxint) -> false |  LoyaltyCardModelTest/addPointsTest |
|  \[0, maxint) | valid  | T1(40, 20) -> true  |  LoyaltyCardModelTest/addPointsTest  |


### **Class *ProductTypeModel* - method *checkBarCodeWithAlgorithm***



**Criteria for method *checkBarCodeWithAlgorithm*:**


-String BarCode





**Predicates for method *checkBarCodeWithAlgorithm*:**

| Criteria | Predicate |
| -------- | --------- |
| BarCode     | BarCode is valid          |
|          | BarCode is not valid          |
|          | BarCode is empty         |
|          |           |





**Combination of predicates**:


| BarCode | Valid / Invalid / Empty | Description of the test case | JUnit test case |
|-------|-------|-------|-------|
|BarCode is valid| valid |(6291041500213)->True|testCorrectBarcodeAlgorithm|
|BarCode is valid| valid |(47845126544844)->True|testCorrectBarcodeAlgorithm|
|BarCode is valid| valid |(989661725630)->True|testCorrectBarcodeAlgorithm|
|BarCode is not valid|  valid|(6291041500211)->False|testWrongBarcodeAlgorithm|
|BarCode is not valid| valid|(1242)->False|testWrongBarcodeAlgorithm|
|BarCode is not valid| valid|("ABCD")->False|testWrongBarcodeAlgorithm|
|BarCode is not valid| valid|("!*){")->False|testWrongBarcodeAlgorithm|
|BarCode is empty| valid |(null)->False|testWrongBarcodeAlgorithm|

### **Class *ProductTypeModel* - method *updateAvailableQuantity***



**Criteria for method *updateAvailableQuantity*:**


- Integer quantityToAdd
- location
- Integer totalQuantity




**Predicates for method *name*:**

| Criteria | Predicate |
| -------- | --------- |
|     QuantityToAdd     |    QuantityToAdd is valid       |
|          |     QuantityToAdd is empty      |
|    Location      |     Location is valid      |
|          |     Location is empty      |
|     TotalQuantity     |     TotalQuantity is valid      |
|          |     TotalQuantity is not valid      |





**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
|     QuantityToAdd     |       (minInt, maxInt)          |
|     TotalQuantity     |       (0, maxInt)          |


**Combination of predicates**:


| quantityToAdd | location | totalQuantity | Valid / Invalid / Empty | Description of the test case | JUnit test case |
|-------|-------|-------|-------|-------|-------|
|quantityToAdd is valid| location is valid | totalQuantity is valid| valid ||testUpdateAvailableQuantity|
|quantityToAdd is empty| location is valid | totalQuantity is valid| valid |updateAvailableQuantity(null)|testUpdateAvailableQuantity|
|quantityToAdd is valid| location is empty | totalQuantity is valid| valid |updateAvailableQuantity(quantityToAdd)|testUpdateAvailableQuantity|
|quantityToAdd is valid| location is valid | totalQuantity is not valid| valid |updateAvailableQuantity(quantityToAdd)|testUpdateAvailableQuantity|
### **Class *CashPayment* - method *computeChange***



**Criteria for method *computeChange*:**


- Sign of Cash

- Sign of Amount



**Predicates for method *computeChange*:**

| Criteria | Predicate |
| -------- | --------- |
|    Sign of Cash     |    (0, maxValue)      |
|          |    (-inf, 0]       |
|    Sign of Amount   |    (0, maxValue)        |
|          |    (-inf, 0]      |






**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
|    Sign of Cash      |       (0, maxValue)          |
|    Sign of Amount    |       (0, maxValue)          |



**Combination of predicates**:


| Sign of Cash | Sign of Amount | Valid / Invalid | Description of the test case | JUnit test case |
|--------------|----------------|-----------------|------------------------------|-----------------|
|+330.25       |+230.10         |valid            |T1(+330.25, +230.10) -> +100.15|CashPaymentTest/cashP.computeChange()                 |
|-10.70        |+10.30          |not valid        |T2(-10.70, +10.30) -> -1    | CashPaymentTest/cashP.computeChange()                |
|+30.20        |-10.50          |not valid        |T3(+30.25, -10.50) -> -1    | CashPaymentTest/cashP.computeChange()                |
<!---
### **Class *class_name* - method *name***



**Criteria for method *name*:**


-
-





**Predicates for method *name*:**

| Criteria | Predicate |
| -------- | --------- |
|          |           |
|          |           |
|          |           |
|          |           |





**Boundaries**:

| Criteria | Boundary values |
| -------- | --------------- |
|          |                 |
|          |                 |



**Combination of predicates**:


| Criteria 1 | Criteria 2 | ... | Valid / Invalid | Description of the test case | JUnit test case |
|-------|-------|-------|-------|-------|-------|
|||||||
|||||||
|||||||
|||||||
|||||||
### **Class *class_name* - method *name***

--->
<!---

# White Box Unit Tests

### Test cases definition

    <JUnit test classes must be in src/test/java/it/polito/ezshop>
    <Report here all the created JUnit test cases, and the units/classes under test >
    <For traceability write the class and method name that contains the test case>


| Unit name | JUnit test case |
|--|--|
|||
|||
||||

### Code coverage report

    <Add here the screenshot report of the statement and branch coverage obtained using
    the Eclemma tool. >


### Loop coverage analysis

    <Identify significant loops in the units and reports the test cases
    developed to cover zero, one or multiple iterations >

|Unit name | Loop rows | Number of iterations | JUnit test case |
|---|---|---|---|
|||||
|||||
||||||

-->
# White Box Unit Tests

### Test cases definition


| Unit name | JUnit test case                |
| --------- | ------------------------------ |
| creditCardPayment  | UnitTest/PaymentTest |

### Code coverage report


![img.png](DocumentationPngs/img.png)

### Loop coverage analysis



| Unit name | Loop rows | Number of iterations | JUnit test case               |
| --------- | --------- | -------------------- | ----------------------------- |
| sendPaymentThroughAPI  | 7-8       | 0                    | UnitTest/PaymentTest/testLoopCoverage() \[T7] |
|           |           | 1                    | UnitTest/PaymentTest/testLoopCoverage() \[T8\] |
|           |           | 2+                   | UnitTest/PaymentTest/testLoopCoverage() \[T1\]|
| sendPaymentThroughAPI  | 9-21       | 0                    | UnitTest/PaymentTest/testLoopCoverage() \[T7\] |
|           |           | 1                    | UnitTest/PaymentTest/testLoopCoverage() \[T8\] |
|           |           | 2+                   | UnitTest/PaymentTest/testLoopCoverage() \[T1\]|

