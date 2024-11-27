# Progetto Sistemi Distribuiti 2023-2024

Domains.com è un sito internet che permette l'acquisto di uno o più domini web.
Tra le funzionalità offerte dalla nostra piattaforma ci sono le seguenti:
- Acquisto di un nuovo dominio
- Acquisto di un dominio da me posseduto in passato e scaduto (a patto che non
  lo abbiano comprato altri)
- Rinnovo di un dominio da me posseduto e non ancora scaduto
- Possibilità di visualizzare le informazioni inerenti ad un dominio posseduto
  da altri
- Possibilità di contattare il proprietario di un dominio a cui sono interessato
  tramite mail
- Visionare il catalogo di tutti i domini in mio possesso (scaduti e non)

## Componenti del gruppo

* Franscesco Bianchi (902251) <f.bianchi85@campus.unimib.it>
* Stefano Brighenti (900153) <s.brighenti4@campus.unimib.it>
* Daniele Buser (894514) <d.buser@campus.unimib.it>

## Compilazione ed esecuzione

Sia il server Web sia il database sono applicazioni Java gestire con Maven.
All'interno delle rispettive cartelle si può trovare il file `pom.xml` in cui è
presenta la configurazione di Maven per il progetto. Si presuppone l'utilizzo
della macchina virtuale di laboratorio, per cui nel `pom.xml` è specificato
l'uso di Java 21.

Il server Web e il database sono dei progetti Java che utilizano Maven per
gestire le dipendenze, la compilazione e l'esecuzione.

### Client Web

Per avviare il client Web è consigliato utilizzare l'estensione "Live Server"
(scaricabile dallo store di `Vs Code`) e una volta avviato il database e il
server-web è possibile aprire il file `index.html` facendo tasto destro su di
esso e selezionando l'opzione "Open with Live Server [Alt + L Alt + O]". Questo
perchè `Live Preview` non ci permetteva di utilizzare le alert javascript.

Si può comunque utilizzare  l'estensione "Live Preview" su Visual Studio Code,
come mostrato durante il laboratorio, perdendo però la funzionalità data
dall'uso delle alert.

Una terza opzione per avviare il client web è quella di aprire il file
`index.html` con `crhome` dalla cartella del progetto una volta avviati database
e server web.

### Server Web

Il server Web utilizza Jetty e Jersey. Si può avviare eseguendo `mvn jetty:run`
all'interno della cartella `server-web`. Espone le API REST all'indirizzo
`localhost` alla porta `8080`.

Per la gestione CORS è stata introdotta una classe `CorsFilter` contenente il
seguente codice:
```
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
```

### Database

Il database è una semplice applicazione Java. Si possono utilizzare i seguenti
comandi Maven all'interno della cartella `database` per startarlo:

* `mvn clean`: per ripulire la cartella dai file temporanei,
* `mvn compile`: per compilare l'applicazione,
* `mvn exec:java`: per avviare l'applicazione (presuppone che la classe
  principale sia `Main.java`). Si pone in ascolto all'indirizzo `localhost` alla
  porta `3030`.

### Lavoro Svolto
Il progetto è stato svolto seguendo questi step:
1. Creazione di un database documentale
2. Il database documentale è composto da una HashMap contenente le collezioni
   necessarie per il nostro progetto. (`registrations`, `domains`, `orders`).
4. Le collezioni sono strutturate come `name` (in formato String) + `documents`
   (ossia una HashMap contenente i vari documenti)
3. Ogni collezione contiene al suo interno i documenti specifici di una delle
   due categorie. Ogni documento è strutturato come `id` (in formato String) +
   `data` (in formato String ma ben formattato per la conversione in JSON)
5. E' stato creato un protocol handler che in base ai comandi ricevuti dal
   `server-web` opera sul database. E' possibile effettuare le seguenti
   operationi: `POST`, `GET`, `PUT`, `DELETE`.
6. E' stato implementato un handler per le connessioni TCP al database
7. Una volta terminata la progettazione del database per capire al meglio quali
   fossero le API da implementare abbiamo deciso di realizzare su figma una
   bozza del frontend del nostro sito.
8. Questa scelta è stata fatta per evitare di scrivere API non necessarie
   all'interazione utente-sistema.

![Grid.png](/screenshots/figma/Grid.png)
![typography.png](/screenshots/figma/Typograhpy.png)
![LoginPage.png](/screenshots/figma/LoginPage.png)
![RegisterPage.png](/screenshots/figma/RegisterPage.png)
![HomePage.png](/screenshots/figma/HomePage.png)
![BuyPage.png](/screenshots/figma/BuyPage.png)
![RenewPage.png](/screenshots/figma/RenewPage.png)
![MyDomains.png](/screenshots/figma/MyDomainsPage.png)
![ErrorPage.png](/screenshots/figma/ErrorPage.png)

9. Abbiamo scritto le API Rest nella cartella `server-web`. Le API permettono
   all'utente di eseguire le seguenti operazioni (`login`, `registration`,
   `search`, `buy`, `renew`, `owner-info`, `my-domains`)
10. Implementato il disegno realizzato su figma in `HTML`, `CSS`, `Javascript`.
11. Fatto in modo grazie ai documenti `.js` che frontend e backend comunicassero
    tra di loro.
12. Effettuato vari test per garantire il funzionamento del sistema