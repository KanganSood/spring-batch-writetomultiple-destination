# complex-property-with-arraylist-and-diff-table

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
