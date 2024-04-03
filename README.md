# Based on the course [Rock the JVM](https://rockthejvm.com/) course [ZIO Rite of Passage](https://rockthejvm.com/courses/enrolled/2132116)

For mac M1 users using [Colima project](https://github.com/abiosoft/colima) with test containers
```bash
brew install --HEAD colima docker docker-compose
sudo ln -s $HOME/.colima/default/docker.sock /var/run/docker.sock
colima start --cpu 2 --memory 8 --disk 60 --arch aarch64 --vm-type=vz --vz-rosetta --mount-type virtiofs --network-address

export TESTCONTAINERS_HOST_OVERRIDE=$(colima ls -j | jq -r '.address' | head -n 1)
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
export DOCKER_HOST=unix://$HOME/.colima/default/docker.sock
```

For running testcontainers locally you need file `.env`
```bash
# For Colima and TestContainers working on MacOS
DOCKER_HOST=unix://$HOME/.colima/default/docker.sock
TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
TESTCONTAINERS_HOST_OVERRIDE=192.168.106.4
```

sbt: ~fastOptJS
npm run start
docker exec -it zio-rite-of-passage-db-1 psql -U docker
\c reviewboard
\d invites



http post localhost:8080/users email='tsg@tsgcompany.com' password='test'
http post localhost:8080/users/login email='tsg@tsgcompany.com' password='test'
http post localhost:8080/companies name='Google' url='google.com' country='USA' location='Boston' industry='tech' tags:='["it","good"]' 'Authorization: Bearer XXX'
http post localhost:8080/companies name='Libertex' url='https://libertex.com/' country='Montenegro' location='Podgorica' industry='fintech' tags:='["it","finance"]' 'Authorization: Bearer XXX'
http post localhost:8080/companies name='Rostec' url='https://rostec.ru/' country='Russia' location=Moscow industry='gos' tags:='["weapons","war"]' 'Authorization: Bearer XXX'
http post localhost:8080/companies/search countries:='["Montenegro"]' locations:='[]' industries:='[]' tags:='[]'

### Add review insert
```sql
insert into reviews(id, company_id, user_id, management, culture, salary, benefits, would_recommend, review, created, updated) values(2, 1, 1, 5, 4, 5, 5, 5, 'Awesome', now(), now());
```

#Add invites insert
```sql
insert into invites(id, user_name, company_id, n_invites, active) values (1, 'tsg@tsgcompany.com',1 ,10, true);

```

### Test invites
export TOKEN=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaXNzIjoidHNnY29tcGFueS5jb20iLCJleHAiOjE3MTIyNTc2MjYsImlhdCI6MTcxMjE3MTIyNiwidXNlcm5hbWUiOiJ0c2dAdHNnY29tcGFueS5jb20ifQ.c8ZaQtcMSUepUx7deWBJH1KIHBZKAxUpWn8Mh0QmhlSI9pqbUSjr_cyb1UOOhn8EONZWeLgCYgOdn3CT68y3eg

http post localhost:8080/invite/add companyId=1 "Authorization: Bearer $TOKEN"
select * from invites;

http get localhost:8080/invite/all "Authorization: Bearer $TOKEN"

http post localhost:8080/invite companyId=1 emails:='["mail1@tsgcompany.com", "mail2@tsgcompany.com", "mail3@tsgcompany.com"]' "Authorization: Bearer $TOKEN"


