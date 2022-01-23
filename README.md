# eRent
Progetto per il corso di Distributed Systems and Big Data del corso di Laurea Magistrale LM-32, Università degli Studi di Catania, A.A. 2021/2022.

Progetto sviluppato da **Daniele Amenta** e **Daniele D'Agosta**.
##
Lo scopo dell’elaborato è quello di realizzare un sistema distribuito per gestire il noleggio di monopattini elettrici, servizio ormai diffuso in gran parte delle città. Il funzionamento prevede la presenza di alcune chiamate che permettono di effettuare, sulla base dei monopattini disponibili, il noleggio temporaneo di quest’ultimi. Al termine del noleggio, il mezzo viene nuovamente bloccato e si passa alla fase conclusiva del flusso, ovvero la generazione della fattura per il noleggio concluso.

Ogni micro-servizio distribuito è un progetto Spring Boot. Per il local testing si è fatto uso di Docker con Docker-Compose, mentre per il Distributed Testing è stato usato Kubernetes con Minikube.

Per maggiori dettagli sulla struttura e funzionamento del progetto, è possibile consultare la relazione dal nome "eRent - docs".

##

Per runnare il progetto:

**DOCKER**
Spostarsi nella cartella del progetto e lanciare il comando:
```ruby
docker-compose up
```

**KUBERNETES**
````
minikube start –memory=5120 –cpus=4
minikube dashboard

minikube docker-env

minikube addons enable ingress
docker build -t erent_users-service -f ./users_microservice/Dockerfile .
docker build -t erent_scooters-service -f ./scooters_microservice/Dockerfile .
docker build -t erent_rentals-service -f ./rentals_microservice/Dockerfile .
docker build -t erent_invoices-service -f ./invoices_microservice/Dockerfile .
docker build -t erent_eureka-service -f ./eureka_service/Dockerfile .
docker build -t erent_gateway -f ./gateway/Dockerfile .

kubectl apply -f k8s/
kubectl get all
````
Per ottenere l'URL del gateway e procedere con le successive chiamate:
````
minikube service gateway-service
````