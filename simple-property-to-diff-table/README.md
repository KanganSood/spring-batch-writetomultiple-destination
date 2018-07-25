# simple-property-to-diff-table

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
