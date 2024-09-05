# Readme.md file describing the setup procedure

## Ensure Spring boot is correctly installed on machine & database login is correct
1. Start your database using the provided script and create the connection in azure data studio
(if issues with database occur, manually drop all tables and attempt to run complete script)

2. build the project file using ./gradlew build
3. run the project using        ./gradlew Bootrun

4. use http://localhost:8080 to access the login page
    (alternatively)   http://localhost:8080/login

5. The existing users in database in username-password-role/s triples
(user,     pass1,            {user})
(it,       pass1,            {it, manager})
(manager,  pass1,            {it, manager})
(Ylin,     Iwishyouagoodday, {it, manager}) // we have to use a specific password for this user

