Distributed Systems Project 2023-2024
Domains.com is a website that allows users to purchase one or more web domains. The platform offers the following features:

Purchase of a new domain.
Purchase of a previously owned but expired domain (provided it has not been purchased by someone else).
Renewal of a currently owned and active domain.
Viewing information about a domain owned by another user.
Contacting the owner of a domain of interest via email.
Viewing a catalog of all owned domains (expired and active).
Team Members
Francesco Bianchi (902251) f.bianchi85@campus.unimib.it
Stefano Brighenti (900153) s.brighenti4@campus.unimib.it
Daniele Buser (894514) d.buser@campus.unimib.it
Compilation and Execution
Both the web server and the database are Java applications managed with Maven.
Within their respective directories, the pom.xml file contains Maven configuration details for the project. The use of the laboratory virtual machine is assumed, and the pom.xml specifies the use of Java 21.

The web server and database utilize Maven for dependency management, compilation, and execution.

Web Client
To launch the web client, it is recommended to use the "Live Server" extension (available in the Visual Studio Code marketplace). Once the database and web server are started, open the index.html file, right-click, and select "Open with Live Server [Alt + L Alt + O]". This is necessary as the Live Preview extension does not support JavaScript alerts.

Alternatively, you may use the Live Preview extension in Visual Studio Code, as demonstrated during the lab sessions, though this option does not support alerts.

A third option is to open the index.html file directly in Chrome from the project directory once the database and web server are running.

Web Server
The web server uses Jetty and Jersey. It can be started by running the following command within the server-web directory:

  mvn jetty:run  

The server exposes REST APIs on localhost, port 8080.
To handle CORS, a CorsFilter class was introduced with the following code:

  package it.unimib.sd2024;

import jakarta.ws.rs.container.ContainerRequestContext;  
import jakarta.ws.rs.container.ContainerResponseContext;  
import jakarta.ws.rs.container.ContainerResponseFilter;  
import jakarta.ws.rs.ext.Provider;  

@Provider  
public class CorsFilter implements ContainerResponseFilter {  
    @Override  
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {  
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");  
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");  
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");  
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");  
    }  
}  

Database
The database is a simple Java application. Use the following Maven commands within the database directory to manage it:

mvn clean: Cleans up temporary files.
mvn compile: Compiles the application.
mvn exec:java: Starts the application (assumes the main class is Main.java).
The database listens on localhost, port 3030.



Work Completed
The project was completed following these steps:

Created a document-based database.
The database is implemented as a HashMap containing the collections required for the project (registrations, domains, orders).
Each collection consists of a name (as a String) and documents (a HashMap containing individual documents).
Each document is structured with an id (as a String) and data (a well-formatted String for JSON conversion).
Implemented a protocol handler that processes commands from the server-web and performs operations on the database. Supported operations: POST, GET, PUT, DELETE.
Created a handler for TCP connections to the database.
Designed a preliminary frontend using Figma to identify the APIs required for user-system interaction. This ensured that no unnecessary APIs were developed.
Developed the frontend design from Figma using HTML, CSS, and JavaScript.










Developed the REST APIs in the server-web folder, enabling the following operations: login, registration, search, buy, renew, owner-info, my-domains.
Ensured seamless communication between frontend and backend using JavaScript scripts.
Conducted extensive testing to ensure system functionality.
