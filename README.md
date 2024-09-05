# Readme.md file describing the setup procedure

1. Start your database using the provided script and create the connection in azure data studio
(if issues with database occur, manually drop all tables and attempt to run complete script)

2. build the project file using ./gradlew build
3. run the project using        ./gradlew Bootrun

4. using http://localhost:8080/cXXXXXXX_cYYYYYYY_cZZZZZZZ_FinalProject/ to access the login page
    (alternatively)   http://localhost:8080/login

5. The existing users in database in username-password-role/s triples
(user,     pass1,            {user})
(it,       pass1,            {it, manager})
(manager,  pass1,            {it, manager})
(Ylin,     Iwishyouagoodday, {it, manager}) // we have to use a specific password for this user


Additional Requirements chosen for a combined weight of 30
8. “The categories are very broad and could congest the Knowledge-Base. Can we have some sub-
categories as well (Appendix 6.2)” – IT staff. (weight 20)

18. “I think we should be able to highlight the recommended solution, some of the proposed
solutions from users might not be very appropriate” IT staff. (weight 10)