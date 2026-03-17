commande pour obtenir son token :
```bash 
curl -X POST "localhost:8080/api/login" -H "Content-Type: application/json" -d "{\"username\": \"admin@lycee.local\", \"password\": \"password\"}" 
```
ou 
```bash
curl -X POST "localhost:8080/api/login" -u admin@lycee.local
```
commande pour recuperre des info : 
```bash
curl -X GET "localhost:8080/api/[page]" -H "Authorization: Bearer [TON_TOKEN]" -H "Content-Type: application/json"
```