Spring Batch Project to solve 2 scenarios:

> Scenario 1 : simple-property-to-diff-table

```java
class PersonData {
    
  // Below property except for Address should be written to Person Table
	private String firstName;
	private String middleName;
	private String lastName;
	private String email;
	private String passportNumber;
  
  // Below should be written to ADDRESS table
  private Address address;
}

```

                      ------------- PERSON (table)
                      |
    PersonData -------|
                      |
                      |
                      ------------- ADDRESS (table)


> Scenario 2 : complex-property-with-arraylist-and-diff-table

```java
class PersonData {
    
  // Below property except for Address should be written to Person Table
	private String firstName;
	private String middleName;
	private String lastName;
	private String email;
	private String passportNumber;
  
  // Below should be written to ADDRESS table
  private List<Address> address;
}

```

                      ------------- PERSON (table)
                      |
                      |
                 firstName,middleName...
                      |
    PersonData -------|
                      |
                      |
                      |
                  List<Address>
                      |
                      ------------- ADDRESS (table)
