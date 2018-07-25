CREATE TABLE ADDRESS(
`passport-number` varchar(20)
,`postal-code`  varchar(15)
,`city`  varchar(100)
,`street`  varchar(100)
,`street-number` varchar(10)
,`apartment-number` varchar(10)
);

CREATE TABLE PERSON(
  `first-name` varchar(20)
, `middle-name` varchar(20)
, `last-name` varchar(20)
, `email` varchar(100)
, `passport-number` varchar(20)
);
